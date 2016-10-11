package uk.ac.ebi.intact.export.mutation.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.helper.FeatureToExportLine;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.UnmodifiedMutationShortlabelEvent;

import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportMutationListener extends AbstractShortlabelGeneratorListener {
    private static final Log log = LogFactory.getLog(ExportMutationListener.class);

    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        try {
            MutationExportProcessor.exportMutationQueue.put(event.getFeatureEvidence());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
