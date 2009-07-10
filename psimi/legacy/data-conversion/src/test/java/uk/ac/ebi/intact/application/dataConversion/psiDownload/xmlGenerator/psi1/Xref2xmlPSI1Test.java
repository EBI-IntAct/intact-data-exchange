// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Xref2xmlFactory;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Xref;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Xref2xmlPSI1Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class Xref2xmlPSI1Test extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( Xref2xmlPSI1Test.class );
    }

    ////////////////////////
    // Tests

    public void testBuildXref_primaryRef_nullArguments() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        // create a container
        Element xrefElement = session.createElement( "xref" );

        // call the method we are testing
        Element primaryRef = null;

        try {
            primaryRef = Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, null );
            fail( "giving a null xref should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( primaryRef );

        // create the IntAct object
        Xref xref = new InteractorXref( owner, uniprot, "P12345", "P67890", "56", identity );

        try {
            primaryRef = Xref2xmlFactory.getInstance( session ).createPrimaryRef( null, xrefElement, xref );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( primaryRef );

        try {
            primaryRef = Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, null, xref );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( primaryRef );
    }

    public void testBuildXref_primaryRef_ok() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        // create a container
        Element xrefElement = session.createElement( "xref" );

        // create the IntAct object
        Xref xref = new InteractorXref( owner, uniprot, "P12345", "P67890", "56", identity );

        // call the method we are testing
        Element primaryRef = Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, xref );
        assertNotNull( primaryRef );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 1, xrefElement.getChildNodes().getLength() );
        Element _primaryRef = (Element) xrefElement.getChildNodes().item( 0 );
        assertEquals( primaryRef, _primaryRef );

        // check content of the tag
        assertEquals( "primaryRef", primaryRef.getNodeName() );
        assertEquals( "P12345", primaryRef.getAttribute( "id" ) );
        assertEquals( "uniprotkb", primaryRef.getAttribute( "db" ) );
        assertEquals( "P67890", primaryRef.getAttribute( "secondary" ) );
        assertEquals( "56", primaryRef.getAttribute( "version" ) );
    }

    public void testBuildXref_secondaryRef_ok() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        // create a container
        Element xrefElement = session.createElement( "xref" );

        // create the IntAct object
        Xref xref = new InteractorXref( owner, uniprot, "P12345", "P67890", "56", identity );

        // call the method we are testing
        Element primaryRef = Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
        assertNotNull( primaryRef );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 1, xrefElement.getChildNodes().getLength() );
        Element _primaryRef = (Element) xrefElement.getChildNodes().item( 0 );
        assertEquals( primaryRef, _primaryRef );

        // check content of the tag
        assertEquals( "secondaryRef", primaryRef.getNodeName() );
        assertEquals( "P12345", primaryRef.getAttribute( "id" ) );
        assertEquals( "uniprotkb", primaryRef.getAttribute( "db" ) );
        assertEquals( "P67890", primaryRef.getAttribute( "secondary" ) );
        assertEquals( "56", primaryRef.getAttribute( "version" ) );
    }
}