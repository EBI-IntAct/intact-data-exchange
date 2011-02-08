package uk.ac.ebi.intact.util.uniprotExport.miscore;

import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.BinaryInteractionFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.BinaryClusterScoreResults;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredBinaryInteractionProcessor {

    private BinaryInteractionFilter filter;

    public ClusteredBinaryInteractionProcessor(BinaryInteractionFilter filter){

        this.filter = filter;
    }

    public void processClusteredBinaryInteractions(String logFile) throws UniprotExportException {

        BinaryClusterScoreResults results = filter.exportInteractions();

        results.getCluster().saveScoresForSpecificInteractions(logFile, results.getInteractionsToExport());
    }

    public BinaryInteractionFilter getFilter() {
        return filter;
    }

    public void setFilter(BinaryInteractionFilter filter) {
        this.filter = filter;
    }
}
