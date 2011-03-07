package uk.ac.ebi.intact.util.uniprotExport.results;

import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the cluster of interactions and the set of clustered interaction identifiers to be exported
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/03/11</pre>
 */

public class ExportedClusteredInteractions {

    /**
     * The mi cluster
     */
    private IntactCluster clusterScore;

    /**
     * The interaction identifiers we want to export from the cluster
     */
    private Set<Integer> interactionsToExport;

    public ExportedClusteredInteractions(IntactCluster clusterScore, Set<Integer> interactionsToExport){
        if (clusterScore == null){
             throw  new IllegalArgumentException("The mi cluster object must be non null.");
        }
        if (interactionsToExport == null){
             throw  new IllegalArgumentException("The set of interaction ids to export must be non null.");
        }

        this.clusterScore = clusterScore;
        this.interactionsToExport = interactionsToExport;
    }

    public ExportedClusteredInteractions(IntactCluster clusterScore){
        if (clusterScore == null){
             throw  new IllegalArgumentException("The mi cluster object must be non null.");
        }

        this.clusterScore = clusterScore;
        this.interactionsToExport = new HashSet<Integer>();
    }

    /**
     *
     * @return the cluster with computed mi scores for each interaction
     */
    public IntactCluster getCluster() {
        return clusterScore;
    }

    /**
     *
     * @return the list of Encore identifiers of the interactions which will be exported
     */
    public Set<Integer> getInteractionsToExport() {
        return interactionsToExport;
    }

    public void setInteractionsToExport(Set<Integer> interactionsToExport) {
        if (interactionsToExport != null){
            this.interactionsToExport = interactionsToExport;
        }
    }
}
