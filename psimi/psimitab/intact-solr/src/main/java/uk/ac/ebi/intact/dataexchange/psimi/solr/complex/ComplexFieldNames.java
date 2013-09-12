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
    String COMPLEX_ORGANISM_ONTOLOGY    = "species_ontology";
    String COMPLEX_ALIAS                = "complex_alias";
    String COMPLEX_TYPE                 = "complex_type";
    String COMPLEX_TYPE_F               = "complex_type_f";
    String COMPLEX_TYPE_ONTOLOGY        = "complex_type_ontology";
    String INTERACTION_TYPE             = "type";
    String INTERACTION_TYPE_F           = "type_f";
    String INTERACTION_TYPE_ONTOLOGY    = "type_ontology";
    String COMPLEX_XREF                 = "complex_xref";
    String COMPLEX_XREF_ONTOLOGY        = "complex_xref_ontology";
    String COMPLEX_AC                   = "complex_AC";
    String DESCRIPTION                  = "description";
    String ORGANISM_NAME                = "organism_name";

    String INTERACTOR_ID                = "id";
    String UDATE                        = "udate";
    String INTERACTOR_ALIAS             = "alias";
    String INTERACTOR_ALIAS_F           = "alias_f";
    String INTERACTOR_TYPE              = "ptype";
    String INTERACTOR_TYPE_ONTOLOGY     = "ptype_ontology";
    String INTERACTOR_XREF              = "pxref";
    String INTERACTOR_XREF_ONTOLOGY     = "pxref_f";
    String STC                          = "stc";
    String STC_F                        = "stc_f";
    String PARAM                        = "param";
    String PARAM_F                      = "param_f";

    String BIOROLE                      = "pbiorole";
    String BIOROLE_F                    = "pbiorole_f";
    String BIOROLE_ONTOLOGY             = "pbiorole_ontology";
    String FEATURES                     = "ftype";
    String FEATURES_F                   = "ftype_f";
    String FEATURES_ONTOLOGY            = "ftype_ontology";
    String SOURCE                       = "source";
    String SOURCE_F                     = "source_f";
    String SOURCE_ONTOLOGY              = "source_ontology";

    String NUMBER_PARTICIPANTS          = "number_participants";
    //String PATHWAY_XREF               = "pathway_xref";
    //String ECO_XREF                   = "eco_xref";
    String PUBLICATION_ID               = "pubid";

}
