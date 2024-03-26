package uk.ac.ebi.intact.util.uniprotExport.exporters;

import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;

import javax.persistence.EntityTransaction;
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

    private final String interactionInvolvedInComponents = "select distinct(c1.dbParentInteraction.ac) from IntactParticipantEvidence c1";

    private final String interactionsInvolvingInteractorsNoUniprotUpdate = "select distinct(i2.ac) from IntactParticipantEvidence c2 join " +
            "c2.dbParentInteraction as i2 join c2.interactor as p join p.dbAnnotations as a where a.topic.shortName = :noUniprotUpdate";
    // negative interactions
    private final String negativeInteractions = "select distinct(i3.ac) from IntactParticipantEvidence c3 join c3.dbParentInteraction as i3 join " +
            "i3.dbAnnotations as a2 where a2.topic.shortName = :negative";
    // interactors with an uniprot identity cross reference
    private final String interactorUniprotIdentity = "select distinct(p2.ac) from IntactInteractor p2 join p2.dbXrefs as refs where " +
            "refs.database.identifier = :uniprot and refs.qualifier.identifier = :identity";
    // interactors which are not a protein
    private final String nonProteinInteractor = "select distinct(p3.ac) from IntactInteractor p3 where p3.objClass <> :protein";
    // interactions with at least one interactor which either doesn't have any uniprot identity cross reference or is not a protein
    private final String interactionInvolvingNonUniprotOrNonProtein = "select distinct(i4.ac) from IntactParticipantEvidence c4 join " +
            "c4.dbParentInteraction as i4 join c4.interactor as p4 where p4.ac not in ("+interactorUniprotIdentity+") or p4.ac " +
            "in ("+nonProteinInteractor+")";

    private final String interactionsFromExperimentNoExport = "select distinct(i7.ac) from IntactInteractionEvidence i7 " +
            "join i7.dbExperiments as e3 join e3.annotations as an3 where an3.topic.shortName = :drExport " +
            "and trim(upper(an3.value)) = :no";
    private final String interactionsFromExperimentExportYes = "select distinct(i8.ac) from IntactInteractionEvidence i8 " +
            "join i8.dbExperiments as e4 join e4.annotations as an4 where an4.topic.shortName = :drExport " +
            "and trim(upper(an4.value)) = :yes";
    private final String interactionsFromExperimentExportConditional = "select distinct(i9.ac) from IntactInteractionEvidence i9 " +
            "join i9.confidences as confidence join i9.dbExperiments as e5 join e5.annotations as an6 where " +
            "an6.topic.shortName = :drExport and " +
            "confidence.type.identifier = :confidence " +
            "and trim(upper(confidence.value)) = trim(upper(an6.value))";
    private final String interactionsFromExperimentExportSpecified = "select distinct(i10.ac) from IntactInteractionEvidence i10 " +
            "join i10.dbExperiments as e6 join e6.annotations as an7 where an7.topic.shortName = :drExport";

    private final String interactionsDrExportNotPassed = "select distinct(i11.ac) from IntactInteractionEvidence i11 " +
            "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
            "and i11.ac not in (" + interactionsFromExperimentExportYes + ") " +
            "and i11.ac not in ("+interactionsFromExperimentExportConditional+")";

    // interactions attached to a publication released or ready-to-be-released
    private final String releasedInteractions = "select distinct(rel.ac) from IntactParticipantEvidence relComp " +
            "join relComp.dbParentInteraction as rel join rel.dbExperiments as relexp " +
            "join relexp.publication as relpub " +
            "where relpub.cvStatus.shortName = :released or relpub.cvStatus.shortName = :ready_for_release";

    // select status of the different methods in IntAct
    private final String methodStatus = "select ci.identifier, a.value " +
            "from IntactCvTerm ci join ci.dbAnnotations as a join a.topic as ct" +
            " where ct.shortName = :export";

    // select components with only one interactor (self interactions)
    private final String selfInteractions= "select distinct(il.ac) from IntactInteractionEvidence il " +
            "join il.participants as ct group by il.ac having count(ct.ac) == 1";

    // negative interactions
    private final String inferredInteractions = "select distinct(infer.ac) from IntactInteractionEvidence as infer join " +
            "infer.dbExperiments as exp join exp.interactionDetectionMethod as det " +
            "where det.identifier = :inferred_author or det.identifier = :inferred_curator";

    private final String isoformsWithDifferentParents = "select iso.ac as ac, x2.primaryid as primaryid2, x1.primaryid as primaryid1 from ia_interactor iso, ia_interactor parent, ia_interactor_xref x1, ia_interactor_xref x1_1, ia_interactor_xref x2 " +
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
    
    private final String interactionsWithGoComponentXrefs = "select interact.ac, goRefs.id " +
            "from IntactInteractionEvidence interact join interact.dbXrefs as goRefs " +
            "where goRefs.database.identifier = :go and goRefs.qualifier.identifier = :component";

    public List<Object[]> getMethodStatusInIntact(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(methodStatus);
        query.setParameter("export", "uniprot-dr-export");

        List<Object []> methods = query.getResultList();
        transaction.commit();

        return methods;
    }

    public List<Object[]> getGoComponentXrefsInIntact(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsWithGoComponentXrefs);
        query.setParameter("go", Xref.GO_MI);
        query.setParameter("component", "MI:0354");

        List<Object []> xrefs = query.getResultList();
        transaction.commit();

        return xrefs;
    }

    public List<Object[]> getTranscriptsWithDifferentParents(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createNativeQuery(isoformsWithDifferentParents);

        List<Object []> methods = query.getResultList();
        transaction.commit();

        return methods;
    }

    public List<String> getInferredInteractions(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(inferredInteractions);
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionInvolvedInComponents(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionInvolvedInComponents);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsInvolvingInteractorsNoUniprotUpdate(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsInvolvingInteractorsNoUniprotUpdate);
        query.setParameter("noUniprotUpdate", "no-uniprot-update");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getNegativeInteractions(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(negativeInteractions);
        query.setParameter("negative", "negative");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractorUniprotIdentity(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactorUniprotIdentity);
        query.setParameter("uniprot", Xref.UNIPROTKB_MI);
        query.setParameter("identity", Xref.IDENTITY_MI);

        List<String> interactors = query.getResultList();

        transaction.commit();

        return interactors;
    }

    public List<String> getNonProteinInteractor(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(nonProteinInteractor);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactors = query.getResultList();

        transaction.commit();

        return interactors;
    }

    public List<String> getInteractionInvolvingNonUniprotOrNonProtein(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionInvolvingNonUniprotOrNonProtein);
        query.setParameter("uniprot", Xref.UNIPROTKB_MI);
        query.setParameter("identity", Xref.IDENTITY_MI);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsFromExperimentNoExport(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsFromExperimentNoExport);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("no", "NO");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportYes(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsFromExperimentExportYes);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("yes", "YES");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportConditional(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsFromExperimentExportConditional);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("confidence", AUTHOR_SCORE_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsFromExperimentExportSpecified(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsFromExperimentExportSpecified);
        query.setParameter("drExport", "uniprot-dr-export");

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getInteractionsDrExportNotPassed(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(interactionsDrExportNotPassed);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getReleasedInteractions(IntactDao intactDao) {
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        Query query = intactDao.getEntityManager().createQuery(releasedInteractions);
        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsToBeProcessedForUniprotExport(IntactDao intactDao){
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        String queryString = "select distinct(i.ac) from IntactInteractionEvidence i where i.ac in ("+interactionInvolvedInComponents + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ")" +
                "and i.ac not in (" + inferredInteractions + ")";
        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = intactDao.getEntityManager().createQuery(queryString);

        query.setParameter("noUniprotUpdate", "no-uniprot-update");
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("negative", "negative");
        query.setParameter("uniprot", Xref.UNIPROTKB_MI);
        query.setParameter("identity", Xref.IDENTITY_MI);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are publicly released, which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport(IntactDao intactDao){
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        String queryString = "select distinct(i.ac) from IntactInteractionEvidence i " +
                "where i.ac in ("+releasedInteractions + ") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+")" +
                "and i.ac not in ("+inferredInteractions+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = intactDao.getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("noUniprotUpdate", "no-uniprot-update");
        query.setParameter("negative", "negative");
        query.setParameter("uniprot", Xref.UNIPROTKB_MI);
        query.setParameter("identity", Xref.IDENTITY_MI);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    /**
     *
     * @return the list of positive interaction accessions which are publicly released and pass the filtering options set in the FilterConfig
     */
    public List<String> getReleasedInteractionAcsPassingFilters(IntactDao intactDao){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from IntactInteractionEvidence i ");
        queryString.append("where i.ac in (");
        queryString.append(releasedInteractions);
        queryString.append(") ");

        // negative interactions will be processed differently and are not in the same list
        queryString.append("and i.ac not in (");
        queryString.append(negativeInteractions);
        queryString.append(") ");

        if (excludeLowConfidenceInteractions){
            queryString.append("and i.ac not in (");
            queryString.append(interactionsDrExportNotPassed);
            queryString.append(") and i.ac not in (");
            queryString.append(interactionsFromExperimentNoExport);
            queryString.append(") ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("and i.ac not in (");
            queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(") and i.ac not in (");
            queryString.append(interactionInvolvingNonUniprotOrNonProtein);
            queryString.append(") ");
        }

        if (excludeInferredInteractions){
            queryString.append("and i.ac not in (");
            queryString.append(inferredInteractions);
            queryString.append(")");
        }

        Query query = intactDao.getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("negative", "negative");

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", "uniprot-dr-export");
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", "no-uniprot-update");
            query.setParameter("uniprot", Xref.UNIPROTKB_MI);
            query.setParameter("identity", Xref.IDENTITY_MI);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);
        }

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getNegativeInteractionsPassingFilter(IntactDao intactDao) {
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNegativeInteractions = config.excludeNegativeInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        if (!excludeNegativeInteractions){
            EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

            StringBuffer queryString = new StringBuffer();
            queryString.append("select distinct i.ac from IntactInteractionEvidence i ");
            queryString.append("where i.ac in (");
            queryString.append(releasedInteractions);
            queryString.append(") and i.ac in (");
            queryString.append(negativeInteractions);
            queryString.append(") ");

            if (excludeLowConfidenceInteractions){
                queryString.append("and i.ac not in (");
                queryString.append(interactionsDrExportNotPassed);
                queryString.append(") and i.ac not in (");
                queryString.append(interactionsFromExperimentNoExport);
                queryString.append(") ");
            }

            if (excludeSpokeExpanded && excludeNonUniprotInteractors){
                queryString.append("and i.ac not in (");
                queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
                queryString.append(") and i.ac not in (");
                queryString.append(interactionInvolvingNonUniprotOrNonProtein);
                queryString.append(") ");
            }

            if (excludeInferredInteractions){
                queryString.append("and i.ac not in (");
                queryString.append(inferredInteractions);
                queryString.append(")");
            }

            Query query = intactDao.getEntityManager().createQuery(queryString.toString());

            query.setParameter("released", RELEASED);
            query.setParameter("ready_for_release", READY_FOR_RELEASE);
            query.setParameter("negative", "negative");

            if (excludeLowConfidenceInteractions){
                query.setParameter("drExport", "uniprot-dr-export");
                query.setParameter("no", "NO");
                query.setParameter("yes", "YES");
                query.setParameter("confidence", AUTHOR_SCORE_MI);
            }

            if (excludeSpokeExpanded && excludeNonUniprotInteractors){
                query.setParameter("noUniprotUpdate", "no-uniprot-update");
                query.setParameter("uniprot", Xref.UNIPROTKB_MI);
                query.setParameter("identity", Xref.IDENTITY_MI);
                query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
            }

            if (excludeInferredInteractions){
                query.setParameter("inferred_author", INFERRED_AUTHOR);
                query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);
            }

            List<String> interactions = query.getResultList();

            transaction.commit();

            return interactions;
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    public List<String> getInteractionAcsExcludedWithFilters(IntactDao intactDao){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from IntactInteractionEvidence i ");
        queryString.append("where i.ac not in (");
        queryString.append(releasedInteractions);
        queryString.append(") ");

        if (excludeLowConfidenceInteractions){
            queryString.append("or i.ac in (");
            queryString.append(interactionsDrExportNotPassed);
            queryString.append(") or i.ac in (");
            queryString.append(interactionsFromExperimentNoExport);
            queryString.append(") ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("or i.ac in (");
            queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(") or i.ac in (");
            queryString.append(interactionInvolvingNonUniprotOrNonProtein);
            queryString.append(") ");
        }

        if (excludeInferredInteractions){
            queryString.append("or i.ac in (");
            queryString.append(inferredInteractions);
            queryString.append(")");
        }

        Query query = intactDao.getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", "uniprot-dr-export");
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", "no-uniprot-update");
            query.setParameter("uniprot", Xref.UNIPROTKB_MI);
            query.setParameter("identity", Xref.IDENTITY_MI);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);
        }

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    public List<String> getReleasedSelfInteractionAcsPassingFilters(IntactDao intactDao){
        FilterConfig config = FilterContext.getInstance().getConfig();
        boolean excludeSpokeExpanded = config.excludeSpokeExpandedInteractions();
        boolean excludeNegativeInteractions = config.excludeNegativeInteractions();
        boolean excludeLowConfidenceInteractions = config.excludeLowConfidenceInteractions();
        boolean excludeNonUniprotInteractors = config.excludeNonUniprotInteractors();
        boolean excludeInferredInteractions = config.isExcludeInferredInteractions();

        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        StringBuffer queryString = new StringBuffer();
        queryString.append("select distinct(i.ac) from IntactInteractionEvidence i ");
        queryString.append("where i.ac in (");
        queryString.append(releasedInteractions);
        queryString.append(") and i.ac in (");
        queryString.append(selfInteractions);
        queryString.append(") ");

        if (excludeLowConfidenceInteractions){
            queryString.append("and i.ac not in (");
            queryString.append(interactionsDrExportNotPassed);
            queryString.append(") and i.ac not in (");
            queryString.append(interactionsFromExperimentNoExport);
            queryString.append(") ");
        }

        if (excludeNegativeInteractions){
            queryString.append("and i.ac not in (");
            queryString.append(negativeInteractions);
            queryString.append(") ");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            queryString.append("and i.ac not in (");
            queryString.append(interactionsInvolvingInteractorsNoUniprotUpdate);
            queryString.append(") and i.ac not in (");
            queryString.append(interactionInvolvingNonUniprotOrNonProtein);
            queryString.append(") ");
        }

        if (excludeInferredInteractions){
            queryString.append("and i.ac not in (");
            queryString.append(inferredInteractions);
            queryString.append(")");
        }

        Query query = intactDao.getEntityManager().createQuery(queryString.toString());

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);

        if (excludeLowConfidenceInteractions){
            query.setParameter("drExport", "uniprot-dr-export");
            query.setParameter("no", "NO");
            query.setParameter("yes", "YES");
            query.setParameter("confidence", AUTHOR_SCORE_MI);
        }

        if (excludeNegativeInteractions){
            query.setParameter("negative", "negative");
        }

        if (excludeSpokeExpanded && excludeNonUniprotInteractors){
            query.setParameter("noUniprotUpdate", "no-uniprot-update");
            query.setParameter("uniprot", Xref.UNIPROTKB_MI);
            query.setParameter("identity", Xref.IDENTITY_MI);
            query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        }

        if (excludeInferredInteractions){
            query.setParameter("inferred_author", INFERRED_AUTHOR);
            query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);
        }

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }


    /**
     *
     * @return the list of interaction accessions which are publicly released, which are involving only uniprot proteins, are not negative and
     * which passed the dr-export annotation at the level of the experiment
     */
    public List<String> getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport(IntactDao intactDao){
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        String queryString = "select distinct(i.ac) from IntactInteractionEvidence i " +
                "where i.ac in ("+releasedInteractions + ") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+inferredInteractions+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = intactDao.getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("drExport", "uniprot-dr-export");
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", AUTHOR_SCORE_MI);
        query.setParameter("negative", "negative");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

    /**
     *
     * @return the list of interaction accessions which are involving only uniprot proteins, are not negative and
     * which are publicly released
     */
    public List<String> getInteractionAcsFromReleasedExperimentsNoFilterDrExport(IntactDao intactDao){
        EntityTransaction transaction = intactDao.getEntityManager().getTransaction();

        String queryString = "select distinct(i.ac) from IntactInteractionEvidence i " +
                "where i.ac in ("+releasedInteractions + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+")" +
                "and i.ac not in ("+inferredInteractions+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = intactDao.getEntityManager().createQuery(queryString);

        query.setParameter("released", RELEASED);
        //query.setParameter("september2005", "01/09/2005");
        //query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("ready_for_release", READY_FOR_RELEASE);
        query.setParameter("noUniprotUpdate", "no-uniprot-update");
        query.setParameter("negative", "negative");
        query.setParameter("uniprot", Xref.UNIPROTKB_MI);
        query.setParameter("identity", Xref.IDENTITY_MI);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");
        query.setParameter("inferred_author", INFERRED_AUTHOR);
        query.setParameter("inferred_curator", Experiment.INFERRED_BY_CURATOR_MI);

        List<String> interactions = query.getResultList();

        transaction.commit();

        return interactions;
    }

}
