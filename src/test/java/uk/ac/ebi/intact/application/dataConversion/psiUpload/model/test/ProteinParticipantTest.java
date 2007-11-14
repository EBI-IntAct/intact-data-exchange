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

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinParticipantTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ProteinParticipantTest( final String name ) {
        super( name );
    }

    private FeatureTag createFeature( String aFeatureType,
                                      String aDetectionType,
                                      String aClusterID,
                                      String aInterproID,
                                      long begin1, long begin2,
                                      long end1, long end2 ) {

        FeatureTypeTag featureType = new FeatureTypeTag( new XrefTag( XrefTag.PRIMARY_REF, aFeatureType, "psi-mi" ) );
        FeatureDetectionTag featureDetection = new FeatureDetectionTag( new XrefTag( XrefTag.PRIMARY_REF, aDetectionType, "psi-mi" ) );

        Collection xrefs = new ArrayList();
        xrefs.add( new XrefTag( XrefTag.PRIMARY_REF, aInterproID, "interpro" ) );
        xrefs.add( new XrefTag( XrefTag.PRIMARY_REF, aClusterID, FeatureTag.FEATURE_CLUSTER_ID_XREF ) );

        LocationTag location = new LocationTag( new LocationIntervalTag( begin1, begin2 ), new LocationIntervalTag( end1, end2 ) );
        FeatureTag feature = new FeatureTag( "feature1", null, featureType, location, featureDetection, xrefs );

        return feature;
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ProteinParticipantTest.class );
    }

    public void testProcess_ok() {

        XrefTag xrefCellType = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xrefCellType, "shortlabel" );

        XrefTag xrefTissue = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xrefTissue, "shortlabel" );

        OrganismTag organism = new OrganismTag( "1234", cellType, tissue );

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, organism );

        AnnotationTag annotation = new AnnotationTag( "expressedIn", "12345:biosource" );
        ExpressedInTag expressedIn = new ExpressedInTag( annotation );

        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor,
                                                                              "bait",
                                                                              expressedIn,
                                                                              null,
                                                                              null,
                                                                              null );

        assertNotNull( proteinParticipant );
        assertEquals( expressedIn, proteinParticipant.getExpressedIn() );
        assertEquals( "bait", proteinParticipant.getRole() );
        assertEquals( proteinInteractor, proteinParticipant.getProteinInteractor() );
        assertNull( proteinParticipant.getOverExpressedProtein() );
        assertNull( proteinParticipant.getTaggedProtein() );
    }


    public void testProcess_ok_minimal_param() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );

        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor,
                                                                              "bait",
                                                                              null,
                                                                              null,
                                                                              Boolean.TRUE,
                                                                              Boolean.FALSE );

        assertNotNull( proteinParticipant );
        assertEquals( null, proteinParticipant.getExpressedIn() );
        assertEquals( "bait", proteinParticipant.getRole() );
        assertEquals( proteinInteractor, proteinParticipant.getProteinInteractor() );
        assertEquals( Boolean.TRUE, proteinParticipant.getTaggedProtein() );
        assertEquals( Boolean.FALSE, proteinParticipant.getOverExpressedProtein() );
    }


    public void testProcess_ok_features() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );


        Collection features = new ArrayList();
        features.add( createFeature( "type1", "detect1", "1", "blabla", 1, 1, 3, 5 ) );
        features.add( createFeature( "type1", "detect1", "2", "blabla", 2, 21, 99, 99 ) );
        features.add( createFeature( "type1", "detect1", "1", "blabla", 7, 7, 8, 8 ) );

        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor,
                                                                              "bait",
                                                                              null,
                                                                              features,
                                                                              Boolean.FALSE,
                                                                              Boolean.TRUE );

        assertNotNull( proteinParticipant );
        assertEquals( null, proteinParticipant.getExpressedIn() );
        assertEquals( "bait", proteinParticipant.getRole() );
        assertEquals( proteinInteractor, proteinParticipant.getProteinInteractor() );
        assertEquals( true, proteinParticipant.hasFeature() );
        assertEquals( 3, proteinParticipant.getFeatures().size() );
        assertEquals( 2, proteinParticipant.getClusteredFeatures().size() );
        assertEquals( Boolean.FALSE, proteinParticipant.getTaggedProtein() );
        assertEquals( Boolean.TRUE, proteinParticipant.getOverExpressedProtein() );
    }


    public void testProcess_error_empty_role() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );

        ProteinParticipantTag proteinParticipant = null;
        try {
            proteinParticipant = new ProteinParticipantTag( proteinInteractor, "", null, null, null, null );
            fail( "Should not allow to create a proteinParticipant with empty role." );
        } catch ( Exception e ) {
            // ok
        }

        assertNull( proteinParticipant );
    }


    public void testProcess_error_role_null() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );

        ProteinParticipantTag proteinParticipant = null;
        try {
            proteinParticipant = new ProteinParticipantTag( proteinInteractor, null, null, null, null, null );
            fail( "Should not allow to create a proteinParticipant with null role." );
        } catch ( Exception e ) {
            // ok
        }

        assertNull( proteinParticipant );
    }


    public void testProcess_error_no_protein() {

        ProteinParticipantTag proteinParticipant = null;
        try {
            proteinParticipant = new ProteinParticipantTag( null, "bait", null, null, null, null );
            fail( "Should not allow to create a proteinParticipant with a null proteinInteractor." );
        } catch ( Exception e ) {
            // ok
        }

        assertNull( proteinParticipant );
    }
}