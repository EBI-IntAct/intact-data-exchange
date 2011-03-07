package uk.ac.ebi.intact.util.uniprotExport.results;

import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Results of the uniprot export using mi cluster score
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class MiClusterScoreResults implements UniprotExportResults{

    /**
     * The mi cluster
     */
    private IntactCluster clusterScore;

    /**
     * The mi cluster containing negative interactions
     */
    private IntactCluster negativeClusterScore;

    /**
     * The context for the mi cluster
     */
    private MiClusterContext clusterContext;

    /**
     * The interaction identifiers we want to export from the cluster
     */
    private Set<Integer> interactionsToExport;

    /**
     * The negative interaction identifiers we want to export from the cluster
     */
    private Set<Integer> negativeInteractionsToExport;

    public MiClusterScoreResults(IntactCluster clusterScore, IntactCluster negativeClusterScore, MiClusterContext clusterContext, Set<Integer> interactionsToExport){
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
        this.negativeInteractionsToExport = new HashSet<Integer>();
        this.negativeClusterScore = negativeClusterScore != null ? negativeClusterScore : new IntActInteractionClusterScore();
    }

    public MiClusterScoreResults(IntactCluster clusterScore, IntactCluster negativeClusterScore, MiClusterContext clusterContext, Set<Integer> interactionsToExport, Set<Integer> negativeInteractionsToExport){
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
        this.negativeInteractionsToExport = negativeInteractionsToExport != null ? negativeInteractionsToExport : new HashSet<Integer>();
        this.negativeInteractionsToExport = new HashSet<Integer>();
        this.negativeClusterScore = negativeClusterScore != null ? negativeClusterScore : new IntActInteractionClusterScore();
    }

    public MiClusterScoreResults(IntactCluster clusterScore, IntactCluster negativeClusterScore, MiClusterContext clusterContext){
        if (clusterScore == null){
             throw  new IllegalArgumentException("The mi cluster object must be non null.");
        }
        if (clusterContext == null){
             throw  new IllegalArgumentException("The mi cluster context must be non null.");
        }

        this.clusterContext = clusterContext;
        this.clusterScore = clusterScore;
        this.interactionsToExport = new HashSet<Integer>();
        this.negativeInteractionsToExport = new HashSet<Integer>();
        this.negativeClusterScore = negativeClusterScore != null ? negativeClusterScore : new IntActInteractionClusterScore();
    }

    /**
     *
     * @return the cluster with computed mi scores for each interaction
     */
    public IntactCluster getCluster() {
        return clusterScore;
    }

    @Override
    public IntactCluster getNegativeCluster() {
        return null;
    }

    /**
     *
     * @return the context of the cluster
     */
    public MiClusterContext getExportContext() {
        return clusterContext;
    }

    /**
     *
     * @return the list of Encore identifiers of the interactions which will be exported
     */
    public Set<Integer> getInteractionsToExport() {
        return interactionsToExport;
    }

    /**
     *
     * @return the list of Encore identifiers of the negative interactions which will be exported
     */
    public Set<Integer> getNegativeInteractionsToExport() {
        return negativeInteractionsToExport;
    }

    public void setInteractionsToExport(Set<Integer> interactionsToExport) {
        if (interactionsToExport != null){
            this.interactionsToExport = interactionsToExport;
        }
    }

    public void setNegativeInteractionsToExport(Set<Integer> interactionsToExport) {
        if (negativeInteractionsToExport != null){
            this.negativeInteractionsToExport = interactionsToExport;
        }
    }
}
