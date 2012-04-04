package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;

/**
 * Interface for synchronizing publication identifiers with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationIdentifierSynchronizer {

    /**
     * It will look at all identifiers attached to the record in IMEx central and see if the publication identifier in IntAct is also attached to the
     * IMEx record
     * @param intactPubId
     * @param imexPublication
     * @return true if the intact publication identifier is in IMEx central, false otherwise
     */
    public boolean isIntactPublicationIdentifierInSyncWithImexCentral(String intactPubId, Publication imexPublication);

    /**
     * Update the IMEx record in case intact publication has a valid pubmed or doi identifier that is not in IMEx central.
     * It will not update the intact publication or the IMEx record if the pubmed or doi identifier is different in IMEx central.
     * @param intactPublication
     * @param imexPublication
     * @throws PublicationImexUpdaterException : if the pubmed/doi identifier in IntAct is different from the one in IMEx central
     * @throws ImexCentralException : if record not found, idnetifier s not recognized or IMEx central is not responding
     */
    public void synchronizePublicationIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException;

}
