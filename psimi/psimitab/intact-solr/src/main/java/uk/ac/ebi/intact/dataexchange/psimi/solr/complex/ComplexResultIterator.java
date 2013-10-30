package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class is for manage the complex results of a search
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 02/08/13
 */
public class ComplexResultIterator implements Iterator <ComplexSearchResults> {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private Iterator < SolrDocument > iterator  = null ;
    // we have been prepared the next value
    private ComplexSearchResults nextResult     = null ;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexResultIterator ( SolrDocumentList results ) {
        if ( results != null ) {
            iterator = results.iterator ( ) ;
            nextResult = iterator.hasNext ( ) ? getFieldValues ( ) : null ;
        }
        else {
            throw new IllegalArgumentException ( "You must pass a SolrDocumentList not null to create a ComplexResultIterator" ) ;
        }
    }


    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public Iterator < SolrDocument > getIterator ( ) { return iterator      ; }

    /*******************************/
    /*      Protected Methods      */
    /*******************************/
    // getFieldValues is a method to retrieve data for a specific field
    protected String getFieldValues ( SolrDocument solrDocument, String field ) {
        StringBuilder result = new StringBuilder ( ) ;
        Collection < Object > fieldValues = solrDocument.getFieldValues ( field ) ;
        // If the SolrDocument has data for this field
        if ( fieldValues != null && ! fieldValues.isEmpty ( ) ) {
            // We iterate through the data and append to the result String
            Iterator < Object > valueIterator = fieldValues.iterator ( ) ;
            while ( valueIterator.hasNext ( ) ) {
                result .append ( valueIterator.next ( ) ) .append ( " " ) ;
            }
        }
        return result .toString ( ) .trim ( ) ;
    }

    // This method was only used for testing
    protected String getAllFieldsValues ( SolrDocument solrDocument ) {
        StringBuilder result = new StringBuilder ( ) ;
        for ( String field : solrDocument.getFieldNames ( ) ) {
            result.append(field).append(":").append(getFieldValues(solrDocument,field)).append("\n");
        }
        return result.toString();
    }

    // getFieldValues is a method to retrieve data for this specific fields
    protected ComplexSearchResults getFieldValues ( ) {
        // How we created this ComplexSearchResults without parameters, we need to set the values using getters
        ComplexSearchResults result = new ComplexSearchResults( ) ;
        SolrDocument solrDocument = iterator.next ( ) ;

        // Get field values for this specific fields
        result.setComplexAC      ( getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_AC    ) ) ;
        result.setComplexName    ( getFieldValues ( solrDocument, ComplexFieldNames.COMPLEX_NAME  ) ) ;
        result.setCuratedComplex ( getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION   ) ) ;
        result.setOrganismName   ( getFieldValues ( solrDocument, ComplexFieldNames.ORGANISM_NAME ) ) ;
        result.setDescription    ( getFieldValues ( solrDocument, ComplexFieldNames.DESCRIPTION   ) ) ;

        return result ;
    }

    /******************************/
    /*      Override Methods      */
    /******************************/
    @Override
    public boolean hasNext ( ) {
        return nextResult != null ;
    }

    @Override
    public ComplexSearchResults next ( ) {
        ComplexSearchResults result = null ;
        if ( iterator != null && iterator.hasNext ( ) ) {
            // Swap values between nextResult and this result
            ComplexSearchResults swap = nextResult ;
            nextResult = getFieldValues ( ) ;
            result = swap ;
        }
        else {
            if ( nextResult != null ) {
                result = nextResult;
                nextResult = null ;
            }
            else { result = null ; }
        }
        return result ;
    }

    @Override
    public void remove ( ) {
        throw new UnsupportedOperationException ( "This method is unsupported in ComplexResultIterator class" ) ;
    }
}

