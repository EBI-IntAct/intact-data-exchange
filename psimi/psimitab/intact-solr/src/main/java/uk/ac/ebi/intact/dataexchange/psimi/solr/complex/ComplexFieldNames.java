package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public interface ComplexFieldNames {

    String ID                           = "doc_id";
    String COMPLEX_ID                   = "complex_id";
    String COMPLEX_NAME                 = "complex_name";
    String COMPLEX_ORGANISM             = "species";
    String COMPLEX_ORGANISM_F           = "species_f";
    String COMPLEX_ORGANISM_EXACT       = "species_e";
    String COMPLEX_ORGANISM_SORT        = "species_s";
    String COMPLEX_ALIAS                = "complex_alias";
    String COMPLEX_TYPE                 = "complex_type";
    String COMPLEX_TYPE_F               = "complex_type_f";
    String COMPLEX_TYPE_EXACT           = "complex_type_e";
    String INTERACTION_TYPE             = "type";
    String INTERACTION_TYPE_F           = "type_f";
    String INTERACTION_TYPE_EXACT = "type_e";
    String COMPLEX_XREF                 = "complex_xref";
    String COMPLEX_XREF_EXACT        = "complex_xref_e";
    String COMPLEX_AC                   = "complex_AC";
    String DESCRIPTION                  = "description";
    String ORGANISM_NAME                = "organism_name";
    String UDATE                        = "udate";

    String INTERACTOR_ID                = "id";
    String INTERACTOR_ALIAS             = "alias";
    String INTERACTOR_TYPE              = "ptype";
    String INTERACTOR_TYPE_F           = "ptype_f";
    String INTERACTOR_TYPE_EXACT = "ptype_e";
    String INTERACTOR_XREF              = "pxref";
    String INTERACTOR_XREF_EXACT = "pxref_e";
    String STC                          = "stc";
    String PARAM                        = "param";

    String BIOROLE                      = "pbiorole";
    String BIOROLE_F                    = "pbiorole_f";
    String BIOROLE_EXACT = "pbiorole_e";
    String FEATURES                     = "ftype";
    String FEATURES_F                   = "ftype_f";
    String FEATURES_EXACT = "ftype_e";
    String SOURCE                       = "source";
    String SOURCE_F                     = "source_f";

    String NUMBER_PARTICIPANTS          = "number_participants";
    //String PATHWAY_XREF               = "pathway_xref";
    //String ECO_XREF                   = "eco_xref";
    String PUBLICATION_ID               = "pubid";

}
