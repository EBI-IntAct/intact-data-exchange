package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class Consumer implements Runnable {
    private static final Log log = LogFactory.getLog(Consumer.class);


    @Override
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public void run() {
        while (MutationExportProcessor.readyToCheckMutations.peek() != null && MutationExportProcessor.checkedMutations.peek() != null) {
            IntactFeatureEvidence intactFeatureEvidence = null;
            try {
                intactFeatureEvidence = MutationExportProcessor.checkedMutations.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FeatureToExportLine featureToExportLine = new FeatureToExportLine();
            log.info("Convert " + intactFeatureEvidence.getAc());
            MutationExportLine line = featureToExportLine.convertFeatureToMutationExportLine(intactFeatureEvidence);
            log.info("Converted " + intactFeatureEvidence);
            try {
                MutationExportProcessor.exportMutations.put(line);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!MutationExportProcessor.producerIsAlive() && MutationExportProcessor.readyToCheckMutations.isEmpty() && MutationExportProcessor.checkedMutations.isEmpty()){
                System.exit(0);
            }
        }
    }
}


