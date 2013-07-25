package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public interface ComplexFieldName {

    String ID                   = "id";
    String COMPLEX_ID           = "complex_id";
    String COMPLEX_NAME         = "complex_name";
    String COMPLEX_ALIAS        = "complex_alias";
    String COMPLEX_TYPE         = "complex_type";
    String COMPLEX_XREF         = "complex_xref";
    String COMPLEX_AC           = "complex_AC";
    String DESCRIPTION          = "description";

    String INTERACTOR_ID        = "interactor_id";
    String INTERACTOR_ALIAS     = "interactor_alias";
    String INTERACTOR_TYPE      = "interactor_type";

    String BIOROLE              = "biorole";
    String FEATURES             = "features";
    String SOURCE               = "source";

    String NUMBER_PARTICIPANTS  = "number_participants";
    String PATHWAY_XREF         = "pathway_xref";
    String ECO_XREF             = "eco_xref";
    String PUBLICATION_ID       = "publication_id";

}
