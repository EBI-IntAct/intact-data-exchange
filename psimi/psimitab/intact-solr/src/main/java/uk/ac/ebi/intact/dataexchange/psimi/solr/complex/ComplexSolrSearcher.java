package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrLogger;


/**
 * Complex Solr Server that wraps a solrServer and allow to search
 * with filters and/or facets
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 29/07/13
 */
public class ComplexSolrSearcher {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private final Logger logger                     = LoggerFactory.getLogger( ComplexSolrSearcher.class ) ;
    protected SolrServer solrServer                 = null      ;
    protected String [ ] solrFields                 = null      ;
    protected String [ ] defaultFields              = null      ;
    // static attributes to set parameters to the query
    private final static String DISMAX_PARAM_NAME   = "qf"      ;
    private final static String DISMAX_TYPE         = "edismax" ;
    private final static String DEFAULT_MM_PARAM    = "mm"      ;
    private final static String QUERY_TYPE          = "defType" ;

    /*********************************/
    /*      Getters for statics      */
    /*********************************/
    public static String getDismaxParamName ( ) { return DISMAX_PARAM_NAME ; }
    public static String getDismaxType ( )      { return DISMAX_TYPE       ; }
    public static String getDefaultMmParam ( )  { return DEFAULT_MM_PARAM  ; }
    public static String getQueryType ( )       { return QUERY_TYPE        ; }

    /*************************/
    /*      Constructor      */
    /*************************/
    public void initFunction( ) {
        // initialize solr fields
        this.solrFields = new String [ ] {
                // Complex fields
                ComplexFieldNames.PUBLICATION_ID,    ComplexFieldNames.COMPLEX_ID,
                ComplexFieldNames.COMPLEX_NAME,      ComplexFieldNames.COMPLEX_ORGANISM,
                ComplexFieldNames.COMPLEX_ALIAS,     ComplexFieldNames.COMPLEX_TYPE,
                ComplexFieldNames.COMPLEX_XREF,      ComplexFieldNames.COMPLEX_AC,
                ComplexFieldNames.DESCRIPTION,       ComplexFieldNames.ORGANISM_NAME,
                // Interactor fields
                ComplexFieldNames.INTERACTOR_ID,     ComplexFieldNames.INTERACTOR_ALIAS,
                ComplexFieldNames.INTERACTOR_TYPE,
                // Other fields
                ComplexFieldNames.BIOROLE,           ComplexFieldNames.FEATURES,
                ComplexFieldNames.SOURCE,            ComplexFieldNames.NUMBER_PARTICIPANTS,
                //ComplexFieldNames.PATHWAY_XREF,      ComplexFieldNames.ECO_XREF,
        } ;
        // initialize default fields to search
        this.defaultFields = new String [ ] {
                ComplexFieldNames.COMPLEX_XREF,     ComplexFieldNames.COMPLEX_ID,
                ComplexFieldNames.COMPLEX_ALIAS,    ComplexFieldNames.INTERACTOR_ID,
                ComplexFieldNames.INTERACTOR_ALIAS
        } ;
        // Initialize the logger
        SolrLogger.readFromLog4j ( ) ;
    }

    public ComplexSolrSearcher ( SolrServer solrServer_ ) {
        if ( solrServer_ == null ) {
            throw new IllegalArgumentException ( "You must pass a not null SolrServer to create a new ComplexSorlServer" ) ;
        }
        this.solrServer = solrServer_ ;

        initFunction();
    }

    public ComplexSolrSearcher ( String solrUrl, int maxConnHost,
                                 int maxConnTotal, int maxRetries,
                                 boolean compresion ) {
        SchemeRegistry schemeRegistry = new SchemeRegistry( ) ;
        schemeRegistry.register ( new Scheme( "http",   80, PlainSocketFactory.getSocketFactory() ) ) ;
        schemeRegistry.register ( new Scheme ( "https", 443, org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory() ) ) ;
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setMaxTotal(maxConnTotal);
        cm.setDefaultMaxPerRoute(maxConnHost);
        HttpClient httpClient = new DefaultHttpClient(cm);
        HttpSolrServer server = new HttpSolrServer(solrUrl, httpClient);
        server.setMaxRetries(maxRetries);
        server.setAllowCompression(compresion);
        this.solrServer = server;

        initFunction();
    }

