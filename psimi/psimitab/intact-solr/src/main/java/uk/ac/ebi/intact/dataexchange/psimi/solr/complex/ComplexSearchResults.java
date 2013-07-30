package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocumentList;

import java.util.Collections;
import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 30/07/13
 */
public class ComplexSearchResults {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private long numberResults;
    private SolrDocumentList results;
    private List<FacetField> facetFields;
    private String[] fieldNames;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSearchResults( SolrDocumentList results_, String[] fieldNames_) {
        this.results    = results_;
        this.fieldNames = fieldNames_;
        this.numberResults = (this.results != null ?  this.results.getNumFound() : 0);
    }

    public ComplexSearchResults( SolrDocumentList results_, String[] fieldNames_, List<FacetField> facetFields_) {
        this(results_, fieldNames_);
        this.facetFields = facetFields_;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public SolrDocumentList getSolrDocumentList()   { return results; }
    public long             getNumberResults()      { return numberResults; }
    public String[]         getFieldNames()         { return fieldNames; }
    public List<FacetField> getFacetFieldList() {
        return facetFields != null ? facetFields : Collections.EMPTY_LIST;
    }
}
