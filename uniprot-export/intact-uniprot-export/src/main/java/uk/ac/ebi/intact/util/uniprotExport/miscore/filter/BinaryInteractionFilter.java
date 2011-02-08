package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.BinaryClusterScoreResults;

/**
 * Filter of clustered binary interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public interface BinaryInteractionFilter {

    /**
     * Apply several rules
     * to select the binary interactions which will be exported.
     * Will not compute any score, rely on the score within binary interactions
     * @return the results of the export
     * @throws uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException
     */
    public BinaryClusterScoreResults exportInteractions() throws UniprotExportException;

    /**
     *
     * @return The exporter of this filter which will apply rules to select binary interactions for uniprot export
     */
    public InteractionExporter getInteractionExporter();

    /**
     * Set the exporter of this filter
     * @param exporter
     */
    public void setInteractionExporter(InteractionExporter exporter);
}
