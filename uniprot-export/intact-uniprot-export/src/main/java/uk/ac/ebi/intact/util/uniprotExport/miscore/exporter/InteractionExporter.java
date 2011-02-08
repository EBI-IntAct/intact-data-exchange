package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;

/**
 * Interface to implement for classes charged to apply rules for uniprot export.
 * The information about the interactions are in the MiScoreResults. It is up to the class to sort out
 * which interaction can be exported and then fill the list of interactions to export in the result
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public interface InteractionExporter {

    /**
     * select the interactions in the results which can be exported and set the list of interactions to export in the results.
     * This list will contain the Encore identifier of the interactions which were selected for uniprot export
     * @param results : contains the results of the clustering and information about the interactions
     * @throws UniprotExportException
     */
    public void exportInteractionsFrom(MiScoreResults results) throws UniprotExportException;

    /**
     *
     * @param interaction
     * @param context
     * @return true if the encore interaction pass the uniprot export rules
     * @throws UniprotExportException
     */
    public boolean canExportEncoreInteraction(EncoreInteraction interaction, MiClusterContext context) throws UniprotExportException;

    /**
     *
     * @param interaction
     * @param context
     * @return true if the binary interaction can be exported
     * @throws UniprotExportException
     */
    public boolean canExportEBinaryInteraction(BinaryInteraction interaction, MiClusterContext context) throws UniprotExportException;
}
