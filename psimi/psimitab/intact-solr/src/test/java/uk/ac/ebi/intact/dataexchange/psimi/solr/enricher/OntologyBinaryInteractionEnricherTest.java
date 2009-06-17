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
package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyBinaryInteractionEnricherTest {

    private SolrJettyRunner solrJettyRunner;
    private IntactSolrIndexer indexer;
    private OntologyBinaryInteractionEnricher enricher;

    @Before
    public void before() throws Exception {
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.start();

        indexer = new IntactSolrIndexer(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB),
                                        solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB));

        enricher = new OntologyBinaryInteractionEnricher(
                new OntologySearcher(solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB)));
    }

    @After
    public void after() throws Exception {
        solrJettyRunner.stop();
        solrJettyRunner = null;

        indexer = null;
        enricher = null;
    }

    @Test
    public void testEnrichXref() throws Exception {
        indexer.indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", OntologyBinaryInteractionEnricherTest.class.getResource("/META-INF/goslim_generic.obo"))
        });

        CrossReference xref = new CrossReferenceImpl("go", "GO:0030246", "lalala");
        enricher.enrich(xref);
        System.out.println(xref);

        Assert.assertEquals("go", xref.getDatabase());
        Assert.assertEquals("GO:0030246", xref.getIdentifier());
        Assert.assertEquals("carbohydrate binding", xref.getText());
    }
}
