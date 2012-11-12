package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.DefaultImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.Operation;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationAdminGroupSynchronizer;

/**
 * This class is for synchronizing the admin group of a publication in imex central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class PublicationAdminGroupSynchronizerImpl extends ImexCentralUpdater implements PublicationAdminGroupSynchronizer{

    private static final Log log = LogFactory.getLog(PublicationAdminGroupSynchronizerImpl.class);

    private static String INTACT_ADMIN = "INTACT";

    public void synchronizePublicationAdminGroup(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException {

        Publication.AdminGroupList adminGroupList = imexPublication.getAdminGroupList();
        String pubId = extractPubIdFromIntactPublication(intactPublication);

        if (!containsAdminGroup(adminGroupList, INTACT_ADMIN)){
            // add first INTACT admin
            imexPublication = imexCentral.updatePublicationAdminGroup(pubId, Operation.ADD, INTACT_ADMIN);
            log.info("Updated publication admin group to: " + INTACT_ADMIN);
        }

        // add other database admin group if it exists
        final String institution = intactPublication.getOwner().getShortLabel().toUpperCase();

        if (!INTACT_ADMIN.equals(institution) && !containsAdminGroup(adminGroupList, institution)){
            try {
                imexPublication = imexCentral.updatePublicationAdminGroup( intactPublication.getPublicationId(), Operation.ADD, institution );
                log.info("Added other publication admin group : " + institution);
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_GROUP ) {
                    // unknown admin group, we cannot add another admin group for this institution
                    log.warn("The institution " + institution + " is not recognized in IMEx central so is ignored.");
                }
                else {
                    throw e;
                }
            }
        }
    }

    private boolean containsAdminGroup(Publication.AdminGroupList adminGroupList, String group){

        if (adminGroupList != null){
            if (adminGroupList.getGroup() != null && adminGroupList.getGroup().contains(group)){
                return true;
            }
        }

        return false;
    }
}
