package uk.ac.ebi.intact.dataexchange.psimi.solr.util;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.BioactiveEntity;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Interactor;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;

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

    public static String getParticipantName(ModelledParticipant participant) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null) {
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                return protein.getGeneName() != null ? protein.getGeneName(): protein.getPreferredName();
            } else if (interactor instanceof BioactiveEntity) {
                BioactiveEntity bioactiveEntity = (BioactiveEntity) interactor;
                return bioactiveEntity.getShortName();
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                return complexParticipant.getRecommendedName();
            } else {
                for (Xref x : interactor.getIdentifiers()) {
                    if (x.getDatabase().getMIIdentifier().equals(RNA_CENTRAL_MI)) {
                        return interactor.getShortName();
                    }
                }
            }
            return interactor.getShortName();
        }
        return null;
    }

    public static String getParticipantIdentifier(ModelledParticipant participant) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null) {
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                return protein.getPreferredIdentifier().getId();
            } else if (interactor instanceof BioactiveEntity) {
                BioactiveEntity bioactiveEntity = (BioactiveEntity) interactor;
                return bioactiveEntity.getChebi();
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                return complexParticipant.getComplexAc();
            } else {
                for (Xref x : interactor.getIdentifiers()) {
                    if (x.getDatabase().getMIIdentifier().equals(RNA_CENTRAL_MI)) {
                        return x.getId();
                    }
                }
            }
            return interactor.getPreferredIdentifier().getId();
        }
        return null;
    }

    public static String getParticipantIdentifierLink(ModelledParticipant participant, String identifier) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null && identifier != null) {
            Annotation searchUrl = AnnotationUtils.collectFirstAnnotationWithTopic(interactor.getPreferredIdentifier().getDatabase().getAnnotations(), SEARCH_MI, SEARCH);
            if (searchUrl != null) {
                return searchUrl.getValue().replaceAll("\\$*\\{ac\\}", identifier);
            }
        }
        return null;
    }

    public static String getParticipantStoichiometry(ModelledParticipant participant) {
        if (participant.getStoichiometry().getMinValue() != 0 || participant.getStoichiometry().getMaxValue() != 0) {
            return participant.getStoichiometry().toString();
        }
        return null;
    }

    public static Collection<ModelledParticipant> mergeParticipants(Collection<ModelledParticipant> participants) {
        if (participants.size() > 1) {
            Comparator<ModelledParticipant> comparator = Comparator.comparing(o -> ((IntactInteractor) o.getInteractor()).getAc());
            List<ModelledParticipant> participantList = (List<ModelledParticipant>) participants;
            participantList.sort(comparator);
            Collection<ModelledParticipant> merged = new ArrayList<>();
            ModelledParticipant aux = participantList.get(0);
            int stochiometry = 0;
            for (ModelledParticipant participant : participantList) {
                if (((IntactInteractor) aux.getInteractor()).getAc().equals(((IntactInteractor) participant.getInteractor()).getAc())) {
                    //Same
                    stochiometry += participant.getStoichiometry().getMinValue();
                } else {
                    //Different
                    aux.setStoichiometry(stochiometry);
                    merged.add(aux);
                    aux = participant;
                    stochiometry = aux.getStoichiometry().getMinValue();
                }
            }
            aux.setStoichiometry(stochiometry);
            merged.add(aux);
            return merged;
        } else {
            return participants;
        }
    }
}
