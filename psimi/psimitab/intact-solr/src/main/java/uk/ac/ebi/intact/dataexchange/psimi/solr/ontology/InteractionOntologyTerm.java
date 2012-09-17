package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

/**
 * contains term name and identifier and the interaction results
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/08/12</pre>
 */

public class InteractionOntologyTerm {

    private String name;
    private String identifier;
    private InteractionOntologyTermResults results;

    public InteractionOntologyTerm(String name, String identifier){
        this.name = name;
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteractionOntologyTerm that = (InteractionOntologyTerm) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public InteractionOntologyTermResults getResults() {
        return results;
    }

    public void setResults(InteractionOntologyTermResults results) {
        this.results = results;
    }
}
