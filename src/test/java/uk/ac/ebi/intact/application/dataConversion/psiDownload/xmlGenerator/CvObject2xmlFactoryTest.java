// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import junit.framework.Test;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1.CvObject2xmlPSI1;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2.CvObject2xmlPSI2;

/**
 * Test the behaviour of the CvObject2xmlFactory.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CvObject2xmlFactoryTest extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( CvObject2xmlFactoryTest.class );
    }

    ////////////////////////
    // Tests

    public void testGetInstance() {

        UserSessionDownload session = null;

        try {
            CvObject2xmlFactory.getInstance( session );
            fail( "giving a null session should not be allowed." );
        } catch ( Exception e ) {
            // ok !
        }
    }

    public void testGetInstancePSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );

        CvObject2xmlI cv = CvObject2xmlFactory.getInstance( session );

        assertNotNull( cv );
        assertTrue( cv instanceof CvObject2xmlPSI1 );
    }

    public void testGetInstancePSI2() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion2() );

        CvObject2xmlI cv = CvObject2xmlFactory.getInstance( session );

        assertNotNull( cv );
        assertTrue( cv instanceof CvObject2xmlPSI2 );
    }

    public void testGetInstancePSI25() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );

        CvObject2xmlI cv = CvObject2xmlFactory.getInstance( session );

        assertNotNull( cv );
        assertTrue( cv instanceof CvObject2xmlPSI2 );
    }
}