package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;

/**
 * Empty implementation to allow writing less code when only implementing part of it.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class AbstractImexUpdateListener implements ImexUpdateListener {

    public void onImexError( ImexErrorEvent evt ) throws ProcessorException { }

    public void onIntactUpdate( IntactUpdateEvent evt ) throws ProcessorException{}

    public void onNewImexAssigned( NewAssignedImexEvent evt ) throws ProcessorException{}
}
