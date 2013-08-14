package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
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

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexSolrConverter ( SolrServer solrServer ) {
        complexSolrEnricher = new ComplexSolrEnricher(new OntologySearcher ( solrServer ) ) ;
    }

    /*****************************/
    /*      Convert Methods      */
    /*****************************/
    public SolrDocument convertComplexToSolrDocument (
            InteractionImpl complex,
            SolrDocument solrDocument )
            throws  Exception {
        // add info to id field:
        //solrDocument.addField ( ComplexFieldNames.ID, ) ;

        // add info to complex_id field: db_id, db and id
        solrDocument.addField ( ComplexFieldNames.COMPLEX_ID, complex.getCrc ( ) ) ;

        // add info to

        // Enrich the Solr Document and return that
        return complexSolrEnricher.enrich ( complex, solrDocument ) ;
    }

    public SolrDocument convertComplexToSolrDocument ( InteractionImpl complex ) throws Exception {
        return convertComplexToSolrDocument ( complex, new SolrDocument ( ) ) ;
    }


}
