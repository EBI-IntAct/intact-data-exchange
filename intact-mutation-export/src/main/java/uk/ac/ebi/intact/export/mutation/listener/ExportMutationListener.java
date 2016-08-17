package uk.ac.ebi.intact.export.mutation.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.export.mutation.MutationExportConfig;
import uk.ac.ebi.intact.export.mutation.MutationExportContext;
import uk.ac.ebi.intact.export.mutation.helper.FeatureToExportLine;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.UnmodifiedMutationShortlabelEvent;

import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportMutationListener extends AbstractShortlabelGeneratorListener {
    private static final Log log = LogFactory.getLog(ExportMutationListener.class);

    private MutationExportConfig config = MutationExportContext.getInstance().getConfig();

    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        FeatureToExportLine featureToExportLine = new FeatureToExportLine();
        log.info("Convert " + event.getFeatureEvidence().getAc());
        MutationExportLine line = featureToExportLine.convertFeatureToMutationExportLine(event.getFeatureEvidence());
        log.info("Converted " + event.getFeatureEvidence().getAc());
        try {
            config.getFileExportHandler().getExportWriter().writeHeaderIfNecessary("Feature AC", "Feature short label", "Feature range(s)", "Original sequence", "Resulting sequence", "Feature type", "Feature annotation", "Affected protein AC", "Affected protein symbol", "Affected protein organism", "Interaction participants", "PubMedID", "Figure legend", "Interaction AC");
            config.getFileExportHandler().getExportWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            if (line != null) {
                for (ExportRange exportRange : line.getExportRange()) {
                    config.getFileExportHandler().getExportWriter().writeColumnValues(line.getFeatureAc(), line.getFeatureShortlabel(),
                            exportRange.getRange(), exportRange.getOriginalSequence(), exportRange.getResultingSequence(), line.getFeatureType(), line.getAnnotations(),
                            line.getAffectedProteinAc(), line.getAffectedProteinSymbol(), line.getAffectedProteinOrganism(),
                            line.getParticipants(), line.getPubmedId(), line.getFigureLegend(), line.getInteractionAc());
                }
            }
            config.getFileExportHandler().getExportWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
