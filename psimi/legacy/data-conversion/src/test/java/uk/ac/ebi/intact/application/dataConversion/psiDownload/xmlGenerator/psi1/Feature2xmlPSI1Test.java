// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Feature2xmlPSI1Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class Feature2xmlPSI1Test extends PsiDownloadTest {

    protected void setUp() throws Exception
    {
        super.setUp();
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
    }


    private Feature buildSimpleFeature() {

        Experiment experiment = new Experiment( owner, "exp-2005-1", yeast );
        Collection experiments = new ArrayList( 1 );
        experiments.add( experiment );


        Protein protein = new ProteinImpl( owner, yeast, "bbc1_yeast", proteinType );
        Interaction interaction = new InteractionImpl( experiments, aggregation, interactionType, "bbc1-xxx", owner );
        Component component = new Component( owner, interaction, protein, bait, unspecified );

        CvFeatureType acetylation = new CvFeatureType( owner, "acetylation" );
        acetylation.addXref( new CvObjectXref( owner, psi, "MI:0121", null, null, identity ) );

        Feature feature = new Feature( owner, "region", component, acetylation );

        Range range = new Range( owner, 2, 34, null );
        feature.addRange( range );

        feature.addXref( new FeatureXref( owner, interpro, "IPRxxxxxxx", null, null, null ) );

        CvFeatureIdentification docking = new CvFeatureIdentification( owner, "docking" );
        docking.addXref( new CvObjectXref( owner, psi, "MI:0035", null, null, identity ) );
        feature.setCvFeatureIdentification( docking );

        return feature;
    }

    ////////////////////////
    // Tests

    private void testBuildFeature_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( version );
        Feature2xmlI f = Feature2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "featureList" );

        // call the method we are testing
        Element element = null;

        try {
            f.create( session, parent, null );
            fail( "giving a null Feature should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object
        Feature feature = buildSimpleFeature();

        try {
            f.create( null, parent, feature );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        try {
            f.create( session, null, feature );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildFeature_nullArguments_PSI1() {
        testBuildFeature_nullArguments( PsiVersion.getVersion1() );
    }

    public void testBuildFeature_full_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );
        Feature2xmlI f = Feature2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "featureList" );

        // call the method we are testing
        Element element = null;

        // create the IntAct object
        Feature feature = buildSimpleFeature();

        // generating the PSI element...
        f.create( session, parent, feature );

        NodeList list = parent.getElementsByTagName( "feature" );
        assertNotNull( list );
        assertEquals( 1, list.getLength() );
        element = (Element) list.item( 0 );

        // starting the checks...
        assertNotNull( parent );
        assertEquals( 1, parent.getChildNodes().getLength() );

        assertNotNull( element );
        // xref featureDescription location featureDetection
        assertEquals( 4, element.getChildNodes().getLength() );

        // Checking xref...
        Element xref = (Element) element.getElementsByTagName( "xref" ).item( 0 );
        assertNotNull( xref );
        assertEquals( 1, xref.getChildNodes().getLength() );
        assertHasPrimaryRef( xref, "IPRxxxxxxx", "interpro", null, null );

        // Checking featureDetection...
        Element featureDetection = (Element) element.getElementsByTagName( "featureDetection" ).item( 0 );
        assertNotNull( featureDetection );
        assertEquals( 2, featureDetection.getChildNodes().getLength() );
        // Checking featureDetection's names...
        Element names = (Element) featureDetection.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 1, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "docking" );
        // Checking featureDetection's PSI ID...
        xref = (Element) featureDetection.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0035", "psi-mi", null, null );

        // Checking featureDescription...
        Element participantDetection = (Element) element.getElementsByTagName( "featureDescription" ).item( 0 );
        assertNotNull( participantDetection );
        assertEquals( 2, participantDetection.getChildNodes().getLength() );
        // Checking featureDescription's names...
        names = (Element) participantDetection.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 1, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "acetylation" );
        // Checking featureDescription's PSI ID...
        xref = (Element) participantDetection.getElementsByTagName( "xref" ).item( 0 );
        assertHasPrimaryRef( xref, "MI:0121", "psi-mi", null, null );

        // checking location...
        Element location = (Element) element.getElementsByTagName( "location" ).item( 0 );
        assertNotNull( location );
        assertEquals( 2, location.getChildNodes().getLength() );
        Element begin = (Element) element.getElementsByTagName( "begin" ).item( 0 );
        assertEquals( "2", begin.getAttribute( "position" ) );
        Element end = (Element) element.getElementsByTagName( "end" ).item( 0 );
        assertEquals( "34", end.getAttribute( "position" ) );
    }


    public void testBuildFeature_multiRange_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion1() );
        Feature2xmlI f = Feature2xmlFactory.getInstance( session );

        // create a container
        Element parent = session.createElement( "featureList" );

        // create the IntAct object
        Feature feature = buildSimpleFeature();
        // add a second range to the feature
        feature.addRange( new Range( owner, 120, 120, 130, 140, null ) ); // will generate a second feature.
        feature.addRange( new Range( owner, 12, 15, 200, 202, null ) ); // will generate a second feature.

        // generating the PSI element...
        f.create( session, parent, feature );

        // starting the checks...
        assertNotNull( parent );
        assertEquals( feature.getRanges().size(), parent.getChildNodes().getLength() ); // one feature per couple feature-range

        NodeList list = parent.getChildNodes();
        int count = list.getLength();
        for ( int i = 0; i < count; i++ ) {

            Element element = (Element) list.item( i );

            assertNotNull( element );
            // xref featureDescription location featureDetection

            assertEquals( 4, element.getChildNodes().getLength() );

            // Checking xref...
            Element xref = (Element) element.getElementsByTagName( "xref" ).item( 0 );
            assertNotNull( xref );
            assertEquals( 2, xref.getChildNodes().getLength() );
            assertHasPrimaryRef( xref, "IPRxxxxxxx", "interpro", null, null );
            String id = FeatureTag.FEATURE_CLUSTER_ID_PREFIX + session.getClusterIdSuffix();
            assertHasSecondaryRef( xref, id, FeatureTag.FEATURE_CLUSTER_ID_XREF, null, null );

            // Checking featureDetection...
            Element featureDetection = (Element) element.getElementsByTagName( "featureDetection" ).item( 0 );
            assertNotNull( featureDetection );
            assertEquals( 2, featureDetection.getChildNodes().getLength() );
            // Checking featureDetection's names...
            Element names = (Element) featureDetection.getElementsByTagName( "names" ).item( 0 );
            assertNotNull( names );
            assertEquals( 1, names.getChildNodes().getLength() );
            assertHasShortlabel( names, "docking" );
            // Checking featureDetection's PSI ID...
            xref = (Element) featureDetection.getElementsByTagName( "xref" ).item( 0 );
            assertHasPrimaryRef( xref, "MI:0035", "psi-mi", null, null );

            // Checking featureDescription...
            Element participantDetection = (Element) element.getElementsByTagName( "featureDescription" ).item( 0 );
            assertNotNull( participantDetection );
            assertEquals( 2, participantDetection.getChildNodes().getLength() );
            // Checking featureDescription's names...
            names = (Element) participantDetection.getElementsByTagName( "names" ).item( 0 );
            assertNotNull( names );
            assertEquals( 1, names.getChildNodes().getLength() );
            assertHasShortlabel( names, "acetylation" );
            // Checking featureDescription's PSI ID...
            xref = (Element) participantDetection.getElementsByTagName( "xref" ).item( 0 );
            assertHasPrimaryRef( xref, "MI:0121", "psi-mi", null, null );

            // checking location...
            Element location = (Element) element.getElementsByTagName( "location" ).item( 0 );

            // XOR(^): one location or the other but not both
            assertTrue( hasLocation( location, 2, 2, 34, 34 )
                        ^ hasLocation( location, 120, 120, 130, 140 )
                        ^ hasLocation( location, 12, 15, 200, 202 ) );
        }
    }

    private boolean hasLocation( Element location, int startBegin, int startEnd, int stopBegin, int stopEnd ) {

        if ( null == location ) {
            return false;
        }

        if ( 2 != location.getChildNodes().getLength() ) {
            return false;
        }

        if ( startBegin == startEnd ) {

            Element begin = (Element) location.getElementsByTagName( "begin" ).item( 0 );
            if ( null == begin ) {
                return false;
            }

            if ( false == ( "" + startBegin ).equals( begin.getAttribute( "position" ) ) ) {
                return false;
            }

        } else {

            Element begin = (Element) location.getElementsByTagName( "beginInterval" ).item( 0 );
            if ( null == begin ) {
                return false;
            }

            if ( false == ( "" + startBegin ).equals( begin.getAttribute( "begin" ) ) ) {
                return false;
            }

            if ( false == ( "" + startEnd ).equals( begin.getAttribute( "end" ) ) ) {
                return false;
            }
        }

        if ( stopBegin == stopEnd ) {

            Element end = (Element) location.getElementsByTagName( "end" ).item( 0 );
            if ( null == end ) {
                return false;
            }

            if ( false == ( "" + stopBegin ).equals( end.getAttribute( "position" ) ) ) {
                return false;
            }

        } else {

            Element end = (Element) location.getElementsByTagName( "endInterval" ).item( 0 );
            if ( null == end ) {
                return false;
            }

            if ( false == ( "" + stopBegin ).equals( end.getAttribute( "begin" ) ) ) {
                return false;
            }

            if ( false == ( "" + stopEnd ).equals( end.getAttribute( "end" ) ) ) {
                return false;
            }
        }

        return true;
    }
}