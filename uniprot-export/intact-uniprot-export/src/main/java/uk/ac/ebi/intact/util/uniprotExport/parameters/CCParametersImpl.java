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

    private String firstGeneName;

    private String firstTaxId;
    private String firstOrganismName;

    private List<SecondCCInteractor> secondCCinteractors;

    public CCParametersImpl(String firstInteractor, String firstGeneName,
                            String firstTaxId, List<SecondCCInteractor> secondInteractors){
        if (firstTaxId == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null first taxId.");
        }
        else if (secondInteractors == null){
            throw new IllegalArgumentException("The CCLine parameters need a non null list of second interactors.");
        }

        this.firstInteractor = firstInteractor;

        this.firstGeneName = firstGeneName != null ? firstGeneName : "-";
        this.firstTaxId = firstTaxId;

        this.secondCCinteractors = secondInteractors;
    }

    @Override
    public String getFirstInteractor() {
        return firstInteractor;
    }

    public String getFirstGeneName() {
        return firstGeneName;
    }

    public String getFirstTaxId() {
        return firstTaxId;
    }

    @Override
    public List<SecondCCInteractor> getSecondCCInteractors() {
        return this.secondCCinteractors;
    }

    public void setFirstGeneName(String firstGeneName) {
        this.firstGeneName = firstGeneName;
    }

    public void setFirstOrganismName(String firstOrganismName) {
        this.firstOrganismName = firstOrganismName;
    }
}
