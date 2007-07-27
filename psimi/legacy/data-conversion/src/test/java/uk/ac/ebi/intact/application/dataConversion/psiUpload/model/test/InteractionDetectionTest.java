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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionDetectionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionDetectionTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public InteractionDetectionTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( InteractionDetectionTest.class );
    }


    public void testProcess_ok() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( xref );
        assertNotNull( interactionDetection );
        assertEquals( xref, interactionDetection.getPsiDefinition() );
    }


    public void testProcess_error_no_xref() {

        InteractionDetectionTag interactionDetection = null;
        try {
            interactionDetection = new InteractionDetectionTag( null );
            fail( "Should not allow to create an interactionDetection with a null Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( interactionDetection );
    }


    public void testProcess_error_wrong_database() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        InteractionDetectionTag interactionDetection = null;
        try {
            interactionDetection = new InteractionDetectionTag( xref );
            fail( "Should not allow to create an interactionDetection with a non PSI Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( interactionDetection );
    }
}