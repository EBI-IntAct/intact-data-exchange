package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;

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
                String ac = MutationExportProcessor.readyToCheckMutations.take();
                log.info("Generate shortlabel of " + ac);
                config.getShortlabelGenerator().generateNewShortLabel(ac);
            }
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
