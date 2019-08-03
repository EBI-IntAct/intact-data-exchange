package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The publication collector collects acs of publications in different cases
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/03/12</pre>
 */
@Service("intactPublicationCollector")
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class IntactPublicationsCollectorImpl implements IntactPublicationCollector{

    private List<String> publicationsHavingImexId;
    private List<String> publicationsWithInteractionsHavingImexId;
    private List<String> publicationsWithExperimentsHavingImexId;

    private List<String> publicationsElligibleForImex;
    private List<String> publicationsHavingImexCurationLevel;

    private List<String> publicationsAcceptedForRelease;
    private List<String> publicationsHavingUniprotDRExportNo;

    private Collection<String> publicationsInvolvingPPI;

    private static final Log log = LogFactory.getLog(IntactPublicationsCollectorImpl.class);

    private boolean isInitialised;

    private final static String CELL_JOURNAL = "Cell (0092-8674)";
    private final static String PROTEOMICS_JOURNAL = "Proteomics (1615-9853)";
    private final static String CANCER_CELL_JOURNAL= "Cancer Cell (1535-6108)";
    private final static String J_MOL_JOURNAL = "J Mol Signal.";
    private final static String ONCOGENE_JOURNAL = "Oncogene (0950-9232)";
    private final static String MOL_CANCER_JOURNAL = "Mol. Cancer";
    private final static String NAT_IMMUNO_JOURNAL = "Nat. Immunol. (1529-2908)";
    private final static String BIOCREATIVE_DATASET = "BioCreative - Critical Assessment of Information Extraction systems in Biology";

    private final static String DATASET_MI = "MI:0875";
    private final static String UNIPROT_DR_EXPORT = "uniprot-dr-export";

    public IntactPublicationsCollectorImpl(){
    }


    private List<String> collectPublicationHavingAtLeastTwoProteins() {
        List<Object[]> publicationsHavingProteinPeptide = collectPublicationsHavingProteinsOrPeptides();

        // collect all the interactions having only protein-protein or peptide interactions
        List<String> publications = collectPublicationsHavingPPIInteractions();

        Map<String, Set<String>> mapOfPublicationsAndInteractions = new HashMap<String, Set<String>>(publicationsHavingProteinPeptide.size());
        Map<String, Long> mapOfNumberParticipants = new HashMap<String, Long>(publicationsHavingProteinPeptide.size());

        Iterator<Object[]> pubIterator = publicationsHavingProteinPeptide.iterator();

        // collect interactions having more than two protein/peptides in addition to other interactors
        while (pubIterator.hasNext()){
            Object[] o = pubIterator.next();

            if (o.length == 3){
                String pubAc = (String) o[0];
                String interactionAc = (String) o[1];
                long number = (Long) o[2];

                if (mapOfPublicationsAndInteractions.containsKey(pubAc)){
                    mapOfPublicationsAndInteractions.get(pubAc).add(interactionAc);
                }
                else {
                    Set<String> interactionList = new HashSet<String>();
                    interactionList.add(interactionAc);
                    mapOfPublicationsAndInteractions.put(pubAc, interactionList);
                }

                if (mapOfNumberParticipants.containsKey(interactionAc)){
                    long newNumber = mapOfNumberParticipants.get(interactionAc)+number;
                    mapOfNumberParticipants.put(interactionAc, newNumber);
                }
                else {
                    mapOfNumberParticipants.put(interactionAc, number);
                }
            }
        }

        for (Map.Entry<String, Set<String>> pubEntry : mapOfPublicationsAndInteractions.entrySet()){
            Set<String> interactionsAcs = pubEntry.getValue();
            String pubAc = pubEntry.getKey();

            for (String interactionAc : interactionsAcs){
                if (mapOfNumberParticipants.containsKey(interactionAc) && mapOfNumberParticipants.get(interactionAc) > 0){
                    publications.add(pubAc);
                    break;
                }
            }
        }

        return publications;
    }


    private List<String> collectPublicationsElligibleForImex(){
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String publicationsToAssign = "select distinct p.ac from IntactPublication as p join p.dbAnnotations as a" +
                " where a.topic.identifier = :curation " +
                "and a.value = :imex " +
                "and ( lower(p.source.shortName) = :intact " +
                "or lower(p.source.shortName) = :i2d " +
                "or lower(p.source.shortName) = :innatedb " +
                "or lower(p.source.shortName) = :molecularConnections " +
                "or lower(p.source.shortName) = :uniprot " +
                "or lower(p.source.shortName) = :mbinfo " +
                "or lower(p.source.shortName) = :mpidb " +
                "or lower(p.source.shortName) = :mint " +
                "or lower(p.source.shortName) = :hpidb " +
                "or lower(p.source.shortName) = :dip " +
                "or lower(p.source.shortName) = :bhf" +
                ") " +
                "and p.ac not in (" +
                "select distinct p2.ac from IntactPublication as p2 join p2.dbXrefs as x where " +
                "x.database.identifier = :imexDatabase " +
                " and x.qualifier.identifier = :imexPrimary " +
                ")";

        Query query = manager.createQuery(publicationsToAssign);
        query.setParameter("curation", "MI:0955");
        query.setParameter("imex", "imex curation");
        query.setParameter("imexDatabase", Xref.IMEX_MI);
        query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);
        query.setParameter("intact", "intact");
        query.setParameter("i2d", "i2d");
        query.setParameter("innatedb", "innatedb");
        query.setParameter("molecularConnections", "molecular connections");
        query.setParameter("uniprot", "uniprot");
        query.setParameter("mbinfo", "mbinfo");
        query.setParameter("mpidb", "mpidb");
        query.setParameter("mint", "mint");
        query.setParameter("bhf", "bhf-ucl");
        query.setParameter("dip", "dip");
        query.setParameter("hpidb", "hpidb");

        return query.getResultList();
    }

    private List<String> collectPublicationAcceptedForRelease() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String datasetQuery = "select distinct p.ac from IntactPublication p join p.cvStatus as s " +
                "where s.shortName = :released or s.shortName = :accepted or s.shortName= :readyForRelease";
        Query query = manager.createQuery(datasetQuery);
        query.setParameter("released", LifeCycleStatus.RELEASED.shortLabel().toLowerCase());
        query.setParameter("readyForRelease", LifeCycleStatus.READY_FOR_RELEASE.shortLabel().toLowerCase());
        query.setParameter("accepted", LifeCycleStatus.ACCEPTED.shortLabel().toLowerCase());

        return query.getResultList();
    }

    private List<String> collectPublicationHavingUniprotDrExportNo() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String uniprotDrQuery = "select distinct p.ac from IntactPublication p join p.experiments as e join e.annotations as a " +
                "where a.topic.shortName = :uniprotDrExport and (a.value = :no or a.value = :no2)";

        Query query = manager.createQuery(uniprotDrQuery);
        query.setParameter("uniprotDrExport", UNIPROT_DR_EXPORT);
        query.setParameter("no", "no");
        query.setParameter("no2", "No");

        List<String> publications = query.getResultList();
        return publications;
    }


    private List<String> collectPublicationCandidatesToImexWithImexCurationLevel() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String datasetQuery = "select distinct p2.ac from IntactPublication p2 join p2.dbAnnotations as a3 " +
                "where a3.topic.identifier = :curation and a3.value = :imex";

        Query query = manager.createQuery(datasetQuery);
        query.setParameter("curation", "MI:0955");
        query.setParameter("imex", "imex curation");

        return query.getResultList();
    }

    public List<Object[]> collectPublicationsHavingProteinsOrPeptides() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String proteinQuery = "select p2.ac, i.ac, count(distinct c.ac) from IntactInteractionEvidence as i join i.dbExperiments as e " +
                "join e.publication as p2 join i.participants as c join c.interactor as interactor " +
                "where i.ac not in " +
                "(select distinct i2.ac from IntactParticipantEvidence c2 join c2.dbParentInteraction as i2 join c2.interactor as interactor2 " +
                " where interactor2.interactorType.identifier <> :protein and interactor2.interactorType.identifier <> :peptide)" +
                "group by p2.ac, i.ac, interactor.interactorType.identifier having (interactor.interactorType.identifier = :protein " +
                "or interactor.interactorType.identifier = :peptide) order by p2.ac, i.ac";

        Query query = manager.createQuery(proteinQuery);
        query.setParameter("protein", Protein.PROTEIN_MI);
        query.setParameter("peptide", Protein.PEPTIDE_MI);

        return query.getResultList();
    }

    private List<String> collectPublicationsHavingPPIInteractions() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();
        String datasetQuery = "select distinct p2.ac from IntactParticipantEvidence c join c.dbParentInteraction as i " +
                "join i.dbExperiments as e " +
                "join e.publication as p2 " +
                "join c.interactor as interactor " +
                "where (interactor.interactorType.identifier = :protein " +
                "or interactor.interactorType.identifier = :peptide) " +
                "and i.ac not in (select distinct i2.ac from IntactInteractionEvidence i2 " +
                "join i2.participants as comp " +
                "join comp.interactor as interactor2 " +
                "where interactor2.interactorType.identifier <> :protein and interactor2.interactorType.identifier <> :peptide)";

        Query query = manager.createQuery(datasetQuery);
        query.setParameter("protein", Protein.PROTEIN_MI);
        query.setParameter("peptide", Protein.PEPTIDE_MI);

        return query.getResultList();
    }

    private List<String> collectPublicationHavingInteractionImexIds() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();

        String imexInteractionQuery = "select distinct p3.ac from IntactInteractionEvidence i join i.dbExperiments as e " +
                "join e.publication as p3 join i.dbXrefs as x " +
                "where x.database.identifier = :imex and x.qualifier.identifier = :imexPrimary";

        Query query = manager.createQuery(imexInteractionQuery);
        query.setParameter("imex", Xref.IMEX_MI);
        query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);

        return query.getResultList();
    }

    private List<String> collectPublicationHavingExperimentImexIds() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();

        String imexExperimentQuery = "select distinct p4.ac from IntactExperiment e2 join e2.publication as p4 join e2.xrefs as x2 " +
                "where x2.database.identifier = :imex and x2.qualifier.identifier = :imexPrimary";

        Query query = manager.createQuery(imexExperimentQuery);
        query.setParameter("imex", Xref.IMEX_MI);
        query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);

        return query.getResultList();
    }

    private List<String> collectPublicationsHavingImexIds() {
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        EntityManager manager = intactDao.getEntityManager();

        String imexPublicationQuery = "select distinct p5.ac from IntactPublication p5 join p5.dbXrefs as x3 " +
                "where x3.database.identifier = :imex and x3.qualifier.identifier = :imexPrimary";

        Query query = manager.createQuery(imexPublicationQuery);
        query.setParameter("imex", Xref.IMEX_MI);
        query.setParameter("imexPrimary", Xref.IMEX_PRIMARY_MI);

        return query.getResultList();
    }

    public Collection<String> getPublicationsInvolvingPPI() {
        return publicationsInvolvingPPI;
    }

    public void setPublicationsInvolvingPPI(Collection<String> publicationsInvolvingPPI) {
        this.publicationsInvolvingPPI = publicationsInvolvingPPI;
    }

    public List<String> getPublicationsHavingImexId() {
        return publicationsHavingImexId;
    }

    public List<String> getPublicationsWithInteractionsHavingImexId() {
        return publicationsWithInteractionsHavingImexId;
    }

    public List<String> getPublicationsWithExperimentsHavingImexId() {
        return publicationsWithExperimentsHavingImexId;
    }

    public List<String> getPublicationsElligibleForImex() {
        return publicationsElligibleForImex;
    }

    public List<String> getPublicationsHavingImexCurationLevel() {
        return publicationsHavingImexCurationLevel;
    }

    public List<String> getPublicationsAcceptedForRelease() {
        return publicationsAcceptedForRelease;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsNeedingAnImexId() {

        if (!isInitialised){
            initialise();
        }

        // publications come from IMEx databases, has imex curation depth and does not have IMEx id
        Collection<String> potentialPublicationsForImex = publicationsElligibleForImex;

        // filter publications not accepted
        Collection<String> publicationsToBeAssignedFiltered = CollectionUtils.intersection(potentialPublicationsForImex, publicationsAcceptedForRelease);

        return publicationsToBeAssignedFiltered;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsHavingIMExIdToUpdate() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex id plus imex curation level
        Collection<String> publicationsWithIMExIdToUpdate = CollectionUtils.intersection(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        // filters publications having imex id but no PPI
        //Collection<String> publicationsWithIMExIdToUpdateFiltered = CollectionUtils.intersection(publicationsWithIMExIdToUpdate, publicationsInvolvingPPI);

        return publicationsWithIMExIdToUpdate;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex id but not imex curation level
        Collection<String> publicationsWithImexAndNotImexCurationLevel = CollectionUtils.subtract(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        return publicationsWithImexAndNotImexCurationLevel;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex curation level but no IMEx id
        Collection<String> publicationsWithImexCurationLevelAndNotImexId = CollectionUtils.subtract(publicationsHavingImexCurationLevel, publicationsHavingImexId);

        // publications having date > 2006, comes from IMEx partner
        Collection<String> potentialPublicationsForImex = publicationsElligibleForImex;

        // publications having only non PPI interactions should be excluded
        //Collection<String> potentialPublicationsForImexFiltered = CollectionUtils.intersection(potentialPublicationsForImex, publicationsInvolvingPPI);

        // publications having imex curation level but are not eligible for automatic IMEx assignment
        Collection<String> publicationsNotEligibleForImexWithImexCurationLevel = CollectionUtils.subtract(publicationsWithImexCurationLevelAndNotImexId, potentialPublicationsForImex);

        return publicationsNotEligibleForImexWithImexCurationLevel;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsWithoutImexButWithExperimentImex() {

        if (!isInitialised){
            initialise();
        }

        // publications without IMEx id but with experiment having IMEx id
        Collection<String> publicationsWithExperimentImexButNoImexId = CollectionUtils.subtract(publicationsWithExperimentsHavingImexId, publicationsHavingImexId);

        return publicationsWithExperimentImexButNoImexId;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsWithoutImexButWithInteractionImex() {

        if (!isInitialised){
            initialise();
        }

        // publications without IMEx id but with interaction having IMEx id
        Collection<String> publicationsWithInteractionImexButNoImexId = CollectionUtils.subtract(publicationsWithInteractionsHavingImexId, publicationsHavingImexId);

        return publicationsWithInteractionImexButNoImexId;
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsHavingIMExIdAndNoPPI() {
        if (!isInitialised){
            initialise();
        }

        Collection<String> publicationsNoPPI = CollectionUtils.subtract(publicationsHavingImexId, publicationsInvolvingPPI);

        return CollectionUtils.intersection(publicationsNoPPI, publicationsAcceptedForRelease);
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Collection<String> getPublicationsHavingIMExCurationLevelAndUniprotDrExportNo() {
        if (!isInitialised){
            initialise();
        }

        Collection<String> publicationsWithoutImexButImexCurationLevel = CollectionUtils.subtract(publicationsHavingImexCurationLevel, publicationsHavingImexId);

        return CollectionUtils.intersection(publicationsWithoutImexButImexCurationLevel, publicationsHavingUniprotDRExportNo);
    }

    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public void initialise() {
        if (publicationsHavingImexId == null){
            publicationsHavingImexId = collectPublicationsHavingImexIds();
        }
        if (publicationsWithInteractionsHavingImexId == null){
            publicationsWithInteractionsHavingImexId = collectPublicationHavingInteractionImexIds();
        }
        if (publicationsWithExperimentsHavingImexId == null){
            publicationsWithExperimentsHavingImexId = collectPublicationHavingExperimentImexIds();
        }
        if (publicationsElligibleForImex == null){
            publicationsElligibleForImex = collectPublicationsElligibleForImex();
        }
        if (publicationsHavingImexCurationLevel == null){
            publicationsHavingImexCurationLevel = collectPublicationCandidatesToImexWithImexCurationLevel();
        }
        if (publicationsAcceptedForRelease == null){
            publicationsAcceptedForRelease = collectPublicationAcceptedForRelease();
        }
        if (publicationsInvolvingPPI == null){

            publicationsInvolvingPPI = collectPublicationHavingAtLeastTwoProteins();
        }
        if (publicationsHavingUniprotDRExportNo == null){

            publicationsHavingUniprotDRExportNo = collectPublicationHavingUniprotDrExportNo();
        }

        isInitialised = true;
    }
}
