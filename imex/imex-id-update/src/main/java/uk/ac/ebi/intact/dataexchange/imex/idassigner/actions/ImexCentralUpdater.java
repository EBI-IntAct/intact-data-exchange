package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;

/**
 * All classes that will update and synchronize publications from IntAct in imexcentral should extend this abstract class
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public abstract class ImexCentralUpdater {

    protected ImexCentralClient imexCentral;
    public static String UNASSIGNED_PREFIX = "unassigned";

    public void ImexCentralUpdater(){
    }
    
    protected String extractPubIdFromImexPublication(Publication publication){
        if (publication.getImexAccession() != null){
            return publication.getImexAccession();
        }
        else if (publication.getIdentifier() != null && !publication.getIdentifier().isEmpty()) {
            return publication.getIdentifier().iterator().next().getAc();
        }
        else {
            return null;
        }
    }

    protected String extractPubIdFromIntactPublication(uk.ac.ebi.intact.model.Publication publication){
        return publication.getPublicationId() != null ? publication.getPublicationId() : publication.getShortLabel();        
    }
    
    protected String extractIdentifierFromPublication(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException {
        String pubId = extractPubIdFromIntactPublication(intactPublication);

        if (pubId == null || pubId.startsWith(UNASSIGNED_PREFIX)){
            pubId = extractPubIdFromImexPublication(imexPublication);
            if (pubId == null) {
                throw new PublicationImexUpdaterException("Impossible to update the status of this publication " + intactPublication.getShortLabel() + " because the identifiers are not recognized in IMEx central.");
            }
        }

        return pubId;
    }

    public ImexCentralClient getImexCentral() {
        return imexCentral;
    }

    public void setImexCentral(ImexCentralClient imexCentral) {
        this.imexCentral = imexCentral;
    }
}
