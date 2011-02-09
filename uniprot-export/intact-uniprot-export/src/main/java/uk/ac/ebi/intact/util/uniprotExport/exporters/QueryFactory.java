package uk.ac.ebi.intact.util.uniprotExport.exporters;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import javax.persistence.Query;
import java.util.List;

/**
 * The class provides pre-formatted queries for extracting proteins from the database with specific criteria
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class QueryFactory {

    private final String interactionInvolvedInComponents = "select distinct(c1.interaction.ac) from Component c1";

    private final String interactionsInvolvingInteractorsNoUniprotUpdate = "select distinct(i2.ac) from Component c2 join " +
            "c2.interaction as i2 join c2.interactor as p join p.annotations as a where a.cvTopic.shortLabel = :noUniprotUpdate";
    // negative interactions
    private final String negativeInteractions = "select distinct(i3.ac) from Component c3 join c3.interaction as i3 join " +
            "i3.annotations as a2 where a2.cvTopic.shortLabel = :negative";
    // interactors with an uniprot identity cross reference
    private final String interactorUniprotIdentity = "select distinct(p2.ac) from InteractorImpl p2 join p2.xrefs as refs where " +
            "refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity";
    // interactors which are not a protein
    private final String nonProteinInteractor = "select distinct(p3.ac) from InteractorImpl p3 where p3.objClass <> :protein";
    // interactions with at least one interactor which either doesn't have any uniprot identity cross reference or is not a protein
    private final String interactionInvolvingNonUniprotOrNonProtein = "select distinct(i4.ac) from Component c4 join " +
            "c4.interaction as i4 join c4.interactor as p4 where p4.ac not in ("+interactorUniprotIdentity+") or p4.ac " +
            "in ("+nonProteinInteractor+")";

    private final String interactionsFromExperimentNoExport = "select distinct(i7.ac) from InteractionImpl i7 " +
            "join i7.experiments as e3 join e3.annotations as an3 where an3.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an3.annotationText)) = :no";
    private final String interactionsFromExperimentExportYes = "select distinct(i8.ac) from InteractionImpl i8 " +
            "join i8.experiments as e4 join e4.annotations as an4 where an4.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an4.annotationText)) = :yes";
    private final String interactionsFromExperimentExportConditional = "select distinct(i9.ac) from InteractionImpl i9 " +
            "join i9.annotations as an5 join i9.experiments as e5 join e5.annotations as an6 where " +
            "an6.cvTopic.shortLabel = :drExport and an5.cvTopic.identifier = :confidence and trim(upper(an5.annotationText)) = trim(upper(an6.annotationText))";
    private final String interactionsFromExperimentExportSpecified = "select distinct(i10.ac) from InteractionImpl i10 " +
            "join i10.experiments as e6 join e6.annotations as an7 where an7.cvTopic.shortLabel = :drExport";

    private final String interactionsDrExportNotPassed = "select distinct(i11.ac) from InteractionImpl i11 " +
            "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
            "and i11.ac not in (" + interactionsFromExperimentExportYes + ") " +
            "and i11.ac not in ("+interactionsFromExperimentExportConditional+")";

    private final String interactionsAccepted = "select distinct(i2.ac) from Component c1 join c1.interaction as i2 join i2.experiments as e " +
            "join e.annotations as an where an.cvTopic.shortLabel = :accepted";
    // interactions with at least one experiment 'on-hold'
    private final String interactionsOnHold = "select distinct(i3.ac) from Component c2 join c2.interaction as i3 join i3.experiments" +
            " as e2 join e2.annotations as an2 where an2.cvTopic.shortLabel = :onhold";

    // select status of the different methods in IntAct
    private final String methodStatus = "select ci.identifier, a.annotationText from CvInteraction ci join ci.annotations as a join a.cvTopic as ct" +
            " where ct.shortLabel = :export";

    public List<Object[]> getMethodStatusInIntact() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(methodStatus);
        query.setParameter("export", CvTopic.UNIPROT_DR_EXPORT);

        List<Object []> methods = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return methods;
    }

    public List<String> getInteractionInvolvedInComponents() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionInvolvedInComponents);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsInvolvingInteractorsNoUniprotUpdate() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsInvolvingInteractorsNoUniprotUpdate);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getNegativeInteractions() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(negativeInteractions);
        query.setParameter("negative", CvTopic.NEGATIVE);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractorUniprotIdentity() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactorUniprotIdentity);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);

        List<String> interactors = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactors;
    }

    public List<String> getNonProteinInteractor() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(nonProteinInteractor);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactors = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactors;
    }

    public List<String> getInteractionInvolvingNonUniprotOrNonProtein() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionInvolvingNonUniprotOrNonProtein);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentNoExport() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsFromExperimentNoExport);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportYes() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsFromExperimentExportYes);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("yes", "YES");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportConditional() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsFromExperimentExportConditional);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportSpecified() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsFromExperimentExportSpecified);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsDrExportNotPassed() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsDrExportNotPassed);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsAccepted() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsAccepted);
        query.setParameter("accepted", CvTopic.ACCEPTED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsOnHold() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsOnHold);
        query.setParameter("onhold", CvTopic.ON_HOLD);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsToBeProcessedForUniprotExport(){
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        String queryString = "select distinct(i.ac) from InteractionImpl i where i.ac in ("+interactionInvolvedInComponents + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ")";
        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are publicly released, which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport(){
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        String queryString = "select distinct(i.ac) from InteractionImpl i " +
                "where i.ac in ("+interactionsAccepted + ") " +
                "and i.ac not in ("+interactionsOnHold+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("accepted", CvTopic.ACCEPTED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("onhold", CvTopic.ON_HOLD);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are publicly released, which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport(){
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        String queryString = "select distinct(i.ac) from InteractionImpl i " +
                "where i.ac in ("+interactionsAccepted + ") " +
                "and i.ac not in ("+interactionsOnHold+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ") " +
                "and i.ac not in ("+negativeInteractions+") ";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("accepted", CvTopic.ACCEPTED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("onhold", CvTopic.ON_HOLD);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        query.setParameter("negative", CvTopic.NEGATIVE);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are involving only uniprot proteins, are not negative and
     * which are publicly released
     */
    public List<String> getInteractionAcsFromReleasedExperimentsNoFilterDrExport(){
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();
        
        String queryString = "select distinct(i.ac) from InteractionImpl i " +
                "where i.ac in ("+interactionsAccepted + ") " +
                "and i.ac not in ("+interactionsOnHold+") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("accepted", CvTopic.ACCEPTED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("onhold", CvTopic.ON_HOLD);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

}
