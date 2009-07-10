// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import junit.framework.Test;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableProtein;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class UserSessionDownloadTest extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( UserSessionDownloadTest.class );
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
    }

    ////////////////////////
    // Tests

    public void testIsExportable() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );
        session.addAnnotationFilter( remark );

        Annotation annotation = new Annotation( owner, remark );
        assertFalse( session.isExportable( annotation ) );

        annotation = new Annotation( owner, comment );
        assertTrue( session.isExportable( annotation ) );
    }

    public void testIsProteinAlreadyDefined() {

        Protein protein = new TestableProtein( "EBI-111", owner, yeast, "prot", proteinType, null );
        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        assertFalse( session.isAlreadyDefined( protein ) );
        session.declareAlreadyDefined( protein );
        assertTrue( session.isAlreadyDefined( protein ) );
    }

    public void testIsExperimentAlreadyDefined() {

        Experiment experiment = new Experiment( owner, "prot", yeast );
        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        assertFalse( session.isAlreadyDefined( experiment ) );
        session.declareAlreadyDefined( experiment );
        assertTrue( session.isAlreadyDefined( experiment ) );
    }

    public void testgetExperimentIdentifier() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        try {
            session.getExperimentIdentifier( null );
            fail( "null is not allowed." );
        } catch ( Exception e ) {
            // ok
        }

        Experiment experiment = new Experiment( owner, "exp", human );
        Experiment experiment2 = new Experiment( owner, "exp2", human );
        Experiment experiment3 = new Experiment( owner, "exp3", yeast );

        long id = session.getExperimentIdentifier( experiment );
        assertEquals( id, session.getExperimentIdentifier( experiment ) );

        long id2 = session.getExperimentIdentifier( experiment2 );
        assertEquals( id2, session.getExperimentIdentifier( experiment2 ) );

        long id3 = session.getExperimentIdentifier( experiment3 );
        assertEquals( id3, session.getExperimentIdentifier( experiment3 ) );

        assertFalse( id == id2 );
        assertFalse( id == id3 );
        assertFalse( id2 == id3 );

        try {
            id = session.getExperimentIdentifier( experiment ); // already generated
        } catch ( Exception e ) {
            //ok
        }
    }

    public void testgetParticipantIdentifier() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        try {
            session.getParticipantIdentifier( null );
            fail( "null is not allowed." );
        } catch ( Exception e ) {
            // ok
        }

        Experiment experiment = new Experiment( owner, "exp", human );
        Collection experiments = new ArrayList( 1 );
        experiments.add( experiment );
        Interaction interaction = new InteractionImpl( experiments, aggregation, interactionType, "interaction", owner );
        Protein protein = new ProteinImpl( owner, yeast, "bbc1_yeast", proteinType );
        Protein protein2 = new ProteinImpl( owner, yeast, "cdc42_yeast", proteinType );

        Component component = new Component( owner, interaction, protein, bait, unspecified );
        Component component2 = new Component( owner, interaction, protein, prey, unspecified );
        Component component3 = new Component( owner, interaction, protein2, prey, unspecified );

        long id1 = session.getParticipantIdentifier( component );
        assertTrue( id1 != session.getParticipantIdentifier( component ) );

        long id2 = session.getParticipantIdentifier( component2 );
        assertTrue( id2 != session.getParticipantIdentifier( component2 ) );
        assertTrue( id1 != id2 );

        long id3 = session.getParticipantIdentifier( component3 );
        assertTrue( id3 != session.getParticipantIdentifier( component3 ) );
        assertTrue( id1 != id3 );
        assertTrue( id2 != id3 );
    }
     /*
    public void testgetFeatureIdentifier() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );
                                                 
        try {
            session.getFeatureIdentifier( null );
            fail( "null is not allowed." );
        } catch ( Exception e ) {
            // ok
        }

        Experiment experiment = new Experiment( owner, "exp", human );
        Collection experiments = new ArrayList( 1 );
        experiments.add( experiment );
        Interaction interaction = new InteractionImpl( experiments, aggregation, interactionType, "interaction", owner );
        Protein protein = new ProteinImpl( owner, yeast, "bbc1_yeast", proteinType );
        Protein protein2 = new ProteinImpl( owner, yeast, "cdc42_yeast", proteinType );

        Component component = new Component( owner, interaction, protein, bait );
        Component component2 = new Component( owner, interaction, protein, prey );
        Component component3 = new Component( owner, interaction, protein2, prey );

        Feature feature = new Feature( owner, "region", component, formylation );
        Feature feature2 = new Feature( owner, "region", component2, formylation );
        Feature feature3 = new Feature( owner, "region", component3, hydroxylation );

        long id = session.getFeatureIdentifier( feature );
        assertEquals( id, session.getFeatureIdentifier( feature ) );

        long id2 = session.getFeatureIdentifier( feature2 );
        assertEquals( id2, session.getFeatureIdentifier( feature2 ) );

        long id3 = session.getFeatureIdentifier( feature3 );
        assertEquals( id3, session.getFeatureIdentifier( feature3 ) );

        assertFalse( id == id2 );
        assertFalse( id == id3 );
        assertFalse( id2 == id3 );

        try {
            id = session.getFeatureIdentifier( feature ); // already generated
        } catch ( Exception e ) {
            //ok
        }
    }  */
     /*
    public void testgetInteractorIdentifier() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        try {
            session.getInteractorIdentifier( null );
            fail( "null is not allowed." );
        } catch ( Exception e ) {
            // ok
        }

        Protein protein = new ProteinImpl( owner, yeast, "bbc1_yeast", proteinType );
        Protein protein2 = new ProteinImpl( owner, yeast, "cdc42_yeast", proteinType );
        Experiment experiment = new Experiment( owner, "exp", human );
        Collection experiments = new ArrayList( 1 );
        experiments.add( experiment );
        Interaction interaction = new InteractionImpl( experiments, aggregation, interactionType, "interaction", owner );


        long id = session.getInteractorIdentifier( protein );
        assertEquals( id, session.getInteractorIdentifier( protein ) );

        long id2 = session.getInteractorIdentifier( protein2 );
        assertEquals( id2, session.getInteractorIdentifier( protein2 ) );

        long id3 = session.getInteractorIdentifier( interaction );
        assertEquals( id3, session.getInteractorIdentifier( interaction ) );

        assertFalse( id == id2 );
        assertFalse( id == id3 );
        assertFalse( id2 == id3 );

        try {
            id = session.getExperimentIdentifier( experiment ); // already generated
        } catch ( Exception e ) {
            //ok
        }
    } */
    /*
    public void testgetInteractionIdentifier() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        try {
            session.getInteractionIdentifier( null );
            fail( "null is not allowed." );
        } catch ( Exception e ) {
            // ok
        }

        Experiment experiment = new Experiment( owner, "exp", human );
        Collection experiments = new ArrayList( 1 );
        experiments.add( experiment );
        Interaction interaction = new InteractionImpl( experiments, aggregation, interactionType, "interaction", owner );
        Interaction interaction2 = new InteractionImpl( experiments, cleavage, interactionType, "interaction2", owner );
        Interaction interaction3 = new InteractionImpl( experiments, aggregation, interactionType, "interaction3", owner );


        long id = session.getInteractionIdentifier( interaction );
        assertEquals( id, session.getInteractionIdentifier( interaction ) );

        long id2 = session.getInteractionIdentifier( interaction2 );
        assertEquals( id2, session.getInteractionIdentifier( interaction2 ) );

        long id3 = session.getInteractionIdentifier( interaction3 );
        assertEquals( id3, session.getInteractionIdentifier( interaction3 ) );

        assertFalse( id == id2 );
        assertFalse( id == id3 );
        assertFalse( id2 == id3 );

        try {
            id = session.getInteractorIdentifier( interaction3 ); // already generated
        } catch ( Exception e ) {
            //ok
        }
    } */
}