package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a document in the Solr index for complex. It is just a convenience
 * class that wraps the access to documents by using the appropriate document fields.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 24/07/13
 */
public final class ComplexDocument {

    /********************************/
    /*      Private attributes      */
    /********************************/

    //Complex attributes
    private String             ID;
    private Collection<String> complexID;
    private String             complexName;
    private Collection<String> complexOrganism;
    private Collection<String> complexAlias;
    private Collection<String> complexType;
    private Collection<String> complexXref;
    private String             complexAC;
    private String             description;
    private String             organismName;

    //Interactor attributes
    private Collection<String> interactorID;
    private Collection<String> interactorAlias;
    private Collection<String> interactorType;

    //Others attributes
    private Collection<String> biorole;
    private Collection<String> features;
    private Collection<String> source;
    private Collection<String> numberParticipants;
    private Collection<String> pathwayXref;
    private Collection<String> ecoXref;
    private Collection<String> publicationID;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexDocument(String ID_,
                           String complexName_,
                           String complexAC_,
                           String description_){

        //String parameters received
        this.ID                 = ID_;
        this.complexName        = complexName_;
        this.complexAC          = complexAC_;
        this.description        = description_;

        //Initialize collections
        this.complexID          = new HashSet<String>();
        this.complexOrganism    = new HashSet<String>();
        this.complexAlias       = new HashSet<String>();
        this.complexType        = new HashSet<String>();
        this.complexXref        = new HashSet<String>();

        this.interactorID       = new HashSet<String>();
        this.interactorAlias    = new HashSet<String>();
        this.interactorType     = new HashSet<String>();
        this.biorole            = new HashSet<String>();
        this.features           = new HashSet<String>();
        this.source             = new HashSet<String>();
        this.numberParticipants = new HashSet<String>();
        this.pathwayXref        = new HashSet<String>();
        this.ecoXref            = new HashSet<String>();
        this.publicationID      = new HashSet<String>();
    }
    public ComplexDocument(String ID_,
                           String complexName_,
                           String complexAC_,
                           String description_,
                           String organismName_){
        this(ID_, complexName_, complexAC_, description_);
        this.organismName = organismName_;
    }

    /*********************/
    /*      Getters      */
    /*********************/
    public String getID()           { return ID; }
    public String getComplexName()  { return complexName; }
    public String getComplexAC()    { return complexAC; }
    public String getDescription()  { return description; }
    public String getOrganismName() { return organismName; }
    public Collection<String> getComplexID()            { return new HashSet<String>(complexID); }
    public Collection<String> getComplexOrganism()      { return new HashSet<String>(complexOrganism); }
    public Collection<String> getComplexAlias()         { return new HashSet<String>(complexAlias); }
    public Collection<String> getComplexType()          { return new HashSet<String>(complexType); }
    public Collection<String> getComplexXref()          { return new HashSet<String>(complexXref); }
    public Collection<String> getInteractorID()         { return new HashSet<String>(interactorID); }
    public Collection<String> getInteractorAlias()      { return new HashSet<String>(interactorAlias); }
    public Collection<String> getInteractorType()       { return new HashSet<String>(interactorType); }
    public Collection<String> getBiorole()              { return new HashSet<String>(biorole); }
    public Collection<String> getFeatures()             { return new HashSet<String>(features); }
    public Collection<String> getSource()               { return new HashSet<String>(source); }
    public Collection<String> getNumberParticipants()   { return new HashSet<String>(numberParticipants); }
    public Collection<String> getPathwayXref()          { return new HashSet<String>(pathwayXref); }
    public Collection<String> getEcoXref()              { return new HashSet<String>(ecoXref); }
    public Collection<String> getPublicationID()        { return new HashSet<String>(publicationID); }

    /********************/
    /*      Adders      */
    /********************/
    public boolean addComplexID(String ID)              { return complexID.add(ID); }
    public boolean addComplexOrganism(String Organism)  { return complexOrganism.add(Organism); }
    public boolean addComplexAlias(String Alias)        { return complexAlias.add(Alias); }
    public boolean addComplexType(String Type)          { return complexType.add(Type); }
    public boolean addComplexXref(String Xref)          { return complexXref.add(Xref); }
    public boolean addInteractorID(String ID)           { return interactorID.add(ID); }
    public boolean addInteractorAlias(String Alias)     { return interactorAlias.add(Alias); }
    public boolean addInteractorType(String Type)       { return interactorType.add(Type); }
    public boolean addBiorole(String Role)              { return biorole.add(Role); }
    public boolean addFeatures(String Feature)          { return features.add(Feature); }
    public boolean addSource(String Source)             { return source.add(Source); }
    public boolean addNumberParticipants(String Number) { return numberParticipants.add(Number); }
    public boolean addPathwayXref(String Xref)          { return pathwayXref.add(Xref); }
    public boolean addEcoXref(String Xref)              { return ecoXref.add(Xref); }
    public boolean addPublicationID(String ID)          { return publicationID.add(ID); }

