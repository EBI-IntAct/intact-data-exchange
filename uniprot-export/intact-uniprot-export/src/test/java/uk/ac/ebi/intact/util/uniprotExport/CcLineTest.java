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

import java.util.*;
import java.io.StringWriter;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.core.persister.PersisterHelper;

public class CcLineTest extends UniprotExportTestCase {

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

    @Test
    public void getEligibleProteins_only_uniprot() throws Exception {

        // build data:
        //             4 uniprot proteins
        //             2 interactions: involving only 3 distinct uniprot proteins

        final BioSource human = getMockBuilder().createBioSource( 9606, "human" );
        final BioSource mouse = getMockBuilder().createBioSource( 10032, "mouse" );
        mouse.setFullName( "Mus musculus" );
        final Protein q9swi1 = getMockBuilder().createProtein( "Q9SWI1", "Q9SWI1_HUMAN", human );
        q9swi1.getAliases().clear();
        q9swi1.addAlias( new InteractorAlias( getMockBuilder().getInstitution(), q9swi1,
                                              getMockBuilder().createCvObject( CvAliasType.class,
                                                                               CvAliasType.GENE_NAME_MI_REF,
                                                                               CvAliasType.GENE_NAME ),
                                              "GN_q9swi1"));
        final Protein p14712 = getMockBuilder().createProtein( "P14712", "P14712_HUMAN", mouse );
        final Protein p14713 = getMockBuilder().createProtein( "P14713", "P14712_HUMAN", human );
        p14713.getAliases().clear();
        p14713.addAlias( new InteractorAlias( getMockBuilder().getInstitution(), p14713,
                                              getMockBuilder().createCvObject( CvAliasType.class,
                                                                               CvAliasType.ORF_NAME_MI_REF,
                                                                               CvAliasType.ORF_NAME ),
                                              "ORF_p14713"));
        final Protein p12345 = getMockBuilder().createProtein( "P12345", "P12345_HUMAN", human );


        final Interaction interaction1 = getMockBuilder().createInteraction( q9swi1, p14713 );
        final Interaction interaction2 = getMockBuilder().createInteraction( q9swi1, p14712 );
        final Experiment exp = getMockBuilder().createDeterministicExperiment();
        final CvTopic uniprotDrExport = getMockBuilder().createCvObject( CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT );
        final Annotation annotation = new Annotation( getMockBuilder().getInstitution(), uniprotDrExport, "yes" );
        exp.addAnnotation( annotation );
        interaction1.addExperiment( exp );
        interaction2.addExperiment( exp );

        PersisterHelper.saveOrUpdate( q9swi1, p14712, p14713, p12345 );
        StringWriter ccWriter = new StringWriter();
        StringWriter goaWriter = new StringWriter();
        CCLineExport exporter = new CCLineExport( ccWriter, goaWriter );

        Collection<String> identifiers = Arrays.asList( "P14713", "P14712", "Q9SWI1" );
        exporter.generateCCLines( identifiers );

        Assert.assertTrue( ccWriter.getBuffer().length() > 0 );
    }
}