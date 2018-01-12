package uk.ac.ebi.intact.export.mutation.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.helper.Checker;
import uk.ac.ebi.intact.export.mutation.helper.Converter;
import uk.ac.ebi.intact.export.mutation.helper.Exporter;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.listener.ExportMutationListener;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportProcessor {
    private static final Log log = LogFactory.getLog(MutationExportProcessor.class);
    public static BlockingQueue<IntactFeatureEvidence> readyToCheckQueue = new LinkedBlockingDeque<>(1000);
    public static BlockingQueue<IntactFeatureEvidence> readyToConvertQueue = new LinkedBlockingDeque<>(2000);
    public static BlockingQueue<MutationExportLine> readyToExportQueue = new LinkedBlockingDeque<>(5000);
    public static Boolean loadedAll = false;
    private static Thread CHECKER;
    private static Thread EXPORTER;

    private final static String MUTATION_MI_ID = "MI:0118";
    private final static String MUTATION_ENABLING_INTERACTION_MI_ID = "MI:2227";
    private final static String MUTATION_DECREASING_MI_ID = "MI:0119";
    private final static String MUTATION_DECREASING_RATE_MI_ID = "MI:1130";
    private final static String MUTATION_DECREASING_STRENGTH_MI_ID = "MI:1133";
    private final static String MUTATION_DISRUPTING_MI_ID = "MI:0573";
    private final static String MUTATION_DISRUPTING_RATE_MI_ID = "MI:1129";
    private final static String MUTATION_DISRUPTING_STRENGTH_MI_ID = "MI:1128";
    private final static String MUTATION_INCREASING_MI_ID = "MI:0382";
    private final static String MUTATION_INCREASING_RATE_MI_ID = "MI:1131";
    private final static String MUTATION_INCREASING_STRENGTH_MI_ID = "MI:1132";
    private final static String MUTATION_WITH_NO_EFFECT_MI_ID = "MI:2226";

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
        List<String> acs = getAllMutationFeaturesAcs();

        Checker checker = new Checker(readyToCheckQueue);
        Exporter exporter = new Exporter(config.getFileExportHandler());


        MutationExportProcessor.CHECKER = new Thread(checker);
        MutationExportProcessor.EXPORTER = new Thread(exporter);

        MutationExportProcessor.CHECKER.start();
        MutationExportProcessor.EXPORTER.start();

        Thread CONVERTER1 = new Thread(new Converter());
        Thread CONVERTER2 = new Thread(new Converter());
        Thread CONVERTER3 = new Thread(new Converter());
        Thread CONVERTER4 = new Thread(new Converter());
        Thread CONVERTER5 = new Thread(new Converter());

        CONVERTER1.start();
        CONVERTER2.start();
        CONVERTER3.start();
        CONVERTER4.start();
        CONVERTER5.start();

        for (String ac : acs) {
            try {
                readyToCheckQueue.put(config.getMutationExportDao().getFeature(ac));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loadedAll = true;
    }

    private List<String> getAllMutationFeaturesAcs() {
        List<String> mutationTerms = new ArrayList<String>();
        mutationTerms.add(MUTATION_MI_ID);
        mutationTerms.add(MUTATION_ENABLING_INTERACTION_MI_ID);
        mutationTerms.add(MUTATION_DECREASING_MI_ID);
        mutationTerms.add(MUTATION_DECREASING_RATE_MI_ID);
        mutationTerms.add(MUTATION_DECREASING_STRENGTH_MI_ID);
        mutationTerms.add(MUTATION_DISRUPTING_MI_ID);
        mutationTerms.add(MUTATION_DISRUPTING_RATE_MI_ID);
        mutationTerms.add(MUTATION_DISRUPTING_STRENGTH_MI_ID);
        mutationTerms.add(MUTATION_INCREASING_MI_ID);
        mutationTerms.add(MUTATION_INCREASING_RATE_MI_ID);
        mutationTerms.add(MUTATION_INCREASING_STRENGTH_MI_ID);
        mutationTerms.add(MUTATION_WITH_NO_EFFECT_MI_ID);

        log.info("Retrieved all child terms of MI:0118 (mutation).");
        List<String> acs = new ArrayList<>();
        mutationTerms.forEach(term -> {
            acs.addAll(config.getMutationExportDao().getFeatureEvidenceByType(term));
        });
        /// for testing acs.add("EBI-10921757");
        log.info("Retrieved all features acs of type mutation. Excluded MI:0429 (necessary binding region)");
        return acs;
    }
}
