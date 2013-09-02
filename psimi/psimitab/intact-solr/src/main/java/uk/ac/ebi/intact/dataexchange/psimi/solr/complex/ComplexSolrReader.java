package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import uk.ac.ebi.intact.model.InteractionImpl;

/**
 * Read complex using Hibernate and implements ItemReader
 * to use that in Spring Batch
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 29/08/13
 */
public class ComplexSolrReader implements ItemReader<InteractionImpl>, ItemStream {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private static final Log log = LogFactory.getLog(ComplexSolrReader.class) ;

    /*************************/
    /*      Constructor      */
    /*************************/


    /******************************/
    /*      Override Methods      */
    /******************************/
    @Override
    public void open ( ExecutionContext executionContext ) throws ItemStreamException {

    }

    @Override
    public void update ( ExecutionContext executionContext ) throws ItemStreamException {

    }

    @Override
    public void close ( ) throws ItemStreamException {

    }

    @Override
    public InteractionImpl read ( ) throws Exception {

        return null;
    }
}
