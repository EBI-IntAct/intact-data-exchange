package uk.ac.ebi.intact.export.mutation.listener;

import uk.ac.ebi.intact.export.mutation.helper.FeatureToExportLine;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.writer.ExportWriter;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.AnnotationFoundEvent;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.UnmodifiedMutationShortlabelEvent;

import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ExportMutationListener extends AbstractShortlabelGeneratorListener {

    private FileExportHandler fileExportHandler;

    public ExportMutationListener(FileExportHandler reportHandler) {
        this.fileExportHandler = reportHandler;
    }

    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        IntactFeatureEvidence featureEvidence = event.getFeatureEvidence();
        MutationExportLine line = FeatureToExportLine.convertFeatureToMutationExportLine(featureEvidence);
        ExportWriter exportWriter = fileExportHandler.getExportWriter();
        try {
            exportWriter.writeHeaderIfNecessary("Feature AC", "Feature short label", "Feature range(s)", "Original sequence", "Resulting sequence", "Feature type", "Feature annotation", "Affected protein AC", "Affected protein symbol", "Affected protein organism", "Interaction participants", "PubMedID", "Figure legend", "Interaction AC");
            exportWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (line != null) {
                for (ExportRange exportRange : line.getExportRange()) {
                    exportWriter.writeColumnValues(line.getFeatureAc(), line.getFeatureShortlabel(),
                            exportRange.getRange(), exportRange.getOriginalSequence(), exportRange.getResultingSequence(), line.getFeatureType(), line.getAnnotations(),
                            line.getAffectedProteinAc(), line.getAffectedProteinSymbol(), line.getAffectedProteinOrganism(),
                            line.getParticipants(), line.getPubmedId(), line.getFigureLegend(), line.getInteractionAc());
                }
            }
            exportWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAnnotationFound(AnnotationFoundEvent event) {
        if (event.getType().equals(AnnotationFoundEvent.AnnotationType.NO_MUTATION_UPDATE)) {

        }
    }

    private void removeLastComma(String s) {
        if (s != null && s.length() != 0 && s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
    }
}
