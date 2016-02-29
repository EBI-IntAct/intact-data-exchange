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

public class GOParameters2 implements GOParameters {
    
    private String masterProtein;
    private String firstProtein;
    private String secondProtein;
    private String pubmedId;
    
    private Set<String> componentXrefs;
    
    public GOParameters2(String firstProtein, String secondProtein, String pubmedIds, String masterProtein, Set<String> componentXrefs) {
        this.firstProtein = firstProtein;
        this.secondProtein = secondProtein;

        this.masterProtein = masterProtein;

        if (componentXrefs == null){
            this.componentXrefs = new HashSet<String>();
        }
        else {
            this.componentXrefs = componentXrefs;
        }

        this.pubmedId = pubmedIds;
    }

    public String getMasterProtein() {
        return masterProtein;
    }

    public Set<String> getComponentXrefs() {
        return componentXrefs;
    }

    @Override
    public String getFirstProtein() {
        return this.firstProtein;
    }

    @Override
    public String getSecondProtein() {
        return this.secondProtein;
    }

    public String getPubmedId() {
        return this.pubmedId;
    }
}
