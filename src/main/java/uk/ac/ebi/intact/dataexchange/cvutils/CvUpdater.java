/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.intact.dataexchange.cvutils;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.OBOSession;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.annotations.IntactFlushMode;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.core.persister.stats.StatsUnit;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import javax.annotation.PostConstruct;
import javax.persistence.FlushModeType;
import java.io.IOException;
import java.util.*;

/**
 * Contains method to Update Cv tables using the PersisterHelper.
 * Basically passes a list of CvDagObjects with all the children
 * and parents set to the saveorUpdate method in the PersisterHelper
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class CvUpdater {

    private static Log log = LogFactory.getLog( CvUpdater.class );

    private Map<String, CvObject> processed;

    private CvUpdaterStatistics stats;

    private CvDatabase nonMiCvDatabase;

    private CvTopic obsoleteTopic;

    private IntactContext intactContext;

    private PersisterHelper persisterHelper;

    /**
     * This map is used internally to keep track of the created topics during the update.
     */
    private Map<String,CvTopic> createdNonMiTopics = new HashMap<String,CvTopic>();


    protected CvUpdater() throws IOException, OBOParseException {
        if (!IntactContext.currentInstanceExists()) {
            throw new IllegalStateException("To instantiate a CvUpdated using no arguments, an instance of IntactContext must exist");
        }

        this.processed = new HashMap<String, CvObject>();
        this.stats = new CvUpdaterStatistics();
        this.intactContext = IntactContext.getCurrentInstance();
        this.persisterHelper = intactContext.getPersisterHelper();
    }


    protected CvUpdater(IntactContext intactContext) {
        this.processed = new HashMap<String, CvObject>();
        this.stats = new CvUpdaterStatistics();
        this.intactContext = intactContext;
        this.persisterHelper = intactContext.getPersisterHelper();
    }

    public static CvUpdater createInstance(IntactContext intactContext) {
        return (CvUpdater) intactContext.getSpringContext().getBean("cvUpdater");
    }

    @PostConstruct
    private void init() {
        this.nonMiCvDatabase = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                             CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT );
    }


    protected Map<String, Class> getMapOfExistingCvs() {

        Map<String, Class> existingMi2Class = new HashMap<String, Class>();
        List<CvObject> allExistingCvs = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getAll();

        for ( CvObject cvObject : allExistingCvs ) {
            existingMi2Class.put( CvObjectUtils.getIdentity( cvObject ), cvObject.getClass() );
        }

        return existingMi2Class;
    }

    /**
     * Starts the creation and update of CVs by using the ontology provided
     *
     * @param allCvs List of all Cvs
     * @return An object containing some statistics about the update
     */
    @Transactional
    @IntactFlushMode(FlushModeType.COMMIT)
    public CvUpdaterStatistics createOrUpdateCVs( List<CvDagObject> allCvs ) {
        return createOrUpdateCVs( allCvs, new AnnotationInfoDataset() );
    }

     /**
     * Starts the creation and update of CVs by using the latest available CVs from internet
     *
     * @return An object containing some statistics about the update
     */
    @Transactional
    @IntactFlushMode(FlushModeType.COMMIT)
    public CvUpdaterStatistics executeUpdateWithLatestCVs() throws IOException{
         OBOSession oboSession = null;
         try {
             oboSession = OboUtils.createOBOSessionFromLatestMi();
         } catch (OBOParseException e) {
             throw new IOException("Problem creating OBO session from latest MI: "+e.getMessage());
         }

         AnnotationInfoDataset annotationInfoDataset = OboUtils.createAnnotationInfoDatasetFromLatestResource();
         return executeUpdate(oboSession, annotationInfoDataset);
     }

    @Transactional
    @IntactFlushMode(FlushModeType.COMMIT)
    public CvUpdaterStatistics executeUpdate(OBOSession oboSession, AnnotationInfoDataset annotationInfoDataset) throws IOException{
         CvObjectOntologyBuilder builder = new CvObjectOntologyBuilder(oboSession);
         return createOrUpdateCVs(builder.getAllCvs(), annotationInfoDataset);
    }

    @Transactional
    @IntactFlushMode(FlushModeType.COMMIT)
    public CvUpdaterStatistics executeUpdate(OBOSession oboSession) throws IOException{
         return executeUpdate(oboSession, new AnnotationInfoDataset());
     }

    /**
     * Starts the creation and update of CVs by using the CVobject List provided
     *
     * @param allValidCvs           List of all valid  Cvs
     * @param annotationInfoDataset A seperate dataset specific for intact
     * @return An object containing some statistics about the update
     */
    @Transactional
    @IntactFlushMode(FlushModeType.COMMIT)
    public CvUpdaterStatistics createOrUpdateCVs( List<CvDagObject> allValidCvs, AnnotationInfoDataset annotationInfoDataset ) {

        if ( allValidCvs == null ) {
            throw new IllegalArgumentException( "You must give a non null collection of CvDagObject" );
        }
        if ( annotationInfoDataset == null ) {
            throw new IllegalArgumentException( "You must give a non null AnnotationInfoDataset" );
        }

        List<CvDagObject> alreadyExistingObsoleteCvList = new ArrayList<CvDagObject>();
        List<CvDagObject> orphanCvList = dealWithOrphans( allValidCvs,alreadyExistingObsoleteCvList );

        if ( log.isDebugEnabled() ){
            log.debug( "Orphan count: " + orphanCvList.size() );
            log.debug( "AlreadyExisting cvs annotated with Obsolete: "+alreadyExistingObsoleteCvList.size() );
        }

        //first step remove the orphan cvs that are not existing in database
        List<CvDagObject> cleanedList = ( List<CvDagObject> ) CollectionUtils.subtract( allValidCvs, orphanCvList );
        if (log.isDebugEnabled()) log.debug( "Size of CV list after removing orphans: " + cleanedList.size() );
        //second step remove the orphan cvs that are are already existing in the database
        cleanedList = ( List<CvDagObject> ) CollectionUtils.subtract( cleanedList, alreadyExistingObsoleteCvList );

        if (log.isDebugEnabled()) log.debug( "Size of CV list after removing obsolete terms: " + cleanedList.size() );

        CorePersister corePersister = persisterHelper.getCorePersister();
        corePersister.setUpdateWithoutAcEnabled(true);

        updateCVsUsingAnnotationDataset( cleanedList, annotationInfoDataset, corePersister );

        CvObject[] cvObjects = cleanedList.toArray(new CvObject[cleanedList.size()]);

        corePersister.saveOrUpdate(cvObjects);

        PersisterStatistics persisterStats = corePersister.getStatistics();
        
        addCvObjectsToUpdaterStats( persisterStats, stats );

        if ( log.isDebugEnabled() ) {
            log.debug( "Persisted: " + persisterStats );
            log.debug( "Processed: " + processed.size() );
            log.debug( stats );
        }

        return stats;
    } //end method

    private List<CvDagObject> dealWithOrphans( List<CvDagObject> allCvs,List<CvDagObject> alreadyExistingObsoleteCvList) {

        List<CvDagObject> orphanCvList = new ArrayList<CvDagObject>();

        for ( CvDagObject cvDag : allCvs ) {
            if ( !isRootObject( CvObjectUtils.getIdentity( cvDag ) ) ) {

                if ( cvDag.getParents() == null || cvDag.getParents().isEmpty() ) {
                    addCvObjectToStatsIfObsolete( cvDag );

                    boolean cvObjectsWithSameId = false;
                    try {
                        cvObjectsWithSameId = checkAndMarkAsObsoleteIfExisted( cvDag, stats );
                    } catch (Throwable e) {
                        throw new RuntimeException("Problem checking orphan: "+cvDag.getShortLabel()+" ("+cvDag.getIdentifier()+")", e);
                    }

                    if ( cvObjectsWithSameId ) {
                        alreadyExistingObsoleteCvList.add( cvDag );
                    } else {
                        if ( !CvObjectUtils.getIdentity( cvDag ).equalsIgnoreCase( CvTopic.OBSOLETE_MI_REF ) ) {
                            stats.addOrphanCv( cvDag );
                            orphanCvList.add( cvDag );
                        }
                    }
                }//end of if
            }//end if
        }//end for

        return orphanCvList;
    }


    private boolean isRootObject( String identity ) {
        return CvObjectOntologyBuilder.mi2Class.keySet().contains( identity );
    }

    protected void updateCVsUsingAnnotationDataset(List<CvDagObject> allCvs, AnnotationInfoDataset annotationInfoDataset, CorePersister corePersister) {

        for ( CvDagObject cvObject : allCvs ) {
            final String identity = CvObjectUtils.getIdentity( cvObject );
            if ( identity != null && annotationInfoDataset.containsCvAnnotation( identity ) ) {
                AnnotationInfo annotInfo = annotationInfoDataset.getCvAnnotation( identity );

                // check if the topic has already been processed
                CvTopic topic = createdNonMiTopics.get(annotInfo.getTopicShortLabel());

                // if not, try to get it from the database
                if (topic == null) {
                    topic = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                                .getCvObjectDao(CvTopic.class).getByShortLabel(CvTopic.class, annotInfo.getTopicShortLabel());

                    // if it is not in the database, create it and persist it.
                    if (topic == null) {
                        topic = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                                  CvTopic.class,
                                                                  null,
                                                                  annotInfo.getTopicShortLabel() );

                        corePersister.saveOrUpdate(topic);

                    }
                    // now it has been created
                    createdNonMiTopics.put(annotInfo.getTopicShortLabel(), topic);
                }

                // create the corresponding annotation
                Annotation annotation = new Annotation( IntactContext.getCurrentInstance().getInstitution(), topic, annotInfo.getReason() );
                addAnnotation( annotation, cvObject, annotInfo.isApplyToChildren() );
            }
        }
    }

    private boolean areEqual(CvObject cv1, CvObject cv2) {
        if ( cv1 == null || cv2 == null ) {
            return false;
        }

        if (cv1.getIdentifier() != null && cv2.getIdentifier() != null) {
            return cv1.getIdentifier().equals(cv2.getIdentifier());
        }

        return cv1.getShortLabel().equals(cv2.getShortLabel());
    }

    private void addAnnotation( Annotation annotation, CvDagObject cvObject, boolean includeChildren ) {
        boolean containsAnnotation = false;

        for ( Annotation annot : cvObject.getAnnotations() ) {

            if (this.areEqual(annot.getCvTopic(),annotation.getCvTopic())) {
                if ( annot.getAnnotationText() != null
                     && annot.getAnnotationText().equals( annotation.getAnnotationText() ) ) {
                    containsAnnotation = true;
                    break;
                } else{
                     //update
                    annot.setAnnotationText( annotation.getAnnotationText() );
                    IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().update( annot );
                    containsAnnotation = true;
                    break;
                }
            }

        }

        if ( !containsAnnotation ) {
            cvObject.getAnnotations().add( annotation );

            if ( log.isDebugEnabled() ) {
                final CvTopic topic = annotation.getCvTopic();
                log.debug( "Added Annotation(" + topic.getShortLabel() + ", '" + annotation.getAnnotationText() +
                           "') onto " + cvObject.getClass().getSimpleName() + "(" + cvObject.getShortLabel() +
                           ")" );
            }
        }

        if ( includeChildren ) {
            for ( CvDagObject child : cvObject.getChildren() ) {
                addAnnotation( annotation, child, includeChildren );
            }
        }
    }

    /**
     * This method should check if the below constraint is violated
     * CONSTRAINT_INDEX_0 ON PUBLIC.IA_CONTROLLEDVOCAB(OBJCLASS, SHORTLABEL)
     *
     * @param allValidCvs List of all Uniq Cvs
     * @return true if violated or false if not
     */

    public boolean isConstraintViolated( List<CvDagObject> allValidCvs ) {
        if ( allValidCvs == null ) {
            throw new NullPointerException( "Cvs Null" );
        }

        Bag hashBag = new HashBag();
        for ( CvDagObject cvDag : allValidCvs ) {
            String primaryKey = cvDag.getObjClass().toString() + ":" + cvDag.getShortLabel();
            if ( log.isTraceEnabled() ) {
                log.trace( "PrimaryKey: " + primaryKey );
            }

            hashBag.add( primaryKey );
        }

        for ( Object aHashBag : hashBag ) {
            String s = ( String ) aHashBag;
            if ( hashBag.getCount( s ) > 1 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Constraint violated by " + s );
                }

                return true;
            }
        }
        
        return false;
    }


    protected boolean checkAndMarkAsObsoleteIfExisted( CvObject orphan, CvUpdaterStatistics stats ) {
        boolean alreadyExistingCv = false;
        String id = CvObjectUtils.getIdentity( orphan );
        Set<String> existingKeys = getMapOfExistingCvs().keySet();

        if ( existingKeys != null && existingKeys.contains( id ) ) {
            alreadyExistingCv = true;
        }

        final CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        Collection<CvObject> existingCvs = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id ) );

        if ( existingCvs.isEmpty() ) {
            existingCvs = cvObjectDao.getByShortLabelLike( orphan.getShortLabel() );
        }

        if ( alreadyExistingCv ) {
            CvTopic obsoleteTopic = createCvTopicObsolete();

            for ( CvObject existingCv : existingCvs ) {
                boolean alreadyContainsObsolete = false;

                for ( Annotation annot : existingCv.getAnnotations() ) {
                    if ( CvTopic.OBSOLETE_MI_REF.equals( CvObjectUtils.getIdentity( annot.getCvTopic() ) ) ) {
                        alreadyContainsObsolete = true;
                    }  //end if
                } //end for

                if ( !alreadyContainsObsolete ) {
                    //get Annotations and find Def for Obsolote
                    String annotatedText = null;
                    for ( Annotation annot : orphan.getAnnotations() ) {
                        if ( CvTopic.OBSOLETE_MI_REF.equals( CvObjectUtils.getIdentity( annot.getCvTopic() ) ) ) {
                            annotatedText = annot.getAnnotationText();
                        }
                    }//end for

                    if ( log.isDebugEnabled() ) log.debug( "Updating CV - adding obsolete annotation to: " + existingCv );

                    final Annotation annotation = new Annotation( existingCv.getOwner(), obsoleteTopic, annotatedText );
                    existingCv.addAnnotation( annotation );
                    stats.addUpdatedCv( existingCv );

                    if (obsoleteTopic.getAc() == null) {
                        try {
                            persisterHelper.save( obsoleteTopic );
                        } catch ( Throwable t ) {
                            throw new PersisterException( "An error occurred while saving CvTopic( '"+ obsoleteTopic.getShortLabel() +"', '"+ obsoleteTopic.getIdentifier() +"' )", t );
                        }
                        stats.addCreatedCv( obsoleteTopic );
                    }

                } //end if
            }//end for
        }//end if
        return alreadyExistingCv;
    }

    private void addObsoleteAnnotation( CvObject existingCv, String obsoleteMessage ) {
        obsoleteTopic.addAnnotation( new Annotation( existingCv.getOwner(), obsoleteTopic, obsoleteMessage ) );
    }

    private CvTopic createCvTopicObsolete() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

        if ( obsoleteTopic != null ) {
            if (daoFactory.getBaseDao().isTransient(obsoleteTopic) && obsoleteTopic.getAc() != null) {
                obsoleteTopic = daoFactory.getCvObjectDao( CvTopic.class ).getByAc( obsoleteTopic.getAc() );
            }
        } else {
            obsoleteTopic = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( CvTopic.OBSOLETE_MI_REF );
        }

        if ( obsoleteTopic == null ) {
            // create the obsolete term (which is obsolete too!)
            obsoleteTopic = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(), CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE );
            obsoleteTopic.setFullName( "obsolete term" );
            addObsoleteAnnotation( obsoleteTopic, "Deprecated CV term that should not be used to annotate entries" );
        }

        return obsoleteTopic;
    }

    private void addCvObjectsToUpdaterStats( PersisterStatistics persisterStats, CvUpdaterStatistics stats ) {
        for ( StatsUnit statsUnit : persisterStats.getPersisted( CvObject.class, true ) ) {
            stats.getCreatedCvs().put( statsUnit.getType(), statsUnit );
        }

        for ( StatsUnit statsUnit : persisterStats.getMerged( CvObject.class, true ) ) {
            stats.getUpdatedCvs().put( statsUnit.getType(), statsUnit );
        }
    }

    private void addCvObjectToStatsIfObsolete( CvObject cvObj ) {
        stats.getObsoleteCvs().put( CvObjectUtils.getIdentity( cvObj ), cvObj.getShortLabel() );
    }

    public CvDatabase getNonMiCvDatabase() {
        return nonMiCvDatabase;
    }

    public void setNonMiCvDatabase( CvDatabase nonMiCvDatabase ) {
        this.nonMiCvDatabase = nonMiCvDatabase;
    }
}