    /******************************************************/
    /*      Methods to interact with the Solr Server      */
    /******************************************************/
    // shutdown is a method to stop the Solr Server
    public void shutdown ( ) {
        if ( logger.isInfoEnabled ( ) ) logger.info ( "Shuting the Solr Server down in the Complex Solr Server" ) ;
        // If the solrServer is not null and is an instance of HttpSolrServer we can shut down it
        if ( this.solrServer != null && this.solrServer instanceof HttpSolrServer ) {
            ( ( HttpSolrServer ) this.solrServer ) .shutdown ( ) ;
        }
    }

    /**********************************************************/
    /*      Protected Methods to set query parameters up      */
    /**********************************************************/
    // checkQuery is a method to check if the query contains a wildcard or is null
    protected String checkQuery ( String query ) {
        if ( query == null ) throw new NullPointerException ( "You have not to search with a null query" ) ;
        // Format wildcard query
        if ( query.equals ( "*" ) ) query = "*:*" ;
        return query ;
    }

    // setParameters is a method to set the default fields in the query
    protected SolrQuery setParameters ( SolrQuery squery ) {
        // Create a proper String with default fields to search
        StringBuilder defaults = new StringBuilder ( ) ;
        for ( String field : defaultFields ){
            defaults .append ( field ) .append ( " " ) ;
        }
        // Use dismax parser for querying default fields
        squery.setParam ( DISMAX_PARAM_NAME, defaults .toString ( ) );
        // Set the query type parameters such as a dismax type
        squery.setParam ( QUERY_TYPE, DISMAX_TYPE ) ;
        // Set the default MM parameter with one
        squery.setParam ( DEFAULT_MM_PARAM, "1" ) ;
        return squery ;
    }

    // setFirstResult is a method to set the first result in the query (an integer)
    protected SolrQuery setFirstResult ( SolrQuery squery, Integer firstResult ) {
        squery.setStart ( firstResult != null ? firstResult : 0 ) ;
        return squery ;
    }

    // setMaxResults is a method to set the maximum result in the query (an integer)
    protected SolrQuery setMaxResults ( SolrQuery squery, Integer maxResults ) {
        // Set max results using the maxResult parameter
        // WARNING in solr 3.6
        // * an *NumberFormatException* occurs if _rows_ > 2147483647
        // * an *ArrayIndexOutOfBoundsException* occurs if _rows_ + _start_ > 2147483647; e.g. _rows_ = 2147483640 and _start_ = 8
        // we need to substract to avoid this exception
        squery.setRows(maxResults != null ? maxResults : Integer.MAX_VALUE - squery.getStart()) ;
        return squery ;
    }

    // setFilters is a method to set the filters in the query (a String array)
    protected SolrQuery setFilters ( SolrQuery squery, String [ ] filters ) {
        // If String array is not null and is longer than 0
        if ( filters != null && filters.length > 0 ){
            // Then we range the String array
            for ( String filter : filters ) {
                // Add the filter to the query if is not equal to *
                if ( ! filter.equals ( "*" ) ) {
                    squery.addFilterQuery ( filter ) ;
                }
            }
        }
        return squery ;
    }

    // setFirstFacet is a method to set the first result with facet (an integer)
    protected SolrQuery setFirstFacet ( SolrQuery squery, Integer firstFacet ) {
        squery.set ( FacetParams.FACET_OFFSET, firstFacet != null ? firstFacet : 0 ) ;
        return squery ;
    }

    // setMaxFacets is a method to set the maximum result in the query (an integer)
    protected SolrQuery setMaxFacets ( SolrQuery squery, Integer maxFacets ) {
        // set max results
        // WARNING in solr 3.6
        // * an *NumberFormatException* occurs if _rows_ > 2147483647
        // * an *ArrayIndexOutOfBoundsException* occurs if _rows_ + _start_ > 2147483647; e.g. _rows_ = 2147483640 and _start_ = 8
        // we need to substract to avoid this exception
        if ( maxFacets != null ) {
            squery.setFacetLimit ( maxFacets ) ;
        }
        else {
            if ( squery.getStart ( ) == null )  {
                squery.setStart ( 0 ) ;
            }
            squery.setFacetLimit ( Integer.MAX_VALUE - squery.getStart ( ) ) ;
        }
        return squery ;
    }

    // setFacets is a method to set the facets in the query (a String array)
    protected SolrQuery setFacets ( SolrQuery squery, String [ ] facets ) {
        // If String array is not null and is longer than 0
        if ( facets != null && facets.length > 0 ){
            // set faceting enable
            squery.setFacet ( true ) ;
            squery.setFacetMinCount ( 1 ) ;
            squery.setFacetSort ( FacetParams.FACET_SORT_COUNT ) ;
            squery.addFacetField ( facets ) ;
        }
        return squery ;
    }

