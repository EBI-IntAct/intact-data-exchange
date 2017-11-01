package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.concurrent.TimeUnit;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class Converter implements Runnable {
    private static final Log log = LogFactory.getLog(Converter.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public void run() {
        while (true) {
            IntactFeatureEvidence intactFeatureEvidence = null;
            if (MutationExportProcessor.loadedAll && MutationExportProcessor.readyToConvertQueue.isEmpty()
                    && MutationExportProcessor.readyToCheckQueue.isEmpty()) {
                break;
            }
            try {
                intactFeatureEvidence = MutationExportProcessor.readyToConvertQueue.poll(30, TimeUnit.SECONDS);
                if(intactFeatureEvidence == null){
                    continue;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FeatureToExportLine featureToExportLine = new FeatureToExportLine();
            log.info("Convert " + intactFeatureEvidence.getAc());
            MutationExportLine line = null;
            try {
                line = featureToExportLine.convertFeatureToMutationExportLine(intactFeatureEvidence);

            } catch (NullPointerException e) {
                e.printStackTrace();

            }
            log.info("Converted " + intactFeatureEvidence);
            try {
                MutationExportProcessor.readyToExportQueue.put(line);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.out.println(intactFeatureEvidence.getAc());
            }
        }
        log.info("Thread killed");
        Thread.currentThread().interrupt();
    }
}


