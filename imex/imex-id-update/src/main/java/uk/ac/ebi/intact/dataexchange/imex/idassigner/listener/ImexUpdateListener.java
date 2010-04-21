package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import java.util.EventListener;

/**
 * Events that can be throws in an IMEx update.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public interface ImexUpdateListener extends EventListener {

    void onProcessPublication( ImexUpdateEvent evt ) throws ProcessorException;
    
    void onProcessImexPublication( ImexUpdateEvent evt ) throws ProcessorException;

    void onPublicationUpToDate( ImexUpdateEvent evt ) throws ProcessorException;

    void onImexIdAssignedToPublication( ImexUpdateEvent evt ) throws ProcessorException;

    void onImexIdAssignedToInteraction( ImexUpdateEvent evt ) throws ProcessorException;

    void onImexIdMismatchFound( ImexUpdateEvent evt ) throws ProcessorException;
}
