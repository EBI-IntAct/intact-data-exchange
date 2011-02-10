package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default implementation of the SecondCCinteractor2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class DefaultSecondCCInteractor2 implements SecondCCParameters2 {

    // duplicated line because of splice variants and feature chains!!!
    private String firstUniprotAc;
    private String secondUniprotAc;

    private String secondIntact;
    private String firstIntact;

    private String geneName;
    private String taxId;
    private String organismName;

    private SortedSet<InteractionDetails> interactionDetails;

    public DefaultSecondCCInteractor2(String firstInteractor, String firstIntactAc, String secondInteractor, String secondIntactAc, String secondGeneName,
                                      String secondTaxId, String secondOrganismName,
                                      SortedSet<InteractionDetails> interactionDetails){

        this.firstUniprotAc = firstInteractor;
        this.secondUniprotAc = secondInteractor;
        this.firstIntact = firstIntactAc;
        this.secondIntact = secondIntactAc;

        this.geneName = secondGeneName;
        this.organismName = secondOrganismName;
        this.taxId = secondTaxId;

        if (interactionDetails == null){
            this.interactionDetails = new TreeSet<InteractionDetails>();
        }
        else {
            this.interactionDetails = interactionDetails;
        }
    }

    @Override
    public String getSecondUniprotAc() {
        return this.secondUniprotAc;
    }

    @Override
    public String getSecondIntactAc() {
        return this.secondIntact;
    }

    @Override
    public String getGeneName() {
        return this.geneName;
    }

    @Override
    public String getTaxId() {
        return this.taxId;
    }

    @Override
    public String getOrganismName() {
        return this.organismName;
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
    public String getFirstUniprotAc() {
        return this.firstUniprotAc;
    }
}
