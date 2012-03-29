package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;

/**
 * Interface for synchronizing admin group of a publication in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationAdminGroupSynchronizer {

    /**
     * Update IMEx central and synchronize the publication ADMIN group
     * @param intactPublication
     * @param imexPublication
     * @throws PublicationImexUpdaterException
     */
    public void synchronizePublicationAdminGroup(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException;
}
