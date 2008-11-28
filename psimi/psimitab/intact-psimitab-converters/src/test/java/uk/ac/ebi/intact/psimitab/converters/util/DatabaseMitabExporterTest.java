/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.util;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.FSDirectory;
import org.junit.*;
import org.obo.dataadapter.OBOParseException;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexWriter;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;
import uk.ac.ebi.intact.bridges.ontologies.util.OntologyUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import java.io.StringWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import psidev.psi.mi.search.SearchResult;

/**
 * DatabaseMitabExporter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DatabaseMitabExporterTest extends IntactBasicTestCase {

    private static OntologyIndexSearcher ontologyIndexSearcher;
    private DatabaseMitabExporter exporter;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Directory ontologiesDir = new RAMDirectory();

        final URL goSlimLocalUrl = DatabaseMitabExporterTest.class.getResource("/META-INF/goslim_generic.obo");

        OntologyUtils.buildIndexFromObo(ontologiesDir,
                                        new OntologyMapping[] {
                                                new OntologyMapping("go", goSlimLocalUrl)
                                        }, true);

        ontologyIndexSearcher = new OntologyIndexSearcher(ontologiesDir);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ontologyIndexSearcher.close();
        ontologyIndexSearcher = null;
    }

    @Before
    public void before() throws Exception {
        exporter = new DatabaseMitabExporter(ontologyIndexSearcher, "go");
        SchemaUtils.createSchema();
    }

    @After
    public void after() throws Exception {
        exporter = null;
        IntactContext.closeCurrentInstance();
    }

    private static OntologyIndexSearcher createGoSlimIndexSearcher() throws OBOParseException, IOException {
        final URL goSlimUrl = DatabaseMitabExporterTest.class.getResource("/META-INF/goslim_generic.obo");

        OntologyIterator ontologyIterator = new OboOntologyIterator("go", goSlimUrl);

        Directory directory = new RAMDirectory();
        OntologyIndexWriter indexer = new OntologyIndexWriter(directory,true);

        while (ontologyIterator.hasNext()) {
            OntologyDocument document = ontologyIterator.next();
            indexer.addDocument(document);
        }

        indexer.flush();
        indexer.optimize();
        indexer.close();

        return new OntologyIndexSearcher(directory);
    }

    @Test
    public void exportAll() throws Exception {
        Interaction interaction = getMockBuilder().createInteraction("a1", "a2", "a3");

        final Interactor interactor = interaction.getComponents().iterator().next().getInteractor();
        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        interactor.addXref(getMockBuilder().createXref(interactor, "GO:0007028", null, goDb));

        PersisterHelper.saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory("interactions");
        Directory interactorsDir = new RAMDirectory("interactors");

        exporter.exportAllInteractors(mitabWriter, interactionsDir, interactorsDir);

        mitabWriter.close();

        final String mitab = mitabWriter.toString();
        Assert.assertTrue(mitab.contains("go:\"GO:0007028\"(cytoplasm organization and biogenesis)"));
        Assert.assertEquals(2, mitab.split("\n").length);

        IndexSearcher interactionSearcher = new IndexSearcher(interactionsDir);
        IndexSearcher interactorSearcher = new IndexSearcher(interactorsDir);

        Assert.assertEquals(2, interactionSearcher.getIndexReader().maxDoc());
        Assert.assertEquals(3, interactorSearcher.getIndexReader().maxDoc());

        interactionsDir.close();
        interactorsDir.close();
    }

    @Test
    public void exportCvExpansion() throws Exception {
        // then build a single binary interaction with 2 interactor having 1 distinct GO term (! overlapping parents)
        final CvDatabase go = getMockBuilder().createCvObject( CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO );

        final Protein p1 = getMockBuilder().createProtein( "P12345", "foo" );
        // add a molecular function, parents are GO:0004871, GO:0003674
        p1.addXref( new InteractorXref( getMockBuilder().getInstitution(), go, "GO:0004872", "receptor activity", null, null ) );

        final Protein p9 = getMockBuilder().createProtein( "P98765", "foo" );
        // add a biological process, parents: GO:0006091, GO:0008152, GO:0008150
        p9.addXref( new InteractorXref( getMockBuilder().getInstitution(), go, "GO:0022904", "respiratory electron transport chain", null, null ) );

        final Interaction interaction = getMockBuilder().createInteraction( "p1-p9",  p1, p9, getMockBuilder().createExperimentEmpty() );
        PersisterHelper.saveOrUpdate( interaction );

        Assert.assertEquals(2, getDaoFactory().getInteractorDao( ProteinImpl.class ).countAll());
        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        // Build indices with GO expansion enabled
        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory();
        Directory interactorsDir = new RAMDirectory();

        exporter = new DatabaseMitabExporter( createGoSlimIndexSearcher(), "go" );
        exporter.exportAllInteractors( mitabWriter, interactionsDir, interactorsDir );

        // check on MITAB
        String mitab = mitabWriter.getBuffer().toString();
        Assert.assertEquals( 1, mitab.split( "\n" ).length );

        // check on interaction index
        Assert.assertEquals(1, new IndexSearcher(interactionsDir).getIndexReader().maxDoc());
        // p1
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0004872\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0006091\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0008152\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0008150\"" ) );
        // p2
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0022904\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0006091\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0008152\"" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "properties:\"GO:0008150\"" ) );
        interactionsDir.close();

        // check on interactor index -- note: properties does aggragate expanded properties of A and B
        Assert.assertEquals(2, new IndexSearcher(interactorsDir).getIndexReader().maxDoc());
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0004872\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0006091\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0008152\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0008150\"" ) );
        // p2
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0022904\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0006091\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0008152\"" ) );
        Assert.assertEquals( 2, searchOnIndex( interactorsDir, "properties:\"GO:0008150\"" ) );
        interactorsDir.close();
    }

    @Test
    public void checkInteractionMerge() throws Exception {

        final Protein p1 = getMockBuilder().createProtein( "P12345", "foo" );
        final Protein p2 = getMockBuilder().createProtein( "Q11111", "lala" );
        final Protein p9 = getMockBuilder().createProtein( "P98765", "bar" );

        final CvInteraction cosedimentation = getMockBuilder().createCvObject( CvInteraction.class,
                                                                               CvInteraction.COSEDIMENTATION_MI_REF,
                                                                               CvInteraction.COSEDIMENTATION );
        final CvInteraction inferred = getMockBuilder().createCvObject( CvInteraction.class,
                                                                        CvInteraction.INFERRED_BY_CURATOR_MI_REF,
                                                                        CvInteraction.INFERRED_BY_CURATOR );

        final CvInteractionType direct = getMockBuilder().createCvObject( CvInteractionType.class,
                                                                          CvInteractionType.DIRECT_INTERACTION_MI_REF,
                                                                          CvInteractionType.DIRECT_INTERACTION );

        final CvInteractionType physical = getMockBuilder().createCvObject( CvInteractionType.class,
                                                                            CvInteractionType.PHYSICAL_INTERACTION_MI_REF,
                                                                            CvInteractionType.PHYSICAL_INTERACTION );

        final Experiment e1 = getMockBuilder().createExperimentEmpty();
        e1.setCvInteraction( cosedimentation );
        final Experiment e2 = getMockBuilder().createExperimentEmpty();
        e2.setCvInteraction( inferred );

        // Let's create interactions

        final Interaction interaction1 = getMockBuilder().createInteraction( "p1-p9-1",  p1, p9, e1 );
        interaction1.setCvInteractionType( direct );
        addFeatureForInteractor( interaction1, p1 );
        final Interaction interaction2 = getMockBuilder().createInteraction( "p1-p2-1",  p1, p2, e1 );
        final Interaction interaction3 = getMockBuilder().createInteraction( "p9-p1-1",  p9, p1, e1 );

        final Interaction interaction4 = getMockBuilder().createInteraction( "p9-p1-2",  p9, p1, e2 );
        interaction1.setCvInteractionType( physical );
        PersisterHelper.saveOrUpdate( interaction1, interaction2, interaction3, interaction4 );

        Assert.assertEquals(3, getDaoFactory().getInteractorDao( ProteinImpl.class ).countAll());
        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());
        Assert.assertEquals(2, getDaoFactory().getExperimentDao().countAll());

        // Build indices with GO expansion enabled
        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory();
        Directory interactorsDir = new RAMDirectory();

        exporter.exportAllInteractors( mitabWriter, interactionsDir, interactorsDir );

        // check on MITAB
        String mitab = mitabWriter.getBuffer().toString();
        Assert.assertEquals( 2, mitab.split( "\n" ).length );

        // check on interaction index -- note: properties does aggragate expanded properties of A and B
        Assert.assertEquals(2, new IndexSearcher(interactionsDir).getIndexReader().maxDoc());
        Assert.assertEquals( 2, searchOnIndex( interactionsDir, "P12345" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "Q11111" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "P98765" ) );

        // P98765 is only involved with P12345 in 2 distinct interactions, it should have been merged
        Assert.assertEquals( 3, countEvidences( interactionsDir, "P98765" ) );
        interactionsDir.close();

        // check on interactor index
        Assert.assertEquals(3, new IndexSearcher(interactorsDir).getIndexReader().maxDoc());
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:P12345" ) );
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:P98765" ) );
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:Q11111" ) );
        interactorsDir.close();
    }

    @Test
    public void checkNaryInteractionMerge() throws Exception {

        final Protein p1 = getMockBuilder().createProtein( "P12345", "foo" );
        final Protein p2 = getMockBuilder().createProtein( "Q11111", "lala" );
        final Protein p9 = getMockBuilder().createProtein( "P98765", "bar" );

        final CvInteraction cosedimentation = getMockBuilder().createCvObject( CvInteraction.class,
                                                                               CvInteraction.COSEDIMENTATION_MI_REF,
                                                                               CvInteraction.COSEDIMENTATION );
        final CvInteraction inferred = getMockBuilder().createCvObject( CvInteraction.class,
                                                                        CvInteraction.INFERRED_BY_CURATOR_MI_REF,
                                                                        CvInteraction.INFERRED_BY_CURATOR );

        final CvInteractionType direct = getMockBuilder().createCvObject( CvInteractionType.class,
                                                                          CvInteractionType.DIRECT_INTERACTION_MI_REF,
                                                                          CvInteractionType.DIRECT_INTERACTION );

        final CvInteractionType physical = getMockBuilder().createCvObject( CvInteractionType.class,
                                                                            CvInteractionType.PHYSICAL_INTERACTION_MI_REF,
                                                                            CvInteractionType.PHYSICAL_INTERACTION );

        final CvExperimentalRole bait = getMockBuilder().createCvObject( CvExperimentalRole.class,
                                                                         CvExperimentalRole.BAIT_PSI_REF,
                                                                         CvExperimentalRole.BAIT );

        final CvExperimentalRole prey = getMockBuilder().createCvObject( CvExperimentalRole.class,
                                                                         CvExperimentalRole.PREY_PSI_REF,
                                                                         CvExperimentalRole.PREY );

        final Experiment e1 = getMockBuilder().createExperimentEmpty();
        e1.setCvInteraction( cosedimentation );
        e1.setBioSource( getMockBuilder().createBioSource( 9606, "human" ) );
        final Experiment e2 = getMockBuilder().createExperimentEmpty();
        e2.setCvInteraction( inferred );
        e2.setBioSource( getMockBuilder().createBioSource( 9999, "alien" ) );

        // Let's create interactions

        final Interaction interaction1 = getMockBuilder().createInteraction( p1, p2, p9 );
        updateInteractorRole( interaction1, p1, bait );
        updateInteractorRole( interaction1, p2, prey );
        updateInteractorRole( interaction1, p9, prey );
        interaction1.setCvInteractionType( direct );
        addFeatureForInteractor( interaction1, p1 );

        final Interaction interaction4 = getMockBuilder().createInteraction( "p9-p1-2",  p9, p1, e2 );
        interaction1.setCvInteractionType( physical );
        
        PersisterHelper.saveOrUpdate( interaction1, interaction4 );

        Assert.assertEquals(3, getDaoFactory().getInteractorDao( ProteinImpl.class ).countAll());
        Assert.assertEquals(2, getDaoFactory().getInteractionDao().countAll());
        Assert.assertEquals(2, getDaoFactory().getExperimentDao().countAll());

        // Build indices with GO expansion enabled
        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory();
        Directory interactorsDir = new RAMDirectory();

        exporter.exportAllInteractors( mitabWriter, interactionsDir, interactorsDir );

        // check on MITAB
        String mitab = mitabWriter.getBuffer().toString();
        Assert.assertEquals( 2, mitab.split( "\n" ).length );

        // check on interaction index -- note: properties does aggragate expanded properties of A and B
        Assert.assertEquals(2, new IndexSearcher(interactionsDir).getIndexReader().maxDoc());
        Assert.assertEquals( 2, searchOnIndex( interactionsDir, "P12345" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "Q11111" ) );
        Assert.assertEquals( 1, searchOnIndex( interactionsDir, "P98765" ) );

        // P98765 is only involved with P12345 in 2 distinct interactions, it should have been merged
        Assert.assertEquals( 2, countEvidences( interactionsDir, "P98765" ) );
        interactionsDir.close();

        // check on interactor index
        Assert.assertEquals(3, new IndexSearcher(interactorsDir).getIndexReader().maxDoc());
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:P12345" ) );
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:P98765" ) );
        Assert.assertEquals( 1, searchOnIndex( interactorsDir, "idA:Q11111" ) );
        interactorsDir.close();
    }

    private void updateInteractorRole( Interaction i, Protein p, CvExperimentalRole role ) {
        for ( Component component : i.getComponents() ) {
            if( component.getInteractor().equals(p) ) {
                component.setExperimentalRoles( Arrays.asList( role ) );
            }
        }
    }

    private void addFeatureForInteractor( Interaction i, Protein p ) {
        for ( Component c : i.getComponents() ) {
            if( c.getInteractor().equals( p ) ) {
                // add a feature
                final Feature feature = getMockBuilder().createFeatureRandom();
                feature.addRange( getMockBuilder().createRangeCTerminal() );
                c.addBindingDomain( feature );
            }
        }
    }

    private int searchOnIndex( Directory index, String query ) throws IOException {
        IntactSearchEngine searchEngine = new IntactSearchEngine( index );
        return searchEngine.search( query, 0, Integer.MAX_VALUE).getTotalCount();
    }

    private int countEvidences( Directory index, String query ) throws IOException {
        IntactSearchEngine searchEngine = new IntactSearchEngine( index );
        final List<IntactBinaryInteraction> interactions = searchEngine.search( query, 0, Integer.MAX_VALUE ).getData();
        Assert.assertEquals(1, interactions.size());
        final IntactBinaryInteraction i = interactions.iterator().next();
        return i.getInteractionAcs().size();
    }

    @Test
    public void exportAll_noInteractorIndex() throws Exception {
        Interaction interaction = getMockBuilder().createInteraction("a1", "a2", "a3");

        final Interactor interactor = interaction.getComponents().iterator().next().getInteractor();
        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        interactor.addXref(getMockBuilder().createXref(interactor, "GO:0007028", null, goDb));

        PersisterHelper.saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory();

        exporter.exportAllInteractors(mitabWriter, interactionsDir, null);

        mitabWriter.close();

        final String mitab = mitabWriter.toString();
        Assert.assertTrue(mitab.contains("go:\"GO:0007028\"(cytoplasm organization and biogenesis)"));
        Assert.assertEquals(2, mitab.split("\n").length);

        IndexSearcher interactionSearcher = new IndexSearcher(interactionsDir);

        Assert.assertEquals(2, interactionSearcher.getIndexReader().maxDoc());

        // check searching by a parent
        Assert.assertEquals( 2, searchOnIndex( interactionsDir, "\"GO:0016043\"" ) );

        interactionsDir.close();
    }
}
