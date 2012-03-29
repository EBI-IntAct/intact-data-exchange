package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.*;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactImexAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
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

    private Pattern interaction_imex_regexp = Pattern.compile("(IM-[1-9][0-9]*)-([1-9][0-9]*])");
    private static String IMEX_SECONDARY_MI = "MI:0952";
    private static String IMEX_SECONDARY = "imex secondary";

    private Collection<ExperimentXref> experimentXrefs = new ArrayList<ExperimentXref>();
    private Collection<InteractorXref> interactionXrefs = new ArrayList<InteractorXref>();
    private Set<String> processedImexIds = new HashSet<String> ();

    @Transactional(propagation = Propagation.SUPPORTS)
    public void assignImexIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<PublicationXref> xrefDao = daoFactory.getXrefDao(PublicationXref.class);
        PublicationDao pubDao = daoFactory.getPublicationDao();

        String pubId = extractIdentifierFromPublication(intactPublication, imexPublication);

        imexPublication = imexCentral.getPublicationImexAccession( pubId, true );

        if (imexPublication.getImexAccession() != null){
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
        }
        else {
            throw new PublicationImexUpdaterException("Impossible to assign an IMEx identifier to publication " + intactPublication.getShortLabel());
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateImexIdentifiersForAllExperiments(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        XrefDao<ExperimentXref> xrefDao = daoFactory.getXrefDao(ExperimentXref.class);

        if (imexId != null){
            for (Experiment exp : intactPublication.getExperiments()){
                experimentXrefs.clear();
                experimentXrefs.addAll(exp.getXrefs());

                boolean hasImexId = false;
                boolean hasConflictingImexId = false;

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
                                }
                            }
                            // null primary identifier but imex id not found, just update the imex id of the ref
                            else if (!hasImexId) {
                                hasImexId = true;

                                ref.setPrimaryId(imexId);
                                xrefDao.update(ref);
                            }
                            // null primary identifier but imex id was found, just delete the ref 
                            else {
                                exp.removeXref(ref);
                                xrefDao.delete(ref);
                            }
                        }
                    }
                }

                if (!hasImexId && !hasConflictingImexId){
                    updateImexIdentifierForExperiment(exp, imexId);
                }
                else if (!hasImexId && hasConflictingImexId){
                    log.error("Experiment " + exp.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                }
            }
        }
        else {
            throw new PublicationImexUpdaterException("Impossible to update IMEx identifiers to experiments of publication " + intactPublication.getShortLabel());
        }

        experimentXrefs.clear();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> collectExistingInteractionImexIdsForPublication(uk.ac.ebi.intact.model.Publication intactPublication){
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexQuery = "select distinct x.primaryId from InteractionImpl i join i.xrefs as x join i.experiments as e join e.publication as p " +
                "where p.ac = :publicationAc and x.cvDatabase.identifier = :imex and x.cvXrefQualifier = :imexPrimary " +
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
                String pubImex = matcher.group(0);

                if (imexId.equals(pubImex)){
                    int index = Integer.parseInt(matcher.group(1));
                    if (number < index){
                        number = index;
                    }
                }
                else {
                    imexIds.remove(id);
                }
            }
        }

        return number++;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void assignImexIdentifiersForAllInteractions(uk.ac.ebi.intact.model.Publication intactPublication, String imexId) throws PublicationImexUpdaterException {

        if (imexId != null){
            List<String> existingImexIds = collectExistingInteractionImexIdsForPublication(intactPublication);
            int nextInteractionIndex = getNextImexChunkNumberAndFilterValidImexIdsFrom(existingImexIds, imexId);
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
            XrefDao<InteractorXref> xrefDao = daoFactory.getXrefDao(InteractorXref.class);

            CvDatabase imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
            if (imex == null){
                imex = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imex);
            }

            CvXrefQualifier imexSecondary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( "" );
            if (imexSecondary == null){
                imexSecondary = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.class, IMEX_SECONDARY_MI, IMEX_SECONDARY);
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(imexSecondary);
            }

            for (Experiment exp : intactPublication.getExperiments()){

                Collection<Interaction> interactions = exp.getInteractions();

                for (Interaction interaction : interactions){
                    interactionXrefs.clear();
                    interactionXrefs.addAll(interaction.getXrefs());

                    String interactionImexId = null;
                    boolean hasConflictingImexId = false;

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
                                        }
                                        // different imex id which have already been processed in other interactions, just delete it
                                        else if (processedImexIds.contains(ref.getPrimaryId())){
                                            interaction.removeXref(ref);
                                            xrefDao.delete(ref);
                                        }
                                        // different IMEx id but the IMEx id is not a valid interaction imex id. Keep it as imex secondary
                                        else if (!existingImexIds.contains(ref.getPrimaryId())){
                                            ref.setCvXrefQualifier(imexSecondary);
                                            xrefDao.update(ref);
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
                                        }
                                        // IMEx id not yet processed
                                        else {
                                            // valid IMEx id, register it
                                            if (existingImexIds.contains(ref.getPrimaryId())){
                                                interactionImexId = ref.getPrimaryId();
                                                processedImexIds.add(ref.getPrimaryId());
                                            }
                                            // invalid interaction IMEx id, put it as imex secondary
                                            else {
                                                ref.setCvXrefQualifier(imexSecondary);
                                                xrefDao.update(ref);
                                            }
                                        }
                                    }
                                }
                                // null primary identifier, just delete the ref
                                else {
                                    interaction.removeXref(ref);
                                    xrefDao.delete(ref);
                                }
                            }
                        }
                    }

                    // need to create a new IMEx id
                    if (interactionImexId != null && !hasConflictingImexId){
                        updateImexIdentifierForInteraction(interaction, imexId + "-" + nextInteractionIndex);
                        // increments the next interaction index
                        nextInteractionIndex ++;
                    }
                    else if (interactionImexId != null && hasConflictingImexId){
                        log.error("Interaction " + interaction.getShortLabel() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+"-"+nextInteractionIndex+")");
                    }
                }
            }
        }
        else {
            throw new PublicationImexUpdaterException("Impossible to update IMEx identifiers to interactions of publication " + intactPublication.getShortLabel());
        }
        
        interactionXrefs.clear();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateImexIdentifierForExperiment(Experiment intactExperiment, String imexId) throws PublicationImexUpdaterException{
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
        }
        else {
            throw new PublicationImexUpdaterException("Impossible to assign an IMEx identifier to experiment " + intactExperiment.getShortLabel());
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateImexIdentifierForInteraction(Interaction intactInteraction, String imexId) throws PublicationImexUpdaterException{
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
        }
        else {
            throw new PublicationImexUpdaterException("Impossible to assign an IMEx identifier to experiment " + intactInteraction.getShortLabel());
        }
    }
}