    // setFacets is a method to set the facets, the first result with facets and the maximum result with facet
    protected SolrQuery setFacets ( SolrQuery squery, String [ ] facets, Integer firstFacet, Integer maxFacets ) {
        // call setFacets to set facets
        squery = setFacets ( squery, facets ) ;
        // and if facets String array is not null and is longer than 0
        if ( facets != null && facets.length > 0 ){
            // set the first result with facets
            squery = setFirstFacet ( squery, firstFacet ) ;
            // set the maximum result with facets
            squery = setMaxFacets ( squery, maxFacets ) ;
        }
        return squery ;
    }

    // indexFields is a method to set the fields (splited by ,) in the query
    protected SolrQuery setFields ( SolrQuery squery ) {
        String [ ] fields = null ;
        // Check if the query has fields
        String gotFields = squery.getFields() ;
        if ( gotFields != null ) {
            // Then add this fields to the query
            fields = ( String [ ] ) ArrayUtils.addAll(this.solrFields, gotFields.split(",")) ;
        }
        // Set the new fields
        squery.setFields ( fields ) ;
        return squery ;
    }

    // checkNegativeFilter is a method to check if the query has at least a negative filter
    protected SolrQuery checkNegativeFilter ( SolrQuery squery ) {
        Boolean hasNegative = false;
        String query = squery.getQuery ( ) ; // Getting the current query
        // String for check if the query has a negative filter
        String negative = new StringBuilder ( ) .append ( "negative" ) .append ( ":" ) .toString ( ) ;
        // If query has a field named such as the name stored is negative
        if ( query != null || query.contains ( negative ) ) { hasNegative = true ; }
        else {
            // Then the query does not have a negative filter but besides we need to check if exist in the filters
            String [ ] queries = squery.getFilterQueries ( ) ;
            // If queries is not null and is longer than 0
            if ( queries != null && queries.length > 0 ) {
                // Check all filters searching a negative filter
                for ( String filter : queries ) {
                    hasNegative = hasNegative || filter.contains ( negative ) ;
                }
            }
        }
        // If the query has negative filter we enable it
        if ( hasNegative ) {
            squery.addFilterQuery ( new StringBuilder ( ) .append ( negative )
                                        .append("false") .toString ( ) ) ;
        }
        return squery ;
    }

    // formatQuery is a method to expand the wildcard if the query has that
    protected String formatQuery ( String query ) {
        // Format the query with the fields saved in the solrFields attribute
        return formatQuery ( query, this.solrFields ) ;
    }

    // formatQuery is a method to expand the wildcard if the query has that with the Solr Fields passed such as parameters
    protected String formatQuery ( String query, String [] checkFields ) {
        boolean hasField ; // It will be used for check if has at least one of the checkFields
        // If query contains a wildcard we need to process that
        if ( query.contains ( "*" ) ) {
            String [ ] tokens = query.split ( " " ) ; // Get token in the query
            StringBuilder sb = new StringBuilder ( query.length ( ) ) ; // This StringBuilder will be the processed query
            // Check all generated tokens
            for ( String token : tokens ) {
                // If token contains * we need to check all available fields
                if ( token.contains ( "*" ) ){
                    // This loop ranges all checkFields and store in hasField if the token starts with anyone
                    hasField = false ;
                    for ( String check : checkFields ){
                        hasField = hasField || token.startsWith ( new StringBuilder ( ) .append ( check ) .append ( ":" ) .toString ( ) ) ;
                    }
                    // If the token starts with any checkFields
                    if ( hasField ) {
                        // Do not change que field name, but cast to lower case the value.
                        // For example: id:ABC-123 ---> id:abc-123
                        //                          or
                        //              ID:ABC-123 ---> ID:abc-123
                        int firstIndex = token.indexOf ( ":" ) ;
                        String prefix  = token.substring ( 0, firstIndex + 1 ) ;
                        String correctedValue = token.substring ( firstIndex + 1 ) .toLowerCase ( ) ;
                        sb.append ( prefix ) .append ( correctedValue ) ;
                    }
                    // The token does not start with any checkFields
                    else {
                        sb.append ( token.toLowerCase ( ) ) ;
                    }
                }
                // The token does not contain *
                else {
                    sb.append ( token ) ;
                }
                // Here we still are inside of the external for
                sb.append ( " " ) ;
            } // End of the external for
            // Create a new query and trim that
            query = sb .toString() .trim() ;
        } // End if query contains a wildcard then return que same query
        return query ;
    }

