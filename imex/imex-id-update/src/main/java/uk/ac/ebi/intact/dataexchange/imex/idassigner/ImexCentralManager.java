package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.PublicationDao;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.*;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.LoggingImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ReportWriterListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.FileImexUpdateReportHandler;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.PublicationXref;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

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
    private static final Log log = LogFactory.getLog(ImexCentralManager.class);

    private ImexCentralPublicationRegister imexCentralRegister;
    private PublicationAdminGroupSynchronizer imexAdminGroupSynchronizer;
    private PublicationAdminUserSynchronizer imexAdminUserSynchronizer;
    private PublicationStatusSynchronizer imexStatusSynchronizer;
    private PublicationIdentifierSynchronizer publicationIdentifierSynchronizer;
    private IntactImexAssigner intactImexAssigner;

    private ImexAssignerConfig imexUpdateConfig;

    public static String NO_IMEX_ID="N/A";
    public static Pattern PUBMED_REGEXP = Pattern.compile("\\d+");

    private Collection<PublicationXref> pubXrefs;
    private Collection<String> interactionAcsChunk;
    private Collection<String> experimentAcsChunk;

    private int maxNumberIntactObjectPerTransaction = 10;

    private boolean updatePublicationStatus = true;
    private boolean updatePublicationAdminGroup = true;
    private boolean updatePublicationAdminUser = true;

    public ImexCentralManager(){
        pubXrefs = new ArrayList<PublicationXref>();
        experimentAcsChunk = new ArrayList<String>();
        interactionAcsChunk = new ArrayList<String>();

    }

    /**
     * Updates a publication having IMEx id and assign IMEx ids to experiments and interactions in INTAct. Returns the IMEx record if valid.
     * @param publicationAc
     * @return the record in IMEx central. Updates the experiments and interactions so they all have a valid IMEx id (only if the record is already in IMEx)
     * @throws PublicationImexUpdaterException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public edu.ucla.mbi.imex.central.ws.v20.Publication updateIntactPublicationHavingIMEx(String publicationAc) throws PublicationImexUpdaterException, ImexCentralException {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        PublicationDao pubDao = daoFactory.getPublicationDao();

        // get publication first
        Publication intactPublication = pubDao.getByAc(publicationAc);

        // the publication does exist in IntAct
        if (intactPublication != null){

            // get the imexId attached to this publication
            String imexId = collectAndCleanUpImexPrimaryReferenceFrom(intactPublication);

            // the publication does have a single valid IMEx id in IntAct
            if (imexId != null && !imexId.equals(NO_IMEX_ID)){

                // collect publication record using IMEx id
                edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication = imexCentralRegister.getExistingPublicationInImexCentral(imexId);

                // the IMExid is recognized in IMEx central
                if (imexPublication != null){

                    String pubId = intactPublication.getPublicationId() != null ? intactPublication.getPublicationId() : intactPublication.getShortLabel();

                    // if the intact publication identifier is not in sync with IMEx central, try to synchronize it first but does not update the intact publication
                    if (!publicationIdentifierSynchronizer.isIntactPublicationIdentifierInSyncWithImexCentral(pubId, imexPublication)){
                        log.info("Publication " + pubId + " is not in sync with IMEx central, identifier will be updated.");
                        publicationIdentifierSynchronizer.synchronizePublicationIdentifier(intactPublication, imexPublication);
                    }

                    // update publication annotations if necessary
                    boolean hasUpdated = intactImexAssigner.updatePublicationAnnotations(intactPublication);

                    // update experiments if necessary
                    Set<String> updatedExperiments = updateImexIdentifiersForAllExperiments(intactPublication, imexId);

                    // update and/or assign interactions if necessary
                    Set<String> updatedInteractions = assignImexIdentifiersForAllInteractions(intactPublication, imexId);

                    // if something has been updated, fire an update evt
                    if (!updatedExperiments.isEmpty() || !updatedInteractions.isEmpty() || hasUpdated){
                        IntactUpdateEvent evt = new IntactUpdateEvent(this, intactPublication.getPublicationId(), imexId, updatedExperiments, updatedInteractions);
                        fireOnIntactUpdate(evt);
                    }

                    // can synchronize admin group, uswer and status
                    synchronizePublicationWithImexCentral(intactPublication, imexPublication);

                }
                // the IMEx id is not recognized in IMEx central, publication needs to be updated manually
                else {
                    ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.imex_not_recognized, intactPublication.getPublicationId(), imexId, null, null, "Publication " + publicationAc + " is not matching any record in IMEx central with id " + imexId);
                    fireOnImexError(errorEvt);
                }

                return imexPublication;
            }
            // impossible to update this publication because does not have a single IMEx id or valid IMEx id
            else {
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, intactPublication.getPublicationId(), imexId, null, null, "Publication " + publicationAc + " does not have a valid IMEx id and is ignored.");
                fireOnImexError(errorEvt);
            }
        }
        // publication does not exist in IntAct
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }

        pubXrefs.clear();
        return null;
    }

    public void synchronizePublicationWithImexCentral(Publication intactPublication, edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication) throws ImexCentralException {

        if (updatePublicationAdminGroup){
            imexAdminGroupSynchronizer.synchronizePublicationAdminGroup(intactPublication, imexPublication);
        }
        if (updatePublicationAdminUser){
            imexAdminUserSynchronizer.synchronizePublicationAdminUser(intactPublication, imexPublication);
        }
        if (updatePublicationStatus){
            imexStatusSynchronizer.synchronizePublicationStatusWithImexCentral(intactPublication, imexPublication);
        }
    }

    /**
     * Create a new record in IMEx central if possible, assign a new IMEx id and update publication/experiments/interactions.
     * The publication should not already have any IMEx accession assigned to it
     * @param publicationAc
     * @return the IMEx central record if updated, null if could not assign
     * @throws PublicationImexUpdaterException
     * @throws ImexCentralException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public edu.ucla.mbi.imex.central.ws.v20.Publication assignImexAndUpdatePublication(String publicationAc) throws PublicationImexUpdaterException, ImexCentralException {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        PublicationDao pubDao = daoFactory.getPublicationDao();

        // get publication in IntAct
        Publication intactPublication = pubDao.getByAc(publicationAc);

        // the publication does exist in IntAct
        if (intactPublication != null){
            // collect intact publication identifier
            String pubId = intactPublication.getPublicationId() != null ? intactPublication.getPublicationId() : intactPublication.getShortLabel();

            // collect the record in IMEx central to check if a publication already exists
            edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication =  imexCentralRegister.getExistingPublicationInImexCentral(pubId);

            // the publication is already registered in IMEx central
            if (imexPublication != null){

                // we already have an IMEx id in IMEx central, it is not possible to update the intact publication because we have a conflict
                if (imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(NO_IMEX_ID)){
                    ImexErrorEvent evt = new ImexErrorEvent(this, ImexErrorType.imex_in_imexCentral_not_in_intact, intactPublication.getPublicationId(), imexPublication.getImexAccession(), null, null, "It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " because it already has a valid IMEx id in IMEx central.");
                    fireOnImexError(evt);
                }
                // the publication has been registered in IMex central but does not have an IMEx id. We cannot assign IMEx id, the curator must have a look at it
                else {
                    ImexErrorEvent evt = new ImexErrorEvent(this, ImexErrorType.publication_already_in_imex, intactPublication.getPublicationId(), imexPublication.getImexAccession(), null, null, "It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " because it already registered in IMEx central with another institution");
                    fireOnImexError(evt);
                }
            }
            // the publication has a valid pubmed identifier and can be registered and assign IMEx id in IMEx central
            else if (Pattern.matches(ImexCentralManager.PUBMED_REGEXP.toString(), pubId)) {
                imexPublication = imexCentralRegister.registerPublicationInImexCentral(intactPublication);

                if (imexPublication != null){
                    // assign IMEx id to publication and update publication annotations
                    assignAndUpdateIntactPublication(intactPublication, imexPublication);
                    synchronizePublicationWithImexCentral(intactPublication, imexPublication);
                }
                else {
                    ImexErrorEvent evt = new ImexErrorEvent(this, ImexErrorType.no_record_created, intactPublication.getPublicationId(), null, null, null, "It is not possible to register the publication " + intactPublication.getShortLabel() + " in IMEx central.");
                    fireOnImexError(evt);
                }

                return imexPublication;
            }
            // unassigned publication, cannot use the webservice to automatically assign IMEx id for now, ask the curator to manually register and assign IMEx id to this publication
            else {
                log.warn("It is not possible to register an unassigned publication. The publication needs to be registered manually by a curator in IMEx central.");
            }
        }
        // the publication does not exist in Intact
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }

        pubXrefs.clear();
        return null;
    }

    /**
     * Assign IMEx id to a publication already registered in IMEx central but without any IMEx primary reference and update IntAct record
     * @param intactPublication
     * @param imexPublication
     * @throws PublicationImexUpdaterException
     * @throws ImexCentralException
     */
    private void assignAndUpdateIntactPublication(Publication intactPublication, edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException {
        // assign IMEx id to publication and update publication annotations
        String imex = intactImexAssigner.assignImexIdentifier(intactPublication, imexPublication);

        if (imex != null && !imex.equals(ImexCentralManager.NO_IMEX_ID)){
            NewAssignedImexEvent evt = new NewAssignedImexEvent(this, intactPublication.getPublicationId(), imex, null, null);
            fireOnNewImexAssigned(evt);

            // update experiments
            Set<String> updatedExperiments = updateImexIdentifiersForAllExperiments(intactPublication, imex);

            // update interactions
            Set<String> updatedInteractions = assignImexIdentifiersForAllInteractions(intactPublication, imex);

            IntactUpdateEvent evt2 = new IntactUpdateEvent(this, intactPublication.getPublicationId(), imex, updatedExperiments, updatedInteractions);
            fireOnIntactUpdate(evt2);
        }
        else {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, intactPublication.getPublicationId(), imexPublication.getImexAccession(), null, null, "It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " in IMEx central.");
            fireOnImexError(errorEvt);
        }
    }

    public edu.ucla.mbi.imex.central.ws.v20.Publication getPublicationInImexCentralFor(String pubId) throws ImexCentralException {
        return imexCentralRegister.getExistingPublicationInImexCentral(pubId);
    }

    /**
     *
     * @param intactPublication
     * @return the unique IMEx id associated with this publication, null otherwise. Cleans the duplicated imex primary references if necessary
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public String collectAndCleanUpImexPrimaryReferenceFrom(Publication intactPublication) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<PublicationXref> xrefDao = daoFactory.getXrefDao(PublicationXref.class);

        pubXrefs.clear();
        pubXrefs.addAll(intactPublication.getXrefs());

        PublicationXref imexPrimaryRef = null;

        boolean hasConflictingImexId = false;
        boolean hasUpdated = false;

        for (PublicationXref ref : pubXrefs){
            // imex xref
            if (ref.getCvDatabase() != null && ref.getCvDatabase().getIdentifier() != null && ref.getCvDatabase().getIdentifier().equals(CvDatabase.IMEX_MI_REF)){
                // imex primary xref
                if (ref.getCvXrefQualifier() != null && ref.getCvXrefQualifier().getIdentifier() != null && ref.getCvXrefQualifier().getIdentifier().equals(CvXrefQualifier.IMEX_PRIMARY_MI_REF)){

                    // non null primary identifier
                    if (ref.getPrimaryId() != null){
                        // different imex id : conflict
                        if (imexPrimaryRef != null && !imexPrimaryRef.getPrimaryId().equals(ref.getPrimaryId())){
                            hasConflictingImexId = true;
                        }
                        // identical primary identifier and imex id was already present so we delete the xref.,
                        else if (imexPrimaryRef != null && imexPrimaryRef.getPrimaryId().equals(ref.getPrimaryId())) {
                            intactPublication.removeXref(ref);
                            xrefDao.delete(ref);
                            hasUpdated = true;
                        }
                        // we found the imex primary ref
                        else {
                            imexPrimaryRef = ref;
                        }
                    }
                    // null primary identifier, we delete the xref
                    else {
                        intactPublication.removeXref(ref);
                        xrefDao.delete(ref);
                        hasUpdated = true;
                    }
                }
            }
        }

        // we found a unique imex identifier
        if (imexPrimaryRef != null && !hasConflictingImexId){

            if (hasUpdated){
                IntactUpdateEvent evt = new IntactUpdateEvent(this, intactPublication.getPublicationId(), imexPrimaryRef.getPrimaryId(), Collections.EMPTY_SET, Collections.EMPTY_SET);
                fireOnIntactUpdate(evt);
            }

            return imexPrimaryRef.getPrimaryId();
        }
        else {
            if (hasUpdated){
                IntactUpdateEvent evt = new IntactUpdateEvent(this, intactPublication.getPublicationId(), null, Collections.EMPTY_SET, Collections.EMPTY_SET);
                fireOnIntactUpdate(evt);
            }

            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.publication_imex_conflict, intactPublication.getPublicationId(), null, null, null, "Publication " + intactPublication.getShortLabel() + " cannot be updated because of IMEx identifier conflicts.");
            fireOnImexError(errorEvt);
        }

        return null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Set<String> updateImexIdentifiersForAllExperiments(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException {

        // imex id is not null and is not default value for missing imex id
        if (imexId != null && !imexId.equals(ImexCentralManager.NO_IMEX_ID)){

            List<String> updatedExp = intactImexAssigner.collectExperimentsToUpdateFrom(intactPublication, imexId);
            Set<String> updatedExperiments = new HashSet<String>(updatedExp.size());

            int processedExperiments = 0;
            int size = updatedExp.size();

            if (size > 0){
                while (processedExperiments < size){
                    int chunk = 0;
                    experimentAcsChunk.clear();

                    while (chunk < maxNumberIntactObjectPerTransaction && processedExperiments < size){
                        experimentAcsChunk.add(updatedExp.get(processedExperiments));
                        chunk++;
                        processedExperiments++;
                    }

                    intactImexAssigner.assignImexIdentifierToExperiments(experimentAcsChunk, imexId, this, updatedExperiments);
                }
            }

            return updatedExperiments;
        }
        else {
            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, intactPublication.getPublicationId(), imexId, null, null, "Impossible to update IMEx identifiers to experiments of publication " + intactPublication.getShortLabel());
            fireOnImexError(errorEvent);

            return Collections.EMPTY_SET;
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Set<String> assignImexIdentifiersForAllInteractions(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException {

        // imex id is not null and is not default value for missing imex id
        if (imexId != null && !imexId.equals(ImexCentralManager.NO_IMEX_ID)){
            // reset the context of imex assigner so the interaction index will be reset
            intactImexAssigner.resetPublicationContext(intactPublication, imexId);

            List<String> interactionsToUpdate = intactImexAssigner.collectInteractionsToUpdateFrom(intactPublication, imexId);
            Set<String> updatedInteractions = new HashSet<String>(interactionsToUpdate.size());

            int processedInteractions = 0;
            int size = interactionsToUpdate.size();

            if (size > 0){
                while (processedInteractions < size){
                    int chunk = 0;
                    interactionAcsChunk.clear();

                    while (chunk < maxNumberIntactObjectPerTransaction && processedInteractions < size){
                        interactionAcsChunk.add(interactionsToUpdate.get(processedInteractions));
                        chunk++;
                        processedInteractions++;
                    }

                    intactImexAssigner.assignImexIdentifierToInteractions(interactionAcsChunk, imexId, this, updatedInteractions);
                }
            }

            return updatedInteractions;
        }
        else {
            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, intactPublication.getPublicationId(), imexId, null, null, "Impossible to update IMEx identifiers to interactions of publication " + intactPublication.getShortLabel());
            fireOnImexError(errorEvent);

            return Collections.EMPTY_SET;
        }
    }

    /**
     * Check if a publication is already registered in IMEx central
     * @param identifier : pubmed, unassigned, doi
     * @return
     * @throws ImexCentralException
     */
    public boolean isPublicationAlreadyRegisteredInImexCentral(String identifier) throws ImexCentralException {
        return imexCentralRegister.getExistingPublicationInImexCentral(identifier) != null;
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

    public PublicationIdentifierSynchronizer getPublicationIdentifierSynchronizer() {
        return publicationIdentifierSynchronizer;
    }

    public void setPublicationIdentifierSynchronizer(PublicationIdentifierSynchronizer publicationIdentifierSynchronizer) {
        this.publicationIdentifierSynchronizer = publicationIdentifierSynchronizer;
    }

    public EventListenerList getListenerList() {
        return listenerList;
    }

    public void setListenerList(EventListenerList listenerList) {
        this.listenerList = listenerList;
    }

    public ImexAssignerConfig getImexUpdateConfig() {
        return imexUpdateConfig;
    }

    public void setImexUpdateConfig(ImexAssignerConfig imexUpdateConfig) {
        this.imexUpdateConfig = imexUpdateConfig;
    }

    // listeners

    protected void registerListeners() {
        addListener( new LoggingImexUpdateListener() );

        final File directory = imexUpdateConfig.getUpdateLogsDirectory();
        if( directory != null ) {
            final FileImexUpdateReportHandler handler;
            try {
                handler = new FileImexUpdateReportHandler( directory );
            } catch ( IOException e ) {
                throw new RuntimeException( "Filed to initialize ReportWriterListener: " + directory );
            }
            addListener( new ReportWriterListener( handler ) );
        }
    }

    public void addListener( ImexUpdateListener listener) {
        listenerList.add(ImexUpdateListener.class, listener);
    }

    public void removeListener( ImexUpdateListener listener) {
        listenerList.remove(ImexUpdateListener.class, listener);
    }

    protected <T> List<T> getListeners(Class<T> listenerClass) {
        List list = new ArrayList();

        Object[] listeners = listenerList.getListenerList();

        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ImexUpdateListener.class) {
                if (listenerClass.isAssignableFrom(listeners[i+1].getClass())) {
                    list.add(listeners[i+1]);
                }
            }
        }
        return list;
    }

    public void registerListenersIfNotDoneYet() {
        if (listenerList.getListenerCount() == 0) {
            registerListeners();
        }

        if (listenerList.getListenerCount() == 0) {
            throw new IllegalStateException("No listener registered for ProteinProcessor");
        }
    }

    public void fireOnImexError( ImexErrorEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onImexError(evt);
        }
    }

    public void fireOnIntactUpdate( IntactUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onIntactUpdate(evt);
        }
    }

    public void fireOnNewImexAssigned( NewAssignedImexEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onNewImexAssigned(evt);
        }
    }

    public int getMaxNumberIntactObjectPerTransaction() {
        return maxNumberIntactObjectPerTransaction;
    }

    public void setMaxNumberIntactObjectPerTransaction(int maxNumberIntactObjectPerTransaction) {
        if (maxNumberIntactObjectPerTransaction > 0){
            this.maxNumberIntactObjectPerTransaction = maxNumberIntactObjectPerTransaction;
        }
    }

    public void resetImexCentralClient(ImexCentralClient imexCentralClient) {

        if (imexCentralClient != null){
            this.imexCentralRegister.setImexCentralClient(imexCentralClient);
            this.imexAdminGroupSynchronizer.setImexCentralClient(imexCentralClient);
            this.imexAdminUserSynchronizer.setImexCentralClient(imexCentralClient);
            this.imexStatusSynchronizer.setImexCentralClient(imexCentralClient);
            this.publicationIdentifierSynchronizer.setImexCentralClient(imexCentralClient);
            this.intactImexAssigner.setImexCentralClient(imexCentralClient);
        }
    }

    public boolean isUpdatePublicationStatus() {
        return updatePublicationStatus;
    }

    public void setUpdatePublicationStatus(boolean updatePublicationStatus) {
        this.updatePublicationStatus = updatePublicationStatus;
    }

    public boolean isUpdatePublicationAdminGroup() {
        return updatePublicationAdminGroup;
    }

    public void setUpdatePublicationAdminGroup(boolean updatePublicationAdminGroup) {
        this.updatePublicationAdminGroup = updatePublicationAdminGroup;
    }

    public boolean isUpdatePublicationAdminUser() {
        return updatePublicationAdminUser;
    }

    public void setUpdatePublicationAdminUser(boolean updatePublicationAdminUser) {
        this.updatePublicationAdminUser = updatePublicationAdminUser;
    }
}
