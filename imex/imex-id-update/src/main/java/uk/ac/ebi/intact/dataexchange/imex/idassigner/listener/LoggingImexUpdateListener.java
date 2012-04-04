package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;

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
    public void onImexError( ImexErrorEvent evt ) throws ProcessorException{
        log.info( "IMEx Error " + evt.getErrorType() + ": " + evt.getErrorMessage() );
    }

    @Override
    public void onIntactUpdate( IntactUpdateEvent evt ) throws ProcessorException{
        log.info( "Intact update " + evt.getPublicationId() + ": "+evt.getUpdatedExp().size()+" experiments updated, " + evt.getUpdatedInteraction().size() + " interactions updated." );
    }

    @Override
    public void onNewImexAssigned( NewAssignedImexEvent evt ) throws ProcessorException{
        log.info( "New Imex id assigned : publication " + evt.getPublicationId() + ", imex = " + evt.getImexId() + ", interaction " + evt.getInteractionAc() + ", interaction imex = " + evt.getInteractionImexId() );
    }
}
