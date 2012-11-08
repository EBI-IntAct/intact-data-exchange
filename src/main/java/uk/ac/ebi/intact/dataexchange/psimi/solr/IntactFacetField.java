package uk.ac.ebi.intact.dataexchange.psimi.solr;

/**
 * Contains the facet field and some parameters for this facet field
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/09/12</pre>
 */

public class IntactFacetField {

    private Integer first;
    private Integer max;
    private String fieldName;

    public IntactFacetField(String fieldName, Integer first, Integer max){

        if (fieldName == null){
            throw  new IllegalArgumentException("FieldName should not be null");
        }

        this.fieldName = fieldName;
        this.first = first;
        this.max = max;
    }

    public Integer getFirst() {
        return first;
    }

    public Integer getMax() {
        return max;
    }

    public String getFieldName() {
        return fieldName;
    }
}
