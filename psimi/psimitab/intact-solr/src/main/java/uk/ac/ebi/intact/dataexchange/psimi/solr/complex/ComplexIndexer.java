package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrLogger;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 23/07/13
 */
public class ComplexIndexer {

    private static final Log log = LogFactory.getLog( ComplexIndexer.class );

    private HttpSolrServer solrServer;

    private int commitInterval = 50000;
    private int numberOfTries = 5;

    /************************************************/
    /*                 Constructor                  */
    /************************************************/
    public ComplexIndexer(HttpSolrServer solrServer){
        // Receive a HttpSolrServer such as parameter
        this.solrServer = solrServer;
        // Activate the Log
        SolrLogger.readFromLog4j();
    }


}