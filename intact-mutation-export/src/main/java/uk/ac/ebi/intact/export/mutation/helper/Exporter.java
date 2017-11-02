package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;

import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class Exporter implements Runnable {
    private static final Log log = LogFactory.getLog(Exporter.class);

    private FileExportHandler fileExportHandler;

    public Exporter(FileExportHandler fileExportHandler) {
        this.fileExportHandler = fileExportHandler;
    }

    @Override
    public void run() {
        try {
            fileExportHandler.getExportWriter().writeHeaderIfNecessary("Feature AC", "Feature short label", "Feature range(s)", "Original sequence", "Resulting sequence", "Feature type", "Feature annotation", "Affected protein AC", "Affected protein symbol", "Affected protein full name", "Affected protein organism", "Interaction participants", "PubMedID", "Figure legend", "Interaction AC");
            fileExportHandler.getExportWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            if (MutationExportProcessor.loadedAll && MutationExportProcessor.readyToCheckQueue.isEmpty()
                    && MutationExportProcessor.readyToConvertQueue.isEmpty() && MutationExportProcessor.readyToExportQueue.isEmpty()) {
                break;
            }
            MutationExportLine line = null;
            try {
                line = MutationExportProcessor.readyToExportQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        }
        log.info("Exporter killed");
        Thread.currentThread().interrupt();
    }
}
