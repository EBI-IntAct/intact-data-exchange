package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.ClusteredMitabFilter;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;

import java.util.HashSet;

/**
 * This class will only apply export rules to a clustered mitab file having pre-computed mi scores
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class ClusteredBinaryInteractionProcessor {
    private static final Logger logger = Logger.getLogger(ClusteredBinaryInteractionProcessor.class);
    private ClusteredMitabFilter filter;

    public ClusteredBinaryInteractionProcessor(ClusteredMitabFilter filter){

        this.filter = filter;
    }

    public void processClusteredBinaryInteractions(String fileExported, String fileExcluded) throws UniprotExportException {

        logger.info("Export binary interactions from a clustered mitab file");
        MiClusterScoreResults results = filter.exportInteractions();

        logger.info("Save results of exported interactions in " + fileExported);
        results.getCluster().saveClusteredInteractions(fileExported, results.getInteractionsToExport());
        logger.info("Save results of excluded interactions in " + fileExcluded);
        results.getCluster().saveClusteredInteractions(fileExcluded, new HashSet(CollectionUtils.subtract(results.getCluster().getAllInteractionIds(), results.getInteractionsToExport())));
    }

    public ClusteredMitabFilter getFilter() {
        return filter;
    }

    public void setFilter(ClusteredMitabFilter filter) {
        this.filter = filter;
    }
}
