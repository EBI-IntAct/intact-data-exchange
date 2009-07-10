/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public FeatureTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( FeatureTest.class );
    }


    public void testProcess_ok_all_param() {

        XrefTag xref1 = new XrefTag( XrefTag.SECONDARY_REF, "id1", "interpro" );
        XrefTag xref2 = new XrefTag( XrefTag.SECONDARY_REF, "id2", "interpro" );
        XrefTag xref3 = new XrefTag( XrefTag.SECONDARY_REF, "id3", "intact" );
        Collection xrefs = new ArrayList( 3 );
        xrefs.add( xref1 );
        xrefs.add( xref2 );
        xrefs.add( xref3 );

        XrefTag xrefFeatureType = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        FeatureTypeTag featureType = new FeatureTypeTag( xrefFeatureType );

        // location
        LocationIntervalTag from = new LocationIntervalTag( 1, 5 );
        LocationIntervalTag to = new LocationIntervalTag( 3, 8 );
        LocationTag location = new LocationTag( from, to );

        XrefTag fd = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        FeatureDetectionTag featureDetection = new FeatureDetectionTag( fd );

        FeatureTag feature = new FeatureTag( "shortlabel", "fullname", featureType,
                                             location, featureDetection,
                                             xrefs );

        assertNotNull( feature );

        assertEquals( "shortlabel", feature.getShortlabel() );
        assertEquals( "fullname", feature.getFullname() );

        assertTrue( xrefs.containsAll( feature.getXrefs() ) );
        assertTrue( feature.getXrefs().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertEquals( feature.getLocation().getFromIntervalStart(), 1 );
        assertEquals( feature.getLocation().getFromIntervalEnd(), 5 );
        assertEquals( feature.getLocation().getToIntervalStart(), 3 );
        assertEquals( feature.getLocation().getToIntervalEnd(), 8 );

        assertTrue( feature.getFeatureDetection().equals( featureDetection ) );
        assertTrue( feature.getFeatureType().equals( featureType ) );
    }

    ////////////////////////////////
    // missing parameter

    public void testProcess_error_no_shortlabel() {

        XrefTag xref1 = new XrefTag( XrefTag.SECONDARY_REF, "id1", "interpro" );
        XrefTag xref2 = new XrefTag( XrefTag.SECONDARY_REF, "id2", "interpro" );
        XrefTag xref3 = new XrefTag( XrefTag.SECONDARY_REF, "id3", "intact" );
        Collection xrefs = new ArrayList( 3 );
        xrefs.add( xref1 );
        xrefs.add( xref2 );
        xrefs.add( xref3 );

        XrefTag xrefFeatureType = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        FeatureTypeTag featureType = new FeatureTypeTag( xrefFeatureType );

        // location
        LocationIntervalTag from = new LocationIntervalTag( 1, 5 );
        LocationIntervalTag to = new LocationIntervalTag( 3, 8 );
        LocationTag location = new LocationTag( from, to );

        XrefTag fd = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        FeatureDetectionTag featureDetection = new FeatureDetectionTag( fd );

        FeatureTag feature = null;

        // Shortlabel
        try {
            feature = new FeatureTag( null, "fullname", featureType,
                                      location, featureDetection,
                                      xrefs );
            assertNotNull( feature );
        } catch ( IllegalArgumentException e ) {
            fail();
        }


        try {
            feature = new FeatureTag( "  ", "fullname", featureType,
                                      location, featureDetection,
                                      xrefs );

            assertNotNull( feature );
        } catch ( IllegalArgumentException e ) {
            fail();
        }


        // location
        feature = null;
        try {
            feature = new FeatureTag( "shortlabel", "fullname", featureType,
                                      null, featureDetection,
                                      xrefs );

        } catch ( IllegalArgumentException e ) {
            // ok
        }
        assertNull( feature );


        // feaure detection
        feature = null;
        try {
            feature = new FeatureTag( "shortlabel", "fullname", featureType,
                                      location, null,
                                      xrefs );

            assertNotNull( feature );
        } catch ( IllegalArgumentException e ) {
            fail();
        }
    }
}