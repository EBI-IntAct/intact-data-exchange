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
import org.junit.*;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.util.OntologyUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;

import java.io.StringWriter;
import java.net.URL;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DatabaseMitabExporterTest extends IntactBasicTestCase {

    private static Directory ontologiesDir;
    private DatabaseMitabExporter exporter;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ontologiesDir = new RAMDirectory();

        final URL goSlimLocalUrl = DatabaseMitabExporterTest.class.getResource("/META-INF/goslim_generic.obo");

        OntologyUtils.buildIndexFromObo(ontologiesDir,
                                        new OntologyMapping[] {
                                                new OntologyMapping("go", goSlimLocalUrl)
                                        }, true);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ontologiesDir = null;
    }

    @Before
    public void before() throws Exception {
        exporter = new DatabaseMitabExporter(ontologiesDir, "go");
    }

    @After
    public void after() throws Exception {
        exporter = null;
    }

    @Test
    public void exportAll() throws Exception {
        Interaction interaction = getMockBuilder().createInteraction("a1", "a2", "a3");

        final Interactor interactor = interaction.getComponents().iterator().next().getInteractor();
        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        interactor.addXref(getMockBuilder().createXref(interactor, "GO:0007028", null, goDb));

        PersisterHelper.saveOrUpdate(interaction);

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

        StringWriter mitabWriter = new StringWriter();
        Directory interactionsDir = new RAMDirectory("interactions");

        exporter.exportAllInteractors(mitabWriter, interactionsDir, null);

        mitabWriter.close();

        final String mitab = mitabWriter.toString();
        Assert.assertTrue(mitab.contains("go:\"GO:0007028\"(cytoplasm organization and biogenesis)"));
        Assert.assertEquals(2, mitab.split("\n").length);

        IndexSearcher interactionSearcher = new IndexSearcher(interactionsDir);

        Assert.assertEquals(2, interactionSearcher.getIndexReader().maxDoc());

        interactionsDir.close();
    }
}
