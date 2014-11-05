package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.bridges.imex.ImexCentralClient;
import psidev.psi.mi.jami.enricher.exception.EnricherException;
import psidev.psi.mi.jami.imex.actions.impl.ImexAssignerImpl;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactImexAssigner;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorType;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.service.ExperimentService;
import uk.ac.ebi.intact.jami.service.InteractionEvidenceService;
import uk.ac.ebi.intact.jami.synchronizer.FinderException;
import uk.ac.ebi.intact.jami.synchronizer.PersisterException;
import uk.ac.ebi.intact.jami.synchronizer.SynchronizerException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;

/**
 * This class will assign an IMEx id to a publication using imex central webservice and update experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/03/12</pre>
 */
public class IntactImexAssignerImpl extends ImexAssignerImpl implements IntactImexAssigner{

    private static final Log log = LogFactory.getLog(IntactImexAssignerImpl.class);

    public IntactImexAssignerImpl(ImexCentralClient client) {
        super(client);
    }


    @Override
    public void resetPublicationContext(IntactPublication pub, String imexId) {
        setCurrentIndex(getNextImexChunkNumberAndFilterValidImexIdsFrom(pub));
    }

    @Transactional(value = "jamiTransationManager", propagation = Propagation.REQUIRED)
    public void assignImexIdentifierToExperiments(Collection<String> expAcs, String imexId, ImexCentralManager imexCentralManager,
                                                  Set<String> updatedExpAcs) throws PublicationImexUpdaterException {
        if (expAcs != null && !expAcs.isEmpty() && imexId != null){
            ExperimentService expService = ApplicationContextProvider.getBean("experimentService");
            // fetch all experiments
            Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("acs", expAcs);
            List<Experiment> experiments = expService.fetchIntactObjects("select e from IntactExperiment e where e.ac in (acs)",
                    parameters, 0, Integer.MAX_VALUE);

            for (Experiment exp : experiments){

                if (exp != null){
                    log.info("Processing experiment " + ((IntactExperiment) exp).getAc());

                    try {
                        if (super.updateImexIdentifierForExperiment(exp, imexId)){
                            // report updated experiments
                            updatedExpAcs.add(((IntactExperiment) exp).getAc());

                            // synchronize experiment
                            expService.saveOrUpdate(exp);
                        }
                    } catch (EnricherException e) {
                        if (imexCentralManager != null){
                            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.experiment_imex_conflict, null, imexId,
                                    ((IntactExperiment)exp).getAc(), null,
                                    "Experiment " + ((IntactExperiment) exp).getAc() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                            imexCentralManager.fireOnImexError(errorEvent);
                        }
                        else {
                            throw new PublicationImexUpdaterException("Experiment " + ((IntactExperiment) exp).getAc() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                        }
                    } catch (FinderException e) {
                        throw new PublicationImexUpdaterException("Experiment " + ((IntactExperiment) exp).getAc() + " cannot be updated", e);
                    } catch (SynchronizerException e) {
                        throw new PublicationImexUpdaterException("Experiment " + ((IntactExperiment) exp).getAc() + " cannot be updated", e);
                    } catch (PersisterException e) {
                        throw new PublicationImexUpdaterException("Experiment " + ((IntactExperiment) exp).getAc() + " cannot be updated", e);
                    }
                }
            }
        }
    }

    @Transactional(value = "jamiTransationManager", propagation = Propagation.REQUIRED)
    public List<String> collectExistingInteractionImexIdsForPublication(IntactPublication intactPublication){
        InteractionEvidenceService intService = ApplicationContextProvider.getBean("interactionEvidenceService");
        // fetch all interaction evidences
        Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("publicationAc", intactPublication.getAc());
        parameters.put("imex", Xref.IMEX_MI);
        parameters.put("imexPrimary", Xref.IMEX_PRIMARY_MI);

        // collect all interaction evidences attached to this publication and having an IMEx id
        Iterator<InteractionEvidence> interactionsIterator = intService.iterateAll(
                "select count(distinct i.ac) from IntactInteractionEvidence i join i.dbXrefs as x join i.experiments as e join e.publication as p " +
                        "where p.ac = :publicationAc " +
                        "and x.database.identifier = :imex " +
                        "and x.qualifier.identifier = :imexPrimary " +
                        "order by x.id",
                "select distinct i from IntactInteractionEvidence i join i.dbXrefs as x join i.experiments as e join e.publication as p " +
                        "where p.ac = :publicationAc " +
                        "and x.database.identifier = :imex " +
                        "and x.qualifier.identifier = :imexPrimary " +
                        "order by x.id",
                parameters);

        List<String> imexIds = new ArrayList<String>();
        while(interactionsIterator.hasNext()){
            InteractionEvidence interaction = interactionsIterator.next();
            imexIds.add(interaction.getImexId());
        }


        return imexIds;
    }

    @Override
    @Transactional(value = "jamiTransationManager", propagation = Propagation.REQUIRED)
    public int getNextImexChunkNumberAndFilterValidImexIdsFrom(Publication publication){
        if (publication instanceof IntactPublication){
            int number = 0;
            List<String> ids = new ArrayList<String>(collectExistingInteractionImexIdsForPublication((IntactPublication)publication));

            for (String id : ids){
                Matcher matcher = getInteraction_imex_regexp().matcher(id);

                if (matcher.find()){
                    String pubImex = matcher.group(1);

                    // valid imex id in sync with publication
                    if (publication.getImexId() != null && publication.getImexId().equals(pubImex)){
                        int index = Integer.parseInt(matcher.group(2));
                        if (number < index){
                            number = index;
                        }
                    }
                }
            }

            return number+1;
        }
        else{
            return super.getNextImexChunkNumberAndFilterValidImexIdsFrom(publication);
        }
    }

    @Transactional(value = "jamiTransationManager", propagation = Propagation.REQUIRED)
    public void assignImexIdentifierToInteractions(Collection<String> interactionAcs, String imexId, ImexCentralManager imexCentralManager,
                                                   Set<String> updatedInteractionAcs) throws PublicationImexUpdaterException {
        if (interactionAcs != null && !interactionAcs.isEmpty() && imexId != null){
            InteractionEvidenceService intService = ApplicationContextProvider.getBean("interactionEvidenceService");
            // fetch all interaction evidences
            Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("acs", interactionAcs);

            // collect all interaction evidences
            List<InteractionEvidence> interactions = intService.fetchIntactObjects(
                    "select i from IntactInteractionEvidence i " +
                            "where i.ac in (:acs)",
                    parameters,
                    0,
                    Integer.MAX_VALUE
            );

            for (InteractionEvidence interaction : interactions){

                if (interaction != null){
                    log.info("Processing interaction " + ((IntactInteractionEvidence) interaction).getAc());

                    try {
                        if (super.updateImexIdentifierForInteraction(interaction, imexId)){
                            // report updated interactions
                            updatedInteractionAcs.add(((IntactInteractionEvidence) interaction).getAc());
                            if (imexCentralManager != null){
                                NewAssignedImexEvent evt = new NewAssignedImexEvent(this, null, imexId, ((IntactInteractionEvidence) interaction).getAc(),
                                        imexId + "-" + getCurrentIndex());
                                imexCentralManager.fireOnNewImexAssigned(evt);
                            }

                            // update currentIndex
                            setCurrentIndex(getCurrentIndex()+1);

                            // synchronize interaction
                            intService.saveOrUpdate(interaction);
                        }
                    } catch (EnricherException e) {
                        if (imexCentralManager != null){
                            ImexErrorEvent errorEvent = new ImexErrorEvent(this, ImexErrorType.interaction_imex_conflict, null, imexId, null,
                                    ((IntactInteractionEvidence) interaction).getAc(), "Interaction " + interaction.getShortName()
                                    + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "
                                    +imexId+"-"+getCurrentIndex()+")");
                            imexCentralManager.fireOnImexError(errorEvent);
                        }
                        else {
                            throw new PublicationImexUpdaterException("Interaction " + ((IntactInteractionEvidence) interaction).getAc() + " cannot be updated because of IMEx identifier conflicts (has another IMEx primary ref than "+imexId+")");
                        }
                    } catch (FinderException e) {
                        throw new PublicationImexUpdaterException("Interaction " + ((IntactInteractionEvidence) interaction).getAc() + " cannot be updated", e);
                    } catch (SynchronizerException e) {
                        throw new PublicationImexUpdaterException("Interaction " + ((IntactInteractionEvidence) interaction).getAc() + " cannot be updated", e);
                    } catch (PersisterException e) {
                        throw new PublicationImexUpdaterException("Interaction " + ((IntactInteractionEvidence) interaction).getAc() + " cannot be updated", e);
                    }
                }
            }
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public List<String> collectExperimentsToUpdateFrom(IntactPublication pub, String imex){
        if (pub != null && pub.getAc() != null && imex != null){
            IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
            EntityManager manager = intactDao.getEntityManager();

            String datasetQuery = "select distinct e.ac from IntactExperiment e " +
                    "where e.publication.ac = :pubAc and (e.ac not in " +
                    "(select e2.ac from IntactExperiment e2 join e2.xrefs as x2 where e2.publication.ac = :pubAc and x2.database.identifier = :imex " +
                    "and x2.qualifier.identifier = :imexPrimary and x2.id = :imexId) or " +
                    "e.ac in (select e3.ac from IntactExperiment e3 join e3.xrefs as x3 where e3.publication.ac = :pubAc and x3.database.identifier = :imex " +
                    "and x3.qualifier.identifier = :imexPrimary and x3.id <> :imexId))";

            Query query = manager.createQuery(datasetQuery);
            query.setParameter("pubAc", ((IntactPublication)pub).getAc());
            query.setParameter("imex", Xref.IMEX_MI);
            query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);
            query.setParameter("imexId", imex);

            return query.getResultList();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public List<String> collectInteractionsToUpdateFrom(IntactPublication pub, String imex){
        if (pub != null && pub.getAc() != null && imex != null){
            IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
            EntityManager manager = intactDao.getEntityManager();

            String datasetQuery = "select distinct i.ac from IntactInteractionEvidence i join i.experiments as e " +
                    "where e.publication.ac = :pubAc and (i.ac not in " +
                    "(select i2.ac from IntactInteractionEvidence i2 join i2.dbXrefs as x2 join i2.experiments as e2 " +
                    "where e2.publication.ac = :pubAc and x2.database.identifier = :imex " +
                    "and x2.database.identifier = :imexPrimary and x2.id like :imexId) or " +
                    "i.ac in (select i3.ac from IntactInteractionEvidence i3 join i3.dbXrefs as x3 join i3.experiments as e3 " +
                    "where e3.publication.ac = :pubAc and x3.database.identifier = :imex " +
                    "and x3.qualifier.identifier = :imexPrimary and x3.id not like :imexId))";

            Query query = manager.createQuery(datasetQuery);
            query.setParameter("pubAc", pub.getAc());
            query.setParameter("imex", Xref.IMEX_MI);
            query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);
            query.setParameter("imexId", imex+"-%");

            return query.getResultList();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }
}
