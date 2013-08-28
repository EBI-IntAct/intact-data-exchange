package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

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

    @After
    public void tearDown() throws Exception {
        this.complexSolrSearcher.shutdown ( ) ;
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
        String dismaxParamName = solrQuery.get ( ComplexSolrSearcher.getDismaxParamName ( ) ) ;
        //System.err.println ( "Parameter Names: " + parameterNames ) ;
        for ( String parameter: this.complexSolrSearcher.defaultFields ) {
            if ( ! dismaxParamName.contains ( parameter ) ) {
                hasParameters = false ;
                System.err.println ( new StringBuilder ( )
                .append ( parameter ) .append ( " does not exist in defaultsFields" )
                .toString ( ) ) ;
            }
        }
        Assert.assertTrue ( "Test parameters default fields", hasParameters ) ;
        Assert.assertTrue ( "Test parameters query type", solrQuery.getParameterNames ( )
                .contains(ComplexSolrSearcher.getQueryType()) ) ;
        Assert.assertTrue ( "Test parameters query type dismax",
                solrQuery.get ( ComplexSolrSearcher.getQueryType ( ) )
                        .equals(ComplexSolrSearcher.getDismaxType()) ) ;
        Assert.assertTrue ( "Test parameters default mm", solrQuery.getParameterNames ( )
                .contains ( ComplexSolrSearcher.getDefaultMmParam() ) ) ;
        Assert.assertTrue ( "Test parameters query type dismax",
                solrQuery.get ( ComplexSolrSearcher.getDefaultMmParam() )
                        .equals ( "1" ) ) ;
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
        Assert.assertTrue ( "Test null parameter", Integer.MAX_VALUE == solrQuery.getRows ( ) ) ;
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
        String [ ] filters = new String [ ] {"First", "*", "Second", "Third", "Fourth", "*"} ;
        String [ ] filtersResult = new String [ ] {"First", "Second", "Third", "Fourth"} ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, null ) ;
        Assert.assertTrue("Test null parameter", new SolrQuery ( "*:*" ) .toString() .equals(solrQuery.toString()) ) ;
        // Test filter array is empty
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, empty ) ;
        Assert.assertTrue ( "Test filter array is empty", new SolrQuery ( "*:*" ).toString() .equals(solrQuery.toString()) ) ;
        // Test filter array has 4 elements: First, *, Second, Third, Forth and *
        // but he need to discard the * filters
        solrQuery = this.complexSolrSearcher.setFilters ( solrQuery, filters ) ;
        Assert.assertArrayEquals ( "Test filter array values", solrQuery.getFilterQueries ( ), filtersResult ) ;
    }

    @Test
    public void testSetFirstFacet() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        Integer firstFacet = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertTrue("Test null parameter", solrQuery.get(FacetParams.FACET_OFFSET).equals("0")) ;
        // Test first facet set to zero
        firstFacet = 0 ;
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertTrue("Test first facet set to zero", solrQuery.get(FacetParams.FACET_OFFSET).equals("0")) ;
        // Test first facet set to twelve
        firstFacet = 12 ;
        solrQuery = this.complexSolrSearcher.setFirstFacet ( solrQuery, firstFacet ) ;
        Assert.assertTrue("Test first facet set to twelve", solrQuery.get(FacetParams.FACET_OFFSET).equals("12")); ;
    }

    @Test
    public void testSetMaxFacets() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        Integer maxFacets = null ;
        // Test null parameter
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertTrue("Test null parameter", Integer.MAX_VALUE == solrQuery.getFacetLimit()) ;
        // Test max facets set to zero
        maxFacets = 0 ;
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertTrue ( "Test max facets set to zero", maxFacets == solrQuery.getFacetLimit()) ;
        // Test max facets set to twelve
        maxFacets = 12 ;
        solrQuery = this.complexSolrSearcher.setMaxFacets ( solrQuery, maxFacets ) ;
        Assert.assertTrue ( "Test max facets set to twelve", maxFacets == solrQuery.getFacetLimit()) ;
    }

    @Test
    public void testSetFacets() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        SolrQuery solrQueryCopy = solrQuery ;
        String [ ] emptyFacets = new String [ ] { } ;
        String [ ] facets = new String [ ] { "First", "Second", "Third", "Fourth" } ;

        //
        // Test without first facet and max facets
        //

        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, null ) ;
        Assert.assertEquals("Test null parameter", solrQueryCopy, solrQuery) ;
        // Test length of facets array is 0
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, emptyFacets ) ;
        Assert.assertEquals ( "Test length of facets array is 0", solrQueryCopy, solrQuery ) ;
        // Test four facets
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, facets ) ;
        Assert.assertTrue ( "Test if facet was enabled", solrQuery.toString ( ) .contains ( "facet=true" ) ) ;
        Assert.assertEquals ( "Test facet min count is equal to one", 1, solrQuery.getFacetMinCount ( ) );
        Assert.assertEquals ( "Test facet sort is equal to facetparams", FacetParams.FACET_SORT_COUNT, solrQuery.getFacetSortString ( ) ) ;
        Assert.assertArrayEquals ( "Test facet array", facets, solrQuery.getFacetFields ( ) ) ;

        //
        // Test with first facet and max facets
        //
        solrQuery = new SolrQuery ( "*:*" ) ;
        solrQueryCopy = solrQuery ;
        Integer firstFacet = 0, maxFacets = 256 ;

        // Test null parameter
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, null, firstFacet, maxFacets ) ;
        Assert.assertEquals("Test null parameter", solrQueryCopy, solrQuery) ;
        Assert.assertNull ( "Test null parameter firstFacet", solrQuery.get ( FacetParams.FACET_OFFSET ) ) ;
        Assert.assertEquals ( "Test null parameter maxFacets", 25, solrQuery.getFacetLimit ( ) ); ;
        // Test length of facets array is 0
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, emptyFacets, firstFacet, maxFacets ) ;
        Assert.assertEquals ( "Test length of facets array is 0", solrQueryCopy, solrQuery ) ;
        Assert.assertNull ( "Test length of facets array is 0 firstFacet", solrQuery.get ( FacetParams.FACET_OFFSET ) ) ;
        Assert.assertEquals ( "Test length of facets array is 0 maxFacets", 25, solrQuery.getFacetLimit ( ) ); ;
        // Test four facets
        solrQuery = this.complexSolrSearcher.setFacets ( solrQuery, facets, firstFacet, maxFacets ) ;
        Assert.assertTrue ( "Test if facet was enabled", solrQuery.toString ( ) .contains ( "facet=true" ) ) ;
        Assert.assertEquals ( "Test facet min count is equal to one", 1, solrQuery.getFacetMinCount ( ) );
        Assert.assertEquals ( "Test facet sort is equal to facetparams", FacetParams.FACET_SORT_COUNT, solrQuery.getFacetSortString ( ) ) ;
        Assert.assertArrayEquals ( "Test facet array", facets, solrQuery.getFacetFields ( ) ) ;
        Assert.assertEquals("Test facet array firstFacet", firstFacet, Integer.valueOf(solrQuery.get(FacetParams.FACET_OFFSET))) ;
        Assert.assertEquals ( "Test facet array maxFacets", maxFacets.intValue ( ) , solrQuery.getFacetLimit ( ) ) ;
    }

    @Test
    public void testSetFields() throws Exception {
        SolrQuery solrQuery = new SolrQuery ( "*:*" ) ;
        String fields = "First,Second,Third,Fourth" ;
        // Test fields is null
        solrQuery = this.complexSolrSearcher.setFields ( solrQuery ) ;
        Assert.assertNull ( "Test null fields", solrQuery.getFields ( ) ) ;
        // Test fields is First,Second,Third,Forth
        solrQuery.setFields ( fields ) ;
        solrQuery = this.complexSolrSearcher.setFields ( solrQuery ) ;
        Assert.assertArrayEquals ( "Test fields is First,Second,Third,Fourth",
                solrQuery.getFields ( ) .split ( "," ) ,
                ArrayUtils.addAll ( this.complexSolrSearcher.solrFields,
                        fields.split ( "," ) ) );
    }

    @Test
    public void testFormatQuery() throws Exception {
        String query1 = "*", query1_answer ;
        String query1_Ranswer = "*" ;
        String query2 = "complex_id:ACB-* complex_alias:BlAbLa#12", query2_answer ;
        String query2_Ranswer = "complex_id:acb-* complex_alias:BlAbLa#12" ;
        String query3 = "complex_id:ACB-* interactor_alias:* complex_alias:BlAbLa#12 Not_In_The_List:TeSt* Not_In_The_List:TeSt2", query3_answer ;
        String query3_Ranswer = "complex_id:acb-* interactor_alias:* complex_alias:BlAbLa#12 not_in_the_list:test* Not_In_The_List:TeSt2" ;
        String query4 = "complex_id:ACB-123 complex_alias:BlAbLa#12", query4_answer ;
        String query4_Ranswer = "complex_id:ACB-123 complex_alias:BlAbLa#12" ;

        // Test query 1
        query1_answer = this.complexSolrSearcher.formatQuery ( query1 ) ;
        //System.err.println ( "Query 1:" + query1_answer ) ;
        Assert.assertTrue ( "Test query 1", query1_answer.equals ( query1_Ranswer ) ) ;
        // Test query 2
        query2_answer = this.complexSolrSearcher.formatQuery ( query2 ) ;
        //System.err.println ( "Query 2:" + query2_answer ) ;
        Assert.assertTrue ( "Test query 2", query2_answer.equals ( query2_Ranswer ) ) ;
        // Test query 3
        query3_answer = this.complexSolrSearcher.formatQuery ( query3 ) ;
        //System.err.println ( "Query 3:" + query3_answer ) ;
        Assert.assertTrue ( "Test query 3", query3_answer.equals ( query3_Ranswer ) ) ;
        // Test query 4
        query4_answer = this.complexSolrSearcher.formatQuery ( query4 ) ;
        //System.err.println ( "Query 4:" + query4_answer ) ;
        Assert.assertTrue ( "Test query 4", query4_answer.equals ( query4_Ranswer ) ) ;
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
}
