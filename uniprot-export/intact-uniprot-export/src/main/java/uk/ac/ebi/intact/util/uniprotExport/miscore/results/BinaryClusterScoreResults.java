package uk.ac.ebi.intact.util.uniprotExport.miscore.results;

import uk.ac.ebi.intact.util.uniprotExport.results.ExportContext;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class BinaryClusterScoreResults implements UniprotExportResults{

    private BinaryClusterScore clusterScore;
    private MiClusterContext clusterContext;
    private Set<Integer> interactionsToExport;

    public BinaryClusterScoreResults(BinaryClusterScore clusterScore, MiClusterContext clusterContext, Set<Integer> interactionsToExport){
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

    public BinaryClusterScoreResults(BinaryClusterScore clusterScore, MiClusterContext clusterContext){
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

    @Override
    public BinaryClusterScore getCluster() {
        return this.clusterScore;
    }

    @Override
    public ExportContext getExportContext() {
        return this.clusterContext;
    }

    @Override
    public Set<Integer> getInteractionsToExport() {
        return this.interactionsToExport;
    }

    @Override
    public void setInteractionsToExport(Set<Integer> interactionsToExport) {
        if (interactionsToExport != null){
            this.interactionsToExport = interactionsToExport;
        }
    }
}
