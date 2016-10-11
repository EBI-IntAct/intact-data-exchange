package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.lang.StringUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Maximilian Koch (mkoch@ebi.excludeAc.uk).
 */
public class FeatureToExportLine {
    private final String MI_FIGURELEGEND = "figure legend";
    private final String MI_IDENTITY = "identity";
    private final String MI_INTACT = "intact";
    private final String MI_UNIPROT = "uniprotkb";
    private final String MI_MULTIPLEPARENT = "multiple parent";
    private final String MI_GENENAME = "gene name";
    private final String EMPTY_STRING = "";
    private final String TAB = "\t";
    private final String NEW_LINE = "\n";
    private final String ONE_SPACE = " ";
    private final String COLON = ":";
//    private final String MI_REMARKINTERNAL = "";
//    private final String MI_NOMUTATIONUPDATE = "";

    public MutationExportLine convertFeatureToMutationExportLine(FeatureEvidence featureEvidence) {
        FeatureToExportLine featureToExportLine = new FeatureToExportLine();
        MutationExportLine line = new MutationExportLine();
        ParticipantEvidence intactParticipantEvidence = (ParticipantEvidence) featureEvidence.getParticipant();
        Interactor intactInteractor = intactParticipantEvidence.getInteractor();
        InteractionEvidence interactionEvidence = intactParticipantEvidence.getInteraction();
        Experiment experiment = interactionEvidence.getExperiment();
        Publication publication = experiment.getPublication();

        line.setFeatureAc(featureToExportLine.extractIntactAc(featureEvidence.getIdentifiers()));
        line.setFeatureShortlabel(featureEvidence.getShortName());
        line.setFeatureType(featureToExportLine.extractFeatureType(featureEvidence.getType()));
        line.setAnnotations(featureToExportLine.extractAnnotations(featureEvidence.getAnnotations()));
        line.setAffectedProteinAc(featureToExportLine.extractUniPortAc(intactInteractor.getIdentifiers()));
        line.setAffectedProteinSymbol(featureToExportLine.extractProteinSymbol(intactInteractor.getShortName(), intactInteractor.getAliases()));
        line.setAffectedProteinFullName(intactInteractor.getFullName());
        line.setAffectedProteinOrganism(featureToExportLine.extractInteractorOrganism(intactInteractor.getOrganism()));
        line.setParticipants(featureToExportLine.extractParticipants(interactionEvidence));
        line.setPubmedId(publication.getPubmedId());
        line.setFigureLegend(featureToExportLine.extractFigureLegend(interactionEvidence.getAnnotations()));
        for (Range range : featureEvidence.getRanges()) {
            line.getExportRange().add(featureToExportLine.buildRange(range));
        }
        line.setInteractionAc(featureToExportLine.extractIntactAc(interactionEvidence.getIdentifiers()));
        return line;
    }

    private String extractFigureLegend(Collection<Annotation> annotations) {
        Annotation annotation = annotations.stream().filter(a -> a.getTopic().getShortName().equals(MI_FIGURELEGEND)).findFirst().orElse(null);
        if (annotation != null) {
            return annotation.getValue().replaceAll(TAB, ONE_SPACE).replaceAll(NEW_LINE, ONE_SPACE);
        }
        return EMPTY_STRING;
    }

