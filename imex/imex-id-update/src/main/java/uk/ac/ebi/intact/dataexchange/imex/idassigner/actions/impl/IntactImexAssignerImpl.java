package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.*;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactImexAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will assign an IMEx id to a publication using imex central webservice and update experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/03/12</pre>
 */
public class IntactImexAssignerImpl extends ImexCentralUpdater implements IntactImexAssigner{

    private static final Log log = LogFactory.getLog(IntactImexAssignerImpl.class);

    private Pattern interaction_imex_regexp = Pattern.compile("(IM-[1-9][0-9]*)-([1-9][0-9]*)");
    public static String IMEX_SECONDARY_MI = "MI:0952";
    public static String IMEX_SECONDARY = "imex secondary";
    public static String FULL_COVERAGE_MI = "MI:0957";
    public static String FULL_COVERAGE = "full coverage";
    public static String IMEX_CURATION_MI = "MI:0959";
    public static String IMEX_CURATION = "imex curation";
    public static String FULL_COVERAGE_TEXT = "Only protein-protein interactions";

    private Collection<ExperimentXref> experimentXrefs = new ArrayList<ExperimentXref>();
    private Collection<InteractorXref> interactionXrefs = new ArrayList<InteractorXref>();
    private Collection<Annotation> publicationAnnot = new ArrayList<Annotation>();
    private Set<String> processedImexIds = new HashSet<String> ();
    List<String> existingImexIds = new ArrayList<String>();

    private int currentIndex = 1;

    @Transactional(propagation = Propagation.SUPPORTS)
    public String assignImexIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException {

        String pubId = extractPubIdFromIntactPublication(intactPublication);

        imexPublication = imexCentral.getPublicationImexAccession( pubId, true );

        if (imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(ImexCentralManager.NO_IMEX_ID)){
            updateImexPrimaryRef(intactPublication, imexPublication);

            updatePublicationAnnotations(intactPublication);

            return imexPublication.getImexAccession();
        }
        else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean updateImexPrimaryRef(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<PublicationXref> xrefDao = daoFactory.getXrefDao(PublicationXref.class);
        PublicationDao pubDao = daoFactory.getPublicationDao();

        CvDatabase imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
        if (imex == null){
            imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
            IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imex);
        }

        CvXrefQualifier imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        if (imexPrimary == null){
            imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
            IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexPrimary);
        }

        PublicationXref pubXref = new PublicationXref( intactPublication.getOwner(), imex, imexPublication.getImexAccession(), imexPrimary );
        intactPublication.addXref( pubXref );

        xrefDao.persist(pubXref);
        pubDao.update(intactPublication);

