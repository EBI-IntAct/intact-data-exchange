package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import java.net.URL;

/**
 * Convenience class to map the URLs to complex names.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 24/07/13
 */
public class ComplexMapping {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private String name;
    private URL url;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexMapping(String name_, URL url_){ this.name = name_; this.url = url_; }

    /*********************/
    /*      Getters      */
    /*********************/
    public String getName() { return name; }
    public URL getUrl()     { return url;  }

    /******************************/
    /*      Override Methods      */
    /******************************/
    //@Override
    public boolean equals( ComplexMapping other ){
        // Object level comparisons
        if ( this == other ) return true;
        if ( other == null || getClass() != other.getClass() ) return false;

        // ComplexMapping level comparisons
        ComplexMapping otherComplexMapping = (ComplexMapping) other;
        if ( name != null ? !name.equals(otherComplexMapping.name) : otherComplexMapping.name != null ) return false;
        if ( url  != null ? !url.equals(otherComplexMapping.url)   : otherComplexMapping.url  != null ) return false;

        // If all checked is equal return true
        return true;
    }

    @Override
    public int hashCode(){
        int result = 0;

        result =                (name != null ? name.hashCode() : 0);
        result = 31 * result +  (url  != null ? url.hashCode()  : 0);

        return result;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append("ComplexMapping{");
        sb.append("name = '").append(name).append("'");
        sb.append("url  = '").append(url).append("'");
        sb.append("}");
        return sb.toString();
    }
}
