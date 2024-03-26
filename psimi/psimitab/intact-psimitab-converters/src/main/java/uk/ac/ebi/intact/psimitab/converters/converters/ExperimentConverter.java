package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

/**
 * This class allows to convert a Intact experiment in MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class ExperimentConverter {
    
    private PublicationConverter publicationConverter;
    private CvObjectConverter cvObjectConverter;
    public BioSourceConverter organismConverter;

    public ExperimentConverter(){
        this.publicationConverter = new PublicationConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.organismConverter = new BioSourceConverter();
    }

    public void intactToMitab(IntactExperiment exp, BinaryInteraction binary, boolean processParticipantDetMethod, boolean processPublication){

        if (exp != null && binary != null){
            // process publication
            IntactPublication pub = (IntactPublication) exp.getPublication();
            if (pub != null && processPublication){
                publicationConverter.intactToMitab(pub, binary);
            }

            // convert interaction detection method
            if (exp.getInteractionDetectionMethod() != null){
                CrossReference detMethod = cvObjectConverter.toCrossReference((IntactCvTerm) exp.getInteractionDetectionMethod());

                if (detMethod != null){
                    binary.getDetectionMethods().add(detMethod);
                }
            }

            // process organism
            if (exp.getHostOrganism() != null){
                Organism organism = organismConverter.intactToMitab((IntactOrganism) exp.getHostOrganism());

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

    public void processParticipantDetectionMethod(IntactExperiment exp, BinaryInteraction binary){
        // process participant detection method
        if (exp != null && exp.getParticipantIdentificationMethod() != null){
            CrossReference detMethod = cvObjectConverter.toCrossReference((IntactCvTerm) exp.getParticipantIdentificationMethod());

            Interactor interactorA = binary.getInteractorA();
            Interactor interactorB = binary.getInteractorB();

            processParticipantDetectionMethodFor(interactorA, detMethod);
            processParticipantDetectionMethodFor(interactorB, detMethod);
        }
    }

    public void addParticipantDetectionMethodForInteractor(IntactExperiment exp, Interactor interactor){
        // process participant detection method
        if (exp != null && exp.getParticipantIdentificationMethod() != null && interactor != null){
            CrossReference detMethod = cvObjectConverter.toCrossReference((IntactCvTerm) exp.getParticipantIdentificationMethod());

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
}
