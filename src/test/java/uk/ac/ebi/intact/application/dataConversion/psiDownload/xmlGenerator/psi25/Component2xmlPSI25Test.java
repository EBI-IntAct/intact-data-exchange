/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2.Component2xmlPSI2;
import uk.ac.ebi.intact.model.*;

/**
 * Component2xmlPSI25 Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Component2xmlPSI25Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 * @since <pre>04/25/2005</pre>
 */
public class Component2xmlPSI25Test extends TestCase {
    public Component2xmlPSI25Test( String name ) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite( Component2xmlPSI25Test.class );
    }

    /////////////////////
    // Tests

    public void testGetInstance() throws Exception {
        //TODO: Test goes here...
    }

    public void testIsTaggedFeature() throws Exception {

        // Build a DAG
        Institution institution = new Institution( "ebi" );
        CvDatabase psi = new CvDatabase( institution, "psi-mi" );
        CvXrefQualifier identity = new CvXrefQualifier( institution, "identity" );

        CvFeatureType featureType1 = new CvFeatureType( institution, "1" );
        featureType1.addXref( new CvObjectXref ( institution, psi, "MI:0001", null, null, identity ) );

        CvFeatureType featureType2 = new CvFeatureType( institution, "2" );
        featureType2.addXref( new CvObjectXref ( institution, psi, "MI:0507", null, null, identity ) );
        featureType2.addParent( featureType1 );

        CvFeatureType featureType3 = new CvFeatureType( institution, "3" );
        featureType3.addXref( new CvObjectXref ( institution, psi, "MI:0003", null, null, identity ) );
        featureType3.addParent( featureType1 );

        CvFeatureType featureType4 = new CvFeatureType( institution, "4" );
        featureType4.addXref( new CvObjectXref ( institution, psi, "MI:0034", null, null, identity ) );
        featureType4.addParent( featureType2 );

        CvFeatureType featureType5 = new CvFeatureType( institution, "5" );
        featureType5.addXref( new CvObjectXref ( institution, psi, "MI:0065", null, null, identity ) );
        featureType5.addParent( featureType2 );

        CvFeatureType featureType6 = new CvFeatureType( institution, "6" );
        featureType6.addXref( new CvObjectXref ( institution, psi, "MI:0222", null, null, identity ) );
        featureType6.addParent( featureType3 );

        CvFeatureType featureType7 = new CvFeatureType( institution, "7" );
        featureType7.addXref( new CvObjectXref ( institution, psi, "MI:9999", null, null, identity ) );
        featureType7.addParent( featureType5 );

        /* Node 2 has the MI we are looking after !
         * hence 2 and its children (4, 5, 7) should return true while the others (1, 3, 6) should return false.
         *             1
         *           /   \
         *         (2)    3
         *         / \     \
         *        4   5     6
         *            |
         *            7
         */

        Component2xmlPSI2 c2x = Component2xmlPSI2.getInstance();

        assertFalse( c2x.isTaggedFeature( featureType1 ) );
        assertTrue( c2x.isTaggedFeature( featureType2 ) );
        assertFalse( c2x.isTaggedFeature( featureType3 ) );
        assertTrue( c2x.isTaggedFeature( featureType4 ) );
        assertTrue( c2x.isTaggedFeature( featureType5 ) );
        assertFalse( c2x.isTaggedFeature( featureType6 ) );
        assertTrue( c2x.isTaggedFeature( featureType7 ) );
    }

    public void testCreate() throws Exception {
        //TODO: Test goes here...
    }
}
