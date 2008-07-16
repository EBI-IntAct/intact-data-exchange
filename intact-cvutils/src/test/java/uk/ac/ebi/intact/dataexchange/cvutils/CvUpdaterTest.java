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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.io.*;
import java.net.URL;
import java.util.*;


/**
 * Test class for CvUpdater
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */

public class CvUpdaterTest extends IntactBasicTestCase {

    private static final Log log = LogFactory.getLog( CvUpdaterTest.class );

    @Before
    public void before() throws Exception {
        SchemaUtils.createSchema();
    }

    @Test
    public void reportDirectlyFromOBOFile() throws Exception {

        //URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        String revision ="1.48";
        URL url =  new URL( OboUtils.PSI_MI_OBO_LOCATION +"?revision="+revision);
        log.debug( "url " + url );

        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
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


            if ( inputLine.startsWith( "[Term]" ) ) {
                termCounter++;
            }

            if ( inputLine.startsWith( "id:" ) ) {
                idCounter++;
            }
            if ( inputLine.matches( "id:\\s+MI:.*" ) ) {
                miCounter++;

            }

            if ( inputLine.contains( "is_obsolete: true" ) ) {
                obsoleteCounter++;
            }
            if ( inputLine.matches( "def:.*?OBSOLETE.*" ) ) {
                obsoleteCounterDef++;
            }
            if ( inputLine.startsWith( "[Typedef]" ) ) {
                typedefCounter++;
            }

            if ( inputLine.matches("subset:\\s+PSI-MI\\s+slim" ) ) {
                psiTerm++;
                 }
            if ( inputLine.matches("subset:\\s+Drugable" ) ) {
                drugTerm++;
                //log.info(drugTerm+"  "+ temp );
            }
        }



        //948+1 with Typedef
        Assert.assertEquals( 949, idCounter );
        Assert.assertEquals( 948, termCounter );
        Assert.assertEquals( 948, miCounter );
        Assert.assertEquals( 53, obsoleteCounter );
        Assert.assertEquals( 53, obsoleteCounterDef );
        Assert.assertEquals( 1, typedefCounter );
        Assert.assertEquals( 844, psiTerm );
        Assert.assertEquals( 124, drugTerm );

