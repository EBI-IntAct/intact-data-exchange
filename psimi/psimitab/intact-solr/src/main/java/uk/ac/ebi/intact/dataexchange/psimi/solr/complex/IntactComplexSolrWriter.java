package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.http.protocol.ExecutionContext;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
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
public class IntactComplexSolrWriter extends SolrItemWriter{
    /********************************/
    /*      Private attributes      */
    /********************************/
    private String complexSorlUrl;
    private ComplexSolrServer complexSearcher;

    /**************************/
    /*      Constructors      */
    /**************************/
    public IntactComplexSolrWriter ( ) { super ( ) ; }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setComplexUrl( String complexSorlUrl_ ){
        this.complexSorlUrl = complexSorlUrl_;
    }

    /******************************/
    /*      Override methods      */
    /******************************/
    @Override
    public void open ( org.springframework.batch.item.ExecutionContext executionContext ) throws ItemStreamException {
        super.open ( executionContext ) ;

        // check if complexServer is null and create one if it is required
        if ( complexSorlUrl != null ) {
            HttpSolrServer complexesSolrServer = new HttpSolrServer(complexSorlUrl, createHttpClient());
            complexSearcher = new ComplexSolrServer(complexesSolrServer);
        }

        // TODO
        //this.solrConverter = new SolrDocumentConverter(solrServer, complexSearcher);
    }
}
