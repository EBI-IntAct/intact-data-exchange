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

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.net.URL;
import java.util.Collection;


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

        URL url = CvObjectOntologyBuilderTest.class.getResource( "/psi-mi25.obo" );

        log.info( "url " + url );

        OBOSession oboSession = OboUtils.createOBOSession( url );
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );


        Assert.assertEquals( 16, ontologyBuilder.getRootOBOObjects().size() );
        Assert.assertEquals( 894, ontologyBuilder.getAllValidCvsAsList().size() );
        Assert.assertEquals( 53, ontologyBuilder.getOrphanCvObjects().size() );
        Assert.assertEquals( 947, ontologyBuilder.getAllValidCvsAsList().size() + ontologyBuilder.getOrphanCvObjects().size() );


        Assert.assertEquals( uk.ac.ebi.intact.model.CvInteraction.class, ontologyBuilder.findCvClassforMI( "MI:0439" ) );
        Assert.assertEquals( uk.ac.ebi.intact.model.CvDatabase.class, ontologyBuilder.findCvClassforMI( "MI:0244" ) );//non-root object
        Assert.assertEquals( uk.ac.ebi.intact.model.CvFeatureIdentification.class, ontologyBuilder.findCvClassforMI( "MI:0003" ) );//root object

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
        // OBOObject testObj = (OBOObject)oboSession.getObject("MI:0244"); //example with 4 annotations + search-url


    } //end method


    public static void testCvObject( CvObject cvObject ) {


        log.info( "******************" + counter + " CvObject Begin*****************************" );
        counter++;
        String ac = cvObject.getAc();
        log.debug( "Ac->" + ac );

        String fullName = cvObject.getFullName();
        log.debug( "fullName->" + fullName );
        String miIdentifier = CvObjectUtils.getIdentity( cvObject );
        log.debug( "miIdentifier->" + miIdentifier );
        String objClass = cvObject.getObjClass();
        log.debug( "objClass->" + objClass );

        Institution owner = cvObject.getOwner();
        log.debug( "owner->" + owner );

        String shortLabel = cvObject.getShortLabel();
        log.debug( "shortLabel->" + shortLabel );

        if ( cvObject.getShortLabel() == null || cvObject.getShortLabel().length() < 1 ) {
            System.exit( 5 );
        }


        Collection<uk.ac.ebi.intact.model.Annotation> annotations = cvObject.getAnnotations();
        int annoCount = 1;
        for ( Annotation annotation : annotations ) {
            if ( annotation != null ) {
                log.debug( annoCount + " AnnotationText->" + annotation.getAnnotationText() );
                if ( annotation.getCvTopic() != null )
                    log.debug( annoCount + " CvTopic->" + annotation.getCvTopic() );

            } //end if
            annoCount++;
        } //end for


        Collection<CvObjectXref> xrefs = cvObject.getXrefs();
        int xrefCount = 1;
        for ( CvObjectXref cvObjectXref : xrefs ) {
            log.debug( xrefCount + " cvObjectXref CvDatabase-> " + cvObjectXref.getCvDatabase() );
            log.debug( xrefCount + " cvObjectXref CvXref Qualifier-> " + cvObjectXref.getCvXrefQualifier() );
            log.debug( xrefCount + " cvObjectXref CvXref PrimaryId-> " + cvObjectXref.getPrimaryId() );


            xrefCount++;
        }//end for


        Collection<CvObjectAlias> aliases = cvObject.getAliases();
        int aliasCount = 1;
        for ( CvObjectAlias cvObjectAlias : aliases ) {
            log.debug( aliasCount + " cvObjectAlias-> " + cvObjectAlias.getName() + "   " + cvObjectAlias.getParent().getShortLabel() );

        } //end for


        log.info( "******************CvObject End*****************************" );
    } //end method


}//end class






