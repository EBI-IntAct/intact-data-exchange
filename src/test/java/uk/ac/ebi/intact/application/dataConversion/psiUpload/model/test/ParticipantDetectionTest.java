/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.Constants;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ParticipantDetectionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ParticipantDetectionTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ParticipantDetectionTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ParticipantDetectionTest.class );
    }


    public void testProcess_ok() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( xref );
        assertNotNull( participantDetection );
        assertEquals( xref, participantDetection.getPsiDefinition() );
    }


    public void testProcess_error_no_xref() {

        ParticipantDetectionTag participantDetection = null;
        try {
            participantDetection = new ParticipantDetectionTag( null );
            fail( "Should not allow to create an participantDetection with a null Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( participantDetection );
    }


    public void testProcess_error_wrong_database() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        ParticipantDetectionTag participantDetection = null;
        try {
            participantDetection = new ParticipantDetectionTag( xref );
            fail( "Should not allow to create an participantDetection with a non PSI Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( participantDetection );
    }
}