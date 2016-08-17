package uk.ac.ebi.intact.export.mutation.helper;

import org.apache.commons.lang.StringUtils;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.export.mutation.helper.model.ExportRange;
import uk.ac.ebi.intact.export.mutation.helper.model.MutationExportLine;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;

import java.util.*;
import java.util.stream.Collectors;

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
        if(line.getAffectedProteinAc() == null || line.getAffectedProteinAc().isEmpty())
            System.err.println(line.getFeatureAc() + " " + line.getFeatureShortlabel());
        for (Range range : featureEvidence.getRanges()) {
            line.getExportRange().add(featureToExportLine.buildRange(range));
        }
        line.setInteractionAc(featureToExportLine.extractIntactAc(interactionEvidence.getIdentifiers()));
        return line;
    }

    private String extractFigureLegend(Collection<Annotation> annotations) {
        Annotation annotation = annotations.stream().filter(a -> a.getTopic().getShortName().equals("figure legend")).findFirst().orElse(null);
        if (annotation == null) {
            return "";
        } else {
            return annotation.getValue().replaceAll("\t", " ").replaceAll("\n", " ");
        }
    }

    private String extractIntactAc(Collection<Xref> identifiers) {
        Xref xref = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("identity") && i.getDatabase().getShortName().equals("intact")).findFirst().orElse(null);
        if (xref == null) {
            return "";
        } else {
            return xref.getId();
        }
    }


    private String extractUniPortAc(Collection<Xref> identifiers) {
        Collection<Xref> xrefs;
        xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("identity") && i.getDatabase().getShortName().equals("uniprotkb")).collect(Collectors.toList());
        if (xrefs == null || xrefs.isEmpty()) {
            xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("multiple parent")).collect(Collectors.toList());
        }
        if (xrefs == null || xrefs.isEmpty()) {
            xrefs = identifiers.stream().filter(i -> i.getQualifier().getShortName().equals("identity") && i.getDatabase().getShortName().equals("intact")).collect(Collectors.toList());
        }
        if (xrefs == null) {
            return "";
        } else if (xrefs.size() == 1) {
            Xref xref = xrefs.iterator().next();
            return xref.getDatabase().getShortName() + ":" + xref.getId();
        } else {
            Iterator<Xref> xrefIterator = xrefs.iterator();
            List<String> strings = new ArrayList<>();
            while (xrefIterator.hasNext()) {
                Xref xref = xrefIterator.next();
                strings.add(xref.getDatabase().getShortName() + ":" + xref.getId());
            }
            return StringUtils.join(strings, ";");
        }
    }

    private String extractIdentityAc(Collection<Xref> dbXrefs) {
        Xref xref = dbXrefs.stream().filter(i -> i.getQualifier().getShortName().equals("identity")).findFirst().orElse(null);
        if (xref == null) {
            return "";
        } else {
            return xref.getDatabase().getShortName() + ":" + xref.getId();
        }
    }

    private String extractParticipants(InteractionEvidence interactionEvidence) {
        Collection<String> participants = new ArrayList<>();
        interactionEvidence.getParticipants().forEach(p -> participants.add(extractIdentityAc(p.getInteractor().getIdentifiers()) +
                "(" + extractFeatureType(p.getInteractor().getInteractorType()) +
                ", " + extractInteractorOrganism(p.getInteractor().getOrganism()) + ")"));
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
        Annotation annotationsFiltered = annotations.stream().filter(a -> !a.getTopic().getShortName().equals("remark-internal") && !a.getTopic().getShortName().equals("no-mutation-update")).findAny().orElse(null);
        if (annotationsFiltered == null) {
            return "";
        } else {
            return StringUtils.join(annotations, ",").replaceAll("\t", " ").replaceAll("\n", " ");
        }
    }

    private String extractFeatureType(CvTerm type) {
        return type.getShortName() + "(" + type.getMIIdentifier() + ")";
    }
}
