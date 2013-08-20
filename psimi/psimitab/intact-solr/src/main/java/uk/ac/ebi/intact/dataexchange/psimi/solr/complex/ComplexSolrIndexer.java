package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ExecutionContext;
import org.xml.sax.SAXException;
import uk.ac.ebi.intact.model.InteractionImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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

    private ComplexSolrWriter complexSolrWriter = null ;
    private int commitInterval = 50000;
    private int numberOfTries = 5;

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrIndexer ( String solrServerURL ) {
        this.complexSolrWriter = new ComplexSolrWriter ( ) ;
        this.complexSolrWriter.setSolrUrl ( solrServerURL ) ;
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
    public void index ( List < InteractionImpl > complexes ) throws Exception {
        this.complexSolrWriter.write(complexes) ;
    }

    public void index ( List < InteractionImpl > complexes, ExecutionContext executionContext ) throws Exception {
        this.complexSolrWriter.open ( executionContext ) ;
        index ( complexes ) ;
    }

}