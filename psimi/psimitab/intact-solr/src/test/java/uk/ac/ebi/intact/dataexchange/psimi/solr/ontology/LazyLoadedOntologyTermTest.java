/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.junit.*;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexWriter;

import java.net.URL;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LazyLoadedOntologyTermTest {

    private static SolrJettyRunner solrJettyRunner;

    private static OntologySearcher searcher;
    private static SolrServer solrServer;

    @BeforeClass
    public static void beforeClass() throws Exception {
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.start();

        solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB);
        searcher = new OntologySearcher(solrServer);

        createIndex();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        //solrJettyRunner.join();
        solrJettyRunner.stop();
        solrJettyRunner = null;

        solrServer = null;
        searcher = null;
    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {

    }

    private static void createIndex() throws Exception {
        final URL goSlimUrl = LazyLoadedOntologyTermTest.class.getResource("/META-INF/goslim_generic.obo");

        OntologyIndexer ontologyIndexer = new OntologyIndexer(solrServer);
        ontologyIndexer.indexObo("go", goSlimUrl);

    }

    @Test
    public void rootNode() throws Exception {
        OntologyTerm term = new LazyLoadedOntologyTerm(searcher, "GO:0008150");

        Assert.assertEquals(term.getName(), "biological_process");

        Assert.assertEquals(0, term.getParents().size());
        Assert.assertEquals(21, term.getChildren().size());

        Assert.assertEquals("GO:0000003", term.getChildren().get(0).getId());
        Assert.assertEquals("GO:0006950", term.getChildren().get(1).getId());
        Assert.assertEquals("GO:0007154", term.getChildren().get(3).getId());
        Assert.assertEquals("cell cycle", term.getChildren().get(2).getName());
        Assert.assertEquals("cell communication", term.getChildren().get(3).getName());
        Assert.assertEquals("GO:0050789", term.getChildren().get(20).getId());
    }

    @Test
    public void parentsAndChildren() throws Exception {
        OntologyTerm term = new LazyLoadedOntologyTerm(searcher, "GO:0030154");
        Assert.assertEquals(1, term.getParents().size());

        OntologyTerm parent = term.getParents().get(0);

        Assert.assertEquals("GO:0008150", parent.getId());
        Assert.assertEquals("biological_process", parent.getName());
    }

    @Test
    public void allParentsToRoot() throws Exception {
        OntologyTerm term = new LazyLoadedOntologyTerm(searcher, "GO:0044238");

        final Set<OntologyTerm> parents = term.getAllParentsToRoot();

        Assert.assertEquals(2, parents.size());

        final Iterator<OntologyTerm> iterator = parents.iterator();
        Assert.assertEquals("GO:0008150", iterator.next().getId());
        Assert.assertEquals("GO:0008152", iterator.next().getId());
    }

    @Test
    public void childrenAtDepth() throws Exception {
        OntologyTerm term = new LazyLoadedOntologyTerm(searcher, "GO:0008150");

        final Collection<OntologyTerm> children = term.getChildrenAtDepth(1);
        Assert.assertEquals(21, children.size());

        final Collection<OntologyTerm> grandChildren = term.getChildrenAtDepth(2);
        Assert.assertEquals(12, grandChildren.size());

        final Collection<OntologyTerm> itself = term.getChildrenAtDepth(0);
        Assert.assertEquals(1, itself.size());

        final Collection<OntologyTerm> superChildren = term.getChildrenAtDepth(40);
        Assert.assertEquals(0, superChildren.size());
    }

    @Test
    public void complexParents() throws Exception {
        //    root
        //    / |\
        // c11  | \
        //  |  /   \
        //  c21    c22          || -> disjoint_from "cyclic" dependency
        //  ||   __/
        //  ||  /
        //  c31  <- parents for this node
        //

        OntologyDocument root = new OntologyDocument("test", null, null, "ROOT", "root", null, false);
        OntologyDocument root_c11 = new OntologyDocument("test", "ROOT", "root", "C1-1", "children 1-1", "OBO_REL:is_a", false);
        OntologyDocument root_c21 = new OntologyDocument("test", "ROOT", "root", "C2-1", "children 2-1", "regulates", false);
        OntologyDocument root_c22 = new OntologyDocument("test", "ROOT", "root", "C2-2", "children 2-2", "OBO_REL:is_a", false);
        OntologyDocument c11_c21 = new OntologyDocument("test", "C1-1", "children 1-1", "C2-1", "children 2-1", "OBO_REL:is_a", false);
        OntologyDocument c21_c31 = new OntologyDocument("test", "C2-1", "children 2-1", "C3-1", "children 3-1", "regulates", false);
        OntologyDocument c31_c21 = new OntologyDocument("test", "C3-1", "children 3-1", "C2-1", "children 2-1", "disjoint_from", false);
        OntologyDocument c22_c31 = new OntologyDocument("test", "C2-2", "children 2-2", "C3-1", "children 3-1", "OBO_REL:is_a", false);
        OntologyDocument c31 = new OntologyDocument("test", "C3-1", "children 3-1", null, null, null, false);

        SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_STAGE);
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
        solrServer.optimize();

        OntologyIndexer indexer = new OntologyIndexer(solrServer);

        indexer.index(root);
        indexer.index(root_c11);
        indexer.index(root_c21);
        indexer.index(root_c22);
        indexer.index(c11_c21);
        indexer.index(c21_c31);
        indexer.index(c22_c31);
        indexer.index(c31_c21);
        indexer.index(c31);

        solrServer.commit();
        solrServer.optimize();

        OntologySearcher testSearcher = new OntologySearcher(solrServer);

        Set<OntologyTerm> parents = new LazyLoadedOntologyTerm(testSearcher, "C3-1").getAllParentsToRoot();

        Assert.assertEquals(2, parents.size());
    }
}