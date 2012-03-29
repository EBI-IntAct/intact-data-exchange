package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;

/**
 * Interface for synchronizing admin user of a publication with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationAdminUserSynchronizer {

    /**
     * Synchronize the admin user of a publication and update IMEx central if necessary
     * @param intactPublication
     * @param imexPublication
     * @throws PublicationImexUpdaterException
     */
    public void synchronizePublicationAdminUser(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException;
}
