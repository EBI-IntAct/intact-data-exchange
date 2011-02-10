package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;

/**
 * SecondCCParameters2 contains the information of a basic SecondCCParameters plus a sorted list of interaction details
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public interface SecondCCParameters2 extends BasicSecondCCParameters{

    /**
     * the organism name of the second interactor
     * @return
     */
    public String getOrganismName();

    /**
     * the list of interaction details
     * @return
     */
    public SortedSet<InteractionDetails> getInteractionDetails();
}
