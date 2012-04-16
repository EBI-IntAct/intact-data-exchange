package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.model.*;

import javax.persistence.Query;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The publication collector collects acs of publications in different cases
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/03/12</pre>
 */

public class IntactPublicationsCollectorImpl implements IntactPublicationCollector{

    private List<String> publicationsHavingImexId;
    private List<String> publicationsWithInteractionsHavingImexId;
    private List<String> publicationsWithExperimentsHavingImexId;

    private List<String> publicationsHavingJournalAndYear;
    private List<String> publicationsHavingDataset;
    private List<String> publicationsHavingImexCurationLevel;

    private List<String> publicationsAcceptedForRelease;

    private Collection<String> publicationsInvolvingOnlyNonPPIInteractions;

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

    public IntactPublicationsCollectorImpl(){
    }

    private List<String> collectPublicationCandidatesToImexWithJournalAndDate() {
        List<Object[]> publicationJournalsAndYear = collectYearAndJournalFromPublicationEligibleImex();

        List<String> publications = new ArrayList<String>(publicationJournalsAndYear.size());

        for (Object[] o : publicationJournalsAndYear){
            if (o.length == 3){
                String pubAc = (String) o[0];
                String journal = (String) o[1];
                try{
                    int date = Integer.parseInt((String) o[2]);
                    
                    if (CELL_JOURNAL.equalsIgnoreCase(journal) || PROTEOMICS_JOURNAL.equalsIgnoreCase(journal)
                            || CANCER_CELL_JOURNAL.equalsIgnoreCase(journal) || J_MOL_JOURNAL.equalsIgnoreCase(journal)
                            || ONCOGENE_JOURNAL.equalsIgnoreCase(journal)){
                        if (date >= 2006){
                            publications.add(pubAc);
                        }
                    }
                    else if (MOL_CANCER_JOURNAL.equalsIgnoreCase(journal)){
                        if (date >= 2010){
                            publications.add(pubAc);
                        }
                    }
                    else if (NAT_IMMUNO_JOURNAL.equalsIgnoreCase(journal)){
                        if (date >= 2011){
                            publications.add(pubAc);
                        }
                    }
                }catch (NumberFormatException e){
                    log.error("Publication date for " + pubAc + "is not valid and is skipped.");
                }

            }
        }

        return publications;
    }

    /**
     *
     * @return list of object[] which are String[3] with publication ac, jounal and year of publication
     * @throws ParseException
     */
    private List<Object[]> collectYearAndJournalFromPublicationEligibleImex() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String journalDateQuery = "select distinct p.ac, a1.annotationText, a2.annotationText from Publication p left join p.annotations as a1 join p.annotations as a2 " +
                "where a1.cvTopic.identifier = :journal and a2.cvTopic.identifier = :dateJournal " +
                "and " +
                "(" +
                "a1.annotationText = :cell or a1.annotationText = :proteomics or a1.annotationText = :cancer " +
                "or a1.annotationText = :molSignal or a1.annotationText = :oncogene or a1.annotationText = :molCancer " +
                "or a1.annotationText = :natImmuno" +
                ")";

        Query query = daoFactory.getEntityManager().createQuery(journalDateQuery);
        query.setParameter("journal", CvTopic.JOURNAL_MI_REF);
        query.setParameter("dateJournal", CvTopic.PUBLICATION_YEAR_MI_REF);
        query.setParameter("cell", CELL_JOURNAL);
        query.setParameter("proteomics", PROTEOMICS_JOURNAL);
        query.setParameter("cancer", CANCER_CELL_JOURNAL);
        query.setParameter("molSignal", J_MOL_JOURNAL);
        query.setParameter("oncogene", ONCOGENE_JOURNAL);
        query.setParameter("molCancer", MOL_CANCER_JOURNAL);
        query.setParameter("natImmuno", NAT_IMMUNO_JOURNAL);

        List<Object[]> publications = query.getResultList();

