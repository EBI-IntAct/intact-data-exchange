package uk.ac.ebi.intact.dataexchange.structuredabstract.model;

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Participant;

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
    private CvTerm detMethod;
    private List<String> interactionAcs;

    public Sentence(CvTerm interactionType, CvTerm detMethod){
        if (interactionType == null || interactionType.getMIIdentifier() == null){
            throw new IllegalArgumentException("The interaction type cannot be null and must have a valid identifier.");
        }
        if (detMethod == null || detMethod.getMIIdentifier() == null){
            throw new IllegalArgumentException("The interaction detection method cannot be null and must have a valid identifier.");
        }
        interactorsObject = new ArrayList<SimpleInteractor>();
        interactorsSubject = new ArrayList<SimpleInteractor>();
        interactionAcs = new ArrayList<String>();
        this.interactionTypeMI = interactionType.getMIIdentifier();
        this.detMethod = detMethod;
    }

    public void addInteractorObject(Participant participant) {
        interactorsObject.add(new SimpleInteractor(participant));
    }

    public void addInteractorSubject(Participant participant) {
        interactorsSubject.add(new SimpleInteractor(participant));
    }

    public void addInteractionAc(String mintAc) {
        interactionAcs.add(mintAc);
    }

    public List<String> getInteractionAcs() {
        return interactionAcs;
    }

    public List<SimpleInteractor> getInteractorsObject() {
        return interactorsObject;
    }

    public List<SimpleInteractor> getInteractorsSubject() {
        return interactorsSubject;
    }

    public String getInteractionTypeMI() {
        return interactionTypeMI;
    }

    public CvTerm getDetMethod() {
        return detMethod;
    }
}
