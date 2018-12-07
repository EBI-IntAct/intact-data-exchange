package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.PublicationStatus;
import psidev.psi.mi.jami.imex.actions.impl.PublicationStatusSynchronizerImpl;
import psidev.psi.mi.jami.model.Publication;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;

/**
 * This class allows to convert intact publication status to publication status in imexcentral. It updates and synchronize the publications status
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class IntactPublicationStatusSynchronizerImpl extends PublicationStatusSynchronizerImpl{

    private static final Log log = LogFactory.getLog(IntactPublicationStatusSynchronizerImpl.class);

    public IntactPublicationStatusSynchronizerImpl(ImexCentralClient client) {
        super(client);
    }

    @Override
    public PublicationStatus getPublicationStatus( Publication publication ) {
        // IMEx central has currently the following publication states available:
        // NEW / RESERVED / INPROGRESS / RELEASED / DISCARDED / INCOMPLETE / PROCESSED

        if (publication instanceof IntactPublication){
            IntactPublication intactPub = (IntactPublication)publication;
            PublicationStatus status;

            LifeCycleStatus intactStatus = intactPub.getStatus();
            switch (intactStatus){
                case NEW:
                    return PublicationStatus.NEW;
                case RESERVED:
                    return PublicationStatus.RESERVED;
                case ASSIGNED:
                    return PublicationStatus.RESERVED;
                case DISCARDED:
                    return PublicationStatus.DISCARDED;
                case READY_FOR_RELEASE:
                    return PublicationStatus.PROCESSED;
                case RELEASED:
                    return PublicationStatus.RELEASED;
                case READY_FOR_CHECKING:
                    return PublicationStatus.REVIEW;
                default:
                    return PublicationStatus.INPROGRESS;
            }
        }
        else{
            return super.getPublicationStatus(publication);
        }
    }
}
