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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
    public void clear() throws Exception {
        SchemaUtils.createSchema();
    }


   @Test
    public void reportDirectlyFromOBOFile( ) {

        URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
       if(log.isDebugEnabled()) log.debug( "url " + url );
        try {
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
                if ( inputLine.startsWith("[Typedef]") ) {
                    typedefCounter++;
                }

            }

            

            //948+1 with Typedef
            Assert.assertEquals(949, idCounter);
            Assert.assertEquals(948, termCounter);
            Assert.assertEquals(948, miCounter);
            Assert.assertEquals(53, obsoleteCounter);
            Assert.assertEquals(53, obsoleteCounterDef);
            Assert.assertEquals(1, typedefCounter);

            in.close();
        } catch ( IOException ioex ) {
            ioex.printStackTrace();
        }
    }//end method
    

  
    @Test
    public void createOrUpdateCVs() throws Exception {

        URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        log.debug( "url " + url );



        OBOSession oboSession = OboUtils.createOBOSession( url );
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );
        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault( 10841 );

        CvUpdater updater = new CvUpdater();
        List<CvDagObject> allCvs = updater.getAllCvsAsList( ontologyBuilder );
        CvUpdaterStatistics stats = updater.createOrUpdateCVs( allCvs, annotationDataset );


        log.debug( "CreatedCvs->" + stats.getCreatedCvs().size() );
        log.debug( "UpdatedCvs->" + stats.getUpdatedCvs().size() );
        log.debug( "ObsoleteCvs->" + stats.getObsoleteCvs().size() );
        log.debug( "InvalidTerms->" + stats.getInvalidTerms().size() );

        int total = getDaoFactory().getCvObjectDao().countAll();
        log.debug( "Total->" + total );

        //it should be 947 as we don't create the root Cv MI:0000
        //Have to check the additional two cvs, probably cause of more than one parent
        
        Assert.assertEquals( 949, stats.getCreatedCvs().size() );
        Assert.assertEquals( 0, stats.getUpdatedCvs().size() );
        //52+1 obsolete term
        Assert.assertEquals( 53, stats.getObsoleteCvs().size() );
        Assert.assertEquals( 11, stats.getInvalidTerms().size() );
        Assert.assertEquals( total, stats.getCreatedCvs().size() );



        // update
        /*    CvUpdaterStatistics stats2 = updater.createOrUpdateCVs(ontologyBuilder,annotationDataset);

      Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

      Assert.assertEquals(0, stats2.getCreatedCvs().size());
      Assert.assertEquals(0, stats2.getUpdatedCvs().size());
      Assert.assertEquals(50, stats2.getObsoleteCvs().size());
      Assert.assertEquals(9, stats2.getInvalidTerms().size()); */
    } //end method

    /*
    Retained as it is from the Old CvUpdaterTest
        @Test
        public void createOrUpdateCVs_existingTermToMarkAsObsolete() throws Exception {
            CvInteractionType aggregation = getMockBuilder().createCvObject(CvInteractionType.class, "MI:0191", "aggregation");
            CvTopic obsolete = getMockBuilder().createCvObject(CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE);
            PersisterHelper.saveOrUpdate(aggregation, obsolete);

            IntactOntology ontology = OboUtils.createOntologyFromOboDefault(10841);
            AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault(10841);

            CvUpdater updater = new CvUpdater();
            CvUpdaterStatistics stats = updater.createOrUpdateCVs(ontology, annotationDataset);
            System.out.println(stats);

            int total = getDaoFactory().getCvObjectDao().countAll();

            Assert.assertEquals(851, total);

            Assert.assertEquals(847, stats.getCreatedCvs().size());
            Assert.assertEquals(1, stats.getUpdatedCvs().size());
            Assert.assertEquals(50, stats.getObsoleteCvs().size());
            Assert.assertEquals(9, stats.getInvalidTerms().size());

        }

        @Test
        @Ignore
        public void createOrUpdateCVs_includingNonMi() throws Exception {
            URL intactObo = CvUpdaterTest.class.getResource("/intact.20071203.obo");

            IntactOntology ontology = OboUtils.createOntologyFromObo(intactObo);

            CvUpdater updater = new CvUpdater();
            CvUpdaterStatistics stats = updater.createOrUpdateCVs(ontology);

            int total = getDaoFactory().getCvObjectDao().countAll();

            System.out.println(stats);

            // TODO adjust numbers when it works
            Assert.assertEquals(stats.getCreatedCvs().size(), 835);
            Assert.assertEquals(stats.getUpdatedCvs().size(), 0);
            Assert.assertEquals(stats.getObsoleteCvs().size(), 50);
            Assert.assertEquals(stats.getInvalidTerms().size(), 9);

            // update
            CvUpdaterStatistics stats2 = updater.createOrUpdateCVs(ontology);

            Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

            Assert.assertEquals(stats2.getCreatedCvs().size(), 0);
            Assert.assertEquals(stats2.getUpdatedCvs().size(), 0);
            Assert.assertEquals(stats2.getObsoleteCvs().size(), 50);
            Assert.assertEquals(stats2.getInvalidTerms().size(), 9);
        }
    */


}  //end class
