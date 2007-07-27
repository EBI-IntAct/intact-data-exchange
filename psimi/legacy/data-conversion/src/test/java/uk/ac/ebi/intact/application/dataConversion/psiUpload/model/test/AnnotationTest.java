/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.AnnotationTag;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public AnnotationTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( AnnotationTest.class );
    }


    public void testProcess_ok() {

        AnnotationTag annotation = new AnnotationTag( "type", "text" );
        assertNotNull( annotation );
        assertEquals( "type", annotation.getType() );
        assertEquals( "text", annotation.getText() );

    }

    public void testProcess_ok_no_text() {

        AnnotationTag annotation = new AnnotationTag( "type", "" );
        assertNotNull( annotation );
        assertEquals( "type", annotation.getType() );
        assertEquals( "", annotation.getText() );

    }

    public void testProcess_error_empty_type() {

        AnnotationTag annotation = null;
        try {
            annotation = new AnnotationTag( "", "text" );
            fail( "An annotationTag without a type should not be allowed to be created." );
        } catch ( Exception e ) {
            // ok
        }
        assertNull( annotation );
    }

    public void testProcess_error_type_null() {

        AnnotationTag annotation = null;
        try {
            annotation = new AnnotationTag( null, "text" );
            fail( "An annotationTag with a null type should not be allowed to be created." );
        } catch ( Exception e ) {
            // ok
        }
        assertNull( annotation );
    }
}
