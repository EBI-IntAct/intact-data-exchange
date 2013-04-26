package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;

import java.util.EventObject;

/**
 * Event for the IMEx update.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ImexUpdateEvent extends EventObject {

    private Publication publication;
    private Interaction interaction;
    private String message;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ImexUpdateEvent( Object source ) {
        super( source );
    }

    public ImexUpdateEvent( Object source, Publication publication ) {
        this( source );
        this.publication = publication;
    }

    public ImexUpdateEvent( Object source, Publication publication, String message ) {
        this( source, publication );
        this.message = message;
    }

    public ImexUpdateEvent( Object source, Publication publication, Interaction interaction, String message ) {
        this( source, publication );
        this.interaction = interaction;
        this.message = message;
    }

    public Publication getPublication() {
        return publication;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public String getMessage() {
        return message;
    }
}
