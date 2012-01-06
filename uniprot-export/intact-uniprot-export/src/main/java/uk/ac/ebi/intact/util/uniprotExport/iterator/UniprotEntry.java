package uk.ac.ebi.intact.util.uniprotExport.iterator;

import java.util.HashSet;
import java.util.Set;

/**
 * A uniprot entry contains a master uniprot ac, a set of uniprot acs which are the actual positiveInteractors for this uniprot entry and another set of uniprot acs for the negative interactors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/01/12</pre>
 */

public class UniprotEntry {
    
    private String masterUniprot;
    private Set<String> positiveInteractors;
    private Set<String> negativeInteractors;

    public UniprotEntry(String masterUniprot){
        positiveInteractors = new HashSet<String>();
    }

    public String getMasterUniprot() {
        return masterUniprot;
    }

    public Set<String> getPositiveInteractors() {
        return positiveInteractors;
    }

    public Set<String> getNegativeInteractors() {
        return negativeInteractors;
    }
}
