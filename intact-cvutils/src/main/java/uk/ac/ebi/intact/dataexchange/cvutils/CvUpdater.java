package uk.ac.ebi.intact.dataexchange.cvutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.core.persister.stats.StatsUnit;
import uk.ac.ebi.intact.dataexchange.cvutils.model.*;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.util.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUpdater {

    private static Log log = LogFactory.getLog(CvUpdater.class);

    private Map<String,CvObject> processed;
    private CvUpdaterStatistics stats;

    private boolean excludeObsolete;
    private CvDatabase nonMiCvDatabase;

    private CvTopic obsoleteTopic;

    public CvUpdater() {
        this.nonMiCvDatabase = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(),
                CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT);
    }

    /**
     * Starts the creation and update of CVs by using the ontology provided
     * @return An object containing some statistics about the update
     */
    public CvUpdaterStatistics createOrUpdateCVs(IntactOntology ontology) {
        return createOrUpdateCVs(ontology, new AnnotationInfoDataset());
    }

    /**
     * Starts the creation and update of CVs by using the ontology provided
     * @return An object containing some statistics about the update
     */
    public CvUpdaterStatistics createOrUpdateCVs(IntactOntology ontology, AnnotationInfoDataset annotationInfoDataset) {
        if (ontology == null) {
            throw new NullPointerException("ontology");
        }
        if (annotationInfoDataset == null) {
            throw new NullPointerException("annotationDataset");
        }

        this.processed = new HashMap<String,CvObject>();

        stats = new CvUpdaterStatistics();

        List<CvObject> rootsAndOrphans = new ArrayList<CvObject>();

        final Collection<Class> ontologyTypes = ontology.getTypes();
        if (log.isDebugEnabled()) log.debug("Ontology types ("+ ontologyTypes.size()+"): "+ ontologyTypes);

        for (Class<? extends CvObject> type : ontologyTypes) {
            final Collection<CvTerm> rootsForType = ontology.getRoots(type);
            if (log.isDebugEnabled()) log.debug("Roots for type "+type.getSimpleName()+" ("+rootsForType.size()+")");

            for (CvTerm root : rootsForType) {
                if (log.isDebugEnabled()) log.debug("\tProcessing root: "+root.getShortName()+" ("+root.getId()+")");

                CvObject cvObjectRoot = toCvObject(type, root);
                rootsAndOrphans.add(cvObjectRoot);
            }
        }

        // handle orphans - create a CvTopic for each
        if (log.isDebugEnabled()) log.debug("Processing orphan terms...");

        List<CvTopic> orphanCvs = new ArrayList<CvTopic>(ontology.getOrphanTerms().size());

        CvTopic obsoleteTopic = createCvTopicObsolete();

        for (CvTerm orphan : ontology.getOrphanTerms()) {

            // skip if it is the "obsolete" term
            if (CvTopic.OBSOLETE_MI_REF.equals(orphan.getId())) {
                continue;
            }

            // all obsolete terms are orphan, we should check if what is now an orphan term
            // was not obsolete before and already exists in the database. If so, it should be
            // added the obsolete term
            int cvObjectsWithSameId = checkAndMarkAsObsoleteIfExisted(orphan, stats);

            // check if we deal with obsolete terms
            if (excludeObsolete && orphan.isObsolete()) {
                continue;
            }

            addCvObjectToStatsIfObsolete(orphan);

            // check if it is valid and persist
            if (cvObjectsWithSameId == 0) {
                if (isValidTerm(orphan)) {
                    CvTopic cvOrphan = toCvObject(CvTopic.class, orphan);

                    if (orphan.isObsolete()) {
                        addObsoleteAnnotation(cvOrphan);
                    }

                    orphanCvs.add(cvOrphan);
                    stats.addOrphanCv(cvOrphan);

                    rootsAndOrphans.add(cvOrphan);
                } else {
                    stats.getInvalidTerms().put(orphan.getId(), orphan.getShortName());
                }
            }
        }

        // workaround, as we create the "obsolete" term in the class, ignoring the one
        // that comes from the OBO file
        if (obsoleteTopic.getAc() == null) {
            rootsAndOrphans.add(obsoleteTopic);
        }
        stats.addObsoleteCv(obsoleteTopic);
        stats.addOrphanCv(obsoleteTopic);

        List<CvDagObject> allCvs = new ArrayList<CvDagObject>();
        for (CvObject rootOrOrphan : rootsAndOrphans) {
            allCvs.addAll(itselfAndChildrenAsList((CvDagObject) rootOrOrphan));
        }

        // process any term from the cv annotations dataset resource
        updateCVsUsingAnnotationDataset(allCvs, annotationInfoDataset);

        PersisterStatistics persisterStats = PersisterHelper.saveOrUpdate(allCvs.toArray(new CvObject[rootsAndOrphans.size()]));
        addCvObjectsToUpdaterStats(persisterStats, stats);

        if (log.isDebugEnabled()) {
            log.debug("Persisted: " + persisterStats);

            log.debug("Processed: "+processed.size());
            log.debug("Terms in ontology: "+ontology.getCvTerms().size());
            log.debug(stats);
        }

        return stats;
    }

    private void updateCVsUsingAnnotationDataset(List<CvDagObject> allCvs, AnnotationInfoDataset annotationInfoDataset) {
        CvTopic hidden = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(),
                                                      CvTopic.class, null, CvTopic.HIDDEN);

         for (CvDagObject cvObject : allCvs) {
            if (cvObject.getMiIdentifier() != null && annotationInfoDataset.containsCvAnnotation(cvObject.getMiIdentifier())) {
                AnnotationInfo annotInfo = annotationInfoDataset.getCvAnnotation(cvObject.getMiIdentifier());

                if (CvTopic.HIDDEN.equals(annotInfo.getTopicShortLabel())) {
                    Annotation annotation = new Annotation(IntactContext.getCurrentInstance().getInstitution(), hidden, annotInfo.getReason());
                    addAnnotation(annotation, cvObject, annotInfo.isApplyToChildren());
                } else {
                    log.warn("Case not implemented: topic short label in annotation info different from 'hidden': "+annotInfo.getTopicShortLabel());
                }
            }
        }
    }

    private void addAnnotation(Annotation annotation, CvDagObject cvObject, boolean includeChildren) {
        boolean containsAnnotation = false;

        for (Annotation annot : cvObject.getAnnotations()) {
            if (annot.getAnnotationText().equals(annotation.getAnnotationText())) {
                containsAnnotation = true;
                break;
            }
        }

        if (!containsAnnotation) {
            cvObject.getAnnotations().add(annotation);
        }

        if (includeChildren) {
            for (CvDagObject child : cvObject.getChildren()) {
                addAnnotation(annotation, child, includeChildren);
            }
        }
    }

    private int checkAndMarkAsObsoleteIfExisted(CvTerm orphan, CvUpdaterStatistics stats) {
        String id = orphan.getId();

        final CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao();
        Collection<CvObject> existingCvs = cvObjectDao.getByPsiMiRefCollection(Collections.singleton(id));

        if (existingCvs.isEmpty()) {
            existingCvs = cvObjectDao.getByShortLabelLike(orphan.getShortName());
        }


        if (!existingCvs.isEmpty()) {
            CvTopic obsoleteTopic = createCvTopicObsolete();

            for (CvObject existingCv : existingCvs) {
                boolean alreadyContainsObsolete = false;

                for (Annotation annot : existingCv.getAnnotations()) {
                    if (CvTopic.OBSOLETE_MI_REF.equals(annot.getCvTopic().getMiIdentifier())) {
                        alreadyContainsObsolete = true;
                    }
                }

                if (!alreadyContainsObsolete) {
                    existingCv.addAnnotation(new Annotation(existingCv.getOwner(), obsoleteTopic, CvTopic.OBSOLETE));
                    stats.addUpdatedCv(existingCv);
                }
            }
        }
        return existingCvs.size();
    }

    private void addObsoleteAnnotation(CvObject existingCv) {
        obsoleteTopic.addAnnotation(new Annotation(existingCv.getOwner(), obsoleteTopic, CvTopic.OBSOLETE));
    }

    private CvTopic createCvTopicObsolete() {
        if (obsoleteTopic != null) {
            return obsoleteTopic;
        }

        obsoleteTopic = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao(CvTopic.class).getByPsiMiRef(CvTopic.OBSOLETE_MI_REF);

        if (obsoleteTopic == null) {
                // create the obsolete term (which is obsolete too!)
                obsoleteTopic = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE);
                obsoleteTopic.setFullName("obsolete term");
                addObsoleteAnnotation(obsoleteTopic);
        }

        return obsoleteTopic;
    }

    private List<CvDagObject> itselfAndChildrenAsList(CvDagObject cv) {
        List<CvDagObject> itselfAndChildren = new ArrayList<CvDagObject>();
        itselfAndChildren.add(cv);

        for (CvDagObject child : cv.getChildren()) {
            itselfAndChildren.addAll(itselfAndChildrenAsList(child));
        }

        return itselfAndChildren;
    }

    private void addCvObjectsToUpdaterStats(PersisterStatistics persisterStats, CvUpdaterStatistics stats) {
        for (StatsUnit statsUnit : persisterStats.getPersisted(CvObject.class, true)) {
            stats.getCreatedCvs().put(statsUnit.getType(), statsUnit);
        }

        for (StatsUnit statsUnit : persisterStats.getMerged(CvObject.class, true)) {
            stats.getUpdatedCvs().put(statsUnit.getType(), statsUnit);
        }
    }

    private void addCvObjectToStatsIfObsolete(CvTerm cvTerm) {
        if (cvTerm.isObsolete()) {
            stats.getObsoleteCvs().put(cvTerm.getId(), cvTerm.getShortName());
        }
    }

    protected boolean isValidTerm(CvTerm term) {
        return term.getId().contains(":");
    }

    protected <T extends CvObject> T toCvObject(Class<T> cvClass, CvTerm cvTerm) {
        String primaryId = cvTerm.getId();

        if (log.isTraceEnabled()) log.trace("\t\t"+cvClass.getSimpleName()+": "+cvTerm.getId()+" ("+cvTerm.getShortName()+")");

        final String processedKey = cvKey(cvClass, primaryId);
        
        if (processed.containsKey(processedKey)) {
            return (T) processed.get(processedKey);
        }

        final Institution institution = IntactContext.getCurrentInstance().getInstitution();

        T cvObject = CvObjectUtils.createCvObject(institution,
                    cvClass, null, calculateShortLabel(cvTerm));
        cvObject.addXref(createIdentityXref(cvObject, cvTerm));

        cvObject.setFullName(cvTerm.getFullName());

        for (CvTermXref cvTermXref : cvTerm.getXrefs()) {
            final CvObjectXref xref = toXref(cvObject, cvTermXref);
            if (xref != null) {
                cvObject.addXref(xref);
            }
        }

        for (CvTermAnnotation cvTermAnnot : cvTerm.getAnnotations()) {
            Annotation annot = toAnnotation(cvTermAnnot);
            if (annot != null) {
                cvObject.addAnnotation(annot);
            }
        }

        // definition
        if (cvTerm.getDefinition() != null) {
            CvTopic definitionTopic = CvObjectUtils.createCvObject(institution, CvTopic.class, null, CvTopic.DEFINITION);
            cvObject.addAnnotation(new Annotation(institution, definitionTopic, cvTerm.getDefinition()));
        }

        processed.put(processedKey, cvObject);

        if (cvObject instanceof CvDagObject) {
            for (CvTerm cvTermChild : cvTerm.getChildren()) {
                // exclude obsoletes
                if (excludeObsolete && cvTermChild.isObsolete()) {
                    continue;
                }

                addCvObjectToStatsIfObsolete(cvTerm);
                
                CvDagObject dagObject = (CvDagObject)cvObject;
                dagObject.getChildren().add((CvDagObject)toCvObject(cvClass, cvTermChild));
            }
        }

        return cvObject;
    }

    private <T extends CvObject> String cvKey(Class<T> cvClass, String primaryId) {
        return cvClass.getSimpleName() + ":" + primaryId;
    }

    protected String calculateShortLabel(CvTerm cvTerm) {
        String shortLabel = cvTerm.getShortName();

        if (shortLabel.length() > 20) {
            shortLabel = shortLabel.substring(0, 20);
        }
        
        return shortLabel;
    }


    protected CvObjectXref createIdentityXref(CvObject parent, CvTerm cvTerm) {
        CvObjectXref idXref;

        String primaryId = cvTerm.getId();

        if (primaryId.startsWith("MI")) {
            idXref = XrefUtils.createIdentityXrefPsiMi(parent, cvTerm.getId());
            idXref.prepareParentMi();
        } else {
            idXref = XrefUtils.createIdentityXref(parent, cvTerm.getId(), nonMiCvDatabase);
        }

        return idXref;
    }

    protected CvObjectXref toXref(CvObject parent, CvTermXref termXref) {
        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvXrefQualifier qualifier = getCvObjectByLabel(CvXrefQualifier.class, termXref.getQualifier());
        CvDatabase database = getCvObjectByLabel(CvDatabase.class, termXref.getDatabase());

        if (qualifier == null || database == null) {
            if (CvDatabase.PUBMED.equals(termXref.getDatabase())) {
                qualifier = CvObjectUtils.createCvObject(owner, CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE);
                database = CvObjectUtils.createCvObject(owner, CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED);
            } else if (CvDatabase.GO.equals(termXref.getDatabase())) {
                qualifier = CvObjectUtils.createCvObject(owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, CvXrefQualifier.IDENTITY);
                database = CvObjectUtils.createCvObject(owner, CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
            } else if (CvDatabase.RESID.equals(termXref.getDatabase())) {
                qualifier = CvObjectUtils.createCvObject(owner, CvXrefQualifier.class, CvXrefQualifier.SEE_ALSO_MI_REF, CvXrefQualifier.SEE_ALSO);
                database = CvObjectUtils.createCvObject(owner, CvDatabase.class, CvDatabase.RESID_MI_REF, CvDatabase.RESID);
            } else if (CvDatabase.SO.equals(termXref.getDatabase())) {
                qualifier = CvObjectUtils.createCvObject(owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, CvXrefQualifier.IDENTITY);
                database = CvObjectUtils.createCvObject(owner, CvDatabase.class, CvDatabase.SO_MI_REF, CvDatabase.SO);
            } else {
                log.error("Unexpected combination qualifier-database found on xref: " + termXref.getQualifier() + " - " + termXref.getDatabase());
                return null;
            }
        }

        return XrefUtils.createIdentityXref(parent, termXref.getId(), qualifier, database);
    }

    protected Annotation toAnnotation(CvTermAnnotation termAnnot) {
        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvTopic topic = getCvObjectByLabel(CvTopic.class, termAnnot.getTopic());

        if (topic == null) {
            if (CvTopic.URL.equals(termAnnot.getTopic())) {
                topic = CvObjectUtils.createCvObject(owner, CvTopic.class, CvTopic.URL_MI_REF, CvTopic.URL);
            } else if (CvTopic.SEARCH_URL.equals(termAnnot.getTopic())) {
                topic = CvObjectUtils.createCvObject(owner, CvTopic.class, CvTopic.SEARCH_URL_MI_REF, CvTopic.SEARCH_URL);
            } else if (CvTopic.XREF_VALIDATION_REGEXP.equals(termAnnot.getTopic())) {
                topic = CvObjectUtils.createCvObject(owner, CvTopic.class, CvTopic.XREF_VALIDATION_REGEXP_MI_REF, CvTopic.XREF_VALIDATION_REGEXP);
            } else if (CvTopic.COMMENT.equals(termAnnot.getTopic())) {
                topic = CvObjectUtils.createCvObject(owner, CvTopic.class, CvTopic.COMMENT_MI_REF, CvTopic.COMMENT);
            } else if (CvTopic.OBSOLETE.equals(termAnnot.getTopic()) || CvTopic.OBSOLETE_OLD.equals(termAnnot.getTopic())) {
                topic = CvObjectUtils.createCvObject(owner, CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE);
                topic.setFullName(CvTopic.OBSOLETE);
            } else {
                log.error("Unexpected topic found on annotation: "+termAnnot.getTopic());
                return null;
            }
        } 

        return new Annotation(owner, topic, termAnnot.getAnnotation());
    }

    protected <T extends CvObject> T getCvObjectByLabel(Class<T> cvObjectClass, String label) {
        for (CvObject cvObject : processed.values()) {
            if (cvObjectClass.isAssignableFrom(cvObject.getClass()) && label.equals(cvObject.getShortLabel())) {
                return (T) cvObject;
            }
        }

        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao(cvObjectClass).getByShortLabel(cvObjectClass, label);
    }

    private DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    public boolean isExcludeObsolete() {
        return excludeObsolete;
    }

    public void setExcludeObsolete(boolean excludeObsolete) {
        this.excludeObsolete = excludeObsolete;
    }

    public CvDatabase getNonMiCvDatabase() {
        return nonMiCvDatabase;
    }

    public void setNonMiCvDatabase(CvDatabase nonMiCvDatabase) {
        this.nonMiCvDatabase = nonMiCvDatabase;
    }
}
