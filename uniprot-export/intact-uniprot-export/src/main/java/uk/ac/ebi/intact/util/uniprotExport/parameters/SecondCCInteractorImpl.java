package uk.ac.ebi.intact.util.uniprotExport.parameters;

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

    private String secondInteractor;

    private String secondIntact;

    private String secondGeneName;
    private String secondTaxId;
    private String secondOrganismName;

    private SortedSet<InteractionDetails> interactionDetails;

    public SecondCCInteractorImpl(String secondInteractor, String secondIntactAc, String secondGeneName,
                                  String secondTaxId, String secondOrganismName,
                                  SortedSet<InteractionDetails> interactionDetails){
        if (secondTaxId == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null second taxId.");
        }

        this.secondInteractor = secondInteractor;
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
}
