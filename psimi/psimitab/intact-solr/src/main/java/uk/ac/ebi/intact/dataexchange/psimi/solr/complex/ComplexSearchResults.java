package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import java.util.List;

/**
 * This class is for represent a result of a complex search
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 30/07/13
 */
public class ComplexSearchResults {
    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexSearchResults ( ) { }
    public ComplexSearchResults(String complexAC_,
                                String complexName_,
                                String organismName_,
                                String description_,
                                Boolean predictedComplex_,
                                List<ComplexInteractor> interactors_) {
        this.complexAC = complexAC_;
        this.complexName = complexName_;
        this.organismName = organismName_;
        this.description = description_;
        this.predictedComplex = predictedComplex_;
        this.interactors = interactors_;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public String getComplexAC() {
        return complexAC;
    }
    public String getComplexName() {
        return complexName;
    }
    public String getOrganismName() {
        return organismName;
    }
    public String getDescription() {
        return description;
    }
    public boolean isPredictedComplex() {
        return predictedComplex != null && predictedComplex;
    }
    public List<ComplexInteractor> getInteractors() {
        return interactors;
    }

    public void setComplexAC(String AC) {
        complexAC = AC;
    }
    public void setComplexName(String Name) {
        complexName = Name;
    }
    public void setOrganismName(String Name) {
        organismName = Name;
    }
    public void setDescription(String Desc) {
        description = Desc;
    }
    public void setPredictedComplex(Boolean Predicted) {
        predictedComplex = Predicted;
    }
    public void setInteractors(List<ComplexInteractor> interactors) {
        this.interactors = interactors;
    }

    /********************************/
    /*      Private attributes      */
    /********************************/
    private String complexAC = null;
    private String complexName = null;
    private String organismName = null;
    private String description = null;
    private Boolean predictedComplex = false;
    private List<ComplexInteractor> interactors = null;

}
