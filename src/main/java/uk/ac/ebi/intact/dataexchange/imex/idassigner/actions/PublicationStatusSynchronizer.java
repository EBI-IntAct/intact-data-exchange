package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.PublicationStatus;

/**
 * interface for synchronizing publication status with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface PublicationStatusSynchronizer {

    /**
     * Synchronize publication status with IMEx central and update the IMEx central record if necessary.
     * @param intactPublication
     * @param imexPublication
     * @throws ImexCentralException is status not recognized or no records could be found or IMEx central is not responding
     */
    public void synchronizePublicationStatusWithImexCentral(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException;

    /**
     *
     * @param publication
     * @return the imex central publication status that is matching the intact status of the publication
     */
    public PublicationStatus getPublicationStatus( uk.ac.ebi.intact.model.Publication publication );

    public ImexCentralClient getImexCentralClient();

    public void setImexCentralClient(ImexCentralClient imexClient);
}
