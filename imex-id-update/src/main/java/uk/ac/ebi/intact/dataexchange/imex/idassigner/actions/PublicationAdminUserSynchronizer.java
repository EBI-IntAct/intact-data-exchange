package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

/**
 * Interface for synchronizing admin user of a publication with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationAdminUserSynchronizer {

    /**
     * Synchronize the admin user of a publication and update IMEx central if necessary. It can only update publication having having a valid pubmed identifier, doi number, jint identifier or IMEx identifier.
     * If the user is not registered in IMEx central, a user 'phantom' will be added to the IMEx record.
     * @param intactPublication
     * @param imexPublication
     * @throws psidev.psi.mi.jami.bridges.exception.BridgeFailedException : if user phantom does not exist in IMEx central (needs to be created) or IMEx central is not responding
     * or the publication identifier is not recognized
     */
    public void synchronizePublicationAdminUser(IntactPublication intactPublication, ImexPublication imexPublication) throws BridgeFailedException;

    public ImexCentralClient getImexCentralClient();
}
