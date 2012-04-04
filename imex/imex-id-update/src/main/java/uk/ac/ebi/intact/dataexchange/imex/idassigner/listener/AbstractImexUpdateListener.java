package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;

/**
 * Empty implementation to allow writing less code when only implementing part of it.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class AbstractImexUpdateListener implements ImexUpdateListener {

    public void onProcessPublication( ImexUpdateEvent evt ) throws ProcessorException { }

    public void onProcessImexPublication( ImexUpdateEvent evt ) throws ProcessorException { }

    public void onPublicationUpToDate( ImexUpdateEvent evt ) throws ProcessorException { }

    public void onImexIdAssignedToPublication( ImexUpdateEvent evt ) throws ProcessorException { }

    public void onImexIdAssignedToInteraction( ImexUpdateEvent evt )  throws ProcessorException { }

    public void onImexIdMismatchFound( ImexUpdateEvent evt ) throws ProcessorException { }

    public void onImexError( ImexErrorEvent evt ) throws ProcessorException { }
}
