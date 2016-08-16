package uk.ac.ebi.intact.export.mutation.helper;

import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.writer.ExportWriter;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class WriterHelper {

    public static List<MutationExportLine> mutationList = new ArrayList<>();

    public static void exportMutations(FileExportHandler fileExportHandler) {
        ExportWriter exportWriter = fileExportHandler.getExportWriter();
        try {
            exportWriter.writeHeaderIfNecessary("Feature AC", "Feature short label", "Feature range(s)", "Original sequence", "Resulting sequence", "Feature type", "Feature annotation", "Affected protein AC", "Affected protein symbol", "Affected protein organism", "Interaction participants", "PubMedID", "Figure legend", "Interaction AC");
            exportWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (MutationExportLine line : mutationList) {


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
    }

}
