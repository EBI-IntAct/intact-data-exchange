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

    /**
     * This group is for publication curated and owned by INTACT
     */
    private static String INTACT_ADMIN = "INTACT";
    /**
     * This group is for publications maintained and reviewed by INTACT but not owned by IntAct
     */
    private static String INTACT_ADMIN_CURATOR = "INTACT CURATORS";

    public void synchronizePublicationAdminGroup(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException {

        Publication.AdminGroupList adminGroupList = imexPublication.getAdminGroupList();
        String pubId = extractPubIdFromIntactPublication(intactPublication);

        // add other database admin group if it exists
        final String institution = intactPublication.getOwner().getShortLabel().toUpperCase();

        if (!containsAdminGroup(adminGroupList, institution)){
            try {
                imexPublication = imexCentral.updatePublicationAdminGroup( intactPublication.getPublicationId(), Operation.ADD, institution );
                log.info("Added other publication admin group : " + institution);

                // now add intact admin group curators for publications maintained by intact but not owned by INTACT
                if (!INTACT_ADMIN.equals(institution) && !containsAdminGroup(adminGroupList, INTACT_ADMIN_CURATOR)){
                    // add first INTACT admin curators
                    try {
                        imexPublication = imexCentral.updatePublicationAdminGroup(pubId, Operation.ADD, INTACT_ADMIN_CURATOR);
                        log.info("Updated publication admin group to: " + INTACT_ADMIN_CURATOR);
                    } catch ( ImexCentralException e ) {
                        IcentralFault f = (IcentralFault) e.getCause();
                        if( f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_GROUP ) {
                            // unknown intact admin group, we cannot add another admin group for this institution
                            log.warn("The intact curator admin group is not recognized in IMEx central, we cannot tag the publication as maintained and review by INTACT.");
                        }
                        else {
                            throw e;
                        }
                    }
                }
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_GROUP ) {
                    // unknown admin group, we cannot add another admin group for this institution
                    log.warn("The institution " + institution + " is not recognized in IMEx central so we will add INTACT admin group.");
                    if (!containsAdminGroup(adminGroupList, INTACT_ADMIN)){
                        // add first INTACT admin
                        imexPublication = imexCentral.updatePublicationAdminGroup(pubId, Operation.ADD, INTACT_ADMIN);
                        log.info("Updated publication admin group to: " + INTACT_ADMIN);
                    }
                }
                else {
                    throw e;
                }
            }
        }
        // now add intact admin group curators for publications maintained by intact but not owned by INTACT
        else if (!INTACT_ADMIN.equals(institution) && !containsAdminGroup(adminGroupList, INTACT_ADMIN_CURATOR)){
            // add first INTACT admin curators
            try {
                imexPublication = imexCentral.updatePublicationAdminGroup(pubId, Operation.ADD, INTACT_ADMIN_CURATOR);
                log.info("Updated publication admin group to: " + INTACT_ADMIN_CURATOR);
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == DefaultImexCentralClient.UNKNOWN_GROUP ) {
                    // unknown intact admin group, we cannot add another admin group for this institution
                    log.warn("The intact curator admin group is not recognized in IMEx central, we cannot tag the publication as maintained and review by INTACT.");
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
