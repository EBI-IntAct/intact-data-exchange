package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import java.util.List;

public class ComplexInteractor {

    /*************************/
    /*      Constructors     */
    /*************************/

    public ComplexInteractor() {
    }

    public ComplexInteractor(String identifier,
                             String identifierLink,
                             String name,
                             String description,
                             String stochiometry,
                             String interactorType,
                             String organismName,
                             List<ComplexInteractorXref> xrefs) {
        this.identifier = identifier;
        this.identifierLink = identifierLink;
        this.name = name;
        this.description = description;
        this.stochiometry = stochiometry;
        this.interactorType = interactorType;
        this.organismName = organismName;
        this.xrefs = xrefs;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifierLink() {
        return identifierLink;
    }

    public void setIdentifierLink(String identifierLink) {
        this.identifierLink = identifierLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStochiometry() {
        return stochiometry;
    }

    public void setStochiometry(String stochiometry) {
        this.stochiometry = stochiometry;
    }

    public String getInteractorType() {
        return interactorType;
    }

    public void setInteractorType(String interactorType) {
        this.interactorType = interactorType;
    }

    public String getOrganismName() {
        return organismName;
    }

    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }

    public List<ComplexInteractorXref> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<ComplexInteractorXref> xrefs) {
        this.xrefs = xrefs;
    }

    /********************************/
    /*      Private attributes      */
    /********************************/

    private String identifier = null;
    private String identifierLink = null;
    private String name = null;
    private String description = null;
    private String stochiometry = null;
    private String interactorType;
    private String organismName;
    private List<ComplexInteractorXref> xrefs;
}
