/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.util.uniprotExport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CcLineTest  {

    private static final Log log = LogFactory.getLog(CcLineTest.class);


    /////////////////////////////////////////
    // Check on the ordering of the CC lines

    private void displayCollection( List list, String title ) {

        log.debug( title );
        log.debug( "----------" );
        for( Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            CcLine ccLine = (CcLine) iterator.next();
            log.debug( "\t" + ccLine.getGeneName() );
        }
    }

    @Test
    public void testCCLinesOrdering() {

        // create a collection of CC Lines to order
        List<CcLine> ccLines = new LinkedList<CcLine>();


        // Note: ASCII( 'a' )=65, and ASCII( 'A' )=97

        ccLines.add( new CcLine( "blablabla", "abCDef", "" ) );
        ccLines.add( new CcLine( "blablabla", "abcdef", "" ) );
        ccLines.add( new CcLine( "blablabla", "Self", "" ) );
        ccLines.add( new CcLine( "blablabla", "fedcba", "" ) );
        ccLines.add( new CcLine( "blablabla", "aBcdEf", "" ) );
        ccLines.add( new CcLine( "blablabla", "aBCdef", "" ) );

        Assert.assertEquals( 6, ccLines.size() );

        displayCollection( ccLines, "Before:" );

        Collections.sort( ccLines );

        Assert.assertEquals( 6, ccLines.size() );

        displayCollection( ccLines, "After:" );

        // check the ordering
        Assert.assertEquals( "Self", ( ccLines.get( 0 ) ).getGeneName() );
        Assert.assertEquals( "aBCdef", ( ccLines.get( 1 ) ).getGeneName() );
        Assert.assertEquals( "aBcdEf", ( ccLines.get( 2 ) ).getGeneName() );
        Assert.assertEquals( "abCDef", ( ccLines.get( 3 ) ).getGeneName() );
        Assert.assertEquals( "abcdef", ( ccLines.get( 4 ) ).getGeneName() );
        Assert.assertEquals( "fedcba", ( ccLines.get( 5 ) ).getGeneName() );
    }

    @Test
    public void testCCLinesOrdering_2() {

        // create a collection of CC Lines to order
        List<CcLine> ccLines = new LinkedList<CcLine>();


        // Note: ASCII( 'a' )=65, and ASCII( 'A' )=97

        ccLines.add( new CcLine( "blablabla", "abCDef", "" ) );
        ccLines.add( new CcLine( "blablabla", "abcdef", "" ) );
        ccLines.add( new CcLine( "blablabla", "fedcba", "" ) );
        ccLines.add( new CcLine( "blablabla", "aBcdEf", "" ) );
        ccLines.add( new CcLine( "blablabla", "aBCdef", "" ) );

        Assert.assertEquals( 5, ccLines.size() );

        displayCollection( ccLines, "Before:" );

        Collections.sort( ccLines );

        Assert.assertEquals( 5, ccLines.size() );

        displayCollection( ccLines, "After:" );


        // check the ordering
        Assert.assertEquals( "aBCdef", ( ccLines.get( 0 ) ).getGeneName() );
        Assert.assertEquals( "aBcdEf", ( ccLines.get( 1 ) ).getGeneName() );
        Assert.assertEquals( "abCDef", ( ccLines.get( 2 ) ).getGeneName() );
        Assert.assertEquals( "abcdef", ( ccLines.get( 3 ) ).getGeneName() );
        Assert.assertEquals( "fedcba", ( ccLines.get( 4 ) ).getGeneName() );
    }
}