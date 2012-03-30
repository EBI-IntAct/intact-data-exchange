package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.DefaultImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.PublicationDao;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.*;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl.PublicationIdentifierSynchronizerImpl;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.PublicationXref;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.Collection;

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

    private ImexCentralClient imexCentral;

    private ImexCentralPublicationRegister imexCentralRegister;
    private PublicationAdminGroupSynchronizer imexAdminGroupSynchronizer;
    private PublicationAdminUserSynchronizer imexAdminUserSynchronizer;
    private PublicationStatusSynchronizer imexStatusSynchronizer;
    private PublicationIdentifierSynchronizer publicationIdentifierSynchronizer;
    private IntactImexAssigner intactImexAssigner;

    private Collection<PublicationXref> pubXrefs = new ArrayList<PublicationXref>();

    public ImexCentralManager( String icUsername, String icPassword, String icEndpoint ) throws ImexCentralException {
        this.imexCentral = new DefaultImexCentralClient( icUsername, icPassword, icEndpoint );
    }

    public ImexCentralManager( ImexCentralClient client ) {
        this.imexCentral = client;
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

        Publication intactPublication = pubDao.getByAc(publicationAc);

        if (intactPublication != null){
            String imexId = collectAndCleanUpImexPrimaryReferenceFrom(intactPublication);

            if (imexId != null){
                edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication = imexCentralRegister.getExistingPublicationInImexCentral(imexId);

                if (imexPublication != null){

                    String pubId = intactPublication.getPublicationId() != null ? intactPublication.getPublicationId() : intactPublication.getShortLabel();

                    // if the intact publication identifier is not in sync with IMEx central, try to synchronize it first
                    if (!publicationIdentifierSynchronizer.isIntactPublicationIdentifierInSyncWithImexCentral(pubId, imexPublication)) {
                        publicationIdentifierSynchronizer.synchronizePublicationIdentifier(intactPublication, imexPublication);
                    }

                    // update publication annotations if necessary
                    intactImexAssigner.updatePublicationAnnotations(intactPublication);

                    // update experiments if necessary
                    intactImexAssigner.updateImexIdentifiersForAllExperiments(intactPublication, imexId);

                    // update and/or assign interactions if necessary
                    intactImexAssigner.assignImexIdentifiersForAllInteractions(intactPublication, imexId);
                }
                else {
                    throw new PublicationImexUpdaterException("Publication " + publicationAc + " is not matching any record in IMEx central with id " + imexId);
                }

                return imexPublication;
            }
            else {
                throw new PublicationImexUpdaterException("Publication " + publicationAc + " does not have a valid IMEx id and is ignored.");
            }
        }
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }

        pubXrefs.clear();
        return null;
    }

    public void synchronizePublicationWithImexCentral(Publication intactPublication, edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication) throws PublicationImexUpdaterException {

        imexAdminGroupSynchronizer.synchronizePublicationAdminGroup(intactPublication, imexPublication);
        imexAdminUserSynchronizer.synchronizePublicationAdminUser(intactPublication, imexPublication);
        imexStatusSynchronizer.synchronizePublicationStatusWithImexCentral(intactPublication, imexPublication);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public edu.ucla.mbi.imex.central.ws.v20.Publication assignImexAndUpdatePublication(String publicationAc) throws PublicationImexUpdaterException, ImexCentralException {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        PublicationDao pubDao = daoFactory.getPublicationDao();

        Publication intactPublication = pubDao.getByAc(publicationAc);

        if (intactPublication != null){
            String pubId = intactPublication.getPublicationId() != null ? intactPublication.getPublicationId() : intactPublication.getShortLabel();

            // the publication can be registered and assign IMEx id in IMEx central
            if (!pubId.startsWith(ImexCentralUpdater.UNASSIGNED_PREFIX)){

                edu.ucla.mbi.imex.central.ws.v20.Publication imexPublication =  imexCentralRegister.getExistingPublicationInImexCentral(pubId);

                // the publication is already registered in IMEx central
                if (imexPublication != null){

                    // we already have an IMEx id in IMEx central
                    if (imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(PublicationIdentifierSynchronizerImpl.NO_IMEX_ID)){
                        // update primary ref in INtact
                        intactImexAssigner.updateImexPrimaryRef(intactPublication, imexPublication);

                        // update publication annotations
                        intactImexAssigner.updatePublicationAnnotations(intactPublication);

                        // update experiments
                        intactImexAssigner.updateImexIdentifiersForAllExperiments(intactPublication, imexPublication.getImexAccession());

                        // update interactions
                        intactImexAssigner.assignImexIdentifiersForAllInteractions(intactPublication, imexPublication.getImexAccession());
                    }
                    else {
                        throw new PublicationImexUpdaterException("It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " in IMEx central.");
                    }
                }
                // needs to register first the publication in IMEx central
                else {
                    imexPublication = imexCentralRegister.registerPublicationInImexCentral(intactPublication);

                    if (imexPublication != null){
                        // assign IMEx id to publication and update publication annotations
                        intactImexAssigner.assignImexIdentifier(intactPublication, imexPublication);

                        if (imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(PublicationIdentifierSynchronizerImpl.NO_IMEX_ID)){
                            // update experiments
                            intactImexAssigner.updateImexIdentifiersForAllExperiments(intactPublication, imexPublication.getImexAccession());

                            // update interactions
                            intactImexAssigner.assignImexIdentifiersForAllInteractions(intactPublication, imexPublication.getImexAccession());
                        }
                        else {
                            throw new PublicationImexUpdaterException("It is not possible to assign a valid IMEx id to the publication " + intactPublication.getShortLabel() + " in IMEx central.");
                        }
                    }
                    else {
                        throw new PublicationImexUpdaterException("It is not possible to register the publication " + intactPublication.getShortLabel() + " in IMEx central.");
                    }
                }
            }
            // unassigned publication, cannot use the webservice to automatically assign IMEx id for now, ask the curator to manually register and assign IMEx id to this publication
            else {
                throw new PublicationImexUpdaterException("It is not possible to assign an IMEx id to a unassigned publication. The publication needs to be registered manually by a curator in IMEx central.");
            }
        }
        else {
            log.error("Publication " + publicationAc + " does not exist in IntAct and is ignored.");
        }

        pubXrefs.clear();
        return null;
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
                    }
                }
            }
        }

        // we found a unique imex identifier
        if (imexPrimaryRef != null && !hasConflictingImexId){
            return imexPrimaryRef.getPrimaryId();
        }
        else {
            log.error("Publication " + intactPublication.getShortLabel() + " cannot be updated because of IMEx identifier conflicts.");
        }

        return null;
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
}
