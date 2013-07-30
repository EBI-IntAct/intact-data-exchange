package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.solr.client.solrj.SolrServer;
import org.biopax.paxtools.impl.level3.InteractionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Complex Solr Server that wraps a solrServer
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 29/07/13
 */
public class ComplexSolrServer {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private final Logger logger = LoggerFactory.getLogger(ComplexSolrServer.class);
    protected SolrServer solrServer;

    private InteractionImpl
}
