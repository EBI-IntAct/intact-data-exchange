package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.IcentralFault;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.PublicationStatus;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationStatusSynchronizer;
import uk.ac.ebi.intact.model.CvPublicationStatus;
import uk.ac.ebi.intact.model.CvPublicationStatusType;

/**
 * This class allows to convert intact publication status to publication status in imexcentral. It updates and synchronize the publications status
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class PublicationStatusSynchronizerImpl extends ImexCentralUpdater implements PublicationStatusSynchronizer{

    private static final Log log = LogFactory.getLog(PublicationStatusSynchronizerImpl.class);

    private static int UNKNOWN_STATUS = 8;

    public void synchronizePublicationStatusWithImexCentral(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException {
        String imexStatus = imexPublication.getStatus();

        PublicationStatus intactStatus = getPublicationStatus(intactPublication);

        if (imexStatus == null || (imexStatus != null && !intactStatus.toString().equalsIgnoreCase(imexStatus))){
            String pubId = extractIdentifierFromPublication(intactPublication, imexPublication);

            try {
                imexCentral.updatePublicationStatus( pubId, intactStatus, null );
                log.info("Updating imex status to " + intactStatus.toString());

            } catch (ImexCentralException e) {
                IcentralFault f = (IcentralFault) e.getCause();
                if( f.getFaultInfo().getFaultCode() == UNKNOWN_STATUS ) {
                    throw new PublicationImexUpdaterException("Impossible to update IMEx status of " + intactPublication.getShortLabel() + " because the status is not recognized as a valid status in IMEx central.", e);

                }
                else {
                    throw new PublicationImexUpdaterException("Impossible to update IMEx status of " + intactPublication.getShortLabel(), e);
                }
            }
        }
    }

    public PublicationStatus getPublicationStatus( uk.ac.ebi.intact.model.Publication publication ) {
        // IMEx central has currently the following publication states available:
        // NEW / RESERVED / INPROGRESS / RELEASED / DISCARDED / INCOMPLETE / PROCESSED

        PublicationStatus status;

        CvPublicationStatus intactStatus = publication.getStatus();

        if (intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.NEW.identifier())){
            return PublicationStatus.NEW;
        }
        else if (intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.RESERVED.identifier())
                || intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.ASSIGNED.identifier())){
            return PublicationStatus.RESERVED;
        }
        else if (intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.DISCARDED.identifier())){
            return PublicationStatus.DISCARDED;
        }
        else if (intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.READY_FOR_RELEASE.identifier())){
            return PublicationStatus.PROCESSED;
        }
        else if (intactStatus.getIdentifier().equalsIgnoreCase(CvPublicationStatusType.RELEASED.identifier())){
            return PublicationStatus.RELEASED;
        }
        else {
            return PublicationStatus.INPROGRESS;
        }
    }
}
