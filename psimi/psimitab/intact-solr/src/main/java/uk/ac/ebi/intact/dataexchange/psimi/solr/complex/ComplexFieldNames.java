package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public interface ComplexFieldNames {

    String ID                           = "id";
    String COMPLEX_ID                   = "complex_id";
    String COMPLEX_NAME                 = "complex_name";
    String COMPLEX_ORGANISM             = "complex_organism";
    String COMPLEX_ORGANISM_F           = "complex_organism_f";
    String COMPLEX_ORGANISM_ONTOLOGY    = "complex_organism_ontology";
    String COMPLEX_ALIAS                = "complex_alias";
    String COMPLEX_TYPE                 = "complex_type";
    String COMPLEX_TYPE_F               = "complex_type_f";
    String COMPLEX_TYPE_ONTOLOGY        = "complex_type_ontology";
    String INTERACTION_TYPE             = "interaction_type";
    String INTERACTION_TYPE_F           = "interaction_type_f";
    String INTERACTION_TYPE_ONTOLOGY    = "interaction_type_ontology";
    String COMPLEX_XREF                 = "complex_xref";
    String COMPLEX_XREF_ONTOLOGY        = "complex_xref_ontology";
    String COMPLEX_AC                   = "complex_AC";
    String DESCRIPTION                  = "description";
    String ORGANISM_NAME                = "organism_name";

    String INTERACTOR_ID                = "interactor_id";
    String INTERACTOR_ALIAS             = "interactor_alias";
    String INTERACTOR_ALIAS_F           = "interactor_alias_f";
    String INTERACTOR_TYPE              = "interactor_type";
    String INTERACTOR_TYPE_ONTOLOGY     = "interactor_type_ontology";

    String BIOROLE                      = "biorole";
    String BIOROLE_F                    = "biorole_f";
    String BIOROLE_ONTOLOGY             = "biorole_ontology";
    String FEATURES                     = "features";
    String FEATURES_F                   = "features_f";
    String FEATURES_ONTOLOGY            = "features_ontology";
    String SOURCE                       = "source";
    String SOURCE_F                     = "source_f";
    String SOURCE_ONTOLOGY              = "source_ontology";

    String NUMBER_PARTICIPANTS          = "number_participants";
    //String PATHWAY_XREF               = "pathway_xref";
    //String ECO_XREF                   = "eco_xref";
    String PUBLICATION_ID               = "publication_id";

}
