package uk.ac.ebi.intact.export.mutation.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.helper.Consumer;
import uk.ac.ebi.intact.export.mutation.helper.Producer;
import uk.ac.ebi.intact.export.mutation.listener.ExportMutationListener;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.utils.OntologyServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportProcessor {
    private static final Log log = LogFactory.getLog(MutationExportProcessor.class);
    public static BlockingQueue<String> readyToCheckMutations = new LinkedBlockingDeque<>(10);
    public static BlockingQueue<IntactFeatureEvidence> checkedMutations = new LinkedBlockingDeque<>(20);
    private static Thread PRODUCER;
    private static Thread CONSUMER;
    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    private void init() {
        initListener();
    }

    private void initListener() {
        log.info("Initialise event listeners...");
        config.getShortlabelGenerator().addListener(new ExportMutationListener());
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public void exportAll() {
        init();
        List<String> acs = getAllMutationFeatures();
        Producer producer = new Producer();
        MutationExportProcessor.PRODUCER = new Thread(producer);
        MutationExportProcessor.PRODUCER.start();
        Consumer consumer = new Consumer(config.getFileExportHandler());
        MutationExportProcessor.CONSUMER = new Thread(consumer);
        MutationExportProcessor.CONSUMER.start();
        for (String ac : acs) {
            try {

                readyToCheckMutations.put(ac);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        MutationExportProcessor.PRODUCER.interrupt();
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    private List<String> getAllMutationFeatures() {
        List<String> mutationTerms = OntologyServiceHelper.getOntologyServiceHelper().getAssociatedMITerms("MI:0118", 10);
        log.info("Retrieved all child terms of MI:0118 (mutation).");
        List<String> acs = new ArrayList<>();
        mutationTerms.stream().filter(term -> !term.equals("MI:0429")).forEach(term -> {
            acs.addAll(config.getMutationExportDao().getFeatureEvidenceByType(term));
        });
        log.info("Retrieved all features of type mutation. Excluded MI:0429(necessary binding region)");
        return acs;
    }

    public static boolean producerIsAlive(){
        return MutationExportProcessor.PRODUCER.isAlive();
    }
}
