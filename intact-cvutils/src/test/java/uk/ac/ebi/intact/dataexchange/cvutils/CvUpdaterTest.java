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
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
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

    @Test
    public void reportDirectlyFromOBOFile() {

        URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        if ( log.isDebugEnabled() ) log.debug( "url " + url );
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


        } catch ( IOException ioex ) {
            ioex.printStackTrace();
        }
    }//end method

    //@Test
    public void isConstraintViolatedTest() throws Exception {

        URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        log.debug( "url " + url );

        OBOSession oboSession = OboUtils.createOBOSession( url );
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allValidCvs = ontologyBuilder.getAllCvsAsList();
        Assert.assertEquals( 947, allValidCvs.size() );

       


        CvUpdater updater = new CvUpdater();
        log.debug( "isConstraintViolated :" + updater.isConstraintViolated( allValidCvs ) );


    }

    @Test
    public void createOrUpdateCVs() throws Exception {



        List<CvObject> allCvsCommittedBefore = getDaoFactory().getCvObjectDao().getAll();
        int cvsBeforeUpdate = allCvsCommittedBefore.size();
        log.debug( "cvsBeforeUpdate->" + cvsBeforeUpdate );
        


        sort( allCvsCommittedBefore, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                CvObject cv1 = ( CvObject ) o1;
                CvObject cv2 = ( CvObject ) o2;

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


        URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        log.debug( "url " + url );

        OBOSession oboSession = OboUtils.createOBOSession( url );
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

       

        List<CvObject> orphanCvs = ontologyBuilder.getOrphanCvObjects();
        Assert.assertEquals( 53, orphanCvs.size() );




        List<CvDagObject> allCvs = ontologyBuilder.getAllCvsAsList();
        Assert.assertEquals( 947, allCvs.size());

        AnnotationInfoDataset annotationDataset = OboUtils.createAnnotationInfoDatasetFromDefault( 10841 );
        CvUpdater updater = new CvUpdater();


        if(!updater.isConstraintViolated(allCvs) && cvsBeforeUpdate==0) {

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

        }//end if
        else{

            throw new Exception("isConstraintViolated == true "+"PK Constraint Violated");
        }
        
        /**
         * Uncomment this block when you want to output the CvObjects to the
         * log file in Teamcity
         */

        /*
                List<CvObject> allCvsCommitted = getDaoFactory().getCvObjectDao().getAll();

                sort( allCvsCommitted, new Comparator() {
                    public int compare( Object o1, Object o2 ) {
                        CvObject cv1 = ( CvObject ) o1;
                        CvObject cv2 = ( CvObject ) o2;

                        String id1 = cv1.getShortLabel();
                        String id2 = cv2.getShortLabel();

                        return id1.compareTo( id2 );
                    }
                } );

                int cvCounter = 1;
                log.debug( "Printing results of getCvObjectDao().getAll() " );
                for ( CvObject cvObject : allCvsCommitted ) {
                    log.debug( cvCounter + "\t" + cvObject.getMiIdentifier() + "\t" + cvObject.getShortLabel() );
                    cvCounter++;
                }


                int cvCounterCreated = 1;
                Multimap<Class, StatsUnit> mmc = stats.getCreatedCvs();
                List<StatsUnit> allStatsUnitsCreated = new ArrayList<StatsUnit>();


                log.debug( "\n\n\nPrinting results of stats.getCreatedCvs() " );
                for ( Class aClass : mmc.keySet() ) {
                    Collection<StatsUnit> statsCol = mmc.get( aClass );
                    allStatsUnitsCreated.addAll( statsCol );
                }//end outer for

                sort( allStatsUnitsCreated, new Comparator() {
                    public int compare( Object o1, Object o2 ) {
                        StatsUnit cv1 = ( StatsUnit ) o1;
                        StatsUnit cv2 = ( StatsUnit ) o2;

                        String id1 = cv1.getShortLabel();
                        String id2 = cv2.getShortLabel();

                        return id1.compareTo( id2 );
                    }
                } );

                for ( StatsUnit statsUnit : allStatsUnitsCreated ) {
                    log.debug( cvCounterCreated + "\t" + statsUnit.getAc() + "\t" + statsUnit.getShortLabel() );
                    cvCounterCreated++;
                }//end for

                //----------------------------------------------------------------

                int cvCounterUpdated = 1;
                Multimap<Class, StatsUnit> mmu = stats.getUpdatedCvs();
                List<StatsUnit> allStatsUnitsUpdated = new ArrayList<StatsUnit>();
                log.debug( "\n\n\nPrinting results of stats.getUpdatedCvs() " );

                for ( Class aClass : mmu.keySet() ) {
                    Collection<StatsUnit> statsCol = mmu.get( aClass );
                    allStatsUnitsUpdated.addAll( statsCol );


                }//end outer for


                sort( allStatsUnitsUpdated, new Comparator() {
                    public int compare( Object o1, Object o2 ) {
                        StatsUnit cv1 = ( StatsUnit ) o1;
                        StatsUnit cv2 = ( StatsUnit ) o2;

                        String id1 = cv1.getShortLabel();
                        String id2 = cv2.getShortLabel();

                        return id1.compareTo( id2 );
                    }
                } );

                for ( StatsUnit statsUnit : allStatsUnitsUpdated ) {
                    log.debug( cvCounterUpdated + "\t" + statsUnit.getAc() + "\t" + statsUnit.getShortLabel() );
                    cvCounterUpdated++;
                }//end for

                //-------------------------------------------------------


                int cvCounterObsolete = 1;
                Map<String, String> mmo = stats.getObsoleteCvs();
                List<String> keys = new ArrayList( mmo.keySet() );
                Collections.sort( keys);
                // List<StatsUnit> allObsoleteCvs =

                log.debug( "\n\n\nPrinting results of stats.getObsoleteCvs() " );

                for ( String id : keys) {
                    log.debug( cvCounterObsolete + "\t" + id + "\t" + mmo.get( id ) );
                    cvCounterObsolete++;

                }//end outer for

                //-------------------------------------------------------

        */
        //it should be 947 as we don't create the root Cv MI:0000
        //The additional two cvs are hidden and definition from annotation dataset


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
