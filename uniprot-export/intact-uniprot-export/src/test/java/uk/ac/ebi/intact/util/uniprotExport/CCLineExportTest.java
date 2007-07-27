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
import java.util.LinkedList;
import java.util.List;

public class CCLineExportTest {

    private static final Log log = LogFactory.getLog(CCLineExportTest.class);


    /////////////////////////////////////////
    // Check on the ordering of the CC lines
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

        log.debug( "Before:" );
        for (CcLine ccLine : ccLines)
        {
            log.debug(ccLine.getGeneName());
        }

        Collections.sort( ccLines );

        Assert.assertEquals( 6, ccLines.size() );

        log.debug( "After:" );
        for( CcLine ccLine : ccLines ) {
            log.debug( ccLine.getGeneName() );
        }

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

        log.debug( "Before:" );
        for (CcLine ccLine : ccLines)
        {
            log.debug(ccLine.getGeneName());
        }

        Collections.sort( ccLines );

        Assert.assertEquals( 5, ccLines.size() );

        log.debug( "After:" );
        for (CcLine ccLine : ccLines)
        {
            log.debug(ccLine.getGeneName());
        }

        // check the ordering
        Assert.assertEquals( "aBCdef", ( ccLines.get( 0 ) ).getGeneName() );
        Assert.assertEquals( "aBcdEf", ( ccLines.get( 1 ) ).getGeneName() );
        Assert.assertEquals( "abCDef", ( ccLines.get( 2 ) ).getGeneName() );
        Assert.assertEquals( "abcdef", ( ccLines.get( 3 ) ).getGeneName() );
        Assert.assertEquals( "fedcba", ( ccLines.get( 4 ) ).getGeneName() );
    }

    @Test
    public void testCCLinesOrdering_3() {

        // create a collection of CC Lines to order
        List<CcLine> ccLines = new LinkedList<CcLine>();


        // Note: ASCII( 'a' )=65, and ASCII( 'A' )=97

//        AC   P24343
//        CC   -!- INTERACTION:
//        CC       Q90888:-; NbExp=1; IntAct=EBI-445651, EBI-445772;
//        CC       Q92171:-; NbExp=2; IntAct=EBI-445651, EBI-445622;
//        CC       P18870:JUN; NbExp=1; IntAct=EBI-445651, EBI-445826;
//        CC       Q90595:MAFF; NbExp=1; IntAct=EBI-445651, EBI-445786;
//        CC       Q90889:MAFG; NbExp=1; IntAct=EBI-445651, EBI-445799;
//        CC       Q90596:MAFK; NbExp=1; IntAct=EBI-445651, EBI-445812;
//        CC       P23091:V-MAF (xeno); NbExp=1; IntAct=EBI-445651, EBI-445752;


        ccLines.add( new CcLine( "blablabla", "V-MAF", "P23091" ) );
        ccLines.add( new CcLine( "blablabla", "MAFF", "Q90595" ) );
        ccLines.add( new CcLine( "blablabla", "JUN", "P18870" ) );
        ccLines.add( new CcLine( "blablabla", "-", "Q90888" ) );
        ccLines.add( new CcLine( "blablabla", "MAFK", "Q90596" ) );
        ccLines.add( new CcLine( "blablabla", "MAFG", "Q90889" ) );
        ccLines.add( new CcLine( "blablabla", "-", "Q92171" ) );

        Assert.assertEquals( 7, ccLines.size() );

        log.debug( "Before:" );
        for( CcLine ccLine : ccLines ) {
            log.debug( ccLine.getGeneName() );
        }

        Collections.sort( ccLines );

        Assert.assertEquals( 7, ccLines.size() );

        log.debug( "After:" );
        for (CcLine ccLine : ccLines)
        {
            log.debug(ccLine.getGeneName());
        }

        // check the ordering
        Assert.assertEquals( "-", ( ccLines.get( 0 ) ).getGeneName() );
        Assert.assertEquals( "-", ( ccLines.get( 1 ) ).getGeneName() );
        Assert.assertEquals( "JUN", ( ccLines.get( 2 ) ).getGeneName() );
        Assert.assertEquals( "MAFF", ( ccLines.get( 3 ) ).getGeneName() );
        Assert.assertEquals( "MAFG", ( ccLines.get( 4 ) ).getGeneName() );
        Assert.assertEquals( "MAFK", ( ccLines.get( 5 ) ).getGeneName() );
        Assert.assertEquals( "V-MAF", ( ccLines.get( 6 ) ).getGeneName() );
    }
}