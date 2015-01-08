package uk.ac.ebi.intact.dataexchange.structuredabstract.writer;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.structuredabstract.model.Sentence;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract writer for structured abstract for interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public abstract class AbstractStructuredAbstractEvidenceWriter extends AbstractStructuredAbstractWriter<InteractionEvidence> {

    public AbstractStructuredAbstractEvidenceWriter() {
        super();
    }

    public AbstractStructuredAbstractEvidenceWriter(Writer writer) {
        super(writer);
    }

    public AbstractStructuredAbstractEvidenceWriter(OutputStream stream) {
        super(stream);
    }

    public AbstractStructuredAbstractEvidenceWriter(File file) throws IOException {
        super(file);
    }

    /**
     * Write structured abstract for publication
     * @param publication
     * @throws IOException
     */
    public void writeStructuredAbstract(Publication publication) throws IOException {
        if (publication == null){
            throw new IllegalArgumentException("The publication cannot be null");
        }

        // clear
        clear();

        //get all experiments
        Collection<Experiment> experiments = publication.getExperiments();
        for (Experiment exp : experiments) {
            // read and collect abstract for each interaction
            for (InteractionEvidence in : exp.getInteractionEvidences()) {
                collectStructuredAbstractFrom(in);
            }
        }

        // write all collected sentences
        writeSentences();
    }

    protected void writeSentences() throws IOException {
        Iterator<Sentence> sentenceIterator = getSentenceMap().values().iterator();
        while (sentenceIterator.hasNext()) {
            writeSentence(sentenceIterator.next());
            if (sentenceIterator.hasNext()){
                writeLineSeparator();
            }
        }
    }

    protected abstract void writeLineSeparator() throws IOException;

    /**
     * Write structured abstract for experiment
     * @param experiment
     * @throws IOException
     */
    public void writeStructuredAbstract(Experiment experiment) throws IOException {
        if (experiment == null){
            throw new IllegalArgumentException("The experiment cannot be null");
        }

        // clear
        clear();

        // read and collect abstract for each interaction
        for (InteractionEvidence in : experiment.getInteractionEvidences()) {
            collectStructuredAbstractFrom(in);
        }

        // write all collected sentences
        writeSentences();
    }

    @Override
    protected boolean isParticipantBait(Participant component) {
        return getBaitMi().contains(((ParticipantEvidence) component)
                .getExperimentalRole().getMIIdentifier());
    }

    @Override
    protected boolean isParticipantPrey(Participant component) {
        return getPreyMi().contains(((ParticipantEvidence) component)
                .getExperimentalRole().getMIIdentifier());
    }

    @Override
    protected CvTerm extractInteractionDetectionMethodFrom(InteractionEvidence interaction) {
        return interaction.getExperiment() != null ? interaction.getExperiment().getInteractionDetectionMethod() : null;
    }
}
