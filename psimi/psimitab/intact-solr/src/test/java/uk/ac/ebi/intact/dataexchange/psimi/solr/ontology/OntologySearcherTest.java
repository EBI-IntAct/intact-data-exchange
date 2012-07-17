package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.dataexchange.psimi.solr.AbstractSolrTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;

import java.net.URL;

/**
 * OntologySearcher Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class OntologySearcherTest extends AbstractSolrTestCase {

    @Test
    public void searchByChildId() throws Exception {

        // index a small ontology
        final URL goURL = OntologySearcherTest.class.getResource( "/META-INF/GO_nuclear_exosome.obo" );
        getIndexer().indexOntologies( new OntologyMapping[]{new OntologyMapping( "go", goURL )} );
        assertCountOntologyTerm( 44, "*:*" ); // 33 is_a + 11 part_of + 1 leaf + 1 root

        final SolrServer server = getSolrJettyRunner().getSolrServer( CoreNames.CORE_ONTOLOGY_PUB );
        OntologySearcher searcher = new OntologySearcher( server );

        assertParentRelationshipCount( searcher, "GO:0000786", 3 );
        assertParentRelationshipCount( searcher, "GO:0000790", 2 );
        assertParentRelationshipCount( searcher, "GO:0000785", 1 );
        assertParentRelationshipCount( searcher, "GO:0005575", 1 );
    }

    @Test
    public void searchByParentId() throws Exception {

        // index a small ontology
        final URL goURL = OntologySearcherTest.class.getResource( "/META-INF/GO_nuclear_exosome.obo" );
        getIndexer().indexOntologies( new OntologyMapping[]{new OntologyMapping( "go", goURL )} );
        assertCountOntologyTerm( 44, "*:*" ); // 33 is_a + 11 part_of + 1 leaf + 1 root

        final SolrServer server = getSolrJettyRunner().getSolrServer( CoreNames.CORE_ONTOLOGY_PUB );
        OntologySearcher searcher = new OntologySearcher( server );

        assertChildrenRelationshipCount( searcher, "GO:0000786", 1 );
        assertChildrenRelationshipCount( searcher, "GO:0000790", 1 );
        assertChildrenRelationshipCount( searcher, "GO:0000785", 2 );
        assertChildrenRelationshipCount( searcher, "GO:0005575", 5 );
    }

    private void assertParentRelationshipCount( OntologySearcher searcher, String termId, int expectedCount ) throws SolrServerException {
        final QueryResponse response = searcher.searchByChildId( termId, 0, Integer.MAX_VALUE );
        Assert.assertNotNull( response );
        final SolrDocumentList documentList = response.getResults();
        Assert.assertEquals( expectedCount, documentList.size() );
    }

    private void assertChildrenRelationshipCount( OntologySearcher searcher, String termId, int expectedCount ) throws SolrServerException {
        final QueryResponse response = searcher.searchByParentId( termId, 0, Integer.MAX_VALUE );
        Assert.assertNotNull( response );
        Assert.assertEquals( expectedCount, response.getResults().size() );
    }
}
