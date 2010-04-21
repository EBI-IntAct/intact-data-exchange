package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Outputs event using a simple logger.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class LoggingImexUpdateListener extends AbstractImexUpdateListener {

    private static final Log log = LogFactory.getLog( LoggingImexUpdateListener.class );

    @Override
    public void onProcessPublication( ImexUpdateEvent evt ) {
        log.info( "Process" );
    }

    @Override
    public void onProcessImexPublication( ImexUpdateEvent evt ) {
        log.info( "onProcessImexPublication" );
    }

    @Override
    public void onPublicationUpToDate( ImexUpdateEvent evt ) {
        log.info( "onPublicationUpToDate" );
    }

    @Override
    public void onImexIdAssignedToPublication( ImexUpdateEvent iue ) {
        log.info( "NewImexIdAssigned" );
    }

    @Override
    public void onImexIdAssignedToInteraction( ImexUpdateEvent evt ) {
        log.info( "onImexIdAssignedToInteraction" );
    }

    @Override
    public void onImexIdMismatchFound( ImexUpdateEvent iue ) {
        log.info( "IMEx id mismatch" );
    }
}
