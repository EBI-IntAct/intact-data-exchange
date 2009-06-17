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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;

import java.net.URL;


/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyIndexerTest  {

     private SolrJettyRunner solrJettyRunner;

    @Before
    public void before() throws Exception {
        solrJettyRunner = new SolrJettyRunner();

        solrJettyRunner.start();

    }

    @After
    public void after() throws Exception {
        //solrJettyRunner.join();
        solrJettyRunner.stop();
        solrJettyRunner = null;
    }

    @Test
    public void testIndexObo() throws Exception{
        SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB);

        OntologyIndexer ontologyIndexer = new OntologyIndexer(solrServer);

        ontologyIndexer.indexObo("psi-mi", new URL("http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/mi/rel25/data/psi-mi25.obo?revision=1.52"));

        SolrQuery query = new SolrQuery("*:*");
        QueryResponse queryResponse = solrServer.query(query);

        Assert.assertEquals(1799, queryResponse.getResults().getNumFound());
    }

     @Test
    public void testIndexTaxonomy() throws Exception{
        SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB);

        OntologyIndexer ontologyIndexer = new OntologyIndexer(solrServer);

        ontologyIndexer.indexOntology(new UniprotTaxonomyOntologyIterator(0, 3));
         
        SolrQuery query = new SolrQuery("*:*");
        QueryResponse queryResponse = solrServer.query(query);

        Assert.assertEquals(3, queryResponse.getResults().getNumFound());
    }
}
