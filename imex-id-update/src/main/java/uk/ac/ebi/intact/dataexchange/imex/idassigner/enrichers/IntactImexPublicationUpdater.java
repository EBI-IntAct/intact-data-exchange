package uk.ac.ebi.intact.dataexchange.imex.idassigner.enrichers;

import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.imex.ImexPublicationUpdater;
import psidev.psi.mi.jami.imex.listener.PublicationImexEnricherListener;
import psidev.psi.mi.jami.model.Publication;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationAdminUserSynchronizer;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

/**
 * This enricher will update a publication having IMEx id and synchronize it with IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/10/14</pre>
 */

public class IntactImexPublicationUpdater extends ImexPublicationUpdater {

    private PublicationAdminUserSynchronizer adminUserSynchronizer;

    public IntactImexPublicationUpdater(ImexCentralClient fetcher) {
        super(fetcher);
    }

    @Override
    protected void processCurationDepth(Publication publicationToEnrich, Publication fetched) {
        super.processCurationDepth(publicationToEnrich, fetched);
        if (publicationToEnrich instanceof IntactPublication
                && ((IntactPublication)publicationToEnrich).getCurrentOwner() != null
                && getAdminUserSynchronizer() != null && fetched instanceof ImexPublication){
            try {
                getAdminUserSynchronizer().synchronizePublicationAdminUser((IntactPublication)publicationToEnrich, (ImexPublication)fetched);
            } catch (BridgeFailedException e) {
                getPublicationEnricherListener().onEnrichmentError(publicationToEnrich, "Cannot update the admin user of publication "+publicationToEnrich+" in IMEx central", e);
            }
        }
    }

    @Override
    protected void processReleasedDate(Publication publicationToEnrich, Publication fetched) {
        if (getStatusSynchronizer() != null && fetched instanceof ImexPublication){
            try {
                getStatusSynchronizer().synchronizePublicationStatusWithImexCentral(publicationToEnrich, (ImexPublication)fetched);
                if (getPublicationEnricherListener() instanceof PublicationImexEnricherListener){
                    ((PublicationImexEnricherListener)getPublicationEnricherListener()).onImexStatusUpdated(publicationToEnrich,
                            ((ImexPublication) fetched).getStatus());
                }
            } catch (BridgeFailedException e) {
                if (getPublicationEnricherListener() != null){
                    getPublicationEnricherListener().onEnrichmentError(publicationToEnrich, "Cannot update the status of publication " + publicationToEnrich + " in IMEx central", e);
                }
            }
        }
    }

    public PublicationAdminUserSynchronizer getAdminUserSynchronizer() {
        return adminUserSynchronizer;
    }

    public void setAdminUserSynchronizer(PublicationAdminUserSynchronizer adminUserSynchronizer) {
        this.adminUserSynchronizer = adminUserSynchronizer;
    }
}


