package uk.ac.ebi.intact.dataexchange.imex.idassigner.events;

import java.util.EventObject;

/**
 * Event when having errors while processing a publication in Imex central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/04/12</pre>
 */

public class ImexErrorEvent extends EventObject {

    private ImexErrorType errorType;
    private String publicationId;
    private String imexId;
    private String experimentAc;
    private String interactionAc;
    private String errorMessage;
    
    public ImexErrorEvent(Object o, ImexErrorType errorType, String publicationId, String imexId, String expAc, String interactionAc, String errorMessage) {
        super(o);
        this.errorType = errorType;
        this.publicationId = publicationId;
        this.experimentAc = expAc;
        this.interactionAc = interactionAc;
        this.imexId = imexId;
        this.errorMessage = errorMessage;
    }

    public ImexErrorType getErrorType() {
        return errorType;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getImexId() {
        return imexId;
    }

    public String getExperimentAc() {
        return experimentAc;
    }

    public String getInteractionAc() {
        return interactionAc;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
