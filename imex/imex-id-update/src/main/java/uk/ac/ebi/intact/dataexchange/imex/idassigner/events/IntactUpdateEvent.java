package uk.ac.ebi.intact.dataexchange.imex.idassigner.events;

import java.util.EventObject;
import java.util.Set;

/**
 * Event for any update in Intact
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/04/12</pre>
 */

public class IntactUpdateEvent extends EventObject{

    private String publicationId;
    private String imexId;
    private Set<String> updatedExp;
    private Set<String> updatedInteraction;
    
    public IntactUpdateEvent(Object o, String pubId, String imexId, Set<String> updatedExp, Set<String> updatedInteraction) {
        super(o);
        this.publicationId = pubId;
        this.imexId = imexId;
        this.updatedExp = updatedExp;
        this.updatedInteraction = updatedInteraction;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getImexId() {
        return imexId;
    }

    public Set<String> getUpdatedExp() {
        return updatedExp;
    }

    public Set<String> getUpdatedInteraction() {
        return updatedInteraction;
    }
}
