package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;

/**
 * Interface to implement for classes charged to export interactions from different sources (intact database, mitab file, etc.)
 * The classes implementing this interface are charged to compute the mi score of a set of binary interactions elligible for uniprot export.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public interface InteractionFilter {

    /**
     * Compute the mi score of a set of binary interactions which are eligible for uniprot export and then apply several rules
     * to select the binary interactions which will be exported.
     * @return the results of the export
     * @throws UniprotExportException
     */
    public MiScoreResults exportInteractions() throws UniprotExportException;

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