        return true;
    }

    @Override
    public void resetPublicationContext(uk.ac.ebi.intact.model.Publication pub, String imexId) {
        this.experimentXrefs.clear();
        this.interactionXrefs.clear();
        this.processedImexIds.clear();
        existingImexIds.clear();
        existingImexIds = collectExistingInteractionImexIdsForPublication(pub);
        this.currentIndex = getNextImexChunkNumberAndFilterValidImexIdsFrom(existingImexIds, imexId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean updatePublicationAnnotations(uk.ac.ebi.intact.model.Publication intactPublication){
        publicationAnnot.clear();
        publicationAnnot.addAll(intactPublication.getAnnotations());

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        AnnotationDao annotationDao = daoFactory.getAnnotationDao();
        PublicationDao publicationDao = daoFactory.getPublicationDao();

        boolean hasImexCuration = false;
        boolean hasFullCoverage = false;

        for (Annotation ann : publicationAnnot){

            // full coverage annot
            if (ann.getCvTopic() != null && ann.getCvTopic().getIdentifier() != null && ann.getCvTopic().getIdentifier().equals(FULL_COVERAGE_MI)){

                // annotation is a duplicate, we delete it
                if (hasFullCoverage){
                    intactPublication.removeAnnotation(ann);
                    annotationDao.delete(ann);
                }
                // first time we see a full coverage, if not the same text, we update it
                else if (ann.getAnnotationText() == null || (ann.getAnnotationText() != null && !ann.getAnnotationText().equals(FULL_COVERAGE_TEXT))){
                    hasFullCoverage = true;

                    ann.setAnnotationText(FULL_COVERAGE_TEXT);
                    annotationDao.update(ann);
                }
                // we found full coverage with same annotation text
                else {
                    hasFullCoverage = true;
                }
            }
            // imex curation annot
            else if (ann.getCvTopic() != null && ann.getCvTopic().getIdentifier() != null && ann.getCvTopic().getIdentifier().equals(IMEX_CURATION_MI)){

                // annotation is a duplicate, we delete it
                if (hasImexCuration){
                    intactPublication.removeAnnotation(ann);
                    annotationDao.delete(ann);
                }
                // we found imex curation
                else {
                    hasImexCuration = true;
                }
            }
        }

        if (!hasFullCoverage){
            CvTopic fullCoverage = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( FULL_COVERAGE_MI );
            if (fullCoverage == null){
                fullCoverage = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, FULL_COVERAGE_MI, FULL_COVERAGE);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(fullCoverage);
            }

            Annotation fullCoverageAnnot = new Annotation( fullCoverage, FULL_COVERAGE_TEXT );
            annotationDao.persist(fullCoverageAnnot);

            intactPublication.addAnnotation(fullCoverageAnnot);

        }

        if (!hasImexCuration){
            CvTopic imexCuration = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( IMEX_CURATION_MI );
            if (imexCuration == null){
                imexCuration = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, IMEX_CURATION_MI, IMEX_CURATION);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexCuration);
            }

            Annotation imexCurationAnnot = new Annotation( imexCuration, null );
            annotationDao.persist(imexCurationAnnot);

            intactPublication.addAnnotation(imexCurationAnnot);
        }

        publicationAnnot.clear();

        if (!hasFullCoverage || !hasImexCuration){
            publicationDao.update(intactPublication);
            return true;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean assignImexIdentifierToExperiment(String expAc, String imexId, ImexCentralManager imexCentralManager) throws PublicationImexUpdaterException {
        DaoFactory factory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<ExperimentXref> xrefDao = factory.getXrefDao(ExperimentXref.class);

        log.info("Processing experiment " + expAc);

        Experiment exp = factory.getExperimentDao().getByAc(expAc);

        if (exp != null){
            experimentXrefs.clear();
            experimentXrefs.addAll(exp.getXrefs());

            boolean hasImexId = false;
            boolean hasConflictingImexId = false;
            boolean isUpdated = false;

            for (ExperimentXref ref : experimentXrefs){

                // imex xref
                if (ref.getCvDatabase() != null && ref.getCvDatabase().getIdentifier() != null && ref.getCvDatabase().getIdentifier().equals(CvDatabase.IMEX_MI_REF)){
                    // imex primary xref
                    if (ref.getCvXrefQualifier() != null && ref.getCvXrefQualifier().getIdentifier() != null && ref.getCvXrefQualifier().getIdentifier().equals(CvXrefQualifier.IMEX_PRIMARY_MI_REF)){

                        // non null primary identifier
                        if (ref.getPrimaryId() != null){
                            // different imex id : conflict
                            if (!ref.getPrimaryId().equalsIgnoreCase(imexId)){
                                hasConflictingImexId = true;
                            }
                            // identical primary identifier and imex id not found, no need to update the experiment
                            else if (!hasImexId) {
                                hasImexId = true;
                            }
                            // identical primary identifier and imex id was already present so we delete the xref.,
                            else {
                                exp.removeXref(ref);
                                xrefDao.delete(ref);
                                isUpdated = true;
                            }
                        }
                        // null primary identifier but imex id not found, just update the imex id of the ref
                        else if (!hasImexId) {
                            hasImexId = true;

                            ref.setPrimaryId(imexId);
                            xrefDao.update(ref);
                            isUpdated = true;
                        }
                        // null primary identifier but imex id was found, just delete the ref 
                        else {
                            exp.removeXref(ref);
                            xrefDao.delete(ref);
                            isUpdated = true;
                        }
                    }
                }
            }

            if (!hasImexId && !hasConflictingImexId){
                boolean isExpUpdated = updateImexIdentifierForExperiment(exp, imexId);

                if (isExpUpdated){
                    isUpdated = true;
                }
            }
            else if (hasConflictingImexId){
                if (imexCentralManager != null){
                    ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.experiment_imex_conflict, null, imexId, exp.getAc(), null, "Experiment " + exp.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                    imexCentralManager.fireOnImexError(errorEvent);
                }
                else {
                    throw new PublicationImexUpdaterException("Experiment " + exp.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                }
            }

            // exp updated
            return isUpdated;
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> collectExistingInteractionImexIdsForPublication(uk.ac.ebi.intact.model.Publication intactPublication){
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexQuery = "select distinct x.primaryId from InteractionImpl i join i.xrefs as x join i.experiments as e join e.publication as p " +
                "where p.ac = :publicationAc and x.cvDatabase.identifier = :imex and x.cvXrefQualifier.identifier = :imexPrimary " +
                "order by x.primaryId";

        Query query = daoFactory.getEntityManager().createQuery(imexQuery);
        query.setParameter("publicationAc", intactPublication.getAc());
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        return query.getResultList();
    }

    private int getNextImexChunkNumberAndFilterValidImexIdsFrom(List<String> imexIds, String imexId){
        int number = 0;
        List<String> ids = new ArrayList<String>(imexIds);

        for (String id : ids){
            Matcher matcher = interaction_imex_regexp.matcher(id);

            if (matcher.find()){
                String pubImex = matcher.group(1);

                // valid imex id in sync with publication
                if (imexId.equals(pubImex)){
                    int index = Integer.parseInt(matcher.group(2));
                    if (number < index){
                        number = index;
                    }
                }
            }
            // not valid imex id
            else {
                imexIds.remove(id);
            }
        }

        return number+1;
    }

    /**
     *
     * @param interaction
     * @return true if it is a PPI interaction, false otherwise
     */
    private boolean involvesOnlyProteins( Interaction interaction ) {
        for ( Component component : interaction.getComponents() ) {
            if ( !( component.getInteractor() instanceof ProteinImpl ) ) {
                return false;
            }
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean assignImexIdentifierToInteraction(String interactionAc, String imexId, ImexCentralManager imexCentralManager) throws PublicationImexUpdaterException {
        DaoFactory factory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<InteractorXref> xrefDao = factory.getXrefDao(InteractorXref.class);

        log.info("Processing interaction " + interactionAc);

        Interaction interaction = factory.getInteractionDao().getByAc(interactionAc);

        if (interaction != null){
            interactionXrefs.clear();
            interactionXrefs.addAll(interaction.getXrefs());

            CvDatabase imex = factory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
            if (imex == null){
                imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imex);
            }

            CvXrefQualifier imexSecondary = factory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( "" );
            if (imexSecondary == null){
                imexSecondary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, IMEX_SECONDARY_MI, IMEX_SECONDARY);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexSecondary);
            }

            // if it is a true PPI interaction, we can assign an IMEx id
            if (involvesOnlyProteins(interaction)){
                String interactionImexId = null;
                boolean hasConflictingImexId = false;
                boolean isUpdated = false;

                for (InteractorXref ref : interactionXrefs){
                    // imex xref
                    if (ref.getCvDatabase() != null && ref.getCvDatabase().getIdentifier() != null && ref.getCvDatabase().getIdentifier().equals(CvDatabase.IMEX_MI_REF)){
                        // imex primary xref
                        if (ref.getCvXrefQualifier() != null && ref.getCvXrefQualifier().getIdentifier() != null && ref.getCvXrefQualifier().getIdentifier().equals(CvXrefQualifier.IMEX_PRIMARY_MI_REF)){

                            // non null primary identifier
                            if (ref.getPrimaryId() != null){
                                // the interaction already have an IMEx id which is valid
                                if (interactionImexId != null){
                                    // duplicated imex primary ref, delete it
                                    if (ref.getPrimaryId().equalsIgnoreCase(interactionImexId)){
                                        interaction.removeXref(ref);
                                        xrefDao.delete(ref);
                                        isUpdated = true;
                                    }
                                    // different imex id which have already been processed in other interactions, just delete it
                                    else if (processedImexIds.contains(ref.getPrimaryId())){
                                        interaction.removeXref(ref);
                                        xrefDao.delete(ref);
                                        isUpdated = true;
                                    }
                                    // different IMEx id but the IMEx id is not a valid interaction imex id. Keep it as imex secondary
                                    else if (!existingImexIds.contains(ref.getPrimaryId())){
                                        ref.setCvXrefQualifier(imexSecondary);
                                        xrefDao.update(ref);
                                        isUpdated = true;
                                    }
                                    // different IMEx id which is valid and not already processed
                                    else {
                                        hasConflictingImexId = true;
                                    }
                                }
                                // No valid iMEx id has been found so far
                                else {
                                    // IMEx id already in use by another interaction, just delete the xref
                                    if (processedImexIds.contains(ref.getPrimaryId())){
                                        interaction.removeXref(ref);
                                        xrefDao.delete(ref);
                                        isUpdated = true;
                                    }
                                    // IMEx id not yet processed
                                    else {
                                        // valid IMEx id, register it
                                        if (existingImexIds.contains(ref.getPrimaryId()) && ref.getPrimaryId().startsWith(imexId)){
                                            interactionImexId = ref.getPrimaryId();
                                            processedImexIds.add(ref.getPrimaryId());
                                        }
                                        // conflicting IMEx id
                                        else if (existingImexIds.contains(ref.getPrimaryId()) && !ref.getPrimaryId().startsWith(imexId)){
                                            hasConflictingImexId = true;
                                        }
                                        // invalid interaction IMEx id, put it as imex secondary
                                        else {
                                            ref.setCvXrefQualifier(imexSecondary);
                                            xrefDao.update(ref);
                                            isUpdated = true;
                                        }
                                    }
                                }
                            }
                            // null primary identifier, just delete the ref
                            else {
                                interaction.removeXref(ref);
                                xrefDao.delete(ref);
                                isUpdated = true;
                            }
                        }
                    }
                }

                // need to create a new IMEx id
                if (interactionImexId == null && !hasConflictingImexId){
                    boolean updatedInteraction = updateImexIdentifierForInteraction(interaction, imexId + "-" + currentIndex);

                    if (updatedInteraction){
                        isUpdated = true;

                        if (imexCentralManager != null){
                            NewAssignedImexEvent evt = new NewAssignedImexEvent(this, null, imexId, interaction.getAc(), imexId + "-" + currentIndex);
                            imexCentralManager.fireOnNewImexAssigned(evt);
                        }

                        // increments current index
                        currentIndex++;

                        return true;
                    }
                }
                else if (hasConflictingImexId){
                    if (imexCentralManager != null){
                        ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.interaction_imex_conflict, null, imexId, null, interaction.getAc(), "Interaction " + interaction.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+"-"+currentIndex+")");
                        imexCentralManager.fireOnImexError(errorEvent);
                    }
                    else {
                        throw new PublicationImexUpdaterException("Interaction " + interaction.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+"-"+currentIndex+")");
                    }
                }

                return isUpdated;
            }
        }

        return false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean updateImexIdentifierForExperiment(Experiment intactExperiment, String imexId) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<ExperimentXref> xrefDao = daoFactory.getXrefDao(ExperimentXref.class);
        ExperimentDao expDao = daoFactory.getExperimentDao();

        if (imexId != null){
            CvDatabase imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
            if (imex == null){
                imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imex);
            }

            CvXrefQualifier imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
            if (imexPrimary == null){
                imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexPrimary);
            }

            ExperimentXref expXref = new ExperimentXref( intactExperiment.getOwner(), imex, imexId, imexPrimary );
            intactExperiment.addXref(expXref);

            xrefDao.persist(expXref);
            expDao.update(intactExperiment);
            return true;
        }
        else {
            return false;
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean updateImexIdentifierForInteraction(Interaction intactInteraction, String imexId){
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<InteractorXref> xrefDao = daoFactory.getXrefDao(InteractorXref.class);
        InteractionDao intDao = daoFactory.getInteractionDao();

        if (imexId != null){
            CvDatabase imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
            if (imex == null){
                imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imex);
            }

            CvXrefQualifier imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
            if (imexPrimary == null){
                imexPrimary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexPrimary);
            }

            InteractorXref intXref = new InteractorXref( intactInteraction.getOwner(), imex, imexId, imexPrimary );
            intactInteraction.addXref( intXref );

            xrefDao.persist(intXref);
            intDao.update((InteractionImpl) intactInteraction);
            return true;
        }
        else {
            return false;
        }
    }
}
