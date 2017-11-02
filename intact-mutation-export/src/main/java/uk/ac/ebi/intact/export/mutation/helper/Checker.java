package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.SourceType;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class Checker implements Runnable {
    private static final Log log = LogFactory.getLog(Checker.class);

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();
    private BlockingQueue<IntactFeatureEvidence> readyToCheckQueue;

    public Checker(BlockingQueue<IntactFeatureEvidence> acs) {
        this.readyToCheckQueue = acs;
    }

    @Override
    public void run() {
        while (true) {
            if (MutationExportProcessor.loadedAll && readyToCheckQueue.isEmpty()) {
                break;
            }
            IntactFeatureEvidence featureEvidence;
            try {
                featureEvidence = readyToCheckQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            log.info("Generate shortlabel of " + featureEvidence.getAc());
            config.getShortlabelGenerator().generateNewShortLabel(featureEvidence);
        }
        Thread.currentThread().interrupt();
    }
}
