package uk.ac.ebi.intact.dataexchange.psimi.solr.util;

import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.model.util.SmallMoleculeUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class ComplexUtils {

    private static final String RNA_CENTRAL_MI = "MI:1357";
    private static final String SEARCH = "search-url";
    private static final String SEARCH_MI = "MI:0615";

    public ComplexUtils() {
    }

    public static String getParticipantName(Component participant) {
        Interactor interactor = participant.getInteractor();
        String name = null;
        if (interactor != null) {
            CvInteractorType interactorType = interactor.getCvInteractorType();
            if (CvObjectUtils.isProteinType(interactorType)) {
                name = ProteinUtils.getGeneName(interactor);
            } else if (CvObjectUtils.isSmallMoleculeType(interactorType)) {
                name = interactor.getShortLabel();
            } else if (CvObjectUtils.isComplexType(interactorType)) {
                name = getComplexName(interactor);
            } else {
                name = getAlias(interactor, RNA_CENTRAL_MI);
            }
            if (name == null) {
                return interactor.getShortLabel();
            }
        }
        return name;
    }

    public static String getParticipantIdentifier(Component participant) {
        InteractorXref xref = getParticipantIdentifierXref(participant);
        if (xref != null) {
            return xref.getPrimaryId();
        }
        return null;
    }

    public static String getParticipantIdentifierLink(Component participant, String identifier) {
        InteractorXref xref = getParticipantIdentifierXref(participant);
        if (xref != null && xref.getParent() != null) {
            Annotation annot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(xref.getCvDatabase(), SEARCH_MI);
            if (annot == null) {
                annot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(xref.getCvDatabase(), SEARCH);
            }

            if (annot != null) {
                return annot.getAnnotationText().replaceAll("\\$*\\{ac\\}", identifier);
            }
        }
        return null;
    }

    public static String getParticipantStoichiometry(Component participant) {
        if (participant.getStoichiometry() != 0 || participant.getMaxStoichiometry() != 0) {
            return "minValue: " + ((int) participant.getStoichiometry()) + ", maxValue: " + ((int) participant.getMaxStoichiometry());
        }
        return null;
    }

    public static Collection<Component> mergeParticipants(Collection<Component> participants) {
        if (participants.size() > 1) {
            Comparator<Component> comparator = Comparator.comparing(o -> o.getInteractor().getAc());
            List<Component> participantList = (List<Component>) participants;
            participantList.sort(comparator);
            Collection<Component> merged = new ArrayList<>();
            Component aux = participantList.get(0);
            int minStochiometry = 0;
            int maxStochiometry = 0;
            for (Component participant : participantList) {
                if (aux.getInteractor().getAc().equals(participant.getInteractor().getAc())) {
                    //Same
                    minStochiometry += (int) participant.getStoichiometry();
                    maxStochiometry += (int) participant.getMaxStoichiometry();
                } else {
                    //Different
                    aux.setStoichiometry((float) minStochiometry);
                    aux.setMaxStoichiometry((float) maxStochiometry);
                    merged.add(aux);
                    aux = participant;
                    minStochiometry = (int) participant.getStoichiometry();
                    maxStochiometry = (int) participant.getMaxStoichiometry();
                }
            }
            aux.setStoichiometry((float) minStochiometry);
            aux.setMaxStoichiometry((float) maxStochiometry);
            merged.add(aux);
            return merged;
        } else {
            return participants;
        }
    }

    public static InteractorXref getComplexPrimaryXref(Interactor complex){
        Collection<InteractorXref> currentComplexXrefs = complex.getXrefs();
        for (InteractorXref xref : currentComplexXrefs) {
            if (xref.getCvXrefQualifier() != null &&
                    xref.getCvXrefQualifier().getIdentifier() != null &&
                    xref.getCvXrefQualifier().getIdentifier().equals("MI:2282") &&
                    xref.getCvXrefQualifier().getShortLabel() != null &&
                    xref.getCvXrefQualifier().getShortLabel().equals("complex-primary")) {
                return xref;
            }
        }

        return null;
    }

    private static String getComplexName(Interactor complex) {
        return getAlias(complex, CvAliasType.COMPLEX_RECOMMENDED_NAME_MI_REF);
    }
    private static String getAlias(Interactor complex, String id) {
        for (Alias alias : complex.getAliases()) {
            if (alias.getName() != null && alias.getCvAliasType() != null && alias.getCvAliasType().getIdentifier().equals(id)) {
                return alias.getName();
            }
        }
        return null;
    }

    private static InteractorXref getParticipantIdentifierXref(Component participant) {
        Interactor interactor = participant.getInteractor();
        InteractorXref xref = null;
        if (interactor != null) {
            CvInteractorType interactorType = interactor.getCvInteractorType();
            if (CvObjectUtils.isProteinType(interactorType)) {
                xref = ProteinUtils.getUniprotXref(interactor);
                if (xref == null) {
                    xref = XrefUtils.getIdentityXref(interactor, CvDatabase.REFSEQ_MI_REF);
                }
            } else if (CvObjectUtils.isSmallMoleculeType(interactorType)) {
                xref = SmallMoleculeUtils.getChebiXref(interactor);
            } else if (CvObjectUtils.isComplexType(interactorType)) {
                xref = getComplexPrimaryXref(interactor);
            } else {
                xref = XrefUtils.getIdentityXref(interactor, RNA_CENTRAL_MI);
            }
            if (xref == null) {
                Collection<InteractorXref> identityXrefs = XrefUtils.getIdentityXrefs(interactor);
                if (!identityXrefs.isEmpty()) {
                    xref = identityXrefs.iterator().next();
                }
            }
        }
        return xref;
    }
}
