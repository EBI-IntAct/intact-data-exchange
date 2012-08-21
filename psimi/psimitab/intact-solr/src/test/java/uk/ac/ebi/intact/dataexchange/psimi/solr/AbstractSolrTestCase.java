package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.junit.After;
import org.junit.Before;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.IntactSolrJettyRunner;

import static org.junit.Assert.assertEquals;

/**
 * Abstract Solr test case and convenience assertion.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class AbstractSolrTestCase {

    private IntactSolrJettyRunner solrJettyRunner;
    private IntactSolrIndexer indexer;

    @Before
    public void before() throws Exception {
        solrJettyRunner = new IntactSolrJettyRunner();
        solrJettyRunner.start();

        indexer = new IntactSolrIndexer( solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ),
                                         (HttpSolrServer) solrJettyRunner.getSolrServer( CoreNames.CORE_ONTOLOGY_PUB ) );
    }

    @After
    public void after() throws Exception {
//        solrJettyRunner.join();
        solrJettyRunner.stop();

        FileUtils.deleteQuietly(solrJettyRunner.getSolrHome());

        solrJettyRunner = null;
        indexer = null;
    }

    public IntactSolrJettyRunner getSolrJettyRunner() {
        return solrJettyRunner;
    }

    public IntactSolrIndexer getIndexer() {
        return indexer;
    }

    public void assertCountOntologyTerm( Number expectedCount, String searchQuery ) throws SolrServerException, PsicquicSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher( solrJettyRunner.getSolrServer( CoreNames.CORE_ONTOLOGY_PUB ) );
        IntactSolrSearchResult result = (IntactSolrSearchResult) searcher.search( searchQuery, null, null, null, null );
        assertEquals( expectedCount.longValue(), result.getNumberResults() );
    }

    public void assertCountInteraction( Number expectedCount, String searchQuery ) throws IntactSolrException, SolrServerException, PsicquicSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher( solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ) );
        IntactSolrSearchResult result = (IntactSolrSearchResult) searcher.search( searchQuery, null, null, null, null );
        assertEquals( expectedCount.longValue(), result.getNumberResults() );
    }

    protected SolrServer getSolrServer() {
        return solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);
    }
}