    /**************************************/
    /*      Protected Search Methods      */
    /**************************************/
    public ComplexResultIterator search ( SolrQuery solrQuery )
        throws SolrServerException {

        // Set default fields to search
        solrQuery = setFields ( solrQuery );
        // Format query to expand wildcard
        String query = formatQuery ( solrQuery.getQuery ( ) ) ;
        // Change all AND, OR and NOT to lower case and set it such as new query
        solrQuery.setQuery ( query
                            .replaceAll ( " and ", " AND " )
                            .replaceAll ( " or ",  " OR " )
                            .replaceAll ( " not ", " NOT " )
                            .replaceAll ( "^not ", "NOT " )
                           ) ;
        // Check if query has negative filters and if it has add a new filter query
        //solrQuery = checkNegativeFilter ( solrQuery ) ;

        // Send the query to the Solr Server and return the answer
        QueryResponse solrResponse = solrServer.query ( solrQuery ) ;
        return solrResponse != null ? new ComplexResultIterator ( solrResponse.getResults ( ) ) : null ;
    }


    /***********************************/
    /*      Public Search Methods      */
    /***********************************/

    // This method is for make easier search with filters. This filters can be null
    public ComplexResultIterator search ( String query,
                                         Integer firstResult,
                                         Integer maxResult,
                                         String queryFilter
                                       )
        throws SolrServerException {

        // Only call to other method but change query filter type.
        return searchWithFilters ( query,
                firstResult,
                maxResult,
                queryFilter != null ? new String[]{queryFilter} : null ) ;

    }

    // This method is for search with filters
    public ComplexResultIterator searchWithFilters ( String query,
                                                    Integer firstResult,
                                                    Integer maxResults,
                                                    String [ ] queryFilters
                                                  )
        throws SolrServerException {
        if ( logger.isInfoEnabled ( ) ) {
            logger.info ( new StringBuilder ( )
                    .append ( "Searching with filters; Query: " )
                    .append ( query )
                    .append ( " Filters: " )
                    .append ( queryFilters )
                    .toString ( ) ) ;
        }
        // First step, check query for null and wildcard
        query = checkQuery ( query ) ;

        // Create a new Solr Query using the query parameter
        SolrQuery solrQuery = new SolrQuery ( query ) ;

        // Set the parameters for the query
        solrQuery = setParameters ( solrQuery ) ;

        // Set first result using the firstResult parameter
        solrQuery = setFirstResult ( solrQuery, firstResult ) ;

        // Set Max result using the maxResults parameter
        solrQuery = setMaxResults ( solrQuery, maxResults ) ;
        
        // Apply any filter using queryFilter parameter
        solrQuery = setFilters ( solrQuery, queryFilters ) ;
        
        // Set query using the query parameter
        solrQuery.setQuery ( query ) ;

        return search ( solrQuery ) ;
    }

    public ComplexResultIterator searchWithFacets ( String query,
                                                   Integer firstResult,
                                                   Integer maxResults,
                                                   String [ ] queryFilters,
                                                   String [ ] facets,
                                                   Integer firstFacet,
                                                   Integer maxFacet
                                                 )
        throws SolrServerException {
        if ( logger.isInfoEnabled ( ) ) {
            logger.info ( new StringBuilder ( )
                    .append ( "Searching with filters and facets; Query: " )
                    .append ( query )
                    .append ( " Filters: " )
                    .append ( queryFilters )
                    .append ( " Facets: " )
                    .append ( facets )
                    .toString ( ) ) ;
        }
        // First step, check query for null and wildcard
        query = checkQuery ( query ) ;

        // Create a new Solr Query using the query parameter
        SolrQuery solrQuery = new SolrQuery ( query ) ;

        // Set the parameters for the query
        solrQuery = setParameters ( solrQuery ) ;

        // Set first result using the firstResult parameter
        solrQuery = setFirstResult ( solrQuery, firstResult ) ;

        // Set Max result using the maxResults parameter
        solrQuery = setMaxResults ( solrQuery, maxResults ) ;

        // Apply any filter using queryFilter parameter
        solrQuery = setFilters ( solrQuery, queryFilters ) ;

        // Set facets using the facets, firstFacet and maxFacet parameters
        solrQuery = setFacets ( solrQuery, facets, firstFacet, maxFacet ) ;

        // Set query using the query parameter
        solrQuery.setQuery ( query ) ;

        return search ( solrQuery ) ;
    }

}
