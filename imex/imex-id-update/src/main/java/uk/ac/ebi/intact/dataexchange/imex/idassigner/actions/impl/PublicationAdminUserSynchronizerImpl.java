package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.Operation;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationAdminUserSynchronizer;

/**
 * This class is for synchronizing admin users to publications registered in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class PublicationAdminUserSynchronizerImpl extends ImexCentralUpdater implements PublicationAdminUserSynchronizer {

    private static final Log log = LogFactory.getLog(PublicationAdminUserSynchronizerImpl.class);

    private static String PHANTOM_CURATOR = "phantom";
    private static int UNKNOWN_USER = 10;

    public void synchronizePublicationAdminUser(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException {
        String curator = intactPublication.getCurrentOwner().getLogin().toLowerCase();

        Publication.AdminUserList adminUserList = imexPublication.getAdminUserList();

        if (curator != null && !containsAdminUser(adminUserList, curator)){
            String pubId = extractPubIdFromIntactPublication(intactPublication);

            try {
                imexCentral.updatePublicationAdminUser( pubId, Operation.ADD, curator );
                log.info("Updated publication admin user to: " + curator);

            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == UNKNOWN_USER && !containsAdminUser(adminUserList, PHANTOM_CURATOR)) {
                    // unknown user, we automaticaly re-assign this record to user 'phantom'
                    imexCentral.updatePublicationAdminUser( pubId, Operation.ADD, PHANTOM_CURATOR );
                    log.info("Updated publication admin user to phantom user ");
                }
                else {
                    throw e;
                }
            }
        }
    }

    private boolean containsAdminUser(Publication.AdminUserList adminUserList, String user){

        if (adminUserList != null){
            if (adminUserList.getUser() != null && adminUserList.getUser().contains(user)){
                return true;
            }
        }

        return false;
    }
}
