package uk.ac.ebi.intact.export.mutation.listener;

import uk.ac.ebi.intact.tools.feature.shortlabel.generator.events.*;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.listener.ShortlabelGeneratorListener;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class AbstractShortlabelGeneratorListener implements ShortlabelGeneratorListener {
    @Override
    public void onRangeError(RangeErrorEvent event) {

    }

    @Override
    public void onModifiedMutationShortlabel(ModifiedMutationShortlabelEvent event) {

    }

    @Override
    public void onUnmodifiedMutationShortlabel(UnmodifiedMutationShortlabelEvent event) {

    }

    @Override
    public void onRetrieveObjectError(ObjRetrieveErrorEvent event) {

    }

    @Override
    public void onAnnotationFound(AnnotationFoundEvent event) {

    }

    @Override
    public void onSequenceError(SequenceErrorEvent event) {

    }

    @Override
    public void onResultingSequenceChanged(ResultingSequenceChangedEvent event) {

    }

    @Override
    public void onObjectTypeError(TypeErrorEvent event) {

    }
}