    private String extractIntactAc(Collection<Xref> identifiers) {
        Xref xref = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals(MI_IDENTITY) && i.getDatabase().getShortName().equals(MI_INTACT)).findFirst().orElse(null);
        if (xref != null) {
            return xref.getId();
        }
        return EMPTY_STRING;
    }

    private String extractUniPortAc(Collection<Xref> identifiers) {
        Collection<Xref> xrefs;
        Xref intactIdentity = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals(MI_IDENTITY) && i.getDatabase().getShortName().equals(MI_INTACT)).collect(Collectors.toList()).get(0);

        xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals(MI_IDENTITY) && i.getDatabase().getShortName().equals(MI_UNIPROT)).collect(Collectors.toList());

        if (xrefs.size() == 1) {
            Xref xref = xrefs.iterator().next();
            return xref.getDatabase().getShortName() + COLON + xref.getId();
        }

        if (xrefs.isEmpty()) {
            xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals(MI_MULTIPLEPARENT)).collect(Collectors.toList());
            List<String> strings = xrefs.stream().map(Xref::getId).collect(Collectors.toList());
            return intactIdentity.getDatabase().getShortName() + COLON + intactIdentity.getId() + "(fusion of uniprotkb:" + StringUtils.join(strings, ";") + ")";
        }
        if (xrefs.isEmpty()) {
            xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("see also")).collect(Collectors.toList());
            List<String> strings = xrefs.stream().map(Xref::getId).collect(Collectors.toList());
            return intactIdentity.getDatabase().getShortName() + COLON + intactIdentity.getId() + "(see uniprotkb:" + StringUtils.join(strings, ";") + ")";
        }
        if (xrefs.isEmpty()) {
            return intactIdentity.getDatabase().getShortName() + COLON + intactIdentity.getId();
        }
        return EMPTY_STRING;
    }

    private String extractIdentityAc(Collection<Xref> dbXrefs) {
        Xref xref = dbXrefs.stream().filter(i -> i.getQualifier().getShortName().equals(MI_IDENTITY)).findFirst().orElse(null);
        if (xref != null) {
            return xref.getDatabase().getShortName() + COLON + xref.getId();

        }
        return EMPTY_STRING;
    }

    private String extractParticipants(InteractionEvidence interactionEvidence) {
        Collection<String> participants = new ArrayList<>();
        interactionEvidence.getParticipants().forEach(p -> participants.add(extractIdentityAc(p.getInteractor().getIdentifiers()) +
                "(" + extractFeatureType(p.getInteractor().getInteractorType()) +
                ", " + extractInteractorOrganism(p.getInteractor().getOrganism()) + ")"));
        if (!participants.isEmpty()) {
            return StringUtils.join(participants, ";");
        }
        return EMPTY_STRING;
    }

    private ExportRange buildRange(Range range) {
        ExportRange exportRange = new ExportRange();
        exportRange.setRange(range.getStart().getStart() + "-" + range.getEnd().getEnd());
        exportRange.setOriginalSequence(range.getResultingSequence().getOriginalSequence());
        exportRange.setResultingSequence(range.getResultingSequence().getNewSequence());
        return exportRange;
    }

    private String extractProteinSymbol(String shortName, Collection<Alias> aliases) {
        Alias alias = aliases.stream().filter(a -> a.getType().getShortName().equals(MI_GENENAME)).findFirst().orElse(null);
        if (alias == null) {
            return shortName;
        } else {
            return alias.getName();
        }
    }

    private String extractInteractorOrganism(Organism organism) {
        return organism.getTaxId() + " - " + organism.getScientificName();
    }

    private String extractAnnotations(Collection<Annotation> annotations) {
        List<Annotation> annotationsFiltered = annotations.stream().filter(annotation -> !annotation.getTopic().getShortName().equals("remark-internal") && !annotation.getTopic().getShortName().equals("no-mutation-update")).collect(Collectors.toList());
        //        Annotation annotationsFiltered = annotations.stream().filter(a -> !a.getTopic().getShortName().equals("remark-internal") && !a.getTopic().getShortName().equals("no-mutation-update")).findAny().orElse(null);
        if (!annotationsFiltered.isEmpty()) {
            return StringUtils.join(annotations, ",").replaceAll(TAB, ONE_SPACE).replaceAll(NEW_LINE, ONE_SPACE);
        }
        return EMPTY_STRING;

    }

    private String extractFeatureType(CvTerm type) {
        return type.getShortName() + "(" + type.getShortName() + ")";
    }
}