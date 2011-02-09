package uk.ac.ebi.intact.util.uniprotExport.results;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Map<Integer, BinaryInteraction<Interactor>> getBinaryInteractionCluster();

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

    /**
     *
     * @return map associating a uniprot ac to a list of clustered interaction identifiers
     */
    public Set<Integer> getAllInteractionIds();

    /**
     * Saves the results of the cluster in a file (which will give a clustered mitab file)
     * @param fileName
     */
    public void saveCluster(String fileName);

    /**
     * Save the specific clustered interactions in a mitab file
     * @param fileName
     * @param interactionIds
     */
    public void saveClusteredInteractions(String fileName, Set<Integer> interactionIds);
}
