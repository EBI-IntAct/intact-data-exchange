package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public interface ComplexFieldNames {

    final static String ID                           = "doc_id";
    final static String COMPLEX_ID                   = "complex_id";
    final static String COMPLEX_NAME                 = "complex_name";
    final static String COMPLEX_ORGANISM             = "species";
    final static String COMPLEX_ORGANISM_F           = "species_f";
    final static String COMPLEX_ORGANISM_EXACT       = "species_e";
    final static String COMPLEX_ORGANISM_SORT        = "species_s";
    final static String COMPLEX_ALIAS                = "complex_alias";
    final static String COMPLEX_TYPE                 = "complex_type";
    final static String COMPLEX_TYPE_F               = "complex_type_f";
    final static String COMPLEX_TYPE_EXACT           = "complex_type_e";
    final static String INTERACTION_TYPE             = "type";
    final static String INTERACTION_TYPE_F           = "type_f";
    final static String INTERACTION_TYPE_EXACT       = "type_e";
    final static String COMPLEX_XREF                 = "complex_xref";
    final static String COMPLEX_XREF_EXACT           = "complex_xref_e";
    final static String COMPLEX_AC                   = "complex_ac";
    final static String AC                           = "ac";
    final static String COMPLEX_VERSION              = "complex_version";
    final static String DESCRIPTION                  = "description";
    final static String ORGANISM_NAME                = "organism_name";
    final static String UDATE                        = "udate";

    final static String INTERACTOR_ID                = "id";
    final static String INTERACTOR_ALIAS             = "alias";
    final static String INTERACTOR_TYPE              = "ptype";
    final static String INTERACTOR_TYPE_F            = "ptype_f";
    final static String INTERACTOR_TYPE_EXACT        = "ptype_e";
    final static String INTERACTOR_XREF              = "pxref";
    final static String INTERACTOR_XREF_EXACT        = "pxref_e";
    final static String STC                          = "stc";
    final static String PARAM                        = "param";

    final static String BIOROLE                      = "pbiorole";
    final static String BIOROLE_F                    = "pbiorole_f";
    final static String BIOROLE_EXACT                = "pbiorole_e";
    final static String FEATURES                     = "ftype";
    final static String FEATURES_F                   = "ftype_f";
    final static String FEATURES_EXACT               = "ftype_e";
    final static String SOURCE                       = "source";
    final static String SOURCE_F                     = "source_f";

    final static String NUMBER_PARTICIPANTS          = "number_participants";
    //final static String PATHWAY_XREF               = "pathway_xref";
    //final static String ECO_XREF                   = "eco_xref";
    final static String PUBLICATION_ID               = "pubid";
    final static String TEXT                         = "text";
    final static String SERIALISED_INTERACTION       = "serialised_interactor";

}
