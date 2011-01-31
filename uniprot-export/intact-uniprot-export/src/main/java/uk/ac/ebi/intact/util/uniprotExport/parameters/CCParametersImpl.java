package uk.ac.ebi.intact.util.uniprotExport.parameters;

import java.util.*;

/**
 * Contains the parameters of a CCLine
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class CCParametersImpl implements CCParameters{

    private String firstInteractor;

    private String secondInteractor;

    private String firstGeneName;

    private String secondGeneName;
    private String firstTaxId;
    private String secondTaxId;
    private String firstOrganismName;
    private String secondOrganismName;

    private SortedSet<InteractionDetails> interactionDetails;

    public CCParametersImpl(String firstInteractor, String secondInteractor, String firstGeneName,
                            String secondGeneName, String firstTaxId, String secondTaxId,
                            String firstOrganismName, String secondOrganismName, SortedSet<InteractionDetails> interactionDetails){
        if (firstInteractor == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null first interactor.");
        }
        else if (secondInteractor == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null second interactor.");
        }
        else if (firstTaxId == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null first taxId.");
        }
        else if (secondTaxId == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null second taxId.");
        }

        this.firstInteractor = firstInteractor;
        this.secondInteractor = secondInteractor;

        this.firstGeneName = firstGeneName != null ? firstGeneName : "-";
        this.secondGeneName = secondGeneName != null ? secondGeneName : "-";
        this.firstOrganismName = firstOrganismName != null ? firstOrganismName : "-";
        this.secondOrganismName = secondOrganismName != null ? secondOrganismName : "-";
        this.firstTaxId = firstTaxId;
        this.secondTaxId = secondTaxId;

        if (interactionDetails == null){
             this.interactionDetails = new TreeSet<InteractionDetails>();
        }
        else {
            this.interactionDetails = interactionDetails;
        }
    }


    public String getFirstInteractor() {
        return firstInteractor;
    }

    public String getSecondInteractor() {
        return secondInteractor;
    }

    public String getFirstGeneName() {
        return firstGeneName;
    }

    public String getSecondGeneName() {
        return secondGeneName;
    }

    public String getFirstTaxId() {
        return firstTaxId;
    }

    public String getSecondTaxId() {
        return secondTaxId;
    }

    public String getFirstOrganismName() {
        return firstOrganismName;
    }

    public String getSecondOrganismName() {
        return secondOrganismName;
    }

    public SortedSet<InteractionDetails> getInteractionDetails() {
        return interactionDetails;
    }

    public void setFirstGeneName(String firstGeneName) {
        this.firstGeneName = firstGeneName;
    }

    public void setSecondGeneName(String secondGeneName) {
        this.secondGeneName = secondGeneName;
    }

    public void setFirstOrganismName(String firstOrganismName) {
        this.firstOrganismName = firstOrganismName;
    }

    public void setSecondOrganismName(String secondOrganismName) {
        this.secondOrganismName = secondOrganismName;
    }
}
