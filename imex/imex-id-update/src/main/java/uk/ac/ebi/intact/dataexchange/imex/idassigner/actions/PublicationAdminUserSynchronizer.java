package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;

/**
 * Interface for synchronizing admin user of a publication with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationAdminUserSynchronizer {

    /**
     * Synchronize the admin user of a publication and update IMEx central if necessary. It can only update publication having valid pubmed identifiers.
     * If the user is not registered in IMEx central, a user 'phantom' will be added to the IMEx record.
     * @param intactPublication
     * @param imexPublication
     * @throws ImexCentralException : if user phantom does not exist in IMEx central (needs to be created) or IMEx central is not responding
     * or the publication identifier is not recognized
     */
    public void synchronizePublicationAdminUser(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException;

    public ImexCentralClient getImexCentralClient();

    public void setImexCentralClient(ImexCentralClient imexClient);
}
