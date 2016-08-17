package uk.ac.ebi.intact.export.mutation.helper;

import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by Maximilian Koch (mkoch@ebi.excludeAc.uk).
 */
public class FeatureToExportLine {

    public MutationExportLine convertFeatureToMutationExportLine(FeatureEvidence featureEvidence) {
        FeatureToExportLine featureToExportLine = new FeatureToExportLine();
        MutationExportLine line = new MutationExportLine();
        ParticipantEvidence intactParticipantEvidence = (ParticipantEvidence) featureEvidence.getParticipant();
        Interactor intactInteractor = intactParticipantEvidence.getInteractor();
        InteractionEvidence interactionEvidence = intactParticipantEvidence.getInteraction();
        Experiment experiment = interactionEvidence.getExperiment();
        Publication publication = experiment.getPublication();
        if(publication.getReleasedDate()==null){
            return null;
        }
        line.setFeatureAc(featureToExportLine.extractAc(featureEvidence.getIdentifiers(), "intact"));
        line.setFeatureShortlabel(featureEvidence.getShortName());
        line.setFeatureType(featureToExportLine.extractFeatureType(featureEvidence.getType()));
        line.setAnnotations(featureToExportLine.extractAnnotations(featureEvidence.getAnnotations()));
        line.setAffectedProteinAc("uniprotkb:" + featureToExportLine.extractAc(intactInteractor.getIdentifiers(), "uniprotkb"));
        line.setAffectedProteinSymbol(featureToExportLine.extractProteinSymbol(intactInteractor.getShortName(), intactInteractor.getAliases()));
        line.setAffectedProteinOrganism(featureToExportLine.extractInteractorOrganism(intactInteractor.getOrganism()));

        line.setParticipants(featureToExportLine.extractParticipants(interactionEvidence, intactParticipantEvidence));
        line.setPubmedId(publication.getPubmedId());
        line.setFigureLegend(featureToExportLine.extractFigureLegend(interactionEvidence.getAnnotations()));
        for (Range range : featureEvidence.getRanges()) {
            line.getExportRange().add(featureToExportLine.buildRange(range));
        }
        line.setInteractionAc(featureToExportLine.extractAc(interactionEvidence.getIdentifiers(), "intact"));
        return line;
    }

    private String extractFigureLegend(Collection<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.getTopic().getShortName().equals("figure legend")) {
                return annotation.getValue();
            }
        }
        return "";
    }

    private String extractAc(Collection<Xref> identifiers, String database) {
        for (Xref xref : identifiers) {
            if (xref.getQualifier().getShortName().equals("identity") && xref.getDatabase().getShortName().equals(database)) {
                return xref.getId();
            }
        }
        return "";
    }

    private String buildProteinAc(Collection<Xref> dbXrefs) {
        for (Xref xref : dbXrefs) {
            if (xref.getQualifier().getShortName().equals("identity")) {
                return xref.getDatabase().getShortName() + ":" + xref.getId();
            }
        }
        return "";
    }

    private String extractParticipants(InteractionEvidence interactionEvidence, ParticipantEvidence excludeAc) {
        String participants = "";
        for (ParticipantEvidence participantEvidence : interactionEvidence.getParticipants()) {
            if (Objects.equals(((IntactParticipantEvidence)participantEvidence).getAc(), ((IntactParticipantEvidence)excludeAc).getAc())) {
                continue;
            }
            if(participantEvidence.getInteractor().getOrganism() == null){
                System.out.println();
            }
            participants += buildProteinAc(participantEvidence.getInteractor().getIdentifiers()) +
                    "(" + extractFeatureType(participantEvidence.getInteractor().getInteractorType()) +
                    ", " + extractInteractorOrganism(participantEvidence.getInteractor().getOrganism()) + ");";
        }
        if (!participants.isEmpty()) {
            participants = participants.substring(0, participants.length() - 1);
        }
        return participants;
    }

    private ExportRange buildRange(Range range) {
        ExportRange exportRange = new ExportRange();
        exportRange.setRange(range.getStart().getStart() + "-" + range.getEnd().getEnd());
        exportRange.setOriginalSequence(range.getResultingSequence().getOriginalSequence());
        exportRange.setResultingSequence(range.getResultingSequence().getNewSequence());
        return exportRange;
    }

    private String extractProteinSymbol(String shortName, Collection<Alias> aliases) {
        for (Alias alias : aliases) {
            if (alias.getType().getShortName().equals("gene name")) {
                return alias.getName();
            }
        }
        return shortName;
    }

    private String extractInteractorOrganism(Organism organism) {
        return organism.getTaxId() + " - " + organism.getScientificName();
    }

    private String extractAnnotations(Collection<Annotation> annotations) {
        String annotationString = "";
        for (Annotation annotation : annotations) {
            if (annotation.getTopic().getShortName().equals("remark-internal")) {
                continue;
            }
            if (annotation.getTopic().getShortName().equals("no-mutation-update")) {
                continue;
            }
            annotationString += annotation.getValue() + ", ";
        }
        if (!annotationString.isEmpty()) {
            annotationString = annotationString.substring(0, annotationString.length() - 1);
        }
        return annotationString;
    }

    private String extractFeatureType(CvTerm type) {
        return type.getShortName() + "(" + type.getMIIdentifier() + ")";
    }
}
