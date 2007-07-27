/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.dataConversion;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for <code>ExperimentListItemTest</code>
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 08/10/2006
 */
public class ExperimentListItemTest extends TestCase {

    public ExperimentListItemTest( String name ) {
        super( name );
    }

    private static final String LABEL_1 = "test-2006-1";
    private static final String LABEL_2 = "test-2006-2";

    private ExperimentListItem mockWithOneLabel;
    private ExperimentListItem mockWithOneLabelLarge;
    private ExperimentListItem mockWithManyLabels;

    public void setUp() throws Exception {
        super.setUp();

        List<String> labels = new ArrayList<String>();
        labels.add( LABEL_1 );

        mockWithOneLabel = new ExperimentListItem( labels, "onelabel", "species", true, null, null );
        mockWithOneLabelLarge = new ExperimentListItem( labels, "onelabellarge", "pmid" + FileHelper.SLASH + "2006", false, 2, 2000 );
        List<String> labels2 = new ArrayList<String>();
        labels2.add( LABEL_1 );
        labels2.add( LABEL_2 );

        mockWithManyLabels = new ExperimentListItem( labels2, "manylabel", "species", false, 3, null );
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStaticParseString() throws Exception {
        ExperimentListItem experimentListItem = ExperimentListItem.parseString( "species/humt-_small.xml kanamori-2003-4" );
        assertEquals( "species/humt-_small.xml", experimentListItem.getFilename() );

        experimentListItem = ExperimentListItem.parseString( "species/yeast_small-11.xml suvorova-2002-4,metz-2006-4,huang-1998b-1,skibbens-2004-1,chilkova-2003-1,hall-2003-2,vandemark-20\\\n" +
                                                             "06-1,millson-2005-1,cairns-1994-2,szerlong-2003-2,bardwell-1996-3,mcnew-1998-1,gadbois-1997-1,treich-1995-2,howe-2002-2,bardw\\\n" +
                                                             "ell-1996-2,guo-1999-2,matsuura-2004-1,yao-2000-2,millson-2005-3,agarwal-2000-3,matsuura-2003-1,sabbioneda-2005-3,slessareva-2\\\n" +
                                                             "006-1,cote-1994-1,li-1995-2,singh-2006-2,gillingham-2004-2,skibbens-2004-2,zachariae-1998-2,measday-2002-1,suvorova-2002-5,mi\\\n" +
                                                             "zumura-1999-2,conibear-2000-3,costa-2005-1,slessareva-2006-3,wilkinson-2006-1,branzei-2006-1,kulp-2006-1,rea-2000-2,wang-2005\\\n" +
                                                             "b-1,armache-2005-1,measday-1997-1,warren-2002-1,xu-2003b-1,whitby-2000-1,ohdate-2003-2,tong-2002a-4,brown-2000-1,park-2002-2,\\\n" +
                                                             "jonsson-2001-1,kofler-2005-5,eisen-2001-4,downey-2006-4,qi-2000-4,tsubouchi-2002-1,hershey-1999-4,madania-1999-2,pike-2004-2,\\\n" +
                                                             "costa-2005-3,rudge-2004-1,wang-1999c-1,dziembowski-2004-1,basmaji-2006-2,kenna-2003-2,pike-2004-3,flores_roza-1998-1,hermjako\\\n" +
                                                             "b-2004-5,karpichev-1997-1,kobor-2004-1,fiedler-2002-1,seol-2001-1,downey-2006-3,chin-2006-2,lalioti-2002a-2,blumer-1988-1,gal\\\n" +
                                                             "arneau-2000-2,chilkova-2003-3,milkereit-2001-2,seol-2001-2,eugster-2000-2,chavan-2003-1,gonzales-2005-3,hershey-1999-1,vignol\\\n" +
                                                             "s-2005-1,matern-2000-1,rodriguez_n-2002-1,english-2006-1,lalo-1996-1,allard-1999-1,mizuguchi-2004-2,singh-2006-3,eugster-2000\\\n" +
                                                             "-3,fiedler-2002-2,chavan-2005-1,kim-2005a-1,zhao-2005-2" );
        assertEquals( "species/yeast_small-11.xml", experimentListItem.getFilename() );

        experimentListItem = ExperimentListItem.parseString( "species/drome_giot-2003-1_01.xml giot-2003-1 [1,2000]" );
        assertEquals( "species/drome_giot-2003-1_01.xml", experimentListItem.getFilename() );

        experimentListItem = ExperimentListItem.parseString( "pmid/2007/unassigned1.xml ewans-2007-1" );
        assertEquals( "pmid/2007/unassigned1.xml", experimentListItem.getFilename() );
    }


    public void testGetFilename() throws Exception {
        assertEquals( "species" + FileHelper.SLASH + "onelabel_negative.xml", mockWithOneLabel.getFilename() );
        assertEquals( "pmid" + FileHelper.SLASH + "2006" + FileHelper.SLASH + "onelabellarge_test-2006-1_02.xml", mockWithOneLabelLarge.getFilename() );
        assertEquals( "species" + FileHelper.SLASH + "manylabel-03.xml", mockWithManyLabels.getFilename() );
    }

    public void testGetPattern() throws Exception {
        assertEquals( "test-2006-1", mockWithOneLabel.getPattern() );
        assertEquals( "test-2006-1", mockWithOneLabelLarge.getPattern() );
        assertEquals( "test-2006-1,test-2006-2", mockWithManyLabels.getPattern() );
    }

    public void testGetChunkNumber() throws Exception {
        assertNull( mockWithOneLabel.getChunkNumber() );
        assertEquals( Integer.valueOf( 3 ), mockWithManyLabels.getChunkNumber() );
    }

    public void testGetLargeScaleChunkSize() throws Exception {
        assertNull( mockWithOneLabel.getLargeScaleChunkSize() );
        assertEquals( Integer.valueOf( 2000 ), mockWithOneLabelLarge.getLargeScaleChunkSize() );
    }

    public void testGetName() throws Exception {
        assertEquals( "onelabel", mockWithOneLabel.getName() );
        assertEquals( "manylabel", mockWithManyLabels.getName() );
    }

    public void testGetExperimentLabels() throws Exception {
        assertEquals( 1, mockWithOneLabel.getExperimentLabels().size() );
        assertEquals( 2, mockWithManyLabels.getExperimentLabels().size() );
    }

    public void testGetInteractionRange() throws Exception {
        assertEquals( "", mockWithOneLabel.getInteractionRange() );
        assertEquals( "[2001,4000]", mockWithOneLabelLarge.getInteractionRange() );
    }

    public void testGetParentFolders() throws Exception {
        assertEquals( "species", mockWithOneLabel.getParentFolders() );
        assertEquals( "pmid" + FileHelper.SLASH + "2006", mockWithOneLabelLarge.getParentFolders() );
        assertEquals( "species", mockWithManyLabels.getParentFolders() );
    }

    public void testToString() throws Exception {
        assertEquals( "species" + FileHelper.SLASH + "onelabel_negative.xml test-2006-1", mockWithOneLabel.toString() );
        assertEquals( "pmid" + FileHelper.SLASH + "2006" + FileHelper.SLASH + "onelabellarge_test-2006-1_02.xml test-2006-1 [2001,4000]", mockWithOneLabelLarge.toString() );
        assertEquals( "species" + FileHelper.SLASH + "manylabel-03.xml test-2006-1,test-2006-2", mockWithManyLabels.toString() );
    }

    public void testParseString() throws Exception {
        assertEquals( mockWithOneLabel, ExperimentListItem.parseString( mockWithOneLabel.toString() ) );
        assertEquals( mockWithOneLabelLarge, ExperimentListItem.parseString( mockWithOneLabelLarge.toString() ) );
        assertEquals( mockWithManyLabels, ExperimentListItem.parseString( mockWithManyLabels.toString() ) );


        ExperimentListItem e = ExperimentListItem.parseString( "BioCreative" + FileHelper.SLASH + "16682412.xml li-2006b-1,li-2006b-2,li-2006b-3,li-2006b-4" );
        assertEquals( "BioCreative", e.getParentFolders() );
        assertEquals( "BioCreative" + FileHelper.SLASH + "16682412.xml", e.getFilename() );
        assertNull( e.getChunkNumber() );

        assertTrue( e.getExperimentLabels().contains( "li-2006b-1" ) );
        assertTrue( e.getExperimentLabels().contains( "li-2006b-2" ) );
        assertTrue( e.getExperimentLabels().contains( "li-2006b-3" ) );
        assertTrue( e.getExperimentLabels().contains( "li-2006b-4" ) );

        assertEquals( "", e.getInteractionRange() );
        assertEquals( null, e.getLargeScaleChunkSize() );
        assertEquals( "li-2006b-1,li-2006b-2,li-2006b-3,li-2006b-4", e.getPattern() );


    }

    public static Test suite() {
        return new TestSuite( ExperimentListItemTest.class );
    }
}
