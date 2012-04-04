package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;

import java.util.EventListener;

/**
 * Events that can be throws in an IMEx update.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public interface ImexUpdateListener extends EventListener {

    public void onProcessPublication( ImexUpdateEvent evt ) throws ProcessorException;
    
    public void onProcessImexPublication( ImexUpdateEvent evt ) throws ProcessorException;

    public void onPublicationUpToDate( ImexUpdateEvent evt ) throws ProcessorException;

    public void onImexIdAssignedToPublication( ImexUpdateEvent evt ) throws ProcessorException;

    public void onImexIdAssignedToInteraction( ImexUpdateEvent evt ) throws ProcessorException;

    public void onImexIdMismatchFound( ImexUpdateEvent evt ) throws ProcessorException;

    public void onImexError( ImexErrorEvent evt ) throws ProcessorException;
}
