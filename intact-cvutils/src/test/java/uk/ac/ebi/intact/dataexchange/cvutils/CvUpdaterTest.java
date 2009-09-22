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


import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.annotations.IntactFlushMode;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDatasetFactory;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import javax.persistence.FlushModeType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Test class for CvUpdater
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */

public class CvUpdaterTest extends IntactBasicTestCase {

    @Autowired
    private CvUpdater cvUpdater;
    
    @Autowired
    private PersisterHelper persisterHelper;

    @Test
    public void reportDirectlyFromOBOFile() throws Exception {

        BufferedReader in = new BufferedReader( new InputStreamReader( CvUpdaterTest.class.getResourceAsStream("/ontologies/psi-mi25-1_51.obo" ) ) );

        String inputLine;

        int termCounter = 0;
        int idCounter = 0;
        int miCounter = 0;
        int obsoleteCounter = 0;
        int obsoleteCounterDef = 0;
        int typedefCounter = 0;

        int drugTerm = 0;
        int psiTerm = 0;

        while ( ( inputLine = in.readLine() ) != null ) {
            if ( inputLine.startsWith( "[Term]" ) ) termCounter++;
            if ( inputLine.startsWith( "id:" ) ) idCounter++;
            if ( inputLine.matches( "id:\\s+MI:.*" ) ) miCounter++;
            if ( inputLine.contains( "is_obsolete: true" ) ) obsoleteCounter++;
            if ( inputLine.matches( "def:.*?OBSOLETE.*" ) ) obsoleteCounterDef++;
            if ( inputLine.startsWith( "[Typedef]" ) ) typedefCounter++;
            if ( inputLine.matches( "subset:\\s+PSI-MI\\s+slim" ) ) psiTerm++;
            if ( inputLine.matches( "subset:\\s+Drugable" ) ) drugTerm++;
        }

        //948+1 with Typedef
        Assert.assertEquals( 959, idCounter );
        Assert.assertEquals( 958, termCounter );
        Assert.assertEquals( 958, miCounter );
        Assert.assertEquals( 54, obsoleteCounter );
        Assert.assertEquals( 54, obsoleteCounterDef );
        Assert.assertEquals( 1, typedefCounter );
        Assert.assertEquals( 854, psiTerm );
        Assert.assertEquals( 125, drugTerm );

        in.close();
    }

    @Test
    public void isConstraintViolatedTest() throws Exception {

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 987, allCvs.size() );

