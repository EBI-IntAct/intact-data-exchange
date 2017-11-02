package uk.ac.ebi.intact.export.mutation.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.UnmodifiedMutationShortlabelEvent;

import static uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor.readyToConvertQueue;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportMutationListener extends AbstractShortlabelGeneratorListener {
    private static final Log log = LogFactory.getLog(ExportMutationListener.class);

    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        try {
            log.info("Ready to convert");
            try {
                readyToConvertQueue.put(event.getFeatureEvidence());
            } catch (NullPointerException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
