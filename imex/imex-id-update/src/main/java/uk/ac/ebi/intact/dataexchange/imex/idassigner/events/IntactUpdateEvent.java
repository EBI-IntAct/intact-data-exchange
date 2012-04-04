package uk.ac.ebi.intact.dataexchange.imex.idassigner.events;

import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

import java.util.EventObject;
import java.util.List;

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
    private List<Experiment> updatedExp;
    private List<Interaction> updatedInteraction;
    
    public IntactUpdateEvent(Object o, String pubId, String imexId, List<Experiment> updatedExp, List<Interaction> updatedInteraction) {
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

    public List<Experiment> getUpdatedExp() {
        return updatedExp;
    }

    public List<Interaction> getUpdatedInteraction() {
        return updatedInteraction;
    }
}
