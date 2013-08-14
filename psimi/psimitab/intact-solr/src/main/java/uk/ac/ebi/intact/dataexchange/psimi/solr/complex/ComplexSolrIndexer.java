package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrLogger;

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
    private HttpSolrServer solrServer;
    private int commitInterval = 50000;
    private int numberOfTries = 5;

    /************************************************/
    /*                 Constructor                  */
    /************************************************/
    public ComplexSolrIndexer(HttpSolrServer solrServer_) throws IntactSolrException {
        if (solrServer_ == null){
            throw new IllegalArgumentException("You must pass a HttpSolrServer to the constructor");
        }
        // Receive a HttpSolrServer such as parameter
        this.solrServer = solrServer_;
        // Activate the Log
        SolrLogger.readFromLog4j();
    }


}