        in.close();

    }//end method




    @Test
    public void isConstraintViolatedTest() throws Exception {

        OBOSession oboSession = OboUtils.createOBOSessionFromDefault("1.48");
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 978, allCvs.size() );

        CvUpdater updater = new CvUpdater();
        Assert.assertFalse( updater.isConstraintViolated( allCvs ) );
    }

    @Test
    public void obsoleteTest() throws Exception {

        new IntactUnit().createSchema( true );

        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();
        //PersisterHelper is adding the intact, psi-mi and identity terms...so we have 3
        Assert.assertEquals( 3, cvsBeforeUpdate );

        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        beginTransaction();
        CvDagObject aggregation = CvObjectUtils.createCvObject( owner, CvInteractionType.class, "MI:0191", "aggregation" );
        PersisterHelper.saveOrUpdate( aggregation );
        commitTransaction();


        List<CvObject> allCvsCommittedAfter = getDaoFactory().getCvObjectDao().getAll();
        int cvsAfterPersist = allCvsCommittedAfter.size();
        //PersisterHelper is adding the intact, psi-mi and identity terms+aggregation...so we have 4
        Assert.assertEquals( 4, cvsAfterPersist );



    }//end method




    @Test
    public void updatingWithNewAnnotations() throws Exception{

        CvObjectDao<CvObject> cvObjectDao;
        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        final int existingCvsCount = cvObjectDao.countAll();

        final CvAliasType alias = getMockBuilder().createCvObject( CvAliasType.class, "MI:0300", "alias type" );

        final CvTopic topicsParent = getMockBuilder().createCvObject( CvTopic.class, "MI:0590", "attribute name" );
        final CvTopic cvTopic1 = getMockBuilder().createCvObject( CvTopic.class, "MI:0631", "3d-r-factors" );
        final CvTopic cvTopic2 = getMockBuilder().createCvObject( CvTopic.class, "MI:0630", "3d-test" );
        cvTopic1.addParent( topicsParent );
        cvTopic2.addParent( topicsParent );

        PersisterHelper.saveOrUpdate( alias, topicsParent, cvTopic1,cvTopic2);
        commitTransaction();

        beginTransaction();
        CvUpdater cvupdater = new CvUpdater();

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();

        final List<CvDagObject> list = new ArrayList<CvDagObject>( );
        final List<CvObject> cvObjectList = cvObjectDao.getAll();
        for ( CvObject cvObject : cvObjectList ) {
            list.add( (CvDagObject ) cvObject );
        }

        int cvsBeforeUpdate = list.size();
        Assert.assertEquals( existingCvsCount + 4, cvsBeforeUpdate);

        final InputStream is = CvUpdaterTest.class.getResourceAsStream( "/annotations_test.csv" );
        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource(is);

        cvupdater.createOrUpdateCVs(list,annotationDataset);
        commitTransaction();


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
    public void updatingExistingAnnotations() throws Exception{

        CvObjectDao<CvObject> cvObjectDao;
        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        final int existingCvsCount = cvObjectDao.countAll();

        final CvAliasType alias = getMockBuilder().createCvObject( CvAliasType.class, "MI:0300", "alias type" );

        final CvTopic topicsParent = getMockBuilder().createCvObject( CvTopic.class, "MI:0590", "attribute name" );
        final CvTopic cvTopic1 = getMockBuilder().createCvObject( CvTopic.class, "MI:0631", "3d-r-factors" );
        cvTopic1.addParent( topicsParent );


        Annotation annot = getMockBuilder().createAnnotation( "no type", null, CvTopic.USED_IN_CLASS);
        cvTopic1.addAnnotation( annot );

        final Annotation annotBeforeUpdate = cvTopic1.getAnnotations().iterator().next();
        Assert.assertEquals( "used-in-class", annotBeforeUpdate.getCvTopic().getShortLabel() );
        Assert.assertEquals( "no type", annotBeforeUpdate.getAnnotationText());

        PersisterHelper.saveOrUpdate( alias, topicsParent, cvTopic1);
        commitTransaction();

        beginTransaction();
        CvUpdater cvupdater = new CvUpdater();

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();

        final List<CvDagObject> list = new ArrayList<CvDagObject>( );
        final List<CvObject> cvObjectList = cvObjectDao.getAll();
        for ( CvObject cvObject : cvObjectList ) {
            list.add( (CvDagObject ) cvObject );
        }

        int cvsBeforeUpdate = list.size();
        Assert.assertEquals( existingCvsCount + 4, cvsBeforeUpdate);

        final InputStream is = CvUpdaterTest.class.getResourceAsStream( "/annotations_test.csv" );
        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource(is);

        cvupdater.createOrUpdateCVs(list,annotationDataset);
        commitTransaction();

        cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        CvObject topic;

        topic = cvObjectDao.getByShortLabel( CvTopic.class, "3d-r-factors" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        final Annotation annotation = topic.getAnnotations().iterator().next();
        Assert.assertEquals( "used-in-class", annotation.getCvTopic().getShortLabel() );
        //updated no type annotation to Interaction 
        Assert.assertEquals( "uk.ac.ebi.intact.model.Interaction", annotation.getAnnotationText());

         //crated new annotation
        topic = cvObjectDao.getByShortLabel( CvAliasType.class, "alias type" );
        Assert.assertNotNull( topic );
        Assert.assertEquals( 1, topic.getAnnotations().size() );
        Assert.assertEquals( "hidden", topic.getAnnotations().iterator().next().getCvTopic().getShortLabel() );

        final int cvCountAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        for(CvObject cv: getDaoFactory().getCvObjectDao().getAll()){
            if ( log.isDebugEnabled() ) {
                log.debug("Cvs created " +cv.toString() );
            }

        }
        Assert.assertEquals( "2 CvObjects expected to be created during update: hidden and obsolete & 1 Cvobject updated used-in-class ",
                             cvsBeforeUpdate + 2, cvCountAfterUpdate );

        //again call cvupdate and this time it should ignore the annotations 
        beginTransaction();
        cvupdater.createOrUpdateCVs(list,annotationDataset);
        commitTransaction();
        Assert.assertEquals( "2 CvObjects expected to be created during update: hidden and obsolete & 1 Cvobject updated used-in-class ",
                                     cvsBeforeUpdate + 2, cvCountAfterUpdate );


    }



    @Test
    public void createOrUpdateCVsWithObsolete() throws Exception {
         new IntactUnit().createSchema( true );

        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();

        //PersisterHelper is adding the intact, psi-mi and identity terms...so we have 3
        Assert.assertEquals( 3, cvsBeforeUpdate );

        //Insert aggregation an obsolete term MI:0191
        CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        Institution owner = IntactContext.getCurrentInstance().getInstitution();
        CvDagObject aggregation = CvObjectUtils.createCvObject( owner, CvInteractionType.class, "MI:0191", "aggregation" );

        beginTransaction();
        PersisterHelper.saveOrUpdate( aggregation );
        commitTransaction();


        List<CvObject> allCvsCommittedAfter = getDaoFactory().getCvObjectDao().getAll();
        for ( CvObject cv : allCvsCommittedAfter ) {
            if ( log.isDebugEnabled() )
                log.debug( "Cv-> " + cv.getIdentifier() + "  shortLabel: " + cv.getShortLabel() );
        }


        Assert.assertEquals( 4, cvObjectDao.countAll() );

        //check if aggregation has obsolote annotation  before createOrUpdateCvs call
        String id_ = CvObjectUtils.getIdentity( aggregation );
        Collection<CvObject> existingCvsBefore = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id_ ) );
        if ( existingCvsBefore.isEmpty() ) {
            existingCvsBefore = cvObjectDao.getByShortLabelLike( aggregation.getShortLabel() );
        }

/*
        //use for testing the latest files--change the file path as in ur system
        OBOSession oboSession = OboUtils.createOBOSession("C:\\intactall\\intact-current\\data-exchange\\intact-cvutils\\src\\test\\resources\\psi-mi25-next.obo");
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

*/

        OBOSession oboSession = OboUtils.createOBOSessionFromDefault("1.48");
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvObject> orphanCvs = ontologyBuilder.getOrphanCvObjects();
        Assert.assertEquals( 53, orphanCvs.size() );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 978, allCvs.size() );

        InputStream is = CvUpdaterTest.class.getResourceAsStream("/additional-annotations.csv");

        if ( is == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Please check the resource" );
            }
        throw new NullPointerException( "InputStream is null" );
        }

        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromResource( is);
        if ( log.isDebugEnabled() ) {
            log.debug( "AnnotationInfoDataset size :   " + annotationDataset.getAll().size()  );
        }
        //AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault( 10841 );

        beginTransaction();

        CvUpdater updater = new CvUpdater();

        Assert.assertFalse( updater.isConstraintViolated( allCvs ) );

        if ( log.isDebugEnabled() ) log.debug( "Constraint not violated and proceeding with Update " );
        CvUpdaterStatistics stats = updater.createOrUpdateCVs( allCvs, annotationDataset );

        commitTransaction();
        //check if aggregation has obsolote annotation  after createOrUpdateCvs call
        String id = CvObjectUtils.getIdentity( aggregation );
        Collection<CvObject> existingCvs = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id ) );
        if ( existingCvs.isEmpty() ) {
            existingCvs = cvObjectDao.getByShortLabelLike( aggregation.getShortLabel() );
        }

        for ( CvObject existingCv : existingCvs ) {
            log.debug( "existingCv MI -> shortLabel  " + existingCv.getIdentifier() + " -> " + existingCv.getShortLabel() );
            for ( Annotation annot : existingCv.getAnnotations() ) {
                if ( log.isDebugEnabled() ) log.debug( "Annot CvTopic " + annot.getCvTopic() );
                if ( log.isDebugEnabled() ) log.debug( "Annot Text " + annot.getAnnotationText() );

            }
        }//end for


        int totalCvsAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        if ( log.isDebugEnabled() ) log.debug( "totalCvsAfterUpdate->" + totalCvsAfterUpdate );
        if ( log.isDebugEnabled() ) log.debug( "stats.getCreatedCvs().size()->" + stats.getCreatedCvs().size() );
        if ( log.isDebugEnabled() ) log.debug( "stats.getUpdatedCvs().size() ->" + stats.getUpdatedCvs().size() );

        Assert.assertEquals( 932, totalCvsAfterUpdate );

        Assert.assertEquals( 925, stats.getCreatedCvs().size() );
        Assert.assertEquals( 1, stats.getUpdatedCvs().size() );

        //53-2 as aggregation was already created and later updated + obsolete term
        Assert.assertEquals( 51, stats.getOrphanCvs().size() );

        //52+1 obsolete term
        Assert.assertEquals( 53, stats.getObsoleteCvs().size() );

        //invalid terms are already filtered out
        Assert.assertEquals( 0, stats.getInvalidTerms().size() );

        //check for annotations
       CvObject cvAlias = cvObjectDao.getByShortLabel( "alias type");

        if ( log.isDebugEnabled() ) {
            log.debug( "cvAlias : "+cvAlias.toString() );
            log.debug( "cvAlias Annotations : "+cvAlias.getAnnotations().toString() );
        }

    } //end method



}  //end class
