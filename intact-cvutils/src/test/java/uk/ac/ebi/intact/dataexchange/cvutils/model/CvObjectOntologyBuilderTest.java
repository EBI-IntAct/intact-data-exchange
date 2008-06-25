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
package uk.ac.ebi.intact.dataexchange.cvutils.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.TermCategory;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;


/**
 * Test class for CvObjectOntologyBuilder
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class CvObjectOntologyBuilderTest {

    private static int counter = 1;

    private static final Log log = org.apache.commons.logging.LogFactory.getLog( CvObjectOntologyBuilderTest.class );

    @Test
    public void build_default() throws Exception {


        OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.48" );
        log.debug( oboSession.getObjects().size() );

        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );


        Assert.assertEquals( 16, ontologyBuilder.getRootOBOObjects().size() );


        int allOrphanCvs = ontologyBuilder.getOrphanCvObjects().size();
        Assert.assertEquals( 53, allOrphanCvs );


        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        int allCvsSize = allCvs.size();
        Assert.assertEquals( 978, allCvsSize );

      


        List<CvDagObject> orderedList = ontologyBuilder.getAllOrderedCvs( allCvs );
        Assert.assertEquals( 978, orderedList.size() );


        for ( CvDagObject cvdag : orderedList ) {
            log.debug( "CvDag: \t" + cvdag.getMiIdentifier() + "\t" + cvdag.getObjClass() + "\t" + cvdag.getShortLabel() );
        }


        if ( log.isDebugEnabled() )
            log.debug( "ontologyBuilder.getAllCvsAsList().size() " + ontologyBuilder.getAllCvs().size() );
        Assert.assertEquals("CvInteraction", ontologyBuilder.findCvClassforMI( "MI:0439" ).getSimpleName());
        Assert.assertEquals("CvDatabase", ontologyBuilder.findCvClassforMI( "MI:0244" ).getSimpleName() );//non-root object
        Assert.assertEquals("CvFeatureIdentification", ontologyBuilder.findCvClassforMI( "MI:0003" ).getSimpleName() );//root object

        //an example term with  3 Aliases and 2 xrefs, a database xref and identity xref
        /**
         * [Term]
         id: MI:0439
         name: random spore analysis
         def: "A technique used to detect genetic interactions between 2 (or more) genes in a sporulating organism by scoring a large population of haploid spores for a phenotype and correlating the phenotype with the presence of single vs double (multiple) mutations. A diploid heterozygous organism harbouring mutations in two (or more) genes is induced to sporulate. Resulting spores are meiotic segregants that are haploid and are either wild type or mutant at each locus. Spores are scored for a phenotype, such as loss of viability." [PMID:14755292]
         subset: PSI-MI slim
         synonym: "random-spore analysis" EXACT PSI-MI-alternate []
         synonym: "RSA" EXACT PSI-MI-alternate []
         synonym: "rsa" EXACT PSI-MI-short []
         synonym: "spore germination" EXACT PSI-MI-alternate []
         is_a: MI:0254 ! genetic interference

         */
        OBOObject testObj = ( OBOObject ) oboSession.getObject( "MI:0439" );
        CvObject cvObject = ontologyBuilder.toCvObject( testObj );
        Assert.assertEquals( "random spore analysis", cvObject.getFullName() );
        Assert.assertEquals( "MI:0439", CvObjectUtils.getIdentity( cvObject ) );
        Assert.assertEquals( "MI:0439", cvObject.getMiIdentifier() );
        Assert.assertEquals( "rsa", cvObject.getShortLabel() );
        Assert.assertEquals( 3, cvObject.getAliases().size() );
        Assert.assertEquals( 2, cvObject.getXrefs().size() );
        Assert.assertTrue( CvObjectUtils.hasIdentity( cvObject, "MI:0439" ) );

        //Obsolote Term test MI:0443
        OBOObject testObsoleteObj = ( OBOObject ) oboSession.getObject( "MI:0443" );
        Assert.assertEquals( true, testObsoleteObj.isObsolete() );

        //947+1=948 root object MI:0000
        Assert.assertEquals( 947, ontologyBuilder.getAllMIOBOObjects().size() );
        Assert.assertEquals( 53, ontologyBuilder.getObsoleteOBOObjects().size() );
        Assert.assertEquals( 53, ontologyBuilder.getOrphanOBOObjects().size() );
        Assert.assertEquals( 11, ontologyBuilder.getInvalidOBOObjects().size() );
        //947+11+1=959
        Assert.assertEquals( 959, ontologyBuilder.getAllOBOObjects().size() );

        OBOObject endogenousObj = ( OBOObject ) oboSession.getObject( "MI:0222" );
        CvObject endogenousCvObject = ontologyBuilder.toCvObject( endogenousObj );
        testCvObject( endogenousCvObject );

        OBOObject rnaCleavage = ( OBOObject ) oboSession.getObject( "MI:0902" );
        CvObject rnaCleavageCv = ontologyBuilder.toCvObject( rnaCleavage );
        testCvObject( rnaCleavageCv );


        OBOObject obsoleteTerm = ( OBOObject ) oboSession.getObject( "MI:0021" );
        CvObject obsoleteCv = ontologyBuilder.toCvObject( obsoleteTerm );
        testCvObject( obsoleteCv );

        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0001");//root Cv interaction detection method
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0012");
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0192");//with GO
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0122");//with unique resid
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0460");   //def with url
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0021");   //def with OBSOLETE
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:2002");   //example with pubmed
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0108"); //example with comment subset PSI-MI slim
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:2120"); //example with comment subset Drugable
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0031"); //example with alias
        //OBOObject testObj = (OBOObject)oboSession.getObject("MI:0244"); //example with 4 annotations + search-url


    } //end method


    @Test
    public void categoryTest() throws Exception

    {
        OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.48" );
        log.debug( oboSession.getObjects().size() );

        OBOObject interactionDetection = ( OBOObject ) oboSession.getObject( "MI:0001" );

        Assert.assertEquals( 2, interactionDetection.getCategories().size() );
        if ( log.isDebugEnabled() )
            log.debug( "interactionDetection: " + interactionDetection.getID() + "    " + interactionDetection.getName() );
        for ( TermCategory term : interactionDetection.getCategories() ) {
            if ( log.isDebugEnabled() )
                log.debug( "Category of MI:0001:  " + term.getName() + "    " + term.getDesc() );
        }

        log.debug( "------------------------------------------------" );
        OBOObject interactorType = ( OBOObject ) oboSession.getObject( "MI:0313" );

        Assert.assertEquals( 2, interactorType.getCategories().size() );
        if ( log.isDebugEnabled() )
            log.debug( "interactorType: " + interactorType.getID() + "    " + interactorType.getName() );
        for ( TermCategory term : interactorType.getCategories() ) {
            if ( log.isDebugEnabled() ) log.debug( "Category of MI:0313: " + term.getName() + "    " + term.getDesc() );
        }


    }


    @Test
    public void build_subset_drugable() throws Exception {

        OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.48" );
        log.debug( oboSession.getObjects().size() );


        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );


        OboCategory oboCatDrug = new OboCategory( OboCategory.DRUGABLE );
        OboCategory oboCatPsi = new OboCategory( OboCategory.PSI_MI_SLIM );


        Collection<IdentifiedObject> testRoot = ontologyBuilder.getRootOBOObjects( oboCatDrug );

        for ( IdentifiedObject identObj : testRoot ) {
            if ( log.isDebugEnabled() ) log.debug( "ID: " + identObj.getID() );
        }


        OBOObject drugableObj = ( OBOObject ) oboSession.getObject( "MI:0686" );//drugable
        OBOObject psiObj = ( OBOObject ) oboSession.getObject( "MI:0439" );//psi-mi


        Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( drugableObj, oboCatDrug ) );
        Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( drugableObj, oboCatPsi ) );


        Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( psiObj, oboCatPsi ) );
        Assert.assertFalse( ontologyBuilder.checkIfCategorySubset( psiObj, oboCatDrug ) );

        /*  log.info( "--------------------------------------------" );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();
        log.info( "All cvs size " + allCvs.size() );

        log.info( "--------------------------------------------" );
        */

        List<CvDagObject> allDrugableCvs = ontologyBuilder.getAllCvs( oboCatDrug );
        if ( log.isDebugEnabled() ) {
            log.debug( "Drug subset size " + allDrugableCvs.size() );
        }

        Collection<String> drugablemis = new ArrayList<String>();
        for ( CvDagObject cvDag : allDrugableCvs ) {
            if ( log.isDebugEnabled() ) log.debug( cvDag.getMiIdentifier() + " -> " + cvDag.getShortLabel() );
            OBOObject drugable = ( OBOObject ) oboSession.getObject( cvDag.getMiIdentifier() );
            Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( drugable, oboCatDrug ) );
            drugablemis.add( drugable.getID() );

        }

        if ( log.isDebugEnabled() ) {
            log.debug( "drugablemis size " + drugablemis.size() );
        }


        Collection<String> crossCheck = crossCheckFromOBOFile( OboCategory.DRUGABLE );

        if ( log.isDebugEnabled() ) log.debug( "crossCheckFromOBOFile().size() " + crossCheck.size() );

        Collection<String> difference = CollectionUtils.subtract( crossCheck, drugablemis );

        if ( log.isDebugEnabled() ) log.debug( "difference size " + difference.size() );
        for ( String diff : difference ) {
            if ( log.isDebugEnabled() ) log.debug( "diff MI: " + diff );
        }

        //Only the root term MI:0000 should be in the difference
        Assert.assertEquals( 1, difference.size() );

    } //end method


    @Test
    public void build_subset_psi() throws Exception {

        OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.48" );
        if ( log.isDebugEnabled() ) log.debug( oboSession.getObjects().size() );

        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );
        OBOObject psiObj = ( OBOObject ) oboSession.getObject( "MI:0005" );//psi-mi

        OboCategory oboCatPsi = new OboCategory( OboCategory.PSI_MI_SLIM );
        Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( psiObj, oboCatPsi ) );


        List<CvDagObject> allPsimiCvs = ontologyBuilder.getAllCvs( oboCatPsi );
        Collection<String> psiMis = new ArrayList<String>();

        for ( CvDagObject cvDag : allPsimiCvs ) {

//            log.info( cvDag.getMiIdentifier() + " -> " + cvDag.getShortLabel() );
            OBOObject psimi = ( OBOObject ) oboSession.getObject( cvDag.getMiIdentifier() );
            Assert.assertTrue( ontologyBuilder.checkIfCategorySubset( psimi, oboCatPsi ) );
//            log.info( "isTrue PSI Term:  " + ontologyBuilder.checkIfCategorySubset( psimi, oboCatPsi ) );
            psiMis.add( psimi.getID() );
        }

        if ( log.isDebugEnabled() ) log.debug( "PSI-MI slim subset size " + allPsimiCvs.size() );
        if ( log.isDebugEnabled() ) log.debug( "PSI-MIs  size " + psiMis.size() );

        Collection<String> crossCheck = crossCheckFromOBOFile( OboCategory.PSI_MI_SLIM );

        if ( log.isDebugEnabled() ) log.debug( "crossCheckFromOBOFile().size() " + crossCheck.size() );
        Collection<String> difference = CollectionUtils.subtract( crossCheck, psiMis );
        log.info( "difference size " + difference.size() );

        for ( String diff : difference ) {
            if ( log.isDebugEnabled() ) log.debug( "diff MI: " + diff );
        }

        //Only the root term MI:0000 should be in the difference
        Assert.assertEquals( 1, difference.size() );


    } //end method


    public static void testCvObject( CvObject cvObject ) {

        if ( log.isDebugEnabled() )
            log.debug( "******************" + counter + " CvObject Begin*****************************" );
        counter++;
        String ac = cvObject.getAc();
        if ( log.isDebugEnabled() ) log.debug( "Ac->" + ac );

        String fullName = cvObject.getFullName();
        if ( log.isDebugEnabled() ) log.debug( "fullName->" + fullName );
        String miIdentifier = CvObjectUtils.getIdentity( cvObject );
        if ( log.isDebugEnabled() ) log.debug( "miIdentifier->" + miIdentifier );
        String objClass = cvObject.getObjClass();
        if ( log.isDebugEnabled() ) log.debug( "objClass->" + objClass );

        Institution owner = cvObject.getOwner();
        if ( log.isDebugEnabled() ) log.debug( "owner->" + owner );

        String shortLabel = cvObject.getShortLabel();
        if ( log.isDebugEnabled() ) log.debug( "shortLabel->" + shortLabel );

        if ( cvObject.getShortLabel() == null || cvObject.getShortLabel().length() < 1 ) {
            System.exit( 5 );
        }


        Collection<uk.ac.ebi.intact.model.Annotation> annotations = cvObject.getAnnotations();
        int annoCount = 1;
        for ( Annotation annotation : annotations ) {
            if ( annotation != null ) {
                if ( log.isDebugEnabled() )
                    log.debug( annoCount + " AnnotationText->" + annotation.getAnnotationText() );
                if ( annotation.getCvTopic() != null )
                    if ( log.isDebugEnabled() ) log.debug( annoCount + " CvTopic->" + annotation.getCvTopic() );

            } //end if
            annoCount++;
        } //end for


        Collection<CvObjectXref> xrefs = cvObject.getXrefs();
        int xrefCount = 1;
        for ( CvObjectXref cvObjectXref : xrefs ) {
            if ( log.isDebugEnabled() ) {
                log.debug( xrefCount + " cvObjectXref CvDatabase-> " + cvObjectXref.getCvDatabase() );
                log.debug( xrefCount + " cvObjectXref CvXref Qualifier-> " + cvObjectXref.getCvXrefQualifier() );
                log.debug( xrefCount + " cvObjectXref CvXref PrimaryId-> " + cvObjectXref.getPrimaryId() );
            }


            xrefCount++;
        }//end for


        Collection<CvObjectAlias> aliases = cvObject.getAliases();
        int aliasCount = 1;
        for ( CvObjectAlias cvObjectAlias : aliases ) {
            if ( log.isDebugEnabled() )
                log.debug( aliasCount + " cvObjectAlias-> " + cvObjectAlias.getName() + "   " + cvObjectAlias.getParent().getShortLabel() );

        } //end for


        log.info( "******************CvObject End*****************************" );
    } //end method


    private static Collection<String> crossCheckFromOBOFile( String category ) throws Exception {

        Collection<String> miCol = new ArrayList<String>();
        //URL url = CvUpdaterTest.class.getResource( "/psi-mi25.obo" );
        String revision = "1.48";
        URL url = new URL( OboUtils.PSI_MI_OBO_LOCATION + "?revision=" + revision );
        log.debug( "url " + url );

        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
        String inputLine;

        int termCounter = 0;
        int miCounter = 0;
        int obsoleteCounter = 0;
        int obsoleteCounterDef = 0;
        int typedefCounter = 0;

        int drugTerm = 0;
        int psiTerm = 0;
        String mi = null;
        while ( ( inputLine = in.readLine() ) != null ) {
            String temp;
            temp = inputLine;


            if ( inputLine.startsWith( "[Term]" ) ) {
                termCounter++;
            } else if ( inputLine.matches( "id:\\s+(MI:.*)" ) ) {

                mi = temp.split( "\\s+" )[1];
                miCounter++;
            } else if ( inputLine.contains( "is_obsolete: true" ) ) {
                obsoleteCounter++;
            } else if ( inputLine.matches( "def:.*?OBSOLETE.*" ) ) {
                obsoleteCounterDef++;
            } else if ( inputLine.startsWith( "[Typedef]" ) ) {
                typedefCounter++;
            } else if ( inputLine.matches( "subset:\\s+PSI-MI\\s+slim" ) ) {
                psiTerm++;
                if ( category.equalsIgnoreCase( OboCategory.PSI_MI_SLIM ) )
                    miCol.add( mi );

            } else if ( inputLine.matches( "subset:\\s+Drugable" ) ) {
                drugTerm++;
                if ( category.equalsIgnoreCase( OboCategory.DRUGABLE ) )
                    miCol.add( mi );

                //log.info(drugTerm+"  "+ temp );
            }
        }

        //948+1 with Typedef
        // Assert.assertEquals( 949, idCounter );
        Assert.assertEquals( 948, termCounter );
        Assert.assertEquals( 948, miCounter );
        Assert.assertEquals( 53, obsoleteCounter );
        Assert.assertEquals( 53, obsoleteCounterDef );
        Assert.assertEquals( 1, typedefCounter );
        Assert.assertEquals( 844, psiTerm );
        Assert.assertEquals( 124, drugTerm );

        in.close();

        return miCol;
    }//end method

    @Test
      public void build_cvsWithSameMi() throws Exception {
        OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.48" );
        log.debug( oboSession.getObjects().size() );

        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allCvs = ontologyBuilder.getAllCvs();

        if ( log.isDebugEnabled() ) {
            log.debug( "allCvs size from Test : " + allCvs.size() );
        }


        boolean foundExpRole = false;
        boolean foundBioRole = false;

        for ( CvDagObject cv : allCvs ) {

            if (cv.getShortLabel().contains("unspecified role")) {
           

                log.debug( "---CvObject--" + cv.getMiIdentifier() + "--" + cv.getShortLabel() );
                

                if ( CvBiologicalRole.class.getName().equals( cv.getObjClass() ) ) {
                    foundBioRole = true;
                }
                if ( CvExperimentalRole.class.getName().equals( cv.getObjClass() ) ) {
                    foundExpRole = true;
                }
            }
        }//end for

        Assert.assertTrue( "There should be an unspecified role of type CvBiologicalRole", foundBioRole );
        Assert.assertTrue( "There should be an unspecified role of type CvExperimentalRole", foundExpRole );
    }




}//end class






