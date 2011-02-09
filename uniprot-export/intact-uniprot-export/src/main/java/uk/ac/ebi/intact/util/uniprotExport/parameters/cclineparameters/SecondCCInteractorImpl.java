package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default implementation of the SecondCCinteractor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class SecondCCInteractorImpl implements SecondCCInteractor{

    // duplicated line because of splice variants and feature chains!!!
    private String firstInteractor;
    private String secondInteractor;

    private String secondIntact;
    private String firstIntact;

    private String secondGeneName;
    private String secondTaxId;
    private String secondOrganismName;

    private SortedSet<InteractionDetails> interactionDetails;

    public SecondCCInteractorImpl(String firstInteractor, String secondInteractor, String firstIntactAc, String secondIntactAc, String secondGeneName,
                                  String secondTaxId, String secondOrganismName,
                                  SortedSet<InteractionDetails> interactionDetails){
        if (secondTaxId == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null second taxId.");
        }

        this.firstInteractor = firstInteractor;
        this.secondInteractor = secondInteractor;
        this.firstIntact = firstIntactAc != null ? firstIntactAc : "-";
        this.secondIntact = secondIntactAc != null ? secondIntactAc : "-";

        this.secondGeneName = secondGeneName != null ? secondGeneName : "-";
        this.secondOrganismName = secondOrganismName != null ? secondOrganismName : "-";
        this.secondTaxId = secondTaxId;

        if (interactionDetails == null){
            this.interactionDetails = new TreeSet<InteractionDetails>();
        }
        else {
            this.interactionDetails = interactionDetails;
        }
    }

    @Override
    public String getSecondInteractor() {
        return this.secondInteractor;
    }

    @Override
    public String getSecondIntactAc() {
        return this.secondIntact;
    }

    @Override
    public String getSecondGeneName() {
        return this.secondGeneName;
    }

    @Override
    public String getSecondTaxId() {
        return this.secondTaxId;
    }

    @Override
    public String getSecondOrganismName() {
        return this.secondOrganismName;
    }

    @Override
    public SortedSet<InteractionDetails> getInteractionDetails() {
        return this.interactionDetails;
    }

    @Override
    public String getFirstIntacAc() {
        return this.firstIntact;
    }

    @Override
    public String getFirstInteractor() {
        return this.firstInteractor;
    }
}
