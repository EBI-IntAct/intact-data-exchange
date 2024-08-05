package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer.feeder;

import psidev.psi.mi.jami.binary.BinaryInteractionEvidence;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.tab.io.writer.feeder.MitabInteractionEvidenceFeeder;
import psidev.psi.mi.jami.tab.utils.MitabUtils;
import psidev.psi.mi.jami.utils.AnnotationUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The intact Mitab column feeder for interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class MitabIntactInteractionEvidenceFeeder extends MitabInteractionEvidenceFeeder {

    public MitabIntactInteractionEvidenceFeeder(Writer writer) {
        super(writer);
    }

    @Override
    public void writeInteractionAnnotations(BinaryInteractionEvidence interaction) throws IOException {
        Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(interaction.getAnnotations(),
                null, "no-export");
        Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(interaction.getAnnotations());
        exportAnnotations.removeAll(noExportAnnotations);

        // writes interaction annotations first
        if (!exportAnnotations.isEmpty()){
            Iterator<Annotation> interactorAnnotationIterator = exportAnnotations.iterator();

            while (interactorAnnotationIterator.hasNext()){
                Annotation annot = interactorAnnotationIterator.next();
                writeAnnotation(annot);

                if(interactorAnnotationIterator.hasNext()){
                    getWriter().write(MitabUtils.FIELD_SEPARATOR);
                }
            }

            if (interaction.getExperiment() != null){
                Publication pub = interaction.getExperiment().getPublication();

                if (pub != null){
                    getWriter().write(MitabUtils.FIELD_SEPARATOR);
                    writeInteractionAnnotationTagsFrom(pub, false);
                }
            }
        }
        else if (interaction.getExperiment() != null){
            Publication pub = interaction.getExperiment().getPublication();

            if (pub != null){
                // writes curation depth first
                writeInteractionAnnotationTagsFrom(pub, true);
            } else {
                getWriter().write(MitabUtils.EMPTY_COLUMN);
            }
        }
        else{
            getWriter().write(MitabUtils.EMPTY_COLUMN);
        }
    }

    @Override
    public void writeParticipantAnnotations(ParticipantEvidence participant) throws IOException {
        if (participant != null){
            Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(participant.getInteractor().getAnnotations(),
                    null, "no-export");
            Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(participant.getInteractor().getAnnotations());
            exportAnnotations.removeAll(noExportAnnotations);
            Collection<Annotation> noExportAnnotations2 = AnnotationUtils.collectAllAnnotationsHavingTopic(participant.getAnnotations(),
                    null, "no-export");
            Collection<Annotation> exportAnnotations2 = new ArrayList<Annotation>(participant.getAnnotations());
            exportAnnotations2.removeAll(noExportAnnotations2);
            // writes interactor annotations first
            if (!exportAnnotations.isEmpty()){
                Iterator<Annotation> interactorAnnotationIterator = exportAnnotations.iterator();

                while (interactorAnnotationIterator.hasNext()){
                    Annotation annot = interactorAnnotationIterator.next();
                    writeAnnotation(annot);

                    if(interactorAnnotationIterator.hasNext()){
                        getWriter().write(MitabUtils.FIELD_SEPARATOR);
                    }
                }

                if (!exportAnnotations2.isEmpty()){
                    getWriter().write(MitabUtils.FIELD_SEPARATOR);
                    Iterator<Annotation> participantAnnotationIterator = exportAnnotations2.iterator();

                    while (participantAnnotationIterator.hasNext()){
                        Annotation annot = participantAnnotationIterator.next();
                        writeAnnotation(annot);

                        if(participantAnnotationIterator.hasNext()){
                            getWriter().write(MitabUtils.FIELD_SEPARATOR);
                        }
                    }
                }
            }
            // writes participant annotations only
            else if (!exportAnnotations2.isEmpty()){
                Iterator<Annotation> participantAnnotationIterator = exportAnnotations2.iterator();

                while (participantAnnotationIterator.hasNext()){
                    Annotation annot = participantAnnotationIterator.next();
                    writeAnnotation(annot);

                    if(participantAnnotationIterator.hasNext()){
                        getWriter().write(MitabUtils.FIELD_SEPARATOR);
                    }
                }
            }
            else{
                getWriter().write(MitabUtils.EMPTY_COLUMN);
            }
        }
        else{
            getWriter().write(MitabUtils.EMPTY_COLUMN);
        }
    }
}
