package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

/**
 * This class allows to convert a Intact experiment in MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class ExperimentConverter {
    
    private PublicationConverter publicationConverter;
    private CvObjectConverter<CvObject> cvObjectConverter;
    public BioSourceConverter organismConverter;

    public ExperimentConverter(){
        this.publicationConverter = new PublicationConverter();
        this.cvObjectConverter = new CvObjectConverter<CvObject>();
        this.organismConverter = new BioSourceConverter();
    }

    public void intactToMitab(Experiment exp, BinaryInteraction binary, boolean processParticipantDetMethod, boolean processPublication){

        if (exp != null && binary != null){
            // process publication
            Publication pub = exp.getPublication();
            if (pub != null && processPublication){
                publicationConverter.intactToMitab(pub, binary);
            }

            // convert interaction detection method
            if (exp.getCvInteraction() != null){
                CrossReference detMethod = cvObjectConverter.toCrossReference(exp.getCvInteraction());

                if (detMethod != null){
                    binary.getDetectionMethods().add(detMethod);
                }
            }

            // process organism
            if (exp.getBioSource() != null){
                Organism organism = organismConverter.intactToMitab(exp.getBioSource());

                if (organism != null){
                    binary.setHostOrganism(organism);
                }
            }

            // process participant detection method
            if (processParticipantDetMethod){
                processParticipantDetectionMethod(exp, binary);
            }
        }

    }

    public void processParticipantDetectionMethod(Experiment exp, BinaryInteraction binary){
        // process participant detection method
        if (exp != null && exp.getCvIdentification() != null){
            CrossReference detMethod = cvObjectConverter.toCrossReference(exp.getCvIdentification());

            Interactor interactorA = binary.getInteractorA();
            Interactor interactorB = binary.getInteractorB();

            processParticipantDetectionMethodFor(interactorA, detMethod);
            processParticipantDetectionMethodFor(interactorB, detMethod);
        }
    }

    public void addParticipantDetectionMethodForInteractor(Experiment exp, Interactor interactor){
        // process participant detection method
        if (exp != null && exp.getCvIdentification() != null && interactor != null){
            CrossReference detMethod = cvObjectConverter.toCrossReference(exp.getCvIdentification());

            if (detMethod != null){

                interactor.getParticipantIdentificationMethods().add(detMethod);
            }
        }
    }

    private void processParticipantDetectionMethodFor(Interactor interactor, CrossReference detMethod){

        if (interactor != null && detMethod != null){

            if (interactor.getParticipantIdentificationMethods().isEmpty()){
                interactor.getParticipantIdentificationMethods().add(detMethod);
            }
        }
    }

    public boolean isAddPublicationIdentifiers() {
        return publicationConverter.isAddPublicationIdentifiers();
    }

    public void setAddPublicationIdentifiers(boolean addPublicationIdentifiers) {
        this.publicationConverter.setAddPublicationIdentifiers(addPublicationIdentifiers);
    }
}
