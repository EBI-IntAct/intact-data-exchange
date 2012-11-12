package uk.ac.ebi.intact.dataexchange.imex.idassigner.events;

import java.util.EventObject;

/**
 * Events for new IMEx id which have been assigned
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/04/12</pre>
 */

public class NewAssignedImexEvent extends EventObject {

    private String publicationId;
    private String imexId;
    private String interactionAc;
    private String interactionImexId;
    
    public NewAssignedImexEvent(Object o, String pubId, String imexId, String intAc, String intImex) {
        super(o);

        this.publicationId = pubId;
        this.imexId = imexId;
        this.interactionAc = intAc;
        this.interactionImexId = intImex;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getImexId() {
        return imexId;
    }

    public String getInteractionAc() {
        return interactionAc;
    }

    public String getInteractionImexId() {
        return interactionImexId;
    }
}
