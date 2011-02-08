package uk.ac.ebi.intact.util.uniprotExport.miscore;

import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.ClusteredMitabFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterScoreResults;

/**
 * This class will only apply export rules to a clustered mitab file having pre-computed mi scores
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredBinaryInteractionProcessor {

    private ClusteredMitabFilter filter;

    public ClusteredBinaryInteractionProcessor(ClusteredMitabFilter filter){

        this.filter = filter;
    }

    public void processClusteredBinaryInteractions(String logFile) throws UniprotExportException {

        MiClusterScoreResults results = filter.exportInteractions();

        results.getCluster().saveClusteredInteractions(logFile, results.getInteractionsToExport());
    }

    public ClusteredMitabFilter getFilter() {
        return filter;
    }

    public void setFilter(ClusteredMitabFilter filter) {
        this.filter = filter;
    }
}
