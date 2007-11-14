/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyIterator;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlySet;

import java.util.HashSet;
import java.util.Set;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ReadOnlySetTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ReadOnlySetTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ReadOnlySetTest.class );
    }

    private static Set myCollection = new HashSet( 3 );

    static {
        myCollection.add( new Integer( 1 ) );
        myCollection.add( new Integer( 3 ) );
        myCollection.add( new Integer( 5 ) );
        myCollection.add( new Integer( 7 ) );
    }

    ////////////////////////
    // Creation

    public void testCreation() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );
        assertNotNull( ros );
        assertEquals( myCollection.size(), ros.size() );
        assertTrue( myCollection.containsAll( ros ) );
        assertTrue( ros.iterator() instanceof ReadOnlyIterator );
    }


    //////////////////////////
    // Forbidden methods

    public void test_forbidden_method_add() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.add( null );
            fail( "ReadOnlySet should not allow add()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_retainAll() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.retainAll( null );
            fail( "ReadOnlySet should not allow retainAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_removeAll() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.removeAll( null );
            fail( "ReadOnlySet should not allow removeAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_remove() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.remove( null );
            fail( "ReadOnlySet should not allow remove()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_clear() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.clear();
            fail( "ReadOnlySet should not allow clear()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_addAll() {

        ReadOnlySet ros = new ReadOnlySet( myCollection );

        try {
            ros.addAll( null );
            fail( "ReadOnlySet should not allow addAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }
}
