package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

public class ComplexInteractorXref {

    /*************************/
    /*      Constructors     */
    /*************************/

    public ComplexInteractorXref() {
    }

    public ComplexInteractorXref(String identifier,
                                 String identifierLink,
                                 String database,
                                 String qualifier) {
        this.identifier = identifier;
        this.identifierLink = identifierLink;
        this.database = database;
        this.qualifier = qualifier;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    /********************************/
    /*      Private attributes      */
    /********************************/

    private String identifier = null;
    private String identifierLink = null;
    private String database = null;
    private String qualifier = null;
}
