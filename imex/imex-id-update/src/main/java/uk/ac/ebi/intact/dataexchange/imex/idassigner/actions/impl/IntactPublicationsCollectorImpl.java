package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.IntactPublicationCollector;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import javax.persistence.Query;
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

    public IntactPublicationsCollectorImpl(){
        publicationsHavingImexId = collectPublicationsHavingImexIds();
        publicationsWithInteractionsHavingImexId = collectPublicationHavingInteractionImexIds();
        publicationsWithExperimentsHavingImexId = collectPublicationHavingExperimentImexIds();
        publicationsHavingDataset = collectPublicationCandidatesToImexWithDataset();
        publicationsHavingJournalAndYear = collectPublicationCandidatesToImexWithJournalAndDate();
        publicationsHavingImexCurationLevel = collectPublicationCandidatesToImexWithImexCurationLevel();
    }

    private List<String> collectPublicationCandidatesToImexWithJournalAndDate() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String journalDateQuery = "select distinct p.ac from Publication p left join p.annotations as a1 join p.annotations as a2 " +
                "where a1.cvTopic.identifier = :journal and a2.cvTopic.identifier = :dateJournal " +
                "and ((a1.annotationText = :cell or a1.annotationText = :proteomics " +
                "or a1.annotationText = :cancer or a1.annotationText = :molSignal)  " +
                "and a2.annotationText = :year1) " +
                "or " +
                "(a1.annotationText = :oncogene and a2.annotationText = :year1) " +
                "or " +
                "((a1.annotationText = :molCancer or a1.annotationText = :molSignal) and a2.annotationText = :year2) " +
                "or " +
                "(a1.annotationText = :natImmuno and a2.annotationText = :year3) ";

        Query query = daoFactory.getEntityManager().createQuery(journalDateQuery);
        query.setParameter("journal", CvTopic.JOURNAL_MI_REF);
        query.setParameter("dateJournal", CvTopic.PUBLICATION_YEAR_MI_REF);
        query.setParameter("cell", "Cell (0092-8674)");
        query.setParameter("proteomics", "Proteomics (1615-9853)");
        query.setParameter("cancer", "Cancer Cell (1535-6108)");
        query.setParameter("molSignal", "J Mol Signal.");
        query.setParameter("year1", "2006");
        query.setParameter("oncogene", "Oncogene (0950-9232)");
        query.setParameter("molCancer", "Mol. Cancer");
        query.setParameter("year2", "2010");
        query.setParameter("natImmuno", "Nat. Immunol. (1529-2908)");
        query.setParameter("year3", "2011");

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );

        return publications;
    }

    private List<String> collectPublicationCandidatesToImexWithDataset() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p2.ac from Publication p2 join p2.annotations as a3 " +
                "where a3.cvTopic.identifier = :dataset and a3.annotationText = :biocreative";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("dataset", CvTopic.DATASET_MI_REF);
        query.setParameter("biocreative", "BioCreative - Critical Assessment of Information Extraction systems in Biology");

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
        return publications;
    }

    private List<String> collectPublicationCandidatesToImexWithImexCurationLevel() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String datasetQuery = "select distinct p2.ac from Publication p2 join p2.annotations as a3 " +
                "where a3.cvTopic.identifier = :curation and a3.annotationText = :imex";

        Query query = daoFactory.getEntityManager().createQuery(datasetQuery);
        query.setParameter("curation", "MI:0955");
        query.setParameter("imex", "imex curation");

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
        return publications;
    }

    private List<String> collectPublicationHavingInteractionImexIds() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexInteractionQuery = "select distinct p3.ac from InteractionImpl i join i.experiments as e join e.publication as p3 join i.xrefs as x " +
                "where x.cvDatabase.identifier = :imex and x.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexInteractionQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
        return publications;
    }

    private List<String> collectPublicationHavingExperimentImexIds() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexExperimentQuery = "select distinct p4.ac from Experiment e2 join e2.publication as p4 join e2.xrefs as x2 " +
                "where x2.cvDatabase.identifier = :imex and x2.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexExperimentQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
        return publications;
    }

    private List<String> collectPublicationsHavingImexIds() {
        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        String imexPublicationQuery = "select distinct p5.ac from Publication p5 join p5.xrefs as x3 " +
                "where x3.cvDatabase.identifier = :imex and x3.cvXrefQualifier.identifier = :imexPrimary";

        Query query = daoFactory.getEntityManager().createQuery(imexPublicationQuery);
        query.setParameter("imex", CvDatabase.IMEX_MI_REF);
        query.setParameter("imexPrimary", CvXrefQualifier.IMEX_PRIMARY_MI_REF);

        List<String> publications = query.getResultList();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
        return publications;
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
    
    public Collection<String> getPublicationsNeedingAnImexId(){

        // publications having specific journal and specific dataset can be IMEx publications
        Collection<String> potentialPublicationsForImex = CollectionUtils.union(publicationsHavingDataset, publicationsHavingJournalAndYear);

        // potentialPublications needing an IMEx id to be assigned = potential publications for imex - publications already having IMEx ids
        Collection<String> potentialPublicationsToBeAssigned = CollectionUtils.subtract(potentialPublicationsForImex, publicationsHavingImexId);

        // publications for which we can assign a new IMEx id = potentialPublications needing an IMEx id to be assigned AND publications having IMEx curation level
        Collection<String> publicationsToBeAssigned = CollectionUtils.intersection(potentialPublicationsForImex, publicationsHavingImexCurationLevel);

        return publicationsToBeAssigned;
    }

    public Collection<String> getPublicationsHavingIMExIdToUpdate() {

        // publications having imex id plus imex curation level
        Collection<String> publicationsWithIMExIdToUpdate = CollectionUtils.intersection(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        return publicationsWithIMExIdToUpdate;
    }

    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel() {

        // publications having imex id but not imex curation level
        Collection<String> publicationsWithImexAndNotImexCurationLevel = CollectionUtils.subtract(publicationsHavingImexId, publicationsHavingImexCurationLevel);

        return publicationsWithImexAndNotImexCurationLevel;
    }

    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex() {

        // publications having imex curation level but no IMEx id
        Collection<String> publicationsWithImexCurationLevelAndNotImexId = CollectionUtils.subtract(publicationsHavingImexCurationLevel, publicationsHavingImexId);

        // publications having specific journal and specific dataset can be IMEx publications
        Collection<String> potentialPublicationsForImex = CollectionUtils.union(publicationsHavingDataset, publicationsHavingJournalAndYear);

        // publications having imex curation level but are not eligible for automatic IMEx assignment
        Collection<String> publicationsNotEligibleForImexWithImexCurationLevel = CollectionUtils.subtract(publicationsWithImexCurationLevelAndNotImexId, potentialPublicationsForImex);

        return publicationsNotEligibleForImexWithImexCurationLevel;
    }

    public Collection<String> getPublicationsWithoutImexButWithExperimentImex() {

        // publications without IMEx id but with experiment having IMEx id
        Collection<String> publicationsWithExperimentImexButNoImexId = CollectionUtils.subtract(publicationsWithExperimentsHavingImexId, publicationsHavingImexId);

        return publicationsWithExperimentImexButNoImexId;
    }

    public Collection<String> getPublicationsWithoutImexButWithInteractionImex() {

        // publications without IMEx id but with interaction having IMEx id
        Collection<String> publicationsWithInteractionImexButNoImexId = CollectionUtils.subtract(publicationsWithInteractionsHavingImexId, publicationsHavingImexId);

        return publicationsWithInteractionImexButNoImexId;
    }
}
