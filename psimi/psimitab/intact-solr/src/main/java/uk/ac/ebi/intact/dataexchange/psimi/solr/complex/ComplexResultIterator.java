package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is for manage the complex results of a search
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 02/08/13
 */
public class ComplexResultIterator implements Iterator <ComplexSearchResults> {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexResultIterator.class);

    /********************************/
    /*      Private attributes      */
    /********************************/
    private final Iterator <SolrDocument> iterator;
    private Map<String,List<FacetField.Count>> facetFields = null ;
    // we have been prepared the next value
    private ComplexSearchResults nextResult;
    private final long numberOfResults;
    private long totalNumberOfResults = 0L;
    private final ObjectMapper mapper;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexResultIterator ( SolrDocumentList results ) {
        this.mapper = new ObjectMapper();
        if ( results != null ) {
            numberOfResults = results.size ( ) ;
            iterator = results.iterator ( ) ;
            nextResult = iterator.hasNext ( ) ? getFieldValues ( ) : null ;
        }
        else {
            throw new IllegalArgumentException ( "You must pass a SolrDocumentList not null to create a ComplexResultIterator" ) ;
        }
    }

    public ComplexResultIterator ( SolrDocumentList results, List<FacetField> facets ) {
        this(results);
        this.facetFields = new HashMap<String, List<FacetField.Count>>();
        for ( FacetField f : facets ) {
            this.facetFields.put(f.getName(), f.getValues());
        }
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public Iterator < SolrDocument > getIterator ( ) { return iterator ; }
    public long getNumberOfResults ( ) { return numberOfResults ; }
    public void setTotalNumberOfResults ( long total ) { totalNumberOfResults = total; }
    public long getTotalNumberOfResults ( ) { return totalNumberOfResults; }
    public Map<String,List<FacetField.Count>> getFacetFields() { return facetFields; }
    public List<FacetField.Count> getFacetField( String facetFieldName ) {
        if ( this.facetFields != null && facetFieldName != null ) {
            return this.facetFields.get(facetFieldName);
        }
        return null;
    }

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

    // getFieldValues is a method to retrieve data for a specific field
    protected Boolean getBooleanFieldValue ( SolrDocument solrDocument, String field ) {
        StringBuilder result = new StringBuilder ( ) ;
        Collection < Object > fieldValues = solrDocument.getFieldValues ( field ) ;
        // If the SolrDocument has data for this field
        if ( fieldValues != null && fieldValues.size() == 1 ) {
            // We iterate through the data and append to the result String
            return (Boolean) fieldValues.iterator().next();
        }
        return null;
    }

    protected List<String> getListOfFieldValues(SolrDocument solrDocument, String field) {
        List<String> result = new ArrayList<>();
        Collection<Object> fieldValues = solrDocument.getFieldValues(field);
        // If the SolrDocument has data for this field
        if (fieldValues != null && ! fieldValues.isEmpty()) {
            // We iterate through the data and append to the result String
            for (Object fieldValue : fieldValues) {
                result.add(String.valueOf(fieldValue));
            }
        }
        return result;
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
        result.setComplexAC(getFieldValues(solrDocument, ComplexFieldNames.COMPLEX_AC));
        result.setPredictedComplex(getBooleanFieldValue(solrDocument, ComplexFieldNames.PREDICTED_COMPLEX));
        result.setComplexName(getFieldValues(solrDocument, ComplexFieldNames.COMPLEX_NAME));
        result.setOrganismName(getFieldValues(solrDocument, ComplexFieldNames.ORGANISM_NAME));
        result.setDescription(getFieldValues(solrDocument, ComplexFieldNames.DESCRIPTION));
        result.setInteractors(getInteractors(solrDocument));

        return result ;
    }

    /*******************************/
    /*      Private Methods        */
    /*******************************/
    private List<ComplexInteractor> getInteractors(SolrDocument solrDocument) {
        return getListOfFieldValues(solrDocument, ComplexFieldNames.SERIALISED_INTERACTION).stream()
                .map(serialisedInteractor -> {
                    try {
                        return mapper.readValue(serialisedInteractor, ComplexInteractor.class);
                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to deserialise interactor", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

