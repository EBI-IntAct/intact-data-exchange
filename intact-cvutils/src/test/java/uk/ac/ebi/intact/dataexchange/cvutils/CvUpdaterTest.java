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
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.List;


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

        URL url = new URL(OboUtils.PSI_MI_OBO_LOCATION);
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
    public void createOrUpdateCVs() throws Exception {
        reportDirectlyFromOBOFile();
                afterBasicTest();
        end();
        before();
        isConstraintViolatedTest();
        afterBasicTest();
        end();
        before();
                
        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();

        Assert.assertEquals(0, cvsBeforeUpdate);

        sort( allCvsCommittedBefore, new Comparator<CvObject>() {
            public int compare( CvObject cv1, CvObject cv2 ) {
                String id1 = cv1.getShortLabel();
                String id2 = cv2.getShortLabel();

                return id1.compareTo( id2 );
            }
        } );

        int cvCounter = 1;
        log.debug( "Printing results of getCvObjectDao().getAll() before update " );
        for ( CvObject cvObject : allCvsCommittedBefore ) {
            log.debug( cvCounter + "\t" + CvObjectUtils.getIdentity( cvObject ) + "\t" + cvObject.getMiIdentifier() + "\t" + cvObject.getShortLabel() );
            cvCounter++;
        }


        //URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        //log.debug( "url " + url );
        //OBOSession oboSession = OboUtils.createOBOSession( url );

        OBOSession oboSession = OboUtils.createOBOSessionFromLatestMi();
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvObject> orphanCvs = ontologyBuilder.getOrphanCvObjects();
        Assert.assertEquals( 53, orphanCvs.size() );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        Assert.assertEquals( 947, allCvs.size());

        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault( 10841 );
        CvUpdater updater = new CvUpdater();

        Assert.assertFalse(updater.isConstraintViolated(allCvs));

        log.debug( "Constraint not violated and proceeding with Update " );
        CvUpdaterStatistics stats = updater.createOrUpdateCVs(allCvs, annotationDataset );

        int totalCvsAfterUpdate = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals( totalCvsAfterUpdate, stats.getCreatedCvs().size() + cvsBeforeUpdate );

        Assert.assertEquals( 949, stats.getCreatedCvs().size() );
        Assert.assertEquals( 0, stats.getUpdatedCvs().size() );

        //52+1 obsolete term
        Assert.assertEquals( 53, stats.getObsoleteCvs().size() );

        //invalid terms are already filtered out
        Assert.assertEquals( 0, stats.getInvalidTerms().size() );
        Assert.assertEquals( totalCvsAfterUpdate, stats.getCreatedCvs().size() );

        log.debug( "totalCvsAfterUpdate->" + totalCvsAfterUpdate );
        log.debug( "stats.getCreatedCvs().size()->" + stats.getCreatedCvs().size() );
        log.debug( "stats.getUpdatedCvs().size() ->" + stats.getUpdatedCvs().size() );

    } //end method



}  //end class
