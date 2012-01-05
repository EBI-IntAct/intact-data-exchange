package uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters;

import java.util.Set;

/**
 * This class contains parameters to write a GOLine
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DefaultGOParameters1 implements GOParameters{

    private String firstProtein;
    private String secondProtein;
    private Set<String> pubmedIds;

    public DefaultGOParameters1(String firstProtein, String secondProtein, Set<String> pubmedIds){

        this.firstProtein = firstProtein;
        this.secondProtein = secondProtein;
        this.pubmedIds = pubmedIds;
    }

    public String getFirstProtein() {
        return firstProtein;
    }

    public String getSecondProtein() {
        return secondProtein;
    }

    public Set<String> getPubmedIds() {
        return pubmedIds;
    }
}
