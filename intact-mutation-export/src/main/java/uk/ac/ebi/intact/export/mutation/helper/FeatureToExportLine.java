package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.lang.StringUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (publication.getReleasedDate() == null) {
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
        Annotation annotation = annotations.stream().filter(a -> a.getTopic().getShortName().equals("figure legend")).findFirst().orElse(null);
        if (annotation == null) {
            return "";
        } else {
            return annotation.getValue();
        }
    }

    private String extractAc(Collection<Xref> identifiers, String database) {
        Xref xref = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("identity") && i.getDatabase().getShortName().equals(database)).findFirst().orElse(null);
        if (xref == null) {
            return "";
        } else {
            return xref.getId();
        }
    }

    private String buildProteinAc(Collection<Xref> dbXrefs) {
        Xref xref = dbXrefs.stream().filter(i -> i.getQualifier().getShortName().equals("identity")).findFirst().orElse(null);
        if(xref == null){
            return "";
        } else {
            return xref.getDatabase().getShortName() + ":" + xref.getId();

        }
    }

    private String extractParticipants(InteractionEvidence interactionEvidence, ParticipantEvidence excludeAc) {
        Collection<String> participants = new ArrayList<>();
        interactionEvidence.getParticipants().stream().filter(p -> !Objects.equals(((IntactParticipantEvidence) p).getAc(), ((IntactParticipantEvidence) excludeAc).getAc())).forEach(p -> {
            participants.add(buildProteinAc(p.getInteractor().getIdentifiers()) +
                    "(" + extractFeatureType(p.getInteractor().getInteractorType()) +
                    ", " + extractInteractorOrganism(p.getInteractor().getOrganism()) + ")");
        });
        if (participants.isEmpty()) {
            return "";
        } else {
            return StringUtils.join(participants, ";");
        }
    }

    private ExportRange buildRange(Range range) {
        ExportRange exportRange = new ExportRange();
        exportRange.setRange(range.getStart().getStart() + "-" + range.getEnd().getEnd());
        exportRange.setOriginalSequence(range.getResultingSequence().getOriginalSequence());
        exportRange.setResultingSequence(range.getResultingSequence().getNewSequence());
        return exportRange;
    }

    private String extractProteinSymbol(String shortName, Collection<Alias> aliases) {
        Alias alias = aliases.stream().filter(a -> a.getType().getShortName().equals("gene name")).findFirst().orElse(null);
        if(alias == null){
            return shortName;
        } else {
            return alias.getName();
        }
    }

    private String extractInteractorOrganism(Organism organism) {
        return organism.getTaxId() + " - " + organism.getScientificName();
    }

    private String extractAnnotations(Collection<Annotation> annotations) {
        Annotation annotationsFiltered = annotations.stream().filter(a -> !a.getTopic().getShortName().equals("remark-internal") && !a.getTopic().getShortName().equals("no-mutation-update")).findAny().orElse(null);
        if(annotationsFiltered == null){
            return "";
        } else {
            return StringUtils.join(annotations, ",");
        }
    }

    private String extractFeatureType(CvTerm type) {
        return type.getShortName() + "(" + type.getMIIdentifier() + ")";
    }
}
