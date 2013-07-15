package uk.ac.ebi.intact.dataexchange;

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvInteractionType;

import java.util.ArrayList;
import java.util.List;

/**
 * The sentence of a structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public class Sentence {

    private List<SimpleInteractor> interactorsObject;
    private List<SimpleInteractor> interactorsSubject;
    private String interactionTypeMI;
    private CvInteraction detMethod;
    private List<String> interactionAcs;

    public Sentence(CvInteractionType interactionType, CvInteraction detMethod){
        if (interactionType == null || interactionType.getIdentifier() == null){
            throw new IllegalArgumentException("The interaction type cannot be null and must have a valid identifier.");
        }
        if (detMethod == null || detMethod.getIdentifier() == null){
            throw new IllegalArgumentException("The interaction detection method cannot be null and must have a valid identifier.");
        }
        interactorsObject = new ArrayList<SimpleInteractor>();
        interactorsSubject = new ArrayList<SimpleInteractor>();
        interactionAcs = new ArrayList<String>();
        this.interactionTypeMI = interactionType.getIdentifier();
        this.detMethod = detMethod;
    }

    public void addProteinsObject(Component participant) {
        interactorsObject.add(new SimpleInteractor(participant));
    }

    public void addProteinsSubject(Component participant) {
        interactorsSubject.add(new SimpleInteractor(participant));
    }

    public void addMintAc(String mintAc) {
        interactionAcs.add(mintAc);
    }
}
