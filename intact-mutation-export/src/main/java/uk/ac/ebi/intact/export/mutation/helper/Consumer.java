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

    private FileExportHandler fileExportHandler;

    public Consumer(FileExportHandler fileExportHandler) {
        this.fileExportHandler = fileExportHandler;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public void run() {
        try {
            fileExportHandler.getExportWriter().writeHeaderIfNecessary("Feature AC", "Feature short label", "Feature range(s)", "Original sequence", "Resulting sequence", "Feature type", "Feature annotation", "Affected protein AC", "Affected protein symbol", "Affected protein full name", "Affected protein organism", "Interaction participants", "PubMedID", "Figure legend", "Interaction AC");
            fileExportHandler.getExportWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            log.info("Export mutations");

            IntactFeatureEvidence intactFeatureEvidence = null;
            try {
                intactFeatureEvidence = MutationExportProcessor.checkedMutations.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Took " + intactFeatureEvidence.getAc() + " from queue.");
            FeatureToExportLine featureToExportLine = new FeatureToExportLine();
            log.info("Convert " + intactFeatureEvidence.getAc());
            MutationExportLine line = featureToExportLine.convertFeatureToMutationExportLine(intactFeatureEvidence);
            log.info("Converted " + intactFeatureEvidence);
            try {
                if (line != null) {
                    for (ExportRange exportRange : line.getExportRange()) {
                        fileExportHandler.getExportWriter().writeColumnValues(line.getFeatureAc(), line.getFeatureShortlabel(),
                                exportRange.getRange(), exportRange.getOriginalSequence(), exportRange.getResultingSequence(), line.getFeatureType(), line.getAnnotations(),
                                line.getAffectedProteinAc(), line.getAffectedProteinSymbol(), line.getAffectedProteinFullName(), line.getAffectedProteinOrganism(),
                                line.getParticipants(), line.getPubmedId(), line.getFigureLegend(), line.getInteractionAc());
                    }
                }
                fileExportHandler.getExportWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!MutationExportProcessor.producerIsAlive() && MutationExportProcessor.readyToCheckMutations.isEmpty() && MutationExportProcessor.checkedMutations.isEmpty()){
                System.exit(0);
            }
        }
    }
}


