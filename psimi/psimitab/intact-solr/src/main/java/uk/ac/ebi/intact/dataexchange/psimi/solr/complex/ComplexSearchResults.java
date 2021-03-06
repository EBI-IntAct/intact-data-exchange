package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

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
    public ComplexSearchResults ( String complexAC_,
                                  String complexName_,
                                  String organismName_,
                                  String description_) {
        this.complexAC      = complexAC_        ;
        this.complexName    = complexName_      ;
        this.organismName   = organismName_     ;
        this.description    = description_      ;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public String getComplexAC      ( ) { return complexAC      ; }
    public String getComplexName    ( ) { return complexName    ; }
    public String getOrganismName   ( ) { return organismName   ; }
    public String getDescription    ( ) { return description    ; }

    public void setComplexAC      ( String AC )      { complexAC = AC           ; }
    public void setComplexName    ( String Name )    { complexName = Name       ; }
    public void setOrganismName   ( String Name )    { organismName = Name      ; }
    public void setDescription    ( String Desc )    { description = Desc       ; }

    /********************************/
    /*      Private attributes      */
    /********************************/
    private String complexAC        = null ;
    private String complexName      = null ;
    private String organismName     = null ;
    private String description      = null ;

}
