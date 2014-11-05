package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.bridges.imex.extension.ImexPublication;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.imex.actions.ImexCentralPublicationRegister;
import psidev.psi.mi.jami.imex.actions.PublicationStatusSynchronizer;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactImexAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.enrichers.IntactImexPublicationAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.enrichers.IntactImexPublicationRegister;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.enrichers.IntactImexPublicationUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.LoggingImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ReportWriterListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.FileImexUpdateReportHandler;

import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.dao.PublicationDao;

import uk.ac.ebi.intact.jami.interceptor.IntactTransactionSynchronization;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

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
@Service
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class ImexCentralManager {
    @Autowired
    @Qualifier("intactDao")
    private IntactDao intactDao;

    @Autowired
    @Qualifier("intactTransactionSynchronization")
    private IntactTransactionSynchronization afterCommitExecutor;

    @Autowired
    @Qualifier("imexCentralClient")
    private ImexCentralClient imexCentralClient;

    @Autowired
    @Qualifier("intactPublicationUpdater")
    private IntactImexPublicationUpdater publicationUpdater;

    @Autowired
    @Qualifier("intactPublicationRegister")
    private IntactImexPublicationRegister publicationRegister;

    @Autowired
    @Qualifier("intactPublicationAssigner")
    private IntactImexPublicationAssigner publicationImexAssigner;

    @Autowired
    @Qualifier("imexCentralRegister")
    private ImexCentralPublicationRegister imexCentralRegister;

    @Autowired
    @Qualifier("intactImexStatusSynchronizer")
    private PublicationStatusSynchronizer imexStatusSynchronizer;

    @Autowired
    @Qualifier("intactImexAssigner")
    private IntactImexAssigner intactImexAssigner;

    @Autowired
    @Qualifier("imexUpdateConfig")
    private ImexAssignerConfig imexUpdateConfig;

    /**
     * List of listeners
     */
    private EventListenerList listenerList = new EventListenerList();
    private static final Log log = LogFactory.getLog(ImexCentralManager.class);

    public static Pattern PUBMED_REGEXP = Pattern.compile("\\d+");

    private Collection<String> interactionAcsChunk;
    private Collection<String> experimentAcsChunk;

    private int maxNumberIntactObjectPerTransaction = 10;

    public static final String INTACT_CURATOR = "intact";
    private boolean hasUpdatedAnnotations = false;

    public ImexCentralManager(){
        experimentAcsChunk = new ArrayList<String>();
        interactionAcsChunk = new ArrayList<String>();
    }

    /**
     * Updates a publication having IMEx id and assign IMEx ids to experiments and interactions in INTAct. Returns the IMEx record if valid.
     * @param publicationAc
     * @return the record in IMEx central. Updates the experiments and interactions so they all have a valid IMEx id (only if the record is already in IMEx)
     * @throws PublicationImexUpdaterException
     */
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void updateIntactPublicationHavingIMEx(String publicationAc) throws PublicationImexUpdaterException, EnricherException {
        // register intactdao in the transaction manager so it can clean cache after transaction commit
        afterCommitExecutor.registerDaoForSynchronization(this.intactDao);
        PublicationDao pubDao = intactDao.getPublicationDao();

        // get publication first
        IntactPublication intactPublication = pubDao.getByAc(publicationAc);
        // reset updated annotations
        hasUpdatedAnnotations = false;

        // the publication does exist in IntAct
        if (intactPublication != null){

            // first update publication using enricher
            getPublicationUpdater().enrich(intactPublication);
            // synchronize pub
            try {
                pubDao.update(intactPublication);
            } catch (FinderException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (SynchronizerException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (PersisterException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            }

            // update experiments and interactions
            assignAndUpdateIntactExperimentsAndInteractions(intactPublication);
        }
        // publication does not exist in IntAct
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }
    }

    /**
     * Create a new record in IMEx central if possible, assign a new IMEx id and update publication/experiments/interactions.
     * The publication should not already have any IMEx accession assigned to it
     * @param publicationAc
     * @return the IMEx central record if updated, null if could not assign
     * @throws PublicationImexUpdaterException
     * @throws psidev.psi.mi.jami.enricher.exception.EnricherException
     */
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void assignImexAndUpdatePublication(String publicationAc) throws PublicationImexUpdaterException, EnricherException {
        // register intactdao in the transaction manager so it can clean cache after transaction commit
        afterCommitExecutor.registerDaoForSynchronization(this.intactDao);
        PublicationDao pubDao = intactDao.getPublicationDao();

        // get publication first
        IntactPublication intactPublication = pubDao.getByAc(publicationAc);
        // reset updated annotations
        hasUpdatedAnnotations = false;

        // the publication does exist in IntAct
        if (intactPublication != null){

            // assign and update publication
            getPublicationImexAssigner().enrich(intactPublication);

            // synchronize pub
            try {
                pubDao.update(intactPublication);
            } catch (FinderException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (SynchronizerException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (PersisterException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            }

            // update experiments and interactions
            assignAndUpdateIntactExperimentsAndInteractions(intactPublication);
        }
        // the publication does not exist in Intact
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void registerAndUpdatePublication(String publicationAc) throws EnricherException {
        // register intactdao in the transaction manager so it can clean cache after transaction commit
        afterCommitExecutor.registerDaoForSynchronization(this.intactDao);
        PublicationDao pubDao = intactDao.getPublicationDao();

        // get publication first
        IntactPublication intactPublication = pubDao.getByAc(publicationAc);
        // reset updated annotations
        hasUpdatedAnnotations = false;

        // the publication does exist in IntAct
        if (intactPublication != null){

            // register and update publication
            getPublicationRegister().enrich(intactPublication);

            // synchronize pub
            try {
                pubDao.update(intactPublication);
            } catch (FinderException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (SynchronizerException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            } catch (PersisterException e) {
                String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
                if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                    Xref id = intactPublication.getXrefs().iterator().next();
                    pubId = id.getId();
                }
                ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.fatal_error, pubId, intactPublication.getImexId(), null, null,
                        "Publication " + intactPublication.getAc() + " cannot be updated: "+e.getCause());
                fireOnImexError(errorEvt);
            }
        }
        // the publication does not exist in Intact
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    /**
     * This method discard a publication in IMEx central if it is already registered in IMEx central
     */
    public void discardPublication(String publicationAc) throws BridgeFailedException {
        // register intactdao in the transaction manager so it can clean cache after transaction commit
        afterCommitExecutor.registerDaoForSynchronization(this.intactDao);
        PublicationDao pubDao = intactDao.getPublicationDao();

        // get publication first
        IntactPublication intactPublication = pubDao.getByAc(publicationAc);
        // reset updated annotations
        hasUpdatedAnnotations = false;

        // the publication does exist in IntAct
        if (intactPublication != null){
            // collect intact publication identifier
            String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
            String source = intactPublication.getPubmedId() != null ? Xref.PUBMED : Xref.DOI;
            if (pubId == null && !intactPublication.getIdentifiers().isEmpty()) {
                Xref id = intactPublication.getXrefs().iterator().next();
                source = id.getDatabase().getShortName();
                pubId = id.getId();
            }

            // collect the record in IMEx central to check if a publication already exists
            ImexPublication imexPublication = (ImexPublication)getImexCentralRegister().getExistingPublicationInImexCentral(pubId, source);

            // the publication is already registered in IMEx central
            if (imexPublication != null && imexPublication.getOwner() != null && imexPublication.getOwner().toLowerCase().equals(INTACT_CURATOR)){

                getImexStatusSynchronizer().discardPublicationInImexCentral(intactPublication, imexPublication);
            }
            // the publication is not in IMEx central, nothing to do
            else {
                log.warn("The publication is not registered in IMEx central by IntAct. We don't have to discard it.");
            }
        }
        // the publication does not exist in Intact
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }
    }

    /**
     * Assign IMEx id to a publication already registered in IMEx central but without any IMEx primary reference and update IntAct record
     * @param intactPublication
     * @throws PublicationImexUpdaterException
     */
    private void assignAndUpdateIntactExperimentsAndInteractions(IntactPublication intactPublication) throws PublicationImexUpdaterException{
        // get assigned IMEx id to publication and update publication annotations
        String imex = intactPublication.getImexId();
        String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
        if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
            Xref id = intactPublication.getXrefs().iterator().next();
            pubId = id.getId();
        }
        if (imex != null){
            // update experiments if necessary
            Set<String> updatedExperiments = updateImexIdentifiersForAllExperiments(intactPublication, imex);

            // update and/or assign interactions if necessary
            Set<String> updatedInteractions = assignImexIdentifiersForAllInteractions(intactPublication, imex);

            // if something has been updated, fire an update evt
            if (!updatedExperiments.isEmpty() || !updatedInteractions.isEmpty() || hasUpdatedAnnotations){

                IntactUpdateEvent evt = new IntactUpdateEvent(this, pubId, imex, updatedExperiments, updatedInteractions);
                fireOnIntactUpdate(evt);
            }
        }
        else {
            ImexErrorEvent errorEvt = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, pubId, imex, null, null,
                    "It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " in IMEx central.");
            fireOnImexError(errorEvt);
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.SUPPORTS)
    public Set<String> updateImexIdentifiersForAllExperiments(IntactPublication intactPublication, String imexId) throws PublicationImexUpdaterException {

        // imex id is not null and is not default value for missing imex id
        if (imexId != null){

            List<String> updatedExp = getIntactImexAssigner().collectExperimentsToUpdateFrom(intactPublication, imexId);
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

                    getIntactImexAssigner().assignImexIdentifierToExperiments(experimentAcsChunk, imexId, this, updatedExperiments);
                }
            }

            return updatedExperiments;
        }
        else {
            String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
            if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                Xref id = intactPublication.getXrefs().iterator().next();
                pubId = id.getId();
            }
            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, pubId, imexId, null, null,
                    "Impossible to update IMEx identifiers to experiments of publication " + pubId);
            fireOnImexError(errorEvent);

            return Collections.EMPTY_SET;
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.SUPPORTS)
    public Set<String> assignImexIdentifiersForAllInteractions(IntactPublication intactPublication, String imexId) throws PublicationImexUpdaterException {

        // imex id is not null and is not default value for missing imex id
        if (imexId != null){
            // reset the context of imex assigner so the interaction index will be reset
            intactImexAssigner.resetPublicationContext(intactPublication, imexId);

            List<String> interactionsToUpdate = getIntactImexAssigner().collectInteractionsToUpdateFrom(intactPublication, imexId);
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

                    getIntactImexAssigner().assignImexIdentifierToInteractions(interactionAcsChunk, imexId, this, updatedInteractions);
                }
            }

            return updatedInteractions;
        }
        else {
            String pubId = intactPublication.getPubmedId() != null ? intactPublication.getPubmedId() : intactPublication.getDoi();
            if (pubId == null && !intactPublication.getIdentifiers().isEmpty()){
                Xref id = intactPublication.getXrefs().iterator().next();
                pubId = id.getId();
            }
            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.no_IMEX_id, pubId, imexId, null, null,
                    "Impossible to update IMEx identifiers to interactions of publication " + pubId);
            fireOnImexError(errorEvent);

            return Collections.EMPTY_SET;
        }
    }

    /**
     * Check if a publication is already registered in IMEx central
     * @param identifier : pubmed, unassigned, doi
     * @return
     * @throws psidev.psi.mi.jami.bridges.exception.BridgeFailedException
     */
    public boolean isPublicationAlreadyRegisteredInImexCentral(String identifier, String source) throws BridgeFailedException {
        return getImexCentralRegister().getExistingPublicationInImexCentral(identifier, source) != null;
    }

    public ImexCentralPublicationRegister getImexCentralRegister() {
        return imexCentralRegister;
    }

    public void setImexCentralRegister(ImexCentralPublicationRegister imexCentralRegister) {
        this.imexCentralRegister = imexCentralRegister;
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

    public void setHasUpdatedAnnotations(boolean hasUpdatedAnnotations) {
        this.hasUpdatedAnnotations = hasUpdatedAnnotations;
    }

    public IntactImexPublicationUpdater getPublicationUpdater() {
        return publicationUpdater;
    }

    public IntactImexPublicationRegister getPublicationRegister() {
        return publicationRegister;
    }

    public IntactImexPublicationAssigner getPublicationImexAssigner() {
        return publicationImexAssigner;
    }

    public void setPublicationUpdater(IntactImexPublicationUpdater publicationUpdater) {
        this.publicationUpdater = publicationUpdater;
    }

    public void setPublicationRegister(IntactImexPublicationRegister publicationRegister) {
        this.publicationRegister = publicationRegister;
    }

    public void setPublicationImexAssigner(IntactImexPublicationAssigner publicationImexAssigner) {
        this.publicationImexAssigner = publicationImexAssigner;
    }
}
