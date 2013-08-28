package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import uk.ac.ebi.intact.model.InteractionImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 28/08/13
 */
public class ComplexSolrWriterTest {
    private ComplexSolrWriter complexSolrWriter = null ;
    String URL = "" ;
    @Before
    public void setUp() throws Exception {
        this.complexSolrWriter = new ComplexSolrWriter ( ) ;
    }

    @After
    public void tearDown() throws Exception {
        this.complexSolrWriter.close ( ) ;
    }

    @Test
    public void testSetSolrUrl() throws Exception {
        this.complexSolrWriter.setSolrUrl(URL) ;
        Assert.assertSame ( "Test set/get url", URL, this.complexSolrWriter.getSolrUrl ( ) ) ;
    }

    @Test
    public void testSetMaxTotalConnections() throws Exception {
        int max = 256 ;
        Assert.assertEquals ( "Test get original max total connections", 128, this.complexSolrWriter.getMaxTotalConnections ( ) ) ;
        this.complexSolrWriter.setMaxTotalConnections ( max ) ;
        Assert.assertEquals("Test set/get max total connections", max, this.complexSolrWriter.getMaxTotalConnections()) ;
    }

    @Test
    public void testSetDefaultMaxConnectionsPerHost() throws Exception {
        int max = 32 ;
        Assert.assertEquals ( "Test get original max connections per host", 24, this.complexSolrWriter.getDefaultMaxConnectionsPerHost ( ) ) ;
        this.complexSolrWriter.setDefaultMaxConnectionsPerHost(max); ;
        Assert.assertEquals ( "Test set/get max connections per host", max, this.complexSolrWriter.getDefaultMaxConnectionsPerHost() ) ;
    }

    @Test
    public void testSetAllowCompression() throws Exception {
        boolean compression = false ;
        Assert.assertTrue ( "Test get original compress", this.complexSolrWriter.getAllowCompression ( ) ) ;
        this.complexSolrWriter.setAllowCompression ( compression ) ;
        Assert.assertFalse ( "Test set/get compress", this.complexSolrWriter.getAllowCompression ( ) ) ;
    }

    @Test
    public void testSetNeedToCommitOnClose() throws Exception {
        boolean commit = true ;
        Assert.assertFalse ( "Test get original commit", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        this.complexSolrWriter.setNeedToCommitOnClose ( commit ) ;
        Assert.assertTrue ( "Test set/get commit", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
    }

    @Test
    public void testGetSolrServer() throws Exception {
        this.complexSolrWriter.setSolrUrl ( URL ) ;
        HttpSolrServer httpSolrServer = (HttpSolrServer) this.complexSolrWriter.getSolrServer ( ) ;
        Assert.assertSame ( "Test URL of the HttpSolrServer", URL, httpSolrServer.getBaseURL ( ) );
    }

    @Test
    public void testOpen() throws Exception {
        ExecutionContext executionContext = new ExecutionContext ( ) ;
        // Test without Solr URL
        this.complexSolrWriter.open ( executionContext ) ;
        Assert.assertNull("Test Open without url", this.complexSolrWriter.solrServer) ;
        // Test with Solr URL
        this.complexSolrWriter.setSolrUrl ( URL ) ;
        this.complexSolrWriter.open ( executionContext ) ;
        Assert.assertNotNull("Test Open without url", this.complexSolrWriter.solrServer) ;
    }

    @Test
    public void testUpdate() throws Exception {
        ExecutionContext executionContext = new ExecutionContext ( ) ;
        this.complexSolrWriter.setSolrUrl ( URL ) ;
        this.complexSolrWriter.open ( executionContext ) ;
        // Test update
        Assert.assertFalse ( "Test update 1", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        this.complexSolrWriter.update ( executionContext ) ;
        Assert.assertTrue("Test update 2", this.complexSolrWriter.getNeedToCommitOnClose()) ;
    }

    @Test
    public void testWrite() throws Exception {
        ExecutionContext executionContext = new ExecutionContext ( ) ;
        List < InteractionImpl > list = new ArrayList < InteractionImpl > ( ) ;
        this.complexSolrWriter.setSolrUrl ( URL ) ;
        this.complexSolrWriter.open ( executionContext ) ;
        this.complexSolrWriter.setNeedToCommitOnClose ( true ) ;
        // Test write an empty list
        this.complexSolrWriter.write ( list );
        Assert.assertTrue ( "Test write with an empty list", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
        // Test write
        list.add ( new InteractionImpl ( ) ) ;
        this.complexSolrWriter.write ( list ) ;
        Assert.assertFalse ( "Test write", this.complexSolrWriter.getNeedToCommitOnClose ( ) ) ;
    }
}
