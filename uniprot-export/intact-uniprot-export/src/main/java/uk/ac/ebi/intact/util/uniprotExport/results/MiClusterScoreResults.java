package uk.ac.ebi.intact.util.uniprotExport.results;

import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

/**
 * Results of the uniprot export using mi cluster score
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class MiClusterScoreResults implements UniprotExportResults{

    /**
     * The mi cluster for positive interactions
     */
    private ExportedClusteredInteractions positiveClusteredInteractions;

    /**
     * The mi cluster containing negative interactions
     */
    private ExportedClusteredInteractions negativeClusteredInteractions;

    /**
     * The context for the mi cluster
     */
    private MiClusterContext clusterContext;

    public MiClusterScoreResults(ExportedClusteredInteractions positiveInteractions, ExportedClusteredInteractions negativeInteractions, MiClusterContext clusterContext){
        if (positiveInteractions == null){
            throw  new IllegalArgumentException("The mi cluster object containing the results of the export for positive interactions must be non null.");
        }
        if (clusterContext == null){
            throw  new IllegalArgumentException("The mi cluster context must be non null.");
        }

        this.clusterContext = clusterContext;
        this.positiveClusteredInteractions = positiveInteractions;
        this.negativeClusteredInteractions = negativeInteractions != null ? negativeInteractions : new ExportedClusteredInteractions(new IntActInteractionClusterScore());
    }

    public MiClusterScoreResults(ExportedClusteredInteractions positiveInteractions, MiClusterContext clusterContext){
        if (positiveInteractions == null){
            throw  new IllegalArgumentException("The mi cluster object containing the results of the export for positive interactions must be non null.");
        }
        if (clusterContext == null){
            throw  new IllegalArgumentException("The mi cluster context must be non null.");
        }

        this.clusterContext = clusterContext;
        this.positiveClusteredInteractions = positiveInteractions;
        this.negativeClusteredInteractions = new ExportedClusteredInteractions(new IntActInteractionClusterScore());
    }

    @Override
    public ExportedClusteredInteractions getPositiveClusteredInteractions() {
        return this.positiveClusteredInteractions;
    }

    @Override
    public ExportedClusteredInteractions getNegativeClusteredInteractions() {
        return this.negativeClusteredInteractions;
    }

    /**
     *
     * @return the context of the cluster
     */
    @Override
    public MiClusterContext getExportContext() {
        return clusterContext;
    }
}
