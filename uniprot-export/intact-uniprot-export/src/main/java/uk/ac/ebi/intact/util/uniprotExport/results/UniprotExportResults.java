package uk.ac.ebi.intact.util.uniprotExport.results;

import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.ExportContext;

import java.util.Set;

/**
 * Interface for classes containing results of uniprot export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public interface UniprotExportResults {

   /**
     *
     * @return the clustered interactions
     */
    public IntactCluster getCluster();

    /**
     *
     * @return the negative clustered interactions
     */
    public IntactCluster getNegativeCluster();

    /**
     *
     * @return the context of the cluster
     */
    public ExportContext getExportContext();

    /**
     *
     * @return the list of interaction identifiers of the interactions which will be exported  (the interaction identifier must be mapped to some interaction in the cluster)
     */
    public Set<Integer> getInteractionsToExport();

    /**
     * Set the list of cluster identifiers which can be exported
     * @param interactionsToExport
     */
    public void setInteractionsToExport(Set<Integer> interactionsToExport);

    /**
     *
     * @return the list of negative interaction identifiers of the interactions which will be exported  (the interaction identifier must be mapped to some interaction in the cluster)
     */
    public Set<Integer> getNegativeInteractionsToExport();

    /**
     * Set the list of cluster identifiers for negative interactions which can be exported
     * @param interactionsToExport
     */
    public void setNegativeInteractionsToExport(Set<Integer> interactionsToExport);
}
