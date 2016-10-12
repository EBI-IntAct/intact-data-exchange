package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.FeatureEvidence;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class Producer implements Runnable {
    private static final Log log = LogFactory.getLog(Producer.class);

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    @Override
    public void run() {
        try {
            while (true) {
                while(MutationExportProcessor.checkedMutations.size() >= 10){
                    Thread.sleep(10000);
                }
                IntactFeatureEvidence ac = MutationExportProcessor.readyToCheckMutations.take();
                log.info("Generate shortlabel of " + ac.getAc());
                config.getShortlabelGenerator().generateNewShortLabel(ac);
            }
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
