package uk.ac.ebi.intact.dataexchange.psimi.solr.util;

//import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Complex;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.SmallMolecule;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
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
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                name = ProteinUtils.getGeneName(protein);
            } else if (interactor instanceof SmallMolecule) {
                SmallMolecule bioactiveEntity = (SmallMolecule) interactor;
                name = bioactiveEntity.getShortLabel();
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                name = getComplexName(complexParticipant);
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
        if (xref != null) {
            Annotation annot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel((AnnotatedObject<?, ?>) xref, SEARCH_MI);
            if (annot == null) {
                annot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel((AnnotatedObject<?, ?>) xref, SEARCH);
            }

            if (annot != null) {
                return annot.getAnnotationText().replaceAll("\\$*\\{ac\\}", identifier);
            }
        }
        return null;
    }

    public static String getParticipantStoichiometry(Component participant) {
        if (participant.getStoichiometry() != 0 || participant.getMaxStoichiometry() != 0) {
            return "minValue: " + participant.getStoichiometry() + ", maxValue: " + participant.getMaxStoichiometry();
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
            int stochiometry = 0;
            for (Component participant : participantList) {
                if (aux.getInteractor().getAc().equals(participant.getInteractor().getAc())) {
                    //Same
                    stochiometry += (int) participant.getStoichiometry();
                } else {
                    //Different
                    aux.setStoichiometry(stochiometry);
                    merged.add(aux);
                    aux = participant;
                    stochiometry = (int) aux.getStoichiometry();
                }
            }
            aux.setStoichiometry(stochiometry);
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

    private static String getComplexName(Complex complex) {
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
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                xref = ProteinUtils.getUniprotXref(protein);
                if (xref == null) {
                    xref = XrefUtils.getIdentityXref(protein, CvDatabase.REFSEQ_MI_REF);
                }
            } else if (interactor instanceof SmallMolecule) {
                SmallMolecule bioactiveEntity = (SmallMolecule) interactor;
                xref = SmallMoleculeUtils.getChebiXref(bioactiveEntity);
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                xref = getComplexPrimaryXref(complexParticipant);
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
