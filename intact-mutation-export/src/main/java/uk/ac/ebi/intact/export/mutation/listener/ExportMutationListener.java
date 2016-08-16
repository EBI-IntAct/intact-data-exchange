package uk.ac.ebi.intact.export.mutation.listener;

import uk.ac.ebi.intact.export.mutation.helper.FeatureToExportLine;
import uk.ac.ebi.intact.export.mutation.helper.WriterHelper;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.export.mutation.processor.MutationExportProcessor;
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



    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {
        MutationExportProcessor.featureEvidences.add(event.getFeatureEvidence());
        System.out.println(MutationExportProcessor.featureEvidences.size());
//        MutationExportLine line = FeatureToExportLine.convertFeatureToMutationExpor
// tLine(featureEvidence);
//        if(line != null){
//            WriterHelper.mutationList.add(line);
//            System.out.println(WriterHelper.mutationList.size());
//        }
    }
}
