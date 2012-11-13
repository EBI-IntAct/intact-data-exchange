package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.util.uniprotExport.filters.mitab.ClusteredMitabFilter;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
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

        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        logger.info("Save results of exported positive interactions in " + fileExported);
        positiveInteractions.getCluster().saveClusteredInteractions(fileExported, positiveInteractions.getInteractionsToExport());
        logger.info("Save results of exported negative interactions in " + fileExported);
        negativeInteractions.getCluster().saveClusteredInteractions(fileExported, negativeInteractions.getInteractionsToExport());

        logger.info("Save results of excluded positive interactions in " + fileExcluded);
        positiveInteractions.getCluster().saveClusteredInteractions(fileExcluded, new HashSet(CollectionUtils.subtract(positiveInteractions.getCluster().getAllInteractionIds(), positiveInteractions.getInteractionsToExport())));
        logger.info("Save results of excluded negative interactions in " + fileExcluded);
        negativeInteractions.getCluster().saveClusteredInteractions(fileExcluded, new HashSet(CollectionUtils.subtract(negativeInteractions.getCluster().getAllInteractionIds(), negativeInteractions.getInteractionsToExport())));
    }

    public ClusteredMitabFilter getFilter() {
        return filter;
    }

    public void setFilter(ClusteredMitabFilter filter) {
        this.filter = filter;
    }
}
