package uk.ac.ebi.intact.util.uniprotExport.filters;

import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;

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
    public MiClusterScoreResults exportInteractions() throws UniprotExportException;

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

    /**
     * Save the results of cluster and filtering without export rules
     * @param mitab
     * @param mitabResults
     * @throws UniprotExportException
     */
    public void saveClusterAndFilterResultsFrom(String mitab, String mitabResults) throws UniprotExportException;
}
