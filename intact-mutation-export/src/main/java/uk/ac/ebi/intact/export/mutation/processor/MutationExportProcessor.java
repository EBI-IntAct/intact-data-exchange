package uk.ac.ebi.intact.export.mutation.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.helper.Consumer;
import uk.ac.ebi.intact.export.mutation.helper.Exporter;
import uk.ac.ebi.intact.export.mutation.helper.Producer;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
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
    public static BlockingQueue<IntactFeatureEvidence> readyToCheckMutations = new LinkedBlockingDeque<>(10);
    public static BlockingQueue<IntactFeatureEvidence> checkedMutations = new LinkedBlockingDeque<>(20);
    public static BlockingQueue<MutationExportLine> exportMutations = new LinkedBlockingDeque<>(20);
    private static Thread PRODUCER;
    private static Thread EXPORTER;

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    public static boolean producerIsAlive() {
        return MutationExportProcessor.PRODUCER.isAlive();
    }

    public static boolean isProducerAlive() {
        return MutationExportProcessor.PRODUCER.isAlive();
    }

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
        Exporter exporter = new Exporter(config.getFileExportHandler());

        MutationExportProcessor.PRODUCER = new Thread(producer);
        MutationExportProcessor.EXPORTER = new Thread(exporter);
        
        Thread CONSUMER1 = new Thread(new Consumer());
        Thread CONSUMER2 = new Thread(new Consumer());
        Thread CONSUMER3 = new Thread(new Consumer());
        Thread CONSUMER4 = new Thread(new Consumer());
        Thread CONSUMER5 = new Thread(new Consumer());
        
        MutationExportProcessor.PRODUCER.start();
        MutationExportProcessor.EXPORTER.start();
        CONSUMER1.start();
        CONSUMER2.start();
        CONSUMER3.start();
        CONSUMER4.start();
        CONSUMER5.start();

        for (String ac : acs) {
            try {
                readyToCheckMutations.put(config.getMutationExportDao().getFeature(ac));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MutationExportProcessor.PRODUCER.interrupt();

        while (true) {
            if (checkedMutations.size() == 0) {
                CONSUMER1.interrupt();
                CONSUMER2.interrupt();
                CONSUMER3.interrupt();
                CONSUMER4.interrupt();
                CONSUMER5.interrupt();
            }
            if (checkedMutations.size() == 0 && exportMutations.size() == 0) {
                MutationExportProcessor.EXPORTER.interrupt();
                log.info("All mutations exported");
                break;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
}
