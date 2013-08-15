package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.http.protocol.ExecutionContext;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.mi.psicquic.indexing.batch.writer.SolrItemWriter;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;

/**
 * Indexes information about complex into a Solr Server.
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 25/07/13
 */
public class ComplexSolrWriter extends SolrItemWriter{
    /********************************/
    /*      Private attributes      */
    /********************************/

    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSolrWriter ( ) { super ( ) ; }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setComplexUrl ( String complexSorlUrl ) {
        this.solrUrl = complexSorlUrl;
    }
    public void setSolrServer ( HttpSolrServer httpSolrServer_ ) {
        this.solrServer = httpSolrServer_ ;
        this.solrUrl = this.solrServer.getBaseURL ( ) ;
    }

    /******************************/
    /*      Override methods      */
    /******************************/
    @Override
    public void open ( org.springframework.batch.item.ExecutionContext executionContext ) throws ItemStreamException {
        super.open ( executionContext ) ;
    }

    public void write ( SolrInputDocument solrInputDocument ) throws Exception {
        if ( solrUrl == null ) { throw new IllegalStateException ( "No URL for SolrServer configured for ComplexSolrWriter" ) ; }
        if ( solrServer == null ) { throw new IllegalStateException ( "No HttpSolrServer configured for ComplexSolrWriter" ) ; }
        solrServer.add ( solrInputDocument ) ;
    }
}
