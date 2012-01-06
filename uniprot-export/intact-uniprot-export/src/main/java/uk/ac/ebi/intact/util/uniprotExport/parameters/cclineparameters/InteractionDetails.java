package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.Set;

/**
 * The interaction details are interaction type, detection method and set of pubmed ids
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface InteractionDetails extends Comparable<InteractionDetailsImpl>{

    /**
     * The interaction type
     * @return
     */
    public String getInteractionType();

    /**
     * The detection method
     * @return
     */
    public String getDetectionMethod();

    /**
     * the boolean value to know if it is spoke expanded
     * @return
     */
    public boolean isSpokeExpanded();

    /**
     * The list of pubmed ids
     * @return
     */
    public Set<String> getPubmedIds();
}
