package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;

/**
 * Interface for synchronizing admin group of a publication in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationAdminGroupSynchronizer {

    /**
     * Update IMEx central and synchronize the publication ADMIN group. It can only be applied on publications having a valid pubmed identifier, doi number, jint identifier or IMEx identifier.
     * It will add INTACT admin group to the record in IMEx central if not already there and it will add the institution if different
     * from INTACT. However, some institutions in IntAct are not in IMEx central and in this case it will not add a new ADMIN group and only keep INTACT
     * admin group
     * @param intactPublication
     * @param imexPublication
     * @throws ImexCentralException if INTACT is not a valid ADMIn group and if IMEx central is not available or the publication id is not recognized
     */
    public void synchronizePublicationAdminGroup(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException;

    public ImexCentralClient getImexCentralClient();

    public void setImexCentralClient(ImexCentralClient imexClient);
}
