package uk.ac.ebi.intact.util.uniprotExport.exporters;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

/**
 * The class provides pre-formatted queries for extracting proteins from the database with specific criteria
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class QueryBuilder {

    private final static String AUTHOR_SCORE = "author score";
    private final static String AUTHOR_SCORE_MI = "MI:1221";
    private final static String INFERRED_AUTHOR = "MI:0363";
    private final static String RELEASED = "released";
    private static final String READY_FOR_RELEASE = "ready for release";

    private final String interactionInvolvedInComponents = "select distinct(c1.interaction.ac) from Component c1";

    private final String interactionsInvolvingInteractorsNoUniprotUpdate = "select i2 from Component c2 join " +
            "c2.interaction as i2 join c2.interactor as p join p.annotations as a where a.cvTopic.shortLabel = :noUniprotUpdate";
    private final String interactionAcsInvolvingInteractorsNoUniprotUpdate = "select distinct(i2.ac) from Component c2 join " +
            "c2.interaction as i2 join c2.interactor as p join p.annotations as a where a.cvTopic.shortLabel = :noUniprotUpdate";

    // negative interactions
    private final String negativeInteractions = "select i3 from Component c3 join c3.interaction as i3 join " +
            "i3.annotations as a2 where a2.cvTopic.shortLabel = :negative";
    private final String negativeInteractionAcs = "select distinct(i3.ac) from Component c3 join c3.interaction as i3 join " +
            "i3.annotations as a2 where a2.cvTopic.shortLabel = :negative";

    // interactors with an uniprot identity cross reference
    private final String interactorUniprotIdentity = "select p2 from InteractorImpl p2 join p2.xrefs as refs where " +
            "refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity";
    private final String interactorUniprotIdentities = "select distinct(p2.ac) from InteractorImpl p2 join p2.xrefs as refs where " +
            "refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity";

    // interactors which are not a protein
    private final String nonProteinInteractor = "select distinct(p3.ac) from InteractorImpl p3 where p3.objClass <> :protein";

    // interactions with at least one interactor which either doesn't have any uniprot identity cross reference or is not a protein
    private final String interactionInvolvingNonUniprotOrNonProtein = "select i4 from Component c4 join " +
            "c4.interaction as i4 join c4.interactor as p4 where ( p4.ac in ("+nonProteinInteractor+") or " +
            "not exists ("+interactorUniprotIdentity+" and p2.ac = p4.ac))";
    private final String interactionAcsInvolvingNonUniprotOrNonProtein = "select distinct(i4.ac) from Component c4 join " +
            "c4.interaction as i4 join c4.interactor as p4 where ( p4.ac in ("+nonProteinInteractor+") or " +
            "not exists ("+interactorUniprotIdentity+" and p2.ac = p4.ac))";

    private final String interactionsFromExperimentNoExport = "select i7 from InteractionImpl i7 " +
            "join i7.experiments as e3 join e3.annotations as an3 where an3.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an3.annotationText)) = :no";
    private final String interactionAcsFromExperimentNoExport = "select distinct(i7.ac) from InteractionImpl i7 " +
            "join i7.experiments as e3 join e3.annotations as an3 where an3.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an3.annotationText)) = :no";

    private final String interactionsFromExperimentExportYes = "select i8 from InteractionImpl i8 " +
            "join i8.experiments as e4 join e4.annotations as an4 where an4.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an4.annotationText)) = :yes";
    private final String interactionAcsFromExperimentExportYes = "select distinct(i8.ac) from InteractionImpl i8 " +
            "join i8.experiments as e4 join e4.annotations as an4 where an4.cvTopic.shortLabel = :drExport " +
            "and trim(upper(an4.annotationText)) = :yes";

    private final String interactionsFromExperimentExportConditional = "select i9 from InteractionImpl i9 " +
            "join i9.confidences as confidence join i9.experiments as e5 join e5.annotations as an6 where " +
            "an6.cvTopic.shortLabel = :drExport and confidence.cvConfidenceType.identifier = :confidence and trim(upper(confidence.value)) = trim(upper(an6.annotationText))";
    private final String interactionAcsFromExperimentExportConditional = "select distinct(i9.ac) from InteractionImpl i9 " +
            "join i9.confidences as confidence join i9.experiments as e5 join e5.annotations as an6 where " +
            "an6.cvTopic.shortLabel = :drExport and confidence.cvConfidenceType.identifier = :confidence and trim(upper(confidence.value)) = trim(upper(an6.annotationText))";

    private final String interactionsFromExperimentExportSpecified = "select distinct(i10.ac) from InteractionImpl i10 " +
            "join i10.experiments as e6 join e6.annotations as an7 where an7.cvTopic.shortLabel = :drExport";

    private final String interactionsDrExportNotPassed = "select i11 from InteractionImpl i11 " +
            "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
            "and not exists (" + interactionsFromExperimentExportYes + " and i8.ac = i11.ac) " +
            "and not exists ("+interactionsFromExperimentExportConditional+" and i9.ac = i11.ac)";
    private final String interactionAcsDrExportNotPassed = "select distinct(i11.ac) from InteractionImpl i11 " +
            "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
            "and not exists (" + interactionsFromExperimentExportYes + " and i8.ac = i11.ac) " +
            "and not exists ("+interactionsFromExperimentExportConditional+" and i9.ac = i11.ac)";

    // interactions attached to a publication released or ready-to-be-released
    private final String releasedInteractions = "select rel from Component relComp join relComp.interaction as rel join rel.experiments" +
            " as relexp join relexp.publication as relpub where (relpub.status.shortLabel = :released or relpub.status.shortLabel = :ready_for_release)";
    private final String releasedInteractionAcs = "select distinct(rel.ac) from Component relComp join relComp.interaction as rel join rel.experiments" +
            " as relexp join relexp.publication as relpub where (relpub.status.shortLabel = :released or relpub.status.shortLabel = :ready_for_release)";

    // select status of the different methods in IntAct
    private final String methodStatus = "select ci.identifier, a.annotationText from CvInteraction ci join ci.annotations as a join a.cvTopic as ct" +
            " where ct.shortLabel = :export";

    // select components with only one interactor (self interactions)
    private final String selfInteractions= "select distinct(il.ac) from InteractionImpl il join il.components as ct group by il.ac having count(ct.ac) == 1";

    // negative interactions
    private final String inferredInteractions = "select infer from InteractionImpl as infer join " +
            "infer.experiments as exp join exp.cvInteraction as det where (det.identifier = :inferred_author or det.identifier = :inferred_curator)";
    private final String inferredInteractionAcs = "select distinct(infer.ac) from InteractionImpl as infer join " +
            "infer.experiments as exp join exp.cvInteraction as det where (det.identifier = :inferred_author or det.identifier = :inferred_curator)";

    private final String isoformsWithDifferentParents = "select iso.ac as ac, x2.primaryid as primaryid2, x1.primaryid as primaryid1 " +
            "from intact.ia_interactor iso, intact.ia_interactor parent, intact.ia_interactor_xref x1, intact.ia_interactor_xref x1_1, intact.ia_interactor_xref x2 " +
            "where iso.ac = x1.parent_ac " +
            "and x1_1.parent_ac = iso.ac " +
            "and parent.ac = x2.parent_ac " +
            "and iso.objclass like '%ProteinImpl' " +
            "and parent.objclass like '%ProteinImpl' " +
            "and x1.database_ac = (select ac from ia_controlledvocab where shortlabel = 'uniprotkb') " +
            "and x1.database_ac = x2.database_ac " +
            "and x1_1.database_ac = (select ac from ia_controlledvocab where shortlabel = 'intact') " +
            "and x1.qualifier_ac = (select ac from ia_controlledvocab where shortlabel = 'identity') " +
            "and x1.qualifier_ac = x2.qualifier_ac " +
            "and x1_1.qualifier_ac in (select ac from ia_controlledvocab where shortlabel = 'chain-parent' or shortlabel = 'isoform-parent') " +
            "and x2.primaryid <> substr(x1.primaryid, 1, 6) " +
            "and parent.ac = x1_1.primaryid";
    
    private final String interactionsWithGoComponentXrefs = "select interact.ac, goRefs.primaryId from InteractionImpl interact join interact.xrefs as goRefs " +
            "where goRefs.cvDatabase.identifier = :go and goRefs.cvXrefQualifier.identifier = :component";

    public List<Object[]> getMethodStatusInIntact() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(methodStatus);
        query.setParameter("export", CvTopic.UNIPROT_DR_EXPORT);

        List<Object []> methods = query.getResultList();
        dataContext.commitTransaction(transactionStatus);

        return methods;
    }

    public List<Object[]> getGoComponentXrefsInIntact() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionsWithGoComponentXrefs);
        query.setParameter("go", CvDatabase.GO_MI_REF);
        query.setParameter("component", CvXrefQualifier.COMPONENT_MI_REF);

        List<Object []> xrefs = query.getResultList();
        dataContext.commitTransaction(transactionStatus);

        return xrefs;
    }

    public List<Object[]> getTranscriptsWithDifferentParents() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createNativeQuery(isoformsWithDifferentParents);

        List<Object []> methods = query.getResultList();
        dataContext.commitTransaction(transactionStatus);

        return methods;
    }

    public List<String> getInferredInteractions() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(inferredInteractionAcs);
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
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

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsInvolvingInteractorsNoUniprotUpdate);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getNegativeInteractions() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(negativeInteractionAcs);
        query.setParameter("negative", CvTopic.NEGATIVE);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractorUniprotIdentity() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactorUniprotIdentities);
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

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsInvolvingNonUniprotOrNonProtein);
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

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsFromExperimentNoExport);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportYes() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsFromExperimentExportYes);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("yes", "YES");

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportConditional() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsFromExperimentExportConditional);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("confidence", AUTHOR_SCORE_MI);

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

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(interactionAcsDrExportNotPassed);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getReleasedInteractions() {
        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(releasedInteractionAcs);
        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

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
                "and not exists ("+interactionsInvolvingInteractorsNoUniprotUpdate+" and i2.ac = i.ac) " +
                "and not exists ("+negativeInteractions+" and i3.ac = i.ac) " +
                "and not exists ("+interactionInvolvingNonUniprotOrNonProtein+" and i4.ac = i.ac) " +
                "and not exists (" + interactionsDrExportNotPassed + " and i11.ac = i.ac) " +
                "and not exists (" + interactionsFromExperimentNoExport + " and i7.ac = i.ac)" +
                "and not exists (" + inferredInteractions + " and infer.ac = i.ac)";
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
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);

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
                "where i.ac in ("+releasedInteractionAcs + ") " +
                "and not exists (" + interactionsDrExportNotPassed + " and i11.ac = i.ac) " +
                "and not exists (" + interactionsFromExperimentNoExport + " and i7.ac = i.ac) " +
                "and not exists ("+interactionsInvolvingInteractorsNoUniprotUpdate+" and i2.ac = i.ac) " +
                "and not exists ("+negativeInteractions+" and i3.ac = i.ac) " +
                "and not exists ("+interactionInvolvingNonUniprotOrNonProtein+" and i4.ac = i.ac)" +
                "and not exists ("+inferredInteractions+" and infer.ac = i.ac)";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     *
     * @return the list of positive interaction accessions which are publicly released and pass the filtering options set in the FilterConfig
     */
    public List<String> getReleasedInteractionAcsPassingFilters(){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from InteractionImpl i ");
        queryString.append("where i.ac in (");
        queryString.append(releasedInteractionAcs);
        queryString.append(") ");

        // negative interactions will be processed differently and are not in the same list
        queryString.append("and not exists (");
        queryString.append(negativeInteractions);
        queryString.append(" and i3.ac = i.ac) ");

        if (excludeLowConfidenceInteractions){
            queryString.append("and not exists (");
            queryString.append(interactionsDrExportNotPassed);
            queryString.append(" and i11.ac = i.ac) and not exists (");
            queryString.append(interactionsFromExperimentNoExport);
            queryString.append(" and i7.ac = i.ac) ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("and not exists (");
            queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(" and i2.ac = i.ac) and not exists (");
            queryString.append(interactionInvolvingNonUniprotOrNonProtein);
            queryString.append(" and i4.ac = i.ac) ");
        }

        if (excludeInferredInteractions){
            queryString.append("and not exists (");
            queryString.append(inferredInteractions);
            queryString.append(" and infer.ac = i.ac)");
        }

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("negative", CvTopic.NEGATIVE);

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
            query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
            query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);
        }

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getNegativeInteractionsPassingFilter() {
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNegativeInteractions = config.excludeNegativeInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        if (!excludeNegativeInteractions){
            DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

            TransactionStatus transactionStatus = dataContext.beginTransaction();

            StringBuffer queryString = new StringBuffer();
            queryString.append("select distinct i.ac from InteractionImpl i ");
            queryString.append("where i.ac in (");
            queryString.append(releasedInteractionAcs);
            queryString.append(") and i.ac in (");
            queryString.append(negativeInteractionAcs);
            queryString.append(") ");

            if (excludeLowConfidenceInteractions){
                queryString.append("and not exists (");
                queryString.append(interactionsDrExportNotPassed);
                queryString.append(" and i11.ac = i.ac) and not exists (");
                queryString.append(interactionsFromExperimentNoExport);
                queryString.append(" and i7.ac = i.ac) ");
            }

            if (excludeSpokeExpanded && excludeNonUniprotInteractors){
                queryString.append("and not exists (");
                queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
                queryString.append(" and i2.ac = i.ac) and not exists (");
                queryString.append(interactionInvolvingNonUniprotOrNonProtein);
                queryString.append(" and i4.ac = i.ac) ");
            }

            if (excludeInferredInteractions){
                queryString.append("and not exists (");
                queryString.append(inferredInteractions);
                queryString.append(" and infer.ac = i.ac)");
            }

            Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString.toString());

            query.setParameter("released", RELEASED);
            query.setParameter("ready_for_release", READY_FOR_RELEASE);
            query.setParameter("negative", CvTopic.NEGATIVE);

            if (excludeLowConfidenceInteractions){
                query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
                query.setParameter("no", "NO");
                query.setParameter("yes", "YES");
                query.setParameter("confidence", AUTHOR_SCORE_MI);
            }

            if (excludeSpokeExpanded && excludeNonUniprotInteractors){
                query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
                query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
                query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
                query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
            }

            if (excludeInferredInteractions){
                query.setParameter("inferred_author", INFERRED_AUTHOR);
                query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);
            }

            List<String> interactions = query.getResultList();

            dataContext.commitTransaction(transactionStatus);

            return interactions;
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    public List<String> getInteractionAcsExcludedWithFilters(){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();
// TODO
        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from InteractionImpl i ");
        queryString.append("where not exists (");
        queryString.append(releasedInteractions);
        queryString.append(" and rel.ac = i.ac) ");

        if (excludeLowConfidenceInteractions){
            queryString.append("or i.ac in (");
            queryString.append(interactionAcsDrExportNotPassed);
            queryString.append(") or i.ac in (");
            queryString.append(interactionAcsFromExperimentNoExport);
            queryString.append(") ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("or i.ac in (");
            queryString.append(interactionAcsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(") or i.ac in (");
            queryString.append(interactionAcsInvolvingNonUniprotOrNonProtein);
            queryString.append(") ");
        }

        if (excludeInferredInteractions){
            queryString.append("or i.ac in (");
            queryString.append(inferredInteractionAcs);
            queryString.append(")");
        }

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
            query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
            query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);
        }

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> getReleasedSelfInteractionAcsPassingFilters(){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNegativeInteractions = config.excludeNegativeInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from InteractionImpl i ");
        queryString.append("where i.ac in (");
        queryString.append(releasedInteractionAcs);
        queryString.append(") and i.ac in (");
        queryString.append(selfInteractions);
        queryString.append(") ");

        if (excludeLowConfidenceInteractions){
            queryString.append("and not exists (");
            queryString.append(interactionsDrExportNotPassed);
            queryString.append(" and i11.ac = i.ac) and not exists (");
            queryString.append(interactionsFromExperimentNoExport);
            queryString.append(" and i7.ac = i.ac) ");
        }

        if (excludeNegativeInteractions){
            queryString.append("and not exists (");
            queryString.append(negativeInteractions);
            queryString.append(" and i3.ac = i.ac) ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("and not exists (");
            queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(" and i2.ac = i.ac) and not exists (");
            queryString.append(interactionInvolvingNonUniprotOrNonProtein);
            queryString.append(" and i4.ac = i.ac) ");
        }

        if (excludeInferredInteractions){
            queryString.append("and not exists (");
            queryString.append(inferredInteractions);
            queryString.append(" and infer.ac = i.ac)");
        }

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeNegativeInteractions){
            query.setParameter("negative", CvTopic.NEGATIVE);
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
            query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
            query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);
        }

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
                "where i.ac in ("+releasedInteractionAcs + ") " +
                "and not exists (" + interactionsDrExportNotPassed + " and i11.ac = i.ac) " +
                "and not exists (" + interactionsFromExperimentNoExport + " and i7.ac = i.ac) " +
                "and not exists ("+negativeInteractions+" and i3.ac = i.ac) " +
                "and not exists ("+inferredInteractions+" and infer.ac = i.ac)";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);

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
                "where i.ac in ("+releasedInteractionAcs + ") " +
                "and not exists ("+interactionsInvolvingInteractorsNoUniprotUpdate+" and i2.ac = i.ac) " +
                "and not exists ("+negativeInteractions+" and i3.ac = i.ac) " +
                "and not exists ("+interactionInvolvingNonUniprotOrNonProtein+" and i4.ac = i.ac)" +
                "and not exists ("+inferredInteractions+" and infer.ac = i.ac)";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", CvInteraction.INFERRED_BY_CURATOR_MI_REF);

        List<String> interactions = query.getResultList();

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

}
