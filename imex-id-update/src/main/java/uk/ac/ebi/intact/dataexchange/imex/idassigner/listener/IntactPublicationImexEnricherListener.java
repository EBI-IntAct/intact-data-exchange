package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.bridges.imex.PublicationStatus;
import psidev.psi.mi.jami.enricher.listener.EnrichmentStatus;
import psidev.psi.mi.jami.imex.listener.PublicationImexEnricherListener;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * A statistics logger which records changes made by the enricher.
 * Each addition, removal or update is counted and, upon the completion of the enrichment of the object,
 * is logged in either a file of successes or failures depending on the enrichmentStatus.
 *
 */
public class IntactPublicationImexEnricherListener
        implements PublicationImexEnricherListener {

    private ImexCentralManager imexCentralManager;
    private static final Log log = LogFactory.getLog(IntactPublicationImexEnricherListener.class);

    public IntactPublicationImexEnricherListener(ImexCentralManager imexCentralManager) {
        if (imexCentralManager == null){
           throw new IllegalArgumentException("The IMEx central manager cannot be null.");
        }
        this.imexCentralManager = imexCentralManager;
    }

    @Override
    public void onImexIdConflicts(Publication intactPublication, Collection<Xref> conflictingXrefs) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_imex_conflict, pubId, null, null, null,
                "Publication " + pubId + " cannot be updated because of IMEx identifier conflicts.");
        getImexCentralManager().fireOnImexError(errorEvt);

        // do not update experiments and interactions
        getImexCentralManager().setEnableExperimentsAndInteractionsUpdate(false);
    }

    @Override
    public void onMissingImexId(Publication intactPublication) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, pubId, null, null, null, "Publication "
                + ((IntactPublication)intactPublication).getAc() + " does not have a valid IMEx id and is ignored.");
        imexCentralManager.fireOnImexError(errorEvt);

        // do not update experiments and interactions
        getImexCentralManager().setEnableExperimentsAndInteractionsUpdate(false);
    }

    @Override
    public void onCurationDepthUpdated(Publication publication, CurationDepth oldDepth) {

    }

    @Override
    public void onImexAdminGroupUpdated(Publication publication, Source oldSource) {

    }

    @Override
    public void onImexStatusUpdated(Publication publication, PublicationStatus oldStatus) {

    }

    @Override
    public void onImexPublicationIdentifierSynchronized(Publication publication) {

    }

    @Override
    public void onPublicationAlreadyRegisteredInImexCentral(Publication intactPublication, String imex) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        // we already have an IMEx id in IMEx central, it is not possible to update the intact publication because we have a conflict
        ImexErrorEvent evt = new ImexErrorEvent(this, ImexErrorType.publication_already_in_imex,
                pubId, imex, null, null, "It is not possible to assign a valid IMEx id to the publication " + pubId
                + " because it already registered in IMEx central with another institution");
        getImexCentralManager().fireOnImexError(evt);

        // do not update experiments and interactions
        getImexCentralManager().setEnableExperimentsAndInteractionsUpdate(false);
    }

    @Override
    public void onPublicationRegisteredInImexCentral(Publication publication) {

    }

    @Override
    public void onPublicationWhichCannotBeRegistered(Publication intactPublication) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }

        // unassigned publication, cannot use the webservice to automatically assign IMEx id for now, ask the curator to manually register and assign IMEx id to this publication
        if (!Pattern.matches(ImexCentralManager.PUBMED_REGEXP.toString(), pubId)){
            log.warn("It is not possible to register an unassigned publication. " +
                    "The publication needs to be registered manually by a curator in IMEx central.");
        }
        // the publication is already registered, we just update status and users
        else {
            ImexErrorEvent evt = new ImexErrorEvent(this, ImexErrorType.no_record_created, pubId, null, null, null,
                    "It is not possible to register the publication " + pubId + " in IMEx central.");
            getImexCentralManager().fireOnImexError(evt);
        }
        // do not update experiments and interactions
        getImexCentralManager().setEnableExperimentsAndInteractionsUpdate(false);

    }

    @Override
    public void onPublicationNotEligibleForImex(Publication publication) {
        log.warn("It is not possible to register an unassigned publication. The publication needs to be registered manually by a curator in IMEx central.");
    }

    @Override
    public void onImexIdAssigned(Publication intactPublication, String imex) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        NewAssignedImexEvent evt = new NewAssignedImexEvent(this, pubId, imex, null, null);
        getImexCentralManager().fireOnNewImexAssigned(evt);
    }

    @Override
    public void onImexIdNotRecognized(Publication intactPublication, String imex) {
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.imex_not_recognized, pubId, imex, null, null,
                "Publication " + ((IntactPublication)intactPublication).getAc() + " is not matching any record in IMEx central with id " + imex);
        getImexCentralManager().fireOnImexError(errorEvt);

        // do not update experiments and interactions
        getImexCentralManager().setEnableExperimentsAndInteractionsUpdate(false);
    }

    @Override
    public void onEnrichmentComplete(Publication object, EnrichmentStatus status, String message) {

    }

    @Override
    public void onEnrichmentError(Publication object, String message, Exception e) {

    }

    @Override
    public void onPubmedIdUpdate(Publication publication, String oldPubmedId) {

    }

    @Override
    public void onDoiUpdate(Publication publication, String oldDoi) {

    }

    @Override
    public void onImexIdentifierUpdate(Publication publication, Xref addedXref) {

    }

    @Override
    public void onTitleUpdated(Publication publication, String oldTitle) {

    }

    @Override
    public void onJournalUpdated(Publication publication, String oldJournal) {

    }

    @Override
    public void onCurationDepthUpdate(Publication publication, CurationDepth oldDepth) {

    }

    @Override
    public void onPublicationDateUpdated(Publication publication, Date oldDate) {

    }

    @Override
    public void onAuthorAdded(Publication publication, String addedAuthor) {

    }

    @Override
    public void onAuthorRemoved(Publication publication, String removedAuthor) {

    }

    @Override
    public void onReleaseDateUpdated(Publication publication, Date oldDate) {

    }

    @Override
    public void onSourceUpdated(Publication publication, Source oldSource) {

    }

    @Override
    public void onAddedAnnotation(Publication o, Annotation added) {
        getImexCentralManager().setHasUpdatedAnnotations(true);
    }

    @Override
    public void onRemovedAnnotation(Publication o, Annotation removed) {
        getImexCentralManager().setHasUpdatedAnnotations(true);
    }

    @Override
    public void onAddedIdentifier(Publication o, Xref added) {

    }

    @Override
    public void onRemovedIdentifier(Publication o, Xref removed) {

    }

    @Override
    public void onAddedXref(Publication o, Xref added) {

    }

    @Override
    public void onRemovedXref(Publication o, Xref removed) {

    }

    public ImexCentralManager getImexCentralManager() {
        return imexCentralManager;
    }


}
