package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

/**
 *  Test all methods of ComplexSolrSearcher
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/08/13
 */
public class ComplexSolrSearcherTest {
    private SolrServer solrServer = null ;
    private ComplexSolrSearcher complexSolrSearcher = null ;

    @Before
    public void setUp() throws Exception {
        this.solrServer = new SolrServer(){
            @Override
            public NamedList<Object> request(SolrRequest solrRequest) throws SolrServerException, IOException {
                return null;
            }
        } ;
        this.complexSolrSearcher = new ComplexSolrSearcher ( this.solrServer ) ;
    }

    @Test
    public void testCheckQuery() throws Exception {
        String query = null;
        // Test wildcard query
        query = "*:*";
        Assert.assertSame ( "Test wildcard query",
                query,
                this.complexSolrSearcher.checkQuery ( "*" ) ) ;
        Assert.assertSame ( "Test wildcard query 2",
                query,
                this.complexSolrSearcher.checkQuery ( query ) ) ;
        // Test a query with an explicit field but with a star in the value
        query = "complex_alias:*" ;
        Assert.assertSame ( "Test field selected and star query in that",
                query,
                this.complexSolrSearcher.checkQuery ( query ) ) ;
        // Test a query with a star in the field but with an explicit value
        query = "*:IM08645" ;
        Assert.assertSame ( "Test field star and query selected",
                query,
                this.complexSolrSearcher.checkQuery ( query ) ) ;
        // Test null pointer exception
        query = null ;
        Throwable exception = null ;
        try {
            this.complexSolrSearcher.checkQuery ( query ) ;
        }
        catch ( Throwable e ) {
            exception = e ;
        }
        Assert.assertTrue("Test query is null", exception instanceof NullPointerException) ;
    }

    @Test
    public void testSetParameters() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        boolean hasParameters = true ;
        // Query
        solrQuery = this.complexSolrSearcher.setParameters(solrQuery) ;

        // Test parameters
        String parameterNames = solrQuery.getFields() ;
        System.err.println ( "Parameter Names: " + parameterNames ) ;
        /*for ( String parameter: this.complexSolrSearcher.defaultFields ) {
            if ( ! parameterNames.contains ( parameter ) ) {
                hasParameters = false ;
                System.err.println ( new StringBuilder ( )
                .append ( parameter ) .append ( " does not exist in defaultsFields" )
                .toString ( ) ) ;
            }
        }*/
        Assert.assertTrue ( "Test parameters", hasParameters ) ;
        // Test

    }

    @Test
    public void testSetFirstResult() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        Integer firstResult = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFirstResult ( solrQuery, firstResult ) ;
        Assert.assertSame ( "Test null parameter", 0, solrQuery.getStart ( ) ) ;
        // Test first result set to zero
        firstResult = 0 ;
        solrQuery = this.complexSolrSearcher.setFirstResult ( solrQuery, firstResult ) ;
        Assert.assertSame ( "Test first result is zero", firstResult, solrQuery.getStart ( ) ) ;
        // Test first result set to twelve
        firstResult = 12 ;
        solrQuery = this.complexSolrSearcher.setFirstResult ( solrQuery, firstResult ) ;
        Assert.assertSame ( "Test first result is twelve", firstResult, solrQuery.getStart ( ) ) ;
    }

    @Test
    public void testSetMaxResults() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        solrQuery.setStart ( 0 ) ;
        Integer maxParameters = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setMaxResults ( solrQuery, maxParameters ) ;
        Assert.assertSame ( "Test null parameter", Integer.MAX_VALUE, solrQuery.getRows ( ) ) ;
        // Test max parameter set to zero
        maxParameters = 0 ;
        solrQuery = this.complexSolrSearcher.setMaxResults ( solrQuery, maxParameters ) ;
        Assert.assertSame ( "Test max parameters is zero", maxParameters, solrQuery.getRows ( ) ) ;
        // Test max parameter set to twelve
        maxParameters = 12 ;
        solrQuery = this.complexSolrSearcher.setMaxResults ( solrQuery, maxParameters ) ;
        Assert.assertSame ( "Test max parameter is twelve", maxParameters, solrQuery.getRows ( ) ) ;
    }

    @Test
    public void testSetFilters() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        String [ ] empty = new String [ ] {} ;
        String [ ] filters = new String [ ] {"First", "*", "Second", "Third", "Forth", "*"} ;
        String [ ] filtersResult = new String [ ] {"First", "Second", "Third", "Forth"} ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, null ) ;
        Assert.assertSame ( "Test null parameter", new SolrQuery ( "*:*" ), solrQuery ) ;
        // Test filter array is empty
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, empty ) ;
        Assert.assertSame ( "Test filter array is empty", new SolrQuery ( "*:*" ), solrQuery ) ;
        // Test filter array has 4 elements: First, *, Second, Third, Forth and *
        // but he need to discard the * filters
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, filters ) ;
        String [ ] getedFilters = solrQuery.getFilterQueries ( ) ;
        Assert.assertSame ( "Test filter array check length", filtersResult.length, getedFilters.length );
        if ( filtersResult.length == getedFilters.length ) {
            for ( int i = 0 ; i < filtersResult.length; i++ ) {
                Assert.assertSame ( "Test filter array values", filtersResult [ i ], getedFilters [ i ] ) ;
            }
        }
    }

    @Test
    public void testSetFirstFacet() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        Integer firstFacet = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertSame ( "Test null parameter", 0, solrQuery.getFacetMinCount ( ) ) ;
        // Test first facet set to zero
        firstFacet = 0 ;
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertSame ( "Test first facet set to zero", firstFacet, solrQuery.getFacetMinCount ( ) ) ;
        // Test first facet set to twelve
        firstFacet = 12 ;
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertSame ( "Test first facet set to twelve", firstFacet, solrQuery.getFacetMinCount ( ) ) ;
    }

    @Test
    public void testSetMaxFacets() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        Integer maxFacets = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertSame ( "Test null parameter", 0, solrQuery.getFacetLimit ( ) ) ;
        // Test max facets set to zero
        maxFacets = 0 ;
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertSame ( "Test max facets set to zero", maxFacets, solrQuery.getFacetLimit ( ) ) ;
        // Test max facets set to twelve
        maxFacets = 12 ;
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertSame ( "Test max facets set to twelve", maxFacets, solrQuery.getFacetLimit ( ) ) ;
    }

    @Test
    public void testSetFacets() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        // Test without first facet and max facets

    }

    @Test
    public void testSetFields() throws Exception {

    }

    @Test
    public void testCheckNegativeFilter() throws Exception {

    }

    @Test
    public void testFormatQuery() throws Exception {

    }

    @Test
    public void testSearch() throws Exception {

    }

    @Test
    public void testSearchWithFilters() throws Exception {

    }

    @Test
    public void testSearchWithFacets() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        this.complexSolrSearcher.shutdown ( ) ;
    }
}
