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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionTypeTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public InteractionTypeTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( InteractionTypeTest.class );
    }


    public void testProcess_ok() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );
        assertNotNull( interactionType );
        assertEquals( xref, interactionType.getPsiDefinition() );
    }


    public void testProcess_error_no_xref() {

        InteractionTypeTag interactionType = null;
        try {
            interactionType = new InteractionTypeTag( null );
            fail( "Should not allow to create an interactionType with a null Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( interactionType );
    }


    public void testProcess_error_wrong_database() {

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        InteractionTypeTag interactionType = null;
        try {
            interactionType = new InteractionTypeTag( xref );
            fail( "Should not allow to create an interactionType without a PSI Xref." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( interactionType );
    }
}