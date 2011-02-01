package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;

/**
 * Interface to implement for classes charged to export interactions from different sources (intact database, mitab file, etc.)
 * and filter them to only compute the mi score of interactions eligible for uniprot export
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public interface InteractionFilter {

    public MiScoreResults exportInteractions() throws UniprotExportException;

    public InteractionExporter getInteractionExporter();

    public void setInteractionExporter(InteractionExporter exporter);
}
