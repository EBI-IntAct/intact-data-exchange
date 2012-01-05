package uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters;

import java.util.HashSet;
import java.util.Set;

/**
 * Parameters to write a GO line, format 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/01/12</pre>
 */

public class DefaultGOParameters2 extends DefaultGOParameters1{
    
    private String masterProtein;
    
    private Set<String> componentXrefs;
    
    public DefaultGOParameters2(String firstProtein, String secondProtein, Set<String> pubmedIds, String masterProtein, Set<String> componentXrefs) {
        super(firstProtein, secondProtein, pubmedIds);

        this.masterProtein = masterProtein;

        if (componentXrefs == null){
            this.componentXrefs = new HashSet<String>();
        }
        else {
            this.componentXrefs = componentXrefs;
        }
    }

    public String getMasterProtein() {
        return masterProtein;
    }

    public Set<String> getComponentXrefs() {
        return componentXrefs;
    }
}
