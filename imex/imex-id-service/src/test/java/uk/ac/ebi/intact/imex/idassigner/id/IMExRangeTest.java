/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.imex.idassigner.id;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ebi.intact.imex.idservice.id.IMExRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * IMExRange Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: IMExRangeTest.java 4871 2006-05-18 08:21:32Z skerrien $
 * @since <pre>05/15/2006</pre>
 */
public class IMExRangeTest {

    ////////////////////////////////
    // Compatibility with JUnit 3

    public static junit.framework.Test suite() {
         return new JUnit4TestAdapter( IMExRangeTest.class );
    }

    /////////////////////
    // Tests

    @Test
    public void testImexRangeFromTo() {

        new IMExRange( 1, 13, 13, "IntAct" );
        new IMExRange( 1, 13, 100000, "IntAct" );
        new IMExRange( 1, 2, 3, "Foo" );

        try {
            new IMExRange( 1, 14, 13, "IntAct" );
            fail();
        } catch ( Exception e ) {
            // ok
        }

        try {
            new IMExRange( 1, 1498259287, 13, "IntAct" );
            fail();
        } catch ( Exception e ) {
            // ok
        }
    }

    @Test
    public void getTimestamp() {
        assertNotNull( new IMExRange( 1, 10, 13, "IntAct" ).getTimestamp() );
        assertNull( new IMExRange( 10, 13 ).getTimestamp() );
    }

    @Test
    public void getSubmissionId() {
        assertEquals( 1L, new IMExRange( 1, 10, 13, "IntAct" ).getSubmissionId() );

        assertEquals( -1L, new IMExRange( 10, 13 ).getSubmissionId() );
    }

    @Test
    public void getPartner() {
        assertEquals( "IntAct", new IMExRange( 1, 10, 13, "IntAct" ).getPartner() );
        assertNull( new IMExRange( 10, 13 ).getPartner() );
    }

    @Test
    public void getFrom() {
        assertEquals( 10L, new IMExRange( 1, 10, 13, "IntAct" ).getFrom() );
        assertEquals( 10L, new IMExRange( 10, 13 ).getFrom() );
    }

    @Test
    public void getTo() {
        assertEquals( 13L, new IMExRange( 1, 10, 13, "IntAct" ).getTo() );
        assertEquals( 13L, new IMExRange( 10, 13 ).getTo() );
    }

    @Test
    public void iterator() {
        IMExRange range = new IMExRange( 1, 10, 13, "IntAct" );

        Collection<Long> values = new ArrayList<Long>();
        Iterator<Long> iterator = range.iterator();

        while ( iterator.hasNext() ) {
            values.add( iterator.next() );
        }

        assertEquals( 4, values.size() );
        values.contains( 10 );
        values.contains( 11 );
        values.contains( 12 );
        values.contains( 13 );
    }
}
