package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;

/**
 * Abstract Solr test case and convenience assertion.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class AbstractSolrTestCase {

    private SolrJettyRunner solrJettyRunner;
    private IntactSolrIndexer indexer;

    @Before
    public void before() throws Exception {
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.start();

        indexer = new IntactSolrIndexer( solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ),
                                         solrJettyRunner.getStreamingSolrServer( CoreNames.CORE_ONTOLOGY_PUB ) );
    }

    @After
    public void after() throws Exception {
//        solrJettyRunner.join();
        solrJettyRunner.stop();
        solrJettyRunner = null;

        indexer = null;
    }

    public SolrJettyRunner getSolrJettyRunner() {
        return solrJettyRunner;
    }

    public IntactSolrIndexer getIndexer() {
        return indexer;
    }

    public void assertCountOntologyTerm( Number expectedCount, String searchQuery ) {
        IntactSolrSearcher searcher = new IntactSolrSearcher( solrJettyRunner.getSolrServer( CoreNames.CORE_ONTOLOGY_PUB ) );
        SolrSearchResult result = searcher.search( searchQuery, null, null );
        assertEquals( expectedCount.longValue(), result.getTotalCount() );
    }

    public void assertCountInteraction( Number expectedCount, String searchQuery ) throws IntactSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher( solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ) );
        SolrSearchResult result = searcher.search( searchQuery, null, null );
        assertEquals( expectedCount.longValue(), result.getTotalCount() );
    }
}
