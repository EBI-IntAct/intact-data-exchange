package uk.ac.ebi.intact.dataexchange.structuredabstract.writer;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.CvTermUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Abstract writer for structured abstract for complexes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public abstract class AbstractStructuredAbstractComplexWriter extends AbstractStructuredAbstractWriter<Complex> {
    public AbstractStructuredAbstractComplexWriter(Writer writer) {
        super(writer);
    }

    public AbstractStructuredAbstractComplexWriter(OutputStream stream) {
        super(stream);
    }

    public AbstractStructuredAbstractComplexWriter(File file) throws IOException {
        super(file);
    }

    @Override
    protected boolean isParticipantBait(Participant component) {
        return false;
    }

    @Override
    protected boolean isParticipantPrey(Participant component) {
        return false;
    }

    @Override
    protected CvTerm extractInteractionDetectionMethodFrom(Complex interaction) {
        return CvTermUtils.createMICvTerm(Experiment.INFERRED_BY_CURATOR, Experiment.INFERRED_BY_CURATOR_MI);
    }
}
