/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyIterator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class Test the behaviour of the ReadOnlyCollection.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ReadOnlyCollectionTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ReadOnlyCollectionTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ReadOnlyCollectionTest.class );
    }

    private static Collection myCollection = new ArrayList( 3 );

    static {
        myCollection.add( new Integer( 1 ) );
        myCollection.add( new Integer( 3 ) );
        myCollection.add( new Integer( 5 ) );
        myCollection.add( new Integer( 7 ) );
    }

    ////////////////////////
    // Creation

    public void testCreation() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );
        assertNotNull( roc );
        assertEquals( myCollection.size(), roc.size() );
        assertTrue( myCollection.containsAll( roc ) );
        assertTrue( roc.iterator() instanceof ReadOnlyIterator );
    }


    //////////////////////////
    // Forbidden methods

    public void test_forbidden_method_add() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.add( null );
            fail( "ReadOnlyCollection should not allow add()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_retainAll() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.retainAll( null );
            fail( "ReadOnlyCollection should not allow retainAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_removeAll() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.removeAll( null );
            fail( "ReadOnlyCollection should not allow removeAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_remove() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.remove( null );
            fail( "ReadOnlyCollection should not allow remove()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_clear() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.clear();
            fail( "ReadOnlyCollection should not allow clear()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }

    public void test_forbidden_method_addAll() {

        ReadOnlyCollection roc = new ReadOnlyCollection( myCollection );

        try {
            roc.addAll( null );
            fail( "ReadOnlyCollection should not allow addAll()" );
        } catch ( UnsupportedOperationException e ) {
            // ok
        }
    }
}
