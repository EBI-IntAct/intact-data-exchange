package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.*;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 14/08/13
 */
public class ComplexSolrConverter {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private ComplexSolrEnricher complexSolrEnricher ;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrConverter ( OntologySearcher ontologySearcher ) {
        this.complexSolrEnricher = new ComplexSolrEnricher ( ontologySearcher ) ;
    }
    public ComplexSolrConverter ( SolrServer solrServer ) {
        this ( new OntologySearcher ( solrServer ) ) ;
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    public SolrInputDocument convertComplexToSolrDocument (
            InteractionImpl complex,
            SolrInputDocument solrDocument )
            throws  Exception {
        //////////////////////////
        ///   COMPLEX FIELDS   ///
        //////////////////////////

        // add info to id field:
        //solrDocument.addField ( ComplexFieldNames.ID,  ) ;
        // add info to complex_id field: db_id, db and id
        final String db ;
        final String id = complex.getCrc ( ) ;
        //solrDocument.addField ( ComplexFieldNames.COMPLEX_ID,  ) ;
        // add info to complex_name field:
        final String name = complex.getShortLabel ( ) ;
        solrDocument.addField ( ComplexFieldNames.ID, name ) ;
        // add info to complex_alias field: type_name, type and name
        final String type = complex.getCvInteractionType ( ) .getShortLabel() ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, new StringBuilder ( )
                                                        .append ( type ) .append ( "_" )
                                                        .append ( name ) .toString ( ) ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, type ) ;
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ALIAS, name ) ;
        // add info to complex_organism field:

        // add info to complex_organism_f field:

        // add info to complex_organism_ontology field:

        // add info to complex_type field:

        // add info to complex_type_f field:

        // add info to complex_type_ontology field:

        // add info to complex_xref field:

        // add info to complex_xref_ontology field:

        // add info to complex_AC field:
        solrDocument.addField ( ComplexFieldNames.COMPLEX_AC, complex.getCvInteractionTypeAc ( ) ) ;
        // add info to description field:

        // add info to organism_name field:


        /////////////////////////////
        ///   INTERACTOR FIELDS   ///
        /////////////////////////////

        // add info to interactor_id field:

        // add info to interactor_alias field:

        // add info to interactor_alias_f field:

        // add info to interactor_type field:

        // add info to interactor_type_ontology field:


        ///////////////////////////
        ///   PSICQUIC FIELDS   ///
        ///////////////////////////

        // add info to biorole field:

        // add info to biorole_f field:

        // add info to biorole_ontology field:

        // add info to features field:

        // add info to features_f field:

        // add info to features_ontology field:

        ////////////////////////
        ///   OTHER FIELDS   ///
        ////////////////////////

        // add info to source field:

        // add info to source_f field:

        // add info to source_ontology field:

        // add info to number_participants field:

        // add info to publication_id field:

        /////////////////////////
        ///   ENRICH FIELDS   ///
        /////////////////////////

        // Enrich the Solr Document and return that
        return complexSolrEnricher.enrich ( complex, solrDocument ) ;
    }

    public SolrInputDocument convertComplexToSolrDocument ( InteractionImpl complex ) throws Exception {
        return convertComplexToSolrDocument ( complex, new SolrInputDocument ( ) ) ;
    }


}
