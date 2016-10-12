package uk.ac.ebi.intact.export.mutation.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.UnmodifiedMutationShortlabelEvent;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportMutationListener extends AbstractShortlabelGeneratorListener {
    private static final Log log = LogFactory.getLog(ExportMutationListener.class);

    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        try {
            MutationExportProcessor.checkedMutations.put(event.getFeatureEvidence());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
