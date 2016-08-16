package uk.ac.ebi.intact.export.mutation.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.FeatureEvidence;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.helper.WriterHelper;
import uk.ac.ebi.intact.export.mutation.listener.ExportMutationListener;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.utils.OntologyServiceHelper;

import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportProcessor {
    private static final Log log = LogFactory.getLog(MutationExportProcessor.class);

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    private void init() {
        initListener();
    }

    private void initListener() {
        log.info("Initialise event listeners...");
        config.getShortlabelGenerator().addListener(new ExportMutationListener());
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    public void updateAll() {
        init();
        Set<IntactFeatureEvidence> intactFeatureEvidences = getAllMutationFeatures();
        updateByACs(intactFeatureEvidences);
    }

    public void updateByACs(Set<IntactFeatureEvidence> intactFeatureEvidences) {
        for (IntactFeatureEvidence intactFeatureEvidence : intactFeatureEvidences) {
            log.info("Generate shortlabel for: " + intactFeatureEvidence.getAc());
            config.getShortlabelGenerator().generateNewShortLabel(intactFeatureEvidence);
        }
        WriterHelper.exportMutations(config.getFileExportHandler());
    }

    @Transactional(propagation = Propagation.REQUIRED, value = "jamiTransactionManager", readOnly = true)
    private Set<IntactFeatureEvidence> getAllMutationFeatures() {
        List<String> mutationTerms = OntologyServiceHelper.getOntologyServiceHelper().getAssociatedMITerms("MI:0118", 10);
        log.info("Retrieved all child terms of MI:0118 (mutation).");
        Set<IntactFeatureEvidence> featureEvidences = new HashSet<>();
        for (String term : mutationTerms) {
            //MI:0429(necessary binding region) should not be taken into account
            if (term.equals("MI:0429")) {
                continue;
            }
            featureEvidences.addAll(config.getMutationExportDao().getFeatureEvidenceByType(term));
        }
        log.info("Retrieved all features of type mutation. Excluded MI:0429(necessary binding region)");
        return featureEvidences;
    }
}