        Assert.assertFalse( cvUpdater.isConstraintViolated( allCvs ) );
    }

    @Test
    public void obsoleteTest() throws Exception {

        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();

        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvDagObject aggregation = CvObjectUtils.createCvObject( owner, CvInteractionType.class, "MI:0191", "aggregation" );
        persisterHelper.save( aggregation );

        List<CvObject> allCvsCommittedAfter = getDaoFactory().getCvObjectDao().getAll();
        int cvsAfterPersist = allCvsCommittedAfter.size();
        //PersisterHelper is adding the intact, psi-mi and other terms+aggregation...
        Assert.assertEquals( cvsBeforeUpdate + 1, cvsAfterPersist );
    }

    @Test
    @DirtiesContext
    public void updatingWithNewAnnotations() throws Exception {

        CvObjectDao<CvObject> cvObjectDao;
        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        final int existingCvsCount = cvObjectDao.countAll();

        final CvAliasType alias = getMockBuilder().createCvObject( CvAliasType.class, "MI:0300", "alias type" );

        final CvTopic topicsParent = getMockBuilder().createCvObject( CvTopic.class, "MI:0590", "attribute name" );
        final CvTopic cvTopic1 = getMockBuilder().createCvObject( CvTopic.class, "MI:0631", "3d-r-factors" );
        final CvTopic cvTopic2 = getMockBuilder().createCvObject( CvTopic.class, "MI:0630", "3d-test" );
        cvTopic1.addParent( topicsParent );
        cvTopic2.addParent( topicsParent );

        persisterHelper.save( alias, topicsParent, cvTopic1, cvTopic2 );

        

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();

        final List<CvDagObject> list = new ArrayList<CvDagObject>();
        final List<CvObject> cvObjectList = cvObjectDao.getAll();
        for ( CvObject cvObject : cvObjectList ) {
            list.add( ( CvDagObject ) cvObject );
        }

        int cvsBeforeUpdate = list.size();
        Assert.assertEquals( existingCvsCount + 4, cvsBeforeUpdate );

        final InputStream is = CvUpdaterTest.class.getResourceAsStream( "/annotations_test.csv" );
        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource( is );

        cvUpdater.createOrUpdateCVs( list, annotationDataset );

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        CvObject topic;

        topic = cvObjectDao.getByShortLabel( CvTopic.class, "3d-test" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 0, topic.getAnnotations().size() );

        topic = cvObjectDao.getByShortLabel( CvTopic.class, "3d-r-factors" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        Assert.assertEquals( "used-in-class", topic.getAnnotations().iterator().next().getCvTopic().getShortLabel() );

        topic = cvObjectDao.getByShortLabel( CvAliasType.class, "alias type" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        Assert.assertEquals( "hidden", topic.getAnnotations().iterator().next().getCvTopic().getShortLabel() );

        final int cvCountAfterUpdate = getDaoFactory().getCvObjectDao().countAll();
        Assert.assertEquals( "3 CvObjects expected to be created during update: hidden, used-in-class and obsolete",
                             cvsBeforeUpdate + 3, cvCountAfterUpdate );
    }

    @Test
    //@DirtiesContext
    public void updatingExistingAnnotations() throws Exception {

        CvObjectDao<CvObject> cvObjectDao;
        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        final int existingCvsCount = cvObjectDao.countAll();

        final CvAliasType alias = getMockBuilder().createCvObject( CvAliasType.class, "MI:0300", "alias type" );

        final CvTopic topicsParent = getMockBuilder().createCvObject( CvTopic.class, "MI:0590", "attribute name" );
        final CvTopic cvTopic1 = getMockBuilder().createCvObject( CvTopic.class, "MI:0631", "3d-r-factors" );
        cvTopic1.addParent( topicsParent );

        Annotation annot = getMockBuilder().createAnnotation( "no type", null, CvTopic.USED_IN_CLASS );
        cvTopic1.addAnnotation( annot );

        final Annotation annotBeforeUpdate = cvTopic1.getAnnotations().iterator().next();
        Assert.assertEquals( "used-in-class", annotBeforeUpdate.getCvTopic().getShortLabel() );
        Assert.assertEquals( "no type", annotBeforeUpdate.getAnnotationText() );

        persisterHelper.save( alias, topicsParent, cvTopic1 );

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();

        final List<CvDagObject> list = new ArrayList<CvDagObject>();
        final List<CvObject> cvObjectList = cvObjectDao.getAll();
        for ( CvObject cvObject : cvObjectList ) {
            list.add( ( CvDagObject ) cvObject );
        }

        int cvsBeforeUpdate = list.size();
        Assert.assertEquals( existingCvsCount + 4, cvsBeforeUpdate );

        final InputStream is = CvUpdaterTest.class.getResourceAsStream( "/annotations_test.csv" );
        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource( is );

        cvUpdater.createOrUpdateCVs( list, annotationDataset );


        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        CvObject topic;

        topic = cvObjectDao.getByShortLabel( CvTopic.class, "3d-r-factors" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        final Annotation annotation = topic.getAnnotations().iterator().next();
        Assert.assertEquals( "used-in-class", annotation.getCvTopic().getShortLabel() );
        //updated no type annotation to Interaction
        Assert.assertEquals( "uk.ac.ebi.intact.model.Interaction", annotation.getAnnotationText() );

        //crated new annotation
        topic = cvObjectDao.getByShortLabel( CvAliasType.class, "alias type" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        Assert.assertEquals( "hidden", topic.getAnnotations().iterator().next().getCvTopic().getShortLabel() );

        final int cvCountAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals( cvsBeforeUpdate + 2, cvCountAfterUpdate );

        //again call cvupdate and this time it should ignore the annotations
        
        cvUpdater.createOrUpdateCVs( list, annotationDataset );
        
        Assert.assertEquals( cvsBeforeUpdate + 2, cvCountAfterUpdate );
    }

    @Test
    @DirtiesContext
    public void obsoleteAggregationTest() throws Exception {

        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();

        //Insert aggregation an obsolete term MI:0191
        Institution owner = IntactContext.getCurrentInstance().getInstitution();
        CvDagObject aggregation = CvObjectUtils.createCvObject( owner, CvInteractionType.class, "MI:0191", "aggregation" );


        persisterHelper.save( aggregation );


        CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        Assert.assertEquals( cvsBeforeUpdate+1, cvObjectDao.countAll() );

        //check if aggregation has obsolote annotation  before createOrUpdateCvs call
        String id_ = CvObjectUtils.getIdentity( aggregation );
        Collection<CvObject> existingCvsBefore = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id_ ) );
        if ( existingCvsBefore.isEmpty() ) {
            existingCvsBefore = cvObjectDao.getByShortLabelLike( aggregation.getShortLabel() );
        }

        Assert.assertNotNull( existingCvsBefore );
        Assert.assertEquals( existingCvsBefore.size(), 1 );
        for ( CvObject existingCv : existingCvsBefore ) {
            Assert.assertEquals( existingCv.getAnnotations().size(), 0 );
        }//end for

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );
        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();

        InputStream is = CvUpdaterTest.class.getResourceAsStream( "/additional-annotations.csv" );

        if ( is == null ) {
            throw new NullPointerException( "InputStream is null" );
        }

        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource( is );

        Assert.assertFalse( cvUpdater.isConstraintViolated( allCvs ) );

        cvUpdater.createOrUpdateCVs( allCvs, annotationDataset );

        
        //check if aggregation has obsolote annotation  after createOrUpdateCvs call
        String id = CvObjectUtils.getIdentity( aggregation );
        Collection<CvObject> existingCvs = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id ) );
        if ( existingCvs.isEmpty() ) {
            existingCvs = cvObjectDao.getByShortLabelLike( aggregation.getShortLabel() );
        }

        int count = 1;
        boolean absoleteTopic = false;
        for ( CvObject existingCv : existingCvs ) {
            count++;
            for ( Annotation annot : existingCv.getAnnotations() ) {
                if ( annot.getCvTopic().getShortLabel().equals( CvTopic.OBSOLETE ) ) {
                    absoleteTopic = true;
                }
            }
        }//end for
        Assert.assertNotNull( existingCvs );
        Assert.assertEquals( existingCvs.size(), 1 );
        Assert.assertEquals( absoleteTopic, true );
    }

    @Test
    @DirtiesContext
    public void createOrUpdateCVsTest() throws Exception {

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvObject> orphanCvs = ontologyBuilder.getOrphanCvObjects();
        Assert.assertEquals( 54, orphanCvs.size() );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 987, allCvs.size() );

        InputStream is = CvUpdaterTest.class.getResourceAsStream( "/additional-annotations.csv" );

        if ( is == null ) {
            throw new NullPointerException( "InputStream is null" );
        }

        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource( is );

        
        
        Assert.assertFalse( cvUpdater.isConstraintViolated( allCvs ) );
        CvUpdaterStatistics stats = cvUpdater.createOrUpdateCVs( allCvs, annotationDataset );
        

        int totalCvsAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals( 938, totalCvsAfterUpdate );
        Assert.assertEquals( 929, stats.getCreatedCvs().size() );

        //54-1 obsolete term
        Assert.assertEquals( 53, stats.getOrphanCvs().size() );
        Assert.assertEquals( 54, stats.getObsoleteCvs().size() );

        //invalid terms are already filtered out
        Assert.assertEquals( 0, stats.getInvalidTerms().size() );
    }

    @Test
    public void checkAnnotationUpdated() throws Exception {
        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.52" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_52.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> cvs = ontologyBuilder.getAllCvs();
        cvs = cvs.subList(0,10);


        List<CvDagObject> copyOfCvs = new ArrayList<CvDagObject>( cvs.size() );
        IntactCloner cloner = new IntactCloner();
        for ( CvDagObject cv : cvs ) {
            final CvDagObject copy = cloner.clone( cv );
            Assert.assertEquals( copy, cv );
            copyOfCvs.add( copy );
        }

        cvUpdater.createOrUpdateCVs( cvs );

        DaoFactory daoFactory = getDaoFactory();
        
        final Collection<CvObject> notPersistedCvs = new ArrayList<CvObject>();
        final Collection<CvObject> updatedCvs = new ArrayList<CvObject>();
        for ( CvDagObject cv : copyOfCvs ) {
            final CvObjectDao<?> cvDao = daoFactory.getCvObjectDao( cv.getClass() );
            final CvObject persistedCv = cvDao.getByShortLabel( cv.getShortLabel() );

            if ( persistedCv == null ) {
                notPersistedCvs.add( cv );
            } else {
                if ( !isSizeOfCollectionTheSame( cv, persistedCv ) ) {
                    updatedCvs.add( persistedCv );
                }
            }
        }

        Assert.assertEquals( 1, updatedCvs.size() );   // identity cv is updated
    }

    private boolean isSizeOfCollectionTheSame( CvObject cv1, CvObject cv2 ) {
        boolean similar = true;

        if( cv1.getXrefs().size() != cv2.getXrefs().size() ) {
            similar = false;
        }

        if ( cv1.getAnnotations().size() != cv2.getAnnotations().size() ) {
            similar = false;
        }

        if ( cv1.getAliases().size() != cv2.getAliases().size() ) {
            similar = false;
        }

        return similar;
    }

    @Test
    public void cvDatabasesWithoutAnnotationsOnFirstPass() throws Exception {
        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );
        InputStream is = CvUpdaterTest.class.getResourceAsStream( "/additional-annotations.csv" );

        AnnotationInfoDataset aid = AnnotationInfoDatasetFactory.buildFromCsv( is );

        cvUpdater.createOrUpdateCVs( ontologyBuilder.getAllCvs(), aid );

        CvDatabase psiMod = getDaoFactory().getCvObjectDao( CvDatabase.class ).getByPsiMiRef( "MI:0897" );
        System.out.println( psiMod.getAnnotations() );
        Assert.assertEquals( 2, psiMod.getAnnotations().size() );
    }

    @Test
    public void institutionWithNonMiAnnotations() throws Exception {

        Institution institution = IntactContext.getCurrentInstance().getInstitution();
        final Annotation annotation = getMockBuilder().createAnnotation("nowhere", "IA:0999", "postaladdress");
        institution.addAnnotation(annotation);
        persisterHelper.save(annotation.getCvTopic());
        persisterHelper.save(institution);

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvUpdaterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 987, allCvs.size() );

        
        cvUpdater.createOrUpdateCVs(allCvs);
    }
}