    /*******************************/
    /*      Collection Adders      */
    /*******************************/
    public boolean addComplexID(Collection<String> IDs){
        boolean result = true;
        for(String ID : IDs)
            result = result || addComplexID(ID);
        return result;
    }
    public boolean addComplexOrganism(Collection<String> Organisms) {
        boolean result = true;
        for(String Organism : Organisms)
            result = result || addComplexOrganism(Organism);
        return result;
    }
    public boolean addComplexAlias(Collection<String> Aliass){
        boolean result = true;
        for(String Alias : Aliass)
            result = result || addComplexAlias(Alias);
        return result;
    }
    public boolean addComplexType(Collection<String> Types){
        boolean result = true;
        for(String Type : Types)
            result = result || addComplexType(Type);
        return result;
    }
    public boolean addComplexXref(Collection<String> Xrefs){
        boolean result = true;
        for(String Xref : Xrefs)
            result = result || addComplexXref(Xref);
        return result;
    }
    public boolean addInteractorID(Collection<String> IDs){
        boolean result = true;
        for(String ID : IDs)
            result = result || addInteractorID(ID);
        return result;
    }
    public boolean addInteractorAlias(Collection<String> Aliass){
        boolean result = true;
        for(String Alias : Aliass)
            result = result || addInteractorAlias(Alias);
        return result;
    }
    public boolean addInteractorType(Collection<String> Types){
        boolean result = true;
        for(String Type : Types)
            result = result || addInteractorType(Type);
        return result;
    }
    public boolean addBiorole(Collection<String> Roles){
        boolean result = true;
        for(String Rol : Roles)
            result = result || addBiorole(Rol);
        return result;
    }
    public boolean addFeatures(Collection<String> Features){
        boolean result = true;
        for(String Feature : Features)
            result = result || addFeatures(Feature);
        return result;
    }
    public boolean addSource(Collection<String> Sources){
        boolean result = true;
        for(String Source : Sources)
            result = result || addSource(Source);
        return result;
    }
    public boolean addNumberParticipants(Collection<String> Numbers){
        boolean result = true;
        for(String Number : Numbers)
            result = result || addNumberParticipants(Number);
        return result;
    }
    public boolean addPathwayXref(Collection<String> Xrefs){
        boolean result = true;
        for(String Xref : Xrefs)
            result = result || addPathwayXref(Xref);
        return result;
    }
    public boolean addEcoXref(Collection<String> Xrefs){
        boolean result = true;
        for(String Xref : Xrefs)
            result = result || addEcoXref(Xref);
        return result;
    }
    public boolean addPublicationID(Collection<String> IDs){
        boolean result = true;
        for(String ID : IDs)
            result = result || addPublicationID(ID);
        return result;
    }

    /******************************/
    /*      Override Methods      */
    /******************************/
    @Override
    public boolean equals(Object other){
        // Object level comparisons
        if ( this == other ) return true;
        if ( other == null || getClass() != other.getClass() ) return false;

        // ComplexDocument level comparisons
        ComplexDocument otherComplexDocument = (ComplexDocument) other;
        if ( ID             != null ? !ID.equals(otherComplexDocument.ID)                     : otherComplexDocument.ID             != null ) return false;
        if ( complexName    != null ? !complexName.equals(otherComplexDocument.complexName)   : otherComplexDocument.complexName    != null ) return false;
        if ( complexAC      != null ? !complexAC.equals(otherComplexDocument.complexAC)       : otherComplexDocument.complexAC      != null ) return false;
        if ( description    != null ? !description.equals(otherComplexDocument.description)   : otherComplexDocument.description    != null ) return false;
        if ( organismName   != null ? !organismName.equals(otherComplexDocument.organismName) : otherComplexDocument.organismName   != null ) return false;

        // If all checked is equal return true
        return true;
    }
    
    @Override
    public int hashCode(){
        int result = 0;

        result =               (ID              != null ? ID.hashCode()             : 0);
        result = 31 * result + (complexName     != null ? complexName.hashCode()    : 0);
        result = 31 * result + (complexAC       != null ? complexAC.hashCode()      : 0);
        result = 31 * result + (description     != null ? description.hashCode()    : 0);
        result = 31 * result + (organismName    != null ? organismName.hashCode()   : 0);

        return result;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append("ComplexDocument{");
        sb.append("id = '").append(ID).append("'");
        sb.append(", complexName = '").append(complexName).append("'");
        sb.append(", complexAC = '").append(complexAC).append("'");
        sb.append(", description = '").append(description).append("'");
        sb.append(", organismName = '").append(organismName).append("'");
        sb.append("}");
        return sb.toString();
    }
}
