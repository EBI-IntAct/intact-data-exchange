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
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.bridges.ontologies.util.OntologyUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import java.io.StringWriter;
import java.net.URL;

import psidev.psi.mi.search.SearchResult;

/**
 * TODO comment that class header
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
    public void exportAll_noInteractorIndex() throws Exception {
        Interaction interaction = getMockBuilder().createInteraction("a1", "a2", "a3");

        final Interactor interactor = interaction.getComponents().iterator().next().getInteractor();
        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        interactor.addXref(getMockBuilder().createXref(interactor, "GO:0007028", null, goDb));

        PersisterHelper.saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = FSDirectory.getDirectory("/tmp/interactions");

        exporter.exportAllInteractors(mitabWriter, interactionsDir, null);

        mitabWriter.close();

        final String mitab = mitabWriter.toString();
        Assert.assertTrue(mitab.contains("go:\"GO:0007028\"(cytoplasm organization and biogenesis)"));
        Assert.assertEquals(2, mitab.split("\n").length);

        IndexSearcher interactionSearcher = new IndexSearcher(interactionsDir);

        Assert.assertEquals(2, interactionSearcher.getIndexReader().maxDoc());

        // check searching by a parent
        IntactSearchEngine searchEngine = new IntactSearchEngine(interactionsDir);
        final SearchResult<IntactBinaryInteraction> result = searchEngine.search("\"GO:0016043\"", 0, 50);
        Assert.assertEquals(2, result.getTotalCount());

        interactionsDir.close();
    }
}
