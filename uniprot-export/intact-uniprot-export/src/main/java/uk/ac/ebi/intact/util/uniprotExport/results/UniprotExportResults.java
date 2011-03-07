package uk.ac.ebi.intact.util.uniprotExport.results;

import uk.ac.ebi.intact.util.uniprotExport.results.contexts.ExportContext;

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
     * @return the results of the export for positive interactions
     */
    public ExportedClusteredInteractions getPositiveClusteredInteractions();

    /**
     *
     * @return the results of the export for negative interactions
     */
    public ExportedClusteredInteractions getNegativeClusteredInteractions();

    /**
     *
     * @return the context of the cluster
     */
    public ExportContext getExportContext();
}
