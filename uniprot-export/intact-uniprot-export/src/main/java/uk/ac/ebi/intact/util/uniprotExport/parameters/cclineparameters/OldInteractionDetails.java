package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

/**
 * Previous interaction details for the old CC line format
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09/02/11</pre>
 */

public class OldInteractionDetails implements InteractionDetails{
    private int numberOfExperiments;

    public OldInteractionDetails(int numberOfExperiments){

        this.numberOfExperiments = numberOfExperiments;

    }

    public int getNumberOfExperiments() {
        return numberOfExperiments;
    }
}
