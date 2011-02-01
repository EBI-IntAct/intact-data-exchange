package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import uk.ac.ebi.intact.util.uniprotExport.miscore.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;

/**
 * Interface to implement for classes charged to apply rules for uniprot export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public interface InteractionExporter {

    public void exportInteractions(MiScoreResults results) throws UniprotExportException;
}
