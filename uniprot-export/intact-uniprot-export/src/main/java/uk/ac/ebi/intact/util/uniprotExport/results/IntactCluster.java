package uk.ac.ebi.intact.util.uniprotExport.results;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.List;
import java.util.Map;

/**
 * Interface to implement for classes which contains clustered interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public interface IntactCluster {

    /**
     *
     * @return map associating clustered interaction identifier associated with a binary interaction
     */
    public Map<Integer, BinaryInteraction> getBinaryInteractionCluster();

    /**
     *
     * @return map associating clustered interaction identifier associated with a Encore interaction
     */
    public Map<Integer, EncoreInteraction> getEncoreInteractionCluster();

    /**
     *
     * @return map associating a uniprot ac to a list of clustered interaction identifiers
     */
    public Map<String, List<Integer>> getInteractorCluster();
}
