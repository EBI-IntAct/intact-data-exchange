// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlI;
import uk.ac.ebi.intact.model.CvFeatureIdentification;
import uk.ac.ebi.intact.model.CvObjectXref;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:CvObject2xmlPSI2Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class CvObject2xmlPSI2Test extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( CvObject2xmlPSI2Test.class );
    }

    ////////////////////////
    // Tests

    private void testBuildCvObject_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( version );

        // create a container
        Element parent = session.createElement( "featureDetection" );

        // call the method we are testing
        Element element = null;

        CvObject2xmlI cvo = CvObject2xmlFactory.getInstance( session );

        try {
            element = cvo.create( session, parent, null );
            fail( "giving a null CvObject should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object
        CvFeatureIdentification cvObject = new CvFeatureIdentification( owner, "feature ident" );

        try {
            element = cvo.create( null, parent, cvObject );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        try {
            element = cvo.create( session, null, cvObject );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildCvObject_nullArguments_PSI2() {
        testBuildCvObject_nullArguments( PsiVersion.getVersion2() );
    }

    public void testBuildXref_ok_PSI2() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion2() );
        CvObject2xmlI cvo = CvObject2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "experimentDescription" );

        // create the IntAct object
        CvFeatureIdentification cvObject = new CvFeatureIdentification( owner, "feature ident" );
        cvObject.setFullName( "longer description of that feature identification." );
        cvObject.addXref( new CvObjectXref( owner, psi, "MI:0001", "secondary", "v1", identity ) );
        cvObject.addXref( new CvObjectXref( owner, pubmed, "12345678", null, null, null ) );

        // TODO create a TestableCvObject that allows to set the AC, then only we can create Aliases for it !!
//        cvObject.addAlias( new Alias( owner, ) );


        assertEquals( 2, cvObject.getXrefs().size() );

        // call the method we are testing
        Element element = cvo.create( session, parent, cvObject );

        assertEquals( 1, parent.getChildNodes().getLength() );

        assertNotNull( element );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 2, element.getChildNodes().getLength() );
        Element _element = (Element) parent.getChildNodes().item( 0 );
        assertEquals( element, _element );

        // check content of the tag
        assertEquals( "featureDetectionMethod", element.getNodeName() );

        // checking names...
        Element names = (Element) element.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 2, names.getChildNodes().getLength() );

        assertHasShortlabel( names, "feature ident" );
        assertHasFullname( names, "longer description of that feature identification." );

        // checking xrefs...
        Element xref = (Element) element.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0001", "psi-mi", "secondary", "v1" );

        // Find out which Xref are we supposed to export into PSI for a CV.
        assertHasSecondaryRef( xref, "12345678", "pubmed", null, null );
    }
}