/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ConfidenceTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ConfidenceTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ConfidenceTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ConfidenceTest.class );
    }


    public void testProcess_ok() {

        ConfidenceTag confidence = new ConfidenceTag( "percent", "50" );
        assertNotNull( confidence );
        assertEquals( "percent", confidence.getUnit() );
        assertEquals( "50", confidence.getValue() );

    }

    public void testProcess_ok_no_value() {

        ConfidenceTag confidence = new ConfidenceTag( "percent", "" );
        assertNotNull( confidence );
        assertEquals( "percent", confidence.getUnit() );
        assertEquals( "", confidence.getValue() );

    }

    public void testProcess_ok_no_unit() {

        ConfidenceTag confidence = new ConfidenceTag( "", "50" );
        assertNotNull( confidence );
        assertEquals( "", confidence.getUnit() );
        assertEquals( "50", confidence.getValue() );

    }

    public void testProcess_error_unit_null() {

        ConfidenceTag confidence = null;
        try {
            confidence = new ConfidenceTag( null, "text" );
            fail( "An ConfidenceTag without a type should not be allowed to be created." );
        } catch ( Exception e ) {
            // ok
        }
        assertNull( confidence );
    }

    public void testProcess_error_type_null() {

        ConfidenceTag confidence = null;
        try {
            confidence = new ConfidenceTag( "percent", null );
            fail( "An ConfidenceTag with a null type should not be allowed to be created." );
        } catch ( Exception e ) {
            // ok
        }
        assertNull( confidence );
    }
}
