package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.Operation;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationAdminGroupSynchronizer;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

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
    private static int UNKNOWN_GROUP = 11;

    public void synchronizePublicationAdminGroup(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException {

        Publication.AdminGroupList adminGroupList = imexPublication.getAdminGroupList();
        String pubId = extractIdentifierFromPublication(intactPublication, imexPublication);

        if (!containsAdminGroup(adminGroupList, INTACT_ADMIN)){
            // add first INTACT admin
            try {
                imexCentral.updatePublicationAdminGroup(pubId, Operation.ADD, INTACT_ADMIN);
                log.info("Updated publication admin group to: " + INTACT_ADMIN);
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == UNKNOWN_GROUP ) {
                    throw new PublicationImexUpdaterException("The institution INTACT is not recognized in IMEx central so is ignored and needs to be registered in IMEx central.", e);
                }
                else {
                    throw new PublicationImexUpdaterException("Cannot add INTACT admin group to publication " + intactPublication.getShortLabel(), e);
                }
            }
        }

        // add other database admin group if it exists
        final String institution = intactPublication.getOwner().getShortLabel().toUpperCase();

        if (!INTACT_ADMIN.equals(institution) && !containsAdminGroup(adminGroupList, institution)){
            try {
                imexCentral.updatePublicationAdminGroup( intactPublication.getPublicationId(), Operation.ADD, institution );
                log.info("Added other publication admin group : " + institution);
            } catch ( ImexCentralException e ) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == UNKNOWN_GROUP ) {
                    // unknown admin group, we cannot add another admin group for this institution
                    log.warn("The institution " + institution + " is not recognized in IMEx central so is ignored.");
                }
                else {
                    throw new PublicationImexUpdaterException("Cannot add "+institution+" admin group to publication " + intactPublication.getShortLabel(), e);
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
