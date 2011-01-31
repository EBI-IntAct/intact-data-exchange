package uk.ac.ebi.intact.util.uniprotExport.parameters;

import java.util.Set;

/**
 * This class contains parameters to write a GOLine
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class GOParameters {

    private String firstProtein;
    private String secondProtein;
    private Set<String> pubmedIds;

    public GOParameters (String firstProtein, String secondProtein, Set<String> pubmedIds){

        if (firstProtein == null){
            throw new IllegalArgumentException("A valid GO parameter has a non null first protein");
        }
        else if (secondProtein == null){
            throw new IllegalArgumentException("A valid GO parameter has a non null second protein");
        }
        else if (pubmedIds == null){
            throw new IllegalArgumentException("A valid GO parameter has a non null set of Pubmed Ids");
        }
        else if (pubmedIds.isEmpty()){
            throw new IllegalArgumentException("A valid GO parameter has a non empty set of Pubmed Ids");
        }

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
