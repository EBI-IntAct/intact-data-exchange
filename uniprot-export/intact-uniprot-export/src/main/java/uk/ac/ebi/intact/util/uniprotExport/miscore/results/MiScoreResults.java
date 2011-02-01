package uk.ac.ebi.intact.util.uniprotExport.miscore.results;

import java.util.HashSet;
import java.util.Set;

/**
 * Results of the uniprot export using mi cluster score
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class MiScoreResults {

    private IntActInteractionClusterScore clusterScore;
    private MiClusterContext clusterContext;
    private Set<Integer> interactionsToExport;

    public MiScoreResults(IntActInteractionClusterScore clusterScore, MiClusterContext clusterContext, Set<Integer> interactionsToExport){
        if (clusterScore == null){
             throw  new IllegalArgumentException("The mi cluster object must be non null.");
        }
        if (clusterContext == null){
             throw  new IllegalArgumentException("The mi cluster context must be non null.");
        }
        if (interactionsToExport == null){
             throw  new IllegalArgumentException("The set of interaction ids to export must be non null.");
        }

        this.clusterContext = clusterContext;
        this.clusterScore = clusterScore;
        this.interactionsToExport = interactionsToExport;
    }

    public MiScoreResults(IntActInteractionClusterScore clusterScore, MiClusterContext clusterContext){
        if (clusterScore == null){
             throw  new IllegalArgumentException("The mi cluster object must be non null.");
        }
        if (clusterContext == null){
             throw  new IllegalArgumentException("The mi cluster context must be non null.");
        }

        this.clusterContext = clusterContext;
        this.clusterScore = clusterScore;
        this.interactionsToExport = new HashSet<Integer>();
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    public MiClusterContext getClusterContext() {
        return clusterContext;
    }

    public Set<Integer> getInteractionsToExport() {
        return interactionsToExport;
    }

    public void setInteractionsToExport(Set<Integer> interactionsToExport) {
        if (interactionsToExport != null){
            this.interactionsToExport = interactionsToExport;
        }
    }
}
