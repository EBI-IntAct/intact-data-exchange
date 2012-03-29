package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import uk.ac.ebi.intact.bridges.imexcentral.DefaultImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.*;

import javax.swing.event.EventListenerList;

/**
 * The IMEx central manager helps to deal with synchronizing publications in IMEx
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public class ImexCentralManager {

    /**
     * List of listeners
     */
    private EventListenerList listenerList = new EventListenerList();

    private ImexCentralClient imexCentral;

    private ImexCentralPublicationRegister imexCentralRegister;
    private PublicationAdminGroupSynchronizer imexAdminGroupSynchronizer;
    private PublicationAdminUserSynchronizer imexAdminUserSynchronizer;
    private PublicationStatusSynchronizer imexStatusSynchronizer;
    private IntactImexAssigner intactImexAssigner;

    public ImexCentralManager( String icUsername, String icPassword, String icEndpoint ) throws ImexCentralException {
        this.imexCentral = new DefaultImexCentralClient( icUsername, icPassword, icEndpoint );
    }

    public ImexCentralManager( ImexCentralClient client ) {
        this.imexCentral = client;
    }

    public ImexCentralPublicationRegister getImexCentralRegister() {
        return imexCentralRegister;
    }

    public void setImexCentralRegister(ImexCentralPublicationRegister imexCentralRegister) {
        this.imexCentralRegister = imexCentralRegister;
    }

    public PublicationAdminGroupSynchronizer getImexAdminGroupSynchronizer() {
        return imexAdminGroupSynchronizer;
    }

    public void setImexAdminGroupSynchronizer(PublicationAdminGroupSynchronizer imexAdminGroupSynchronizer) {
        this.imexAdminGroupSynchronizer = imexAdminGroupSynchronizer;
    }

    public PublicationAdminUserSynchronizer getImexAdminUserSynchronizer() {
        return imexAdminUserSynchronizer;
    }

    public void setImexAdminUserSynchronizer(PublicationAdminUserSynchronizer imexAdminUserSynchronizer) {
        this.imexAdminUserSynchronizer = imexAdminUserSynchronizer;
    }

    public PublicationStatusSynchronizer getImexStatusSynchronizer() {
        return imexStatusSynchronizer;
    }

    public void setImexStatusSynchronizer(PublicationStatusSynchronizer imexStatusSynchronizer) {
        this.imexStatusSynchronizer = imexStatusSynchronizer;
    }

    public IntactImexAssigner getIntactImexAssigner() {
        return intactImexAssigner;
    }

    public void setIntactImexAssigner(IntactImexAssigner intactImexAssigner) {
        this.intactImexAssigner = intactImexAssigner;
    }
}
