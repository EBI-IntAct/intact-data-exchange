/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyHashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ReadOnlyHashMapTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ReadOnlyHashMapTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ReadOnlyHashMapTest.class );
    }

    private static Map myMap = new HashMap( 3 );

    static {
        myMap.put( "1", new Integer( 1 ) );
        myMap.put( "2", new Integer( 3 ) );
        myMap.put( "3", new Integer( 7 ) );
    }

    ////////////////////////
    // Creation

    public void testCreation() {

        ReadOnlyHashMap rohm = new ReadOnlyHashMap( myMap );
        assertNotNull( rohm );
        assertEquals( myMap.size(), rohm.size() );
        assertTrue( myMap.values().containsAll( rohm.values() ) );
        for ( Iterator iterator = myMap.keySet().iterator(); iterator.hasNext(); ) {
            String s = (String) iterator.next();

            Object o1 = myMap.get( s );
            Object o2 = rohm.get( s );
            assertEquals( o1, o2 );
        }
    }


    //////////////////////////
    // Forbidden methods

    public void test_forbidden_method_clear() {

        ReadOnlyHashMap rohm = new ReadOnlyHashMap( myMap );

        try {
            rohm.clear();
            fail( "ReadOnlyHashMap should not allow clear()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_put() {

        ReadOnlyHashMap rohm = new ReadOnlyHashMap( myMap );

        try {
            rohm.put( null, null );
            fail( "ReadOnlyHashMap should not allow put()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_putAll() {

        ReadOnlyHashMap rohm = new ReadOnlyHashMap( myMap );

        try {
            rohm.putAll( null );
            fail( "ReadOnlyHashMap should not allow putAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_remove() {

        ReadOnlyHashMap rohm = new ReadOnlyHashMap( myMap );

        try {
            rohm.remove( null );
            fail( "ReadOnlyHashMap should not allow remove()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }
}
