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
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import static java.util.Collections.sort;
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

        URL url = new URL( OboUtils.PSI_MI_OBO_LOCATION );
        log.debug( "url " + url );

        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
        String inputLine;

        int termCounter = 0;
        int idCounter = 0;
        int miCounter = 0;
        int obsoleteCounter = 0;
        int obsoleteCounterDef = 0;
        int typedefCounter = 0;

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

        }

        //948+1 with Typedef
        Assert.assertEquals( 949, idCounter );
        Assert.assertEquals( 948, termCounter );
        Assert.assertEquals( 948, miCounter );
        Assert.assertEquals( 53, obsoleteCounter );
        Assert.assertEquals( 53, obsoleteCounterDef );
        Assert.assertEquals( 1, typedefCounter );

        in.close();

    }//end method

    @Test
    public void isConstraintViolatedTest() throws Exception {
        OBOSession oboSession = OboUtils.createOBOSessionFromLatestMi();
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allValidCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 947, allValidCvs.size() );

        CvUpdater updater = new CvUpdater();
        Assert.assertFalse( updater.isConstraintViolated( allValidCvs ) );
    }

    @Test
    public void obsoleteTest() throws Exception {


        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();
        Assert.assertEquals( 0, cvsBeforeUpdate );


        IntactContext intactContext = IntactContext.getCurrentInstance();
        Institution owner = IntactContext.getCurrentInstance().getInstitution();


        beginTransaction();
        CvDagObject aggregation = CvObjectUtils.createCvObject( owner, CvInteractionType.class, "MI:0191", "aggregation" );
        PersisterHelper.saveOrUpdate( aggregation );
        commitTransaction();
     

        List<CvObject> allCvsCommittedAfter = getDaoFactory().getCvObjectDao().getAll();
        int cvsAfterPersist = allCvsCommittedAfter.size();
        //PersisterHelper is adding the psi-mi and identity terms...so we have 3
        Assert.assertEquals( 3, cvsAfterPersist );



    }//end method


    @Test
    public void createOrUpdateCVsWithObsolete() throws Exception {


        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();

        Assert.assertEquals( 0, cvsBeforeUpdate );

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
                log.debug( "Cv->" + cv.getMiIdentifier() + "shortLabel: " + cv.getShortLabel() );
        }


        Assert.assertEquals( 3, cvObjectDao.countAll() );

        //check if aggregation has obsolote annotation  before createOrUpdateCvs call
        String id_ = CvObjectUtils.getIdentity( aggregation );
        Collection<CvObject> existingCvsBefore = cvObjectDao.getByPsiMiRefCollection( Collections.singleton( id_ ) );
        if ( existingCvsBefore.isEmpty() ) {
            existingCvsBefore = cvObjectDao.getByShortLabelLike( aggregation.getShortLabel() );
        }

        for ( CvObject existingCv : existingCvsBefore ) {
            if ( log.isDebugEnabled() )
                log.debug( "existingCv MI -> shortLabel ->Ac" + existingCv.getMiIdentifier() + " -> " + existingCv.getShortLabel() + " ->" + existingCv.getAc() );
            for ( Annotation annot : existingCv.getAnnotations() ) {
                if ( log.isDebugEnabled() ) log.debug( "Annot CvTopic " + annot.getCvTopic() );
                if ( log.isDebugEnabled() ) log.debug( "Annot Text " + annot.getAnnotationText() );

            }
        }//end for


        OBOSession oboSession = OboUtils.createOBOSessionFromLatestMi();
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvObject> orphanCvs = ontologyBuilder.getOrphanCvObjects();
        Assert.assertEquals( 53, orphanCvs.size() );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 947, allCvs.size() );


        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault( 10841 );

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
            log.debug( "existingCv MI -> shortLabel" + existingCv.getMiIdentifier() + " -> " + existingCv.getShortLabel() );
            for ( Annotation annot : existingCv.getAnnotations() ) {
                if ( log.isDebugEnabled() ) log.debug( "Annot CvTopic " + annot.getCvTopic() );
                if ( log.isDebugEnabled() ) log.debug( "Annot Text " + annot.getAnnotationText() );

            }
        }//end for


        int totalCvsAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        if ( log.isDebugEnabled() ) log.debug( "totalCvsAfterUpdate->" + totalCvsAfterUpdate );
        if ( log.isDebugEnabled() ) log.debug( "stats.getCreatedCvs().size()->" + stats.getCreatedCvs().size() );
        if ( log.isDebugEnabled() ) log.debug( "stats.getUpdatedCvs().size() ->" + stats.getUpdatedCvs().size() );


        Assert.assertEquals( 899, totalCvsAfterUpdate );

        //947-(53-1(aggregation))= 895  , one addition because of exp.role has two parents
        Assert.assertEquals( 896, stats.getCreatedCvs().size() );
        Assert.assertEquals( 1, stats.getUpdatedCvs().size() );

        //53-2 as aggregation was already created and later updated + obsolete term
        Assert.assertEquals( 51, stats.getOrphanCvs().size() );

        //52+1 obsolete term
        Assert.assertEquals( 53, stats.getObsoleteCvs().size() );

        //invalid terms are already filtered out
        Assert.assertEquals( 0, stats.getInvalidTerms().size() );


    } //end method

  

}  //end class
