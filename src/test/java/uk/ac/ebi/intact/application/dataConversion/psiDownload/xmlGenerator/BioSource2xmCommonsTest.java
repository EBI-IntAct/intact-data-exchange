// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;

/**
 * Test the behaviour of the Biosource2xmlFactory
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class BioSource2xmCommonsTest extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( BioSource2xmCommonsTest.class );
    }

    ////////////////////////
    // Tests

    public void test1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );

        BioSource2xmlI bsi = BioSource2xmlFactory.getInstance( session );

        Element parent = session.createElement( "hostOrganismList" );
        Element parent2 = session.createElement( "hostOrganismList" );
        Element parent3 = session.createElement( "hostOrganismList" );

        Element b = BioSource2xmlFactory.getInstance( session ).createHostOrganism( session, parent, human );
        BioSource2xmlFactory.getInstance( session ).createHostOrganism( session, parent2, human );
        parent3.appendChild( b.cloneNode( true ) );

//        session.getExperimentListElement().appendChild( parent );
//        session.getExperimentListElement().appendChild( parent2 );
    }
}