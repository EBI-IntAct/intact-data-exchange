package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import java.util.HashSet;
import java.util.Set;

/**
 * This class conatins the name of a term and its synonyms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/07/12</pre>
 */

public class OntologyNames {

    private String name;
    private Set<String> synonyms;

    public OntologyNames(String name){
        this.name = name;
        this.synonyms = new HashSet<String>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }
}
