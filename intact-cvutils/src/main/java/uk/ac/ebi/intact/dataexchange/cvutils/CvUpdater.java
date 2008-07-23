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
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.core.persister.stats.StatsUnit;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

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


    public CvUpdater() throws IOException, OBOParseException {
        this.nonMiCvDatabase = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                             CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT );
        this.processed = new HashMap<String, CvObject>();
        this.stats = new CvUpdaterStatistics();
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
    public CvUpdaterStatistics createOrUpdateCVs( List<CvDagObject> allCvs ) {
        return createOrUpdateCVs( allCvs, new AnnotationInfoDataset() );
    }

    /**
     * Starts the creation and update of CVs by using the CVobject List provided
     *
     * @param allValidCvs           List of all valid  Cvs
     * @param annotationInfoDataset A seperate dataset specific for intact
     * @return An object containing some statistics about the update
     */
    public CvUpdaterStatistics createOrUpdateCVs( List<CvDagObject> allValidCvs, AnnotationInfoDataset annotationInfoDataset ) {

        if ( allValidCvs == null ) {
            throw new IllegalArgumentException( "You must give a non null collection of CvDagObject" );
        }
        if ( annotationInfoDataset == null ) {
            throw new IllegalArgumentException( "You must give a non null AnnotationInfoDataset" );
        }

        List<CvDagObject> orphanCvList = dealWithOrphans( allValidCvs );

        if ( log.isDebugEnabled() ) log.debug( "Orphan count: " + orphanCvList.size() );

        List<CvDagObject> cleanedList = ( List<CvDagObject> ) CollectionUtils.subtract( allValidCvs, orphanCvList );

        if (log.isDebugEnabled()) log.debug( "Cleaned list size: " + cleanedList.size() );

         // update the cvs using the annotation info dataset
        updateCVsUsingAnnotationDataset( cleanedList, annotationInfoDataset );

        CorePersister corePersister = new CorePersister();
        corePersister.setUpdateWithoutAcEnabled(true);

        PersisterStatistics persisterStats = PersisterHelper.saveOrUpdate( corePersister, cleanedList.toArray( new CvObject[cleanedList.size()] ) );
        addCvObjectsToUpdaterStats( persisterStats, stats );

        if ( log.isDebugEnabled() ) {
            log.debug( "Persisted: " + persisterStats );
            log.debug( "Processed: " + processed.size() );
            log.debug( stats );
        }

        return stats;
    } //end method

    private List<CvDagObject> dealWithOrphans( List<CvDagObject> allCvs ) {

        List<CvDagObject> orphanCvList = new ArrayList<CvDagObject>();

        List<CvDagObject> alreadyExistingObsoleteCvList = new ArrayList<CvDagObject>();

        for ( CvDagObject cvDag : allCvs ) {
            if ( !isRootObject( CvObjectUtils.getIdentity( cvDag ) ) ) {

                if ( cvDag.getParents() == null || cvDag.getParents().isEmpty() ) {
                    addCvObjectToStatsIfObsolete( cvDag );

                    boolean cvObjectsWithSameId = checkAndMarkAsObsoleteIfExisted( cvDag, stats );

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

    private void updateCVsUsingAnnotationDataset( List<CvDagObject> allCvs, AnnotationInfoDataset annotationInfoDataset ) {

        for ( CvDagObject cvObject : allCvs ) {
            final String identity = CvObjectUtils.getIdentity( cvObject );
            if ( identity != null && annotationInfoDataset.containsCvAnnotation( identity ) ) {
                AnnotationInfo annotInfo = annotationInfoDataset.getCvAnnotation( identity );

                CvTopic topic = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                              CvTopic.class,
                                                              null,
                                                              annotInfo.getTopicShortLabel() );
                PersisterHelper.saveOrUpdate( topic );

                Annotation annotation = new Annotation( IntactContext.getCurrentInstance().getInstitution(), topic, annotInfo.getReason() );
                addAnnotation( annotation, cvObject, annotInfo.isApplyToChildren() );

            }
        }
    }


    private void addAnnotation( Annotation annotation, CvDagObject cvObject, boolean includeChildren ) {
        boolean containsAnnotation = false;

        for ( Annotation annot : cvObject.getAnnotations() ) {

            if ( annot.getCvTopic().getShortLabel().equals( annotation.getCvTopic().getShortLabel() ) ) {
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

                    PersisterHelper.saveOrUpdate( obsoleteTopic );
                    
                    stats.addCreatedCv( obsoleteTopic );
                } //end if
            }//end for
        }//end if
        return alreadyExistingCv;
    }

    private void addObsoleteAnnotation( CvObject existingCv, String obsoleteMessage ) {
        obsoleteTopic.addAnnotation( new Annotation( existingCv.getOwner(), obsoleteTopic, obsoleteMessage ) );
    }

    private CvTopic createCvTopicObsolete() {
        if ( obsoleteTopic != null ) {
            return obsoleteTopic;
        }

        obsoleteTopic = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao( CvTopic.class ).getByPsiMiRef( CvTopic.OBSOLETE_MI_REF );

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
