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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOObject;
import uk.ac.ebi.intact.context.IntactContext;
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
import java.net.URL;
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

    private static Log log = LogFactory.getLog(CvUpdater.class);

    private Map<String,CvObject> processed;
    private CvUpdaterStatistics stats;
    private List<CvDagObject> allCvs;

   
    private CvDatabase nonMiCvDatabase;

    private CvTopic obsoleteTopic;


    public CvUpdater() throws IOException, OBOParseException {
        this.nonMiCvDatabase = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(),
                CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT);

        URL url = CvUpdater.class.getResource( "/psi-mi25.obo" );
        log.info("url "+ url );



        this.processed = new HashMap<String,CvObject>();
        this.stats = new CvUpdaterStatistics();


    }//end constructor

    /**
     * Starts the creation and update of CVs by using the ontology provided
     * @param allCvs List of all Cvs
     * @return An object containing some statistics about the update
     */
    public CvUpdaterStatistics createOrUpdateCVs(List<CvDagObject> allCvs) {
        return createOrUpdateCVs(allCvs, new AnnotationInfoDataset());
    }


    public List<CvDagObject> getAllCvsAsList(CvObjectOntologyBuilder ontologyBuilder){

               if (ontologyBuilder == null) {
                   throw new NullPointerException("ontologyBuilder");
               }



               //until here
               List<CvObject> rootsAndOrphans = new ArrayList<CvObject>();
               Collection<IdentifiedObject> rootOboObjects=ontologyBuilder.getRootOBOObjects();

        for ( IdentifiedObject rootOboObject : rootOboObjects ) {
            OBOObject rootObject = ( OBOObject ) rootOboObject;
            CvObject cvObjectRoot = ontologyBuilder.toCvObject( rootObject );
            rootsAndOrphans.add( cvObjectRoot );

        }//end for

               log.debug("rootsAndOrphans size :"+rootsAndOrphans.size());


              
               CvTopic obsoleteTopic = createCvTopicObsolete();

        if (obsoleteTopic.getAc() != null) {
            rootsAndOrphans.add(obsoleteTopic);
        }

               for (IdentifiedObject orphanObo : ontologyBuilder.getOrphanOBOObjects()) {
                   if (orphanObo instanceof OBOObject) {
                       OBOObject orphanObj=(OBOObject)orphanObo;
                       CvObject cvOrphan=ontologyBuilder.toCvObject(orphanObj);


                       int cvObjectsWithSameId = checkAndMarkAsObsoleteIfExisted(cvOrphan, stats);


                       String defText=orphanObj.getDefinition();
                       if(orphanObj.isObsolete() || defText.contains("\nOBSOLETE")){
                           addCvObjectToStatsIfObsolete(cvOrphan);
                          // continue;
                       }
                       // check if it is valid and persist
                       if (cvObjectsWithSameId == 0) {
                           if (isValidTerm(cvOrphan)) {
                               //CvTopic cvOrphan = toCvObject(CvTopic.class, orphan);
                               stats.addOrphanCv(cvOrphan);

                               rootsAndOrphans.add(cvOrphan);
                           }
                       }
                   }

                  // stats.addObsoleteCv(obsoleteTopic);
                  // stats.addOrphanCv(obsoleteTopic);
               }//end for
                   //until here


             //deal with Invalid Terms seperately

               for (IdentifiedObject invalidObo : ontologyBuilder.getInvalidOBOObjects()) {
                  if (invalidObo instanceof OBOObject) {
                           OBOObject invalidObj=(OBOObject)invalidObo;
                   stats.getInvalidTerms().put(invalidObj.getID(), invalidObj.getName());
                  }
               } //



              log.info("rootsAndOrphans size() "+rootsAndOrphans.size());
               allCvs = new ArrayList<CvDagObject>();
               for (CvObject rootOrOrphan : rootsAndOrphans) {
                   allCvs.addAll(itselfAndChildrenAsList((CvDagObject) rootOrOrphan));
               }

               log.info("all Cvs size() "+allCvs.size());
               setAllCvs(allCvs);
               //until here


      return allCvs;


    } //end of method


    /**
     * Starts the creation and update of CVs by using the CVobject List provided
     * @param allCvs   List of all Cvs
     * @param annotationInfoDataset    A seperate dataset specific for intact
     * @return An object containing some statistics about the update
     */


    public CvUpdaterStatistics createOrUpdateCVs(List<CvDagObject> allCvs, AnnotationInfoDataset annotationInfoDataset) {

        if (allCvs == null) {
            throw new NullPointerException("Cvs Null");
        }
        if (annotationInfoDataset == null) {
            throw new NullPointerException("annotationDataset");
        }

       // process any term from the cv annotations dataset resource
        updateCVsUsingAnnotationDataset(allCvs, annotationInfoDataset);



        //PersisterStatistics persisterStats = PersisterHelper.saveOrUpdate(allCvs.toArray(new CvObject[rootsAndOrphans.size()]));
        PersisterStatistics persisterStats = PersisterHelper.saveOrUpdate(allCvs.toArray(new CvObject[allCvs.size()]));
        addCvObjectsToUpdaterStats(persisterStats, stats);

        if (log.isDebugEnabled()) {
            log.debug("Persisted: " + persisterStats);
            log.debug("Processed: "+  processed.size());
            log.debug(stats);
        }




        return stats;
    } //end method

    private void updateCVsUsingAnnotationDataset(List<CvDagObject> allCvs, AnnotationInfoDataset annotationInfoDataset) {
        CvTopic hidden = CvObjectUtils.createCvObject(IntactContext.getCurrentInstance().getInstitution(),
                CvTopic.class, null, CvTopic.HIDDEN);

        for (CvDagObject cvObject : allCvs) {
            if (CvObjectUtils.getIdentity(cvObject) != null && annotationInfoDataset.containsCvAnnotation(cvObject.getMiIdentifier())) {
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

    private int checkAndMarkAsObsoleteIfExisted(CvObject orphan, CvUpdaterStatistics stats) {
        String id = CvObjectUtils.getIdentity( orphan );

        final CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao();
        Collection<CvObject> existingCvs = cvObjectDao.getByPsiMiRefCollection(Collections.singleton(id));

        if (existingCvs.isEmpty()) {
            existingCvs = cvObjectDao.getByShortLabelLike(orphan.getShortLabel());
        }


        if (!existingCvs.isEmpty()) {
            CvTopic obsoleteTopic = createCvTopicObsolete();

            for (CvObject existingCv : existingCvs) {
                boolean alreadyContainsObsolete = false;

                for (Annotation annot : existingCv.getAnnotations()) {
                    if (CvTopic.OBSOLETE_MI_REF.equals(CvObjectUtils.getIdentity(annot.getCvTopic()))) {
                        alreadyContainsObsolete = true;
                    }
                }

                if (!alreadyContainsObsolete) {
                    //get Annotations and find Def for Obsolote
                    String annotatedText=null;
                    for (Annotation annot:  orphan.getAnnotations()){
                        if (CvTopic.OBSOLETE_MI_REF.equals(CvObjectUtils.getIdentity(annot.getCvTopic()))) {
                            annotatedText=annot.getAnnotationText();
                        }
                    }


                    existingCv.addAnnotation(new Annotation(existingCv.getOwner(), obsoleteTopic, annotatedText));
                    stats.addUpdatedCv(existingCv);
                }
            }
        }
        return existingCvs.size();
    }

    private void addObsoleteAnnotation(CvObject existingCv, String obsoleteMessage) {
        obsoleteTopic.addAnnotation(new Annotation(existingCv.getOwner(), obsoleteTopic, obsoleteMessage));
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
            addObsoleteAnnotation(obsoleteTopic, "Deprecated CV term that should not be used to annotate entries");
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

    private void addCvObjectToStatsIfObsolete(CvObject cvObj) {

        stats.getObsoleteCvs().put(CvObjectUtils.getIdentity( cvObj ), cvObj.getShortLabel());

    }

    protected boolean isValidTerm(CvObject term) {
        return CvObjectUtils.getIdentity( term ).contains(":");
    }

   

    public CvDatabase getNonMiCvDatabase() {
        return nonMiCvDatabase;
    }

    public void setNonMiCvDatabase(CvDatabase nonMiCvDatabase) {
        this.nonMiCvDatabase = nonMiCvDatabase;
    }

    public List<CvDagObject> getAllCvs() {
        return allCvs;
    }

    public void setAllCvs(List<CvDagObject> allCvs) {
        this.allCvs = allCvs;
    }


}   //end class
