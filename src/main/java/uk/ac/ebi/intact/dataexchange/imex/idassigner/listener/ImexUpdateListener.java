package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;

import java.util.EventListener;

/**
 * Events that can be throws in an IMEx update.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public interface ImexUpdateListener extends EventListener {

    public void onImexError( ImexErrorEvent evt ) throws ProcessorException;

    public void onIntactUpdate( IntactUpdateEvent evt ) throws ProcessorException;

    public void onNewImexAssigned( NewAssignedImexEvent evt ) throws ProcessorException;
}
