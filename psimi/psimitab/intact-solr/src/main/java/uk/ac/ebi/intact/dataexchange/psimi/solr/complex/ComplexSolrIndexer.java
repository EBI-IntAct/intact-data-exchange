package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ExecutionContext;
import uk.ac.ebi.intact.model.InteractionImpl;

import java.util.List;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public class ComplexSolrIndexer {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog( ComplexSolrIndexer.class );
    private HttpSolrServer complexSolrServer = null ;
    private ComplexSolrConverter complexSolrConverter = null ;
    private ComplexSolrWriter complexSolrWriter = null ;
    private int commitInterval = 50000;
    private int numberOfTries = 5;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrIndexer ( HttpSolrServer httpSolrServer ) {
        this.complexSolrServer = httpSolrServer ;
        // create converter (and enricher)
        this.complexSolrConverter = new ComplexSolrConverter ( this.complexSolrServer ) ;
        // create writer
        this.complexSolrWriter = new ComplexSolrWriter ( ) ;
        this.complexSolrWriter.setSolrServer ( this.complexSolrServer ) ;
    }

    public ComplexSolrIndexer ( HttpSolrServer httpSolrServer, ExecutionContext executionContext) {
        this ( httpSolrServer ) ;
        this.complexSolrWriter.open ( executionContext ) ;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setExecutionContext ( ExecutionContext executionContext ) {
        this.complexSolrWriter.open ( executionContext ) ;
    }

    /***************************/
    /*      Index Methods      */
    /***************************/
    public void index ( InteractionImpl complex ) throws Exception {
        // convert complex to a SolrInputDocument
        SolrInputDocument solrInputDocument = this.complexSolrConverter.convertComplexToSolrDocument ( complex ) ;
        // then add this document to the SolrServer
        this.complexSolrWriter.write( solrInputDocument ) ;
        this.complexSolrServer.commit ( ) ;
        // if you called this method directly you need to do an optimize manually
    }
    public void index ( InteractionImpl complex, ExecutionContext executionContext ) throws Exception {
        this.complexSolrWriter.open ( executionContext ) ;
        index ( complex ) ;
        this.complexSolrServer.optimize ( ) ;
    }

    public void index ( List < InteractionImpl > complexes ) throws Exception {
        for ( InteractionImpl complex : complexes ) {
            index(complex);
        }
        this.complexSolrServer.optimize ( ) ;
    }

    public void index ( List < InteractionImpl > complexes, ExecutionContext executionContext ) throws Exception {
        this.complexSolrWriter.open ( executionContext ) ;
        index ( complexes ) ;
    }

}