        return publications;
    }

    private List<String> collectPublicationAcceptedForRelease() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p.ac from Publication p join p.status as s " +
                "where s.identifier = :released or s.identifier = :accepted or s.identifier= :readyForRelease " +
                "or s.identifier = :acceptedOnHold";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("released", CvPublicationStatusType.RELEASED.identifier());
        query.setParameter("readyForRelease", CvPublicationStatusType.READY_FOR_RELEASE.identifier());
        query.setParameter("accepted", CvPublicationStatusType.ACCEPTED.identifier());
        query.setParameter("acceptedOnHold", CvPublicationStatusType.ACCEPTED_ON_HOLD.identifier());

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationCandidatesToImexWithDataset() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p2.ac from Publication p2 join p2.annotations as a3 " +
                "where a3.cvTopic.identifier = :dataset and a3.annotationText = :biocreative";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("dataset", CvTopic.DATASET_MI_REF);
        query.setParameter("biocreative", BIOCREATIVE_DATASET);

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationCandidatesToImexWithImexCurationLevel() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p2.ac from Publication p2 join p2.annotations as a3 " +
                "where a3.cvTopic.identifier = :curation and a3.annotationText = :imex";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("curation", "MI:0955");
        query.setParameter("imex", "imex curation");

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationsHavingNonPPIInteractions() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p1.ac from Component c join c.interaction as i join i.experiments as e join e.publication as p1 join c.interactor as interactor " +
                "where interactor.objClass <> :protein";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("protein", ProteinImpl.class.getCanonicalName());

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationsHavingPPIInteractions() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p2.ac from Component c join c.interaction as i join i.experiments as e join e.publication as p2 join c.interactor as interactor " +
                "where (interactor.cvInteractorType.identifier = :protein or interactor.cvInteractorType.identifier = :peptide) and i.ac not in (select distinct i2.ac from InteractionImpl i2 join i2.components as comp join comp.interactor as interactor2 " +
                "where interactor2.cvInteractorType.identifier <> :protein and interactor2.cvInteractorType.identifier <> :peptide)";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("protein", CvInteractorType.PROTEIN_MI_REF);
        query.setParameter("peptide", CvInteractorType.PEPTIDE_MI_REF);

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationHavingInteractionImexIds() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexInteractionQuery = "select distinct p3.ac from InteractionImpl i join i.experiments as e join e.publication as p3 join i.xrefs as x " +
                "where x.cvDatabase.identifier = :imex and x.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexInteractionQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationHavingExperimentImexIds() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexExperimentQuery = "select distinct p4.ac from Experiment e2 join e2.publication as p4 join e2.xrefs as x2 " +
                "where x2.cvDatabase.identifier = :imex and x2.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexExperimentQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        return publications;
    }

    private List<String> collectPublicationsHavingImexIds() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexPublicationQuery = "select distinct p5.ac from Publication p5 join p5.xrefs as x3 " +
                "where x3.cvDatabase.identifier = :imex and x3.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexPublicationQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        return publications;
    }

    public Collection<String> getPublicationsInvolvingOnlyNonPPIInteractions() {
        return publicationsInvolvingOnlyNonPPIInteractions;
    }

    public void setPublicationsInvolvingOnlyNonPPIInteractions(Collection<String> publicationsInvolvingOnlyNonPPIInteractions) {
        this.publicationsInvolvingOnlyNonPPIInteractions = publicationsInvolvingOnlyNonPPIInteractions;
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

    public List<String> getPublicationsHavingJournalAndYear() {
        return publicationsHavingJournalAndYear;
    }

    public List<String> getPublicationsHavingDataset() {
        return publicationsHavingDataset;
    }

    public List<String> getPublicationsHavingImexCurationLevel() {
        return publicationsHavingImexCurationLevel;
    }

    public List<String> getPublicationsAcceptedForRelease() {
        return publicationsAcceptedForRelease;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsNeedingAnImexId() {

        if (!isInitialised){
            initialise();
        }

        // publications having specific journal and specific dataset can be IMEx publications
        Collection<String> potentialPublicationsForImex = CollectionUtils.union(publicationsHavingDataset, publicationsHavingJournalAndYear);

        // publications having only non PPI interactions should be excluded
        Collection<String> potentialPublicationsForImexFiltered = CollectionUtils.subtract(potentialPublicationsForImex, publicationsInvolvingOnlyNonPPIInteractions);

        // potentialPublications needing an IMEx id to be assigned = potential publications for imex - publications already having IMEx ids
        Collection<String> potentialPublicationsToBeAssigned = CollectionUtils.subtract(potentialPublicationsForImexFiltered, publicationsHavingImexId);

        // publications for which we can assign a new IMEx id = potentialPublications needing an IMEx id to be assigned AND publications having IMEx curation level
        Collection<String> publicationsToBeAssigned = CollectionUtils.intersection(potentialPublicationsToBeAssigned, publicationsHavingImexCurationLevel);

        // filter publications not accepted
        Collection<String> publicationsToBeAssignedFiltered = CollectionUtils.intersection(publicationsToBeAssigned, publicationsAcceptedForRelease);

        return publicationsToBeAssignedFiltered;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsHavingIMExIdToUpdate() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex id plus imex curation level
        Collection<String> publicationsWithIMExIdToUpdate = CollectionUtils.intersection(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        // filters publications having imex id but no PPI
        Collection<String> publicationsWithIMExIdToUpdateFiltered = CollectionUtils.subtract(publicationsWithIMExIdToUpdate, publicationsInvolvingOnlyNonPPIInteractions);

        return publicationsWithIMExIdToUpdateFiltered;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex id but not imex curation level
        Collection<String> publicationsWithImexAndNotImexCurationLevel = CollectionUtils.subtract(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        return publicationsWithImexAndNotImexCurationLevel;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex() {

        if (!isInitialised){
            initialise();
        }

        // publications having imex curation level but no IMEx id
        Collection<String> publicationsWithImexCurationLevelAndNotImexId = CollectionUtils.subtract(publicationsHavingImexCurationLevel, publicationsHavingImexId);

        // publications having specific journal and specific dataset can be IMEx publications
        Collection<String> potentialPublicationsForImex = CollectionUtils.union(publicationsHavingDataset, publicationsHavingJournalAndYear);

        // publications having only non PPI interactions should be excluded
        Collection<String> potentialPublicationsForImexFiltered = CollectionUtils.subtract(potentialPublicationsForImex, publicationsInvolvingOnlyNonPPIInteractions);

        // publications having imex curation level but are not eligible for automatic IMEx assignment
        Collection<String> publicationsNotEligibleForImexWithImexCurationLevel = CollectionUtils.subtract(publicationsWithImexCurationLevelAndNotImexId, potentialPublicationsForImexFiltered);

        return publicationsNotEligibleForImexWithImexCurationLevel;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsWithoutImexButWithExperimentImex() {

        if (!isInitialised){
            initialise();
        }

        // publications without IMEx id but with experiment having IMEx id
        Collection<String> publicationsWithExperimentImexButNoImexId = CollectionUtils.subtract(publicationsWithExperimentsHavingImexId, publicationsHavingImexId);

        return publicationsWithExperimentImexButNoImexId;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsWithoutImexButWithInteractionImex() {

        if (!isInitialised){
            initialise();
        }

        // publications without IMEx id but with interaction having IMEx id
        Collection<String> publicationsWithInteractionImexButNoImexId = CollectionUtils.subtract(publicationsWithInteractionsHavingImexId, publicationsHavingImexId);

        return publicationsWithInteractionImexButNoImexId;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Collection<String> getPublicationsHavingIMExIdAndNoPPI() {
        if (!isInitialised){
            initialise();
        }

        return CollectionUtils.intersection(publicationsHavingImexId, publicationsInvolvingOnlyNonPPIInteractions);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
        if (publicationsHavingDataset == null){
            publicationsHavingDataset = collectPublicationCandidatesToImexWithDataset();
        }
        if (publicationsHavingJournalAndYear == null){
            publicationsHavingJournalAndYear = collectPublicationCandidatesToImexWithJournalAndDate();
        }
        if (publicationsHavingImexCurationLevel == null){
            publicationsHavingImexCurationLevel = collectPublicationCandidatesToImexWithImexCurationLevel();
        }
        if (publicationsAcceptedForRelease == null){
            publicationsAcceptedForRelease = collectPublicationAcceptedForRelease();
        }
        if (publicationsInvolvingOnlyNonPPIInteractions == null){
            Collection<String> proteinsInvolvingPPI = collectPublicationsHavingPPIInteractions();
            Collection<String> proteinsInvolvingNonPPI = collectPublicationsHavingNonPPIInteractions();

            publicationsInvolvingOnlyNonPPIInteractions = CollectionUtils.subtract(proteinsInvolvingNonPPI, proteinsInvolvingPPI);
        }

        isInitialised = true;
    }
}
