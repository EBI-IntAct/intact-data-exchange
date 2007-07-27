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
public class InteractionTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public InteractionTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( InteractionTest.class );
    }

    ///////////////////////////
    // Utility methods

    private ExperimentDescriptionTag getExperimentDescription1() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                                       bibRef,
                                                                                       null, // otherBibRef
                                                                                       null, // xrefs
                                                                                       null, // annotations
                                                                                       hostOrganism,
                                                                                       interactionDetection,
                                                                                       participantDetection );
        return experimentDescription;
    }

    private ExperimentDescriptionTag getExperimentDescription2() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        XrefTag bibRef2 = new XrefTag( XrefTag.SECONDARY_REF, "123006789", CvDatabase.PUBMED );
        XrefTag bibRef3 = new XrefTag( XrefTag.SECONDARY_REF, "123456700", CvDatabase.PUBMED );
        Collection otherBibRef = new ArrayList( 2 );
        otherBibRef.add( bibRef2 );
        otherBibRef.add( bibRef3 );

        XrefTag xref1 = new XrefTag( XrefTag.SECONDARY_REF, "id1", "interpro" );
        XrefTag xref2 = new XrefTag( XrefTag.SECONDARY_REF, "id2", "interpro" );
        XrefTag xref3 = new XrefTag( XrefTag.SECONDARY_REF, "id3", "intact" );
        Collection xrefs = new ArrayList( 3 );
        xrefs.add( xref1 );
        xrefs.add( xref2 );
        xrefs.add( xref3 );

        AnnotationTag annotation1 = new AnnotationTag( "comment", "blababla" );
        AnnotationTag annotation2 = new AnnotationTag( "remark", "bladibla" );
        Collection annotations = new ArrayList( 2 );
        annotations.add( annotation1 );
        annotations.add( annotation2 );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                                       bibRef,
                                                                                       otherBibRef,
                                                                                       xrefs,
                                                                                       annotations,
                                                                                       hostOrganism,
                                                                                       interactionDetection,
                                                                                       participantDetection );
        return experimentDescription;
    }

    private ProteinParticipantTag getProteinParticipant1() {

        XrefTag xrefCellType = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        CellTypeTag cellType = new CellTypeTag( xrefCellType, "shortlabel" );

        XrefTag xrefTissue = new XrefTag( XrefTag.PRIMARY_REF, "id", "db" );
        TissueTag tissue = new TissueTag( xrefTissue, "shortlabel" );

        OrganismTag organism = new OrganismTag( "1234", cellType, tissue );

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P12345", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, organism );

        AnnotationTag annotation = new AnnotationTag( "expressedIn", "12345:biosource" );
        ExpressedInTag expressedIn = new ExpressedInTag( annotation );

        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor, "bait", expressedIn, null, null, null );

        return proteinParticipant;
    }

    private ProteinParticipantTag getProteinParticipant2() {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, "P99945", "uniprot" );

        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );

        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor, "prey", null, null, null, null );

        return proteinParticipant;
    }


    ///////////////////////////////
    // Tests creation

    public void testProcess_ok() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        Collection xrefs = new ArrayList( 4 );
        XrefTag xref1 = new XrefTag( XrefTag.PRIMARY_REF, "id1", "db1" );
        XrefTag xref2 = new XrefTag( XrefTag.PRIMARY_REF, "id2", "db1" );
        XrefTag xref3 = new XrefTag( XrefTag.PRIMARY_REF, "id3", "db2" );
        XrefTag xref4 = new XrefTag( XrefTag.PRIMARY_REF, "id4", "db3" );
        xrefs.add( xref1 );
        xrefs.add( xref2 );
        xrefs.add( xref3 );
        xrefs.add( xref4 );

        Collection annotations = new ArrayList( 2 );
        AnnotationTag annotation1 = new AnnotationTag( "comment", "sakbvksbv" );
        AnnotationTag annotation2 = new AnnotationTag( "remark", "saighlaigh" );
        annotations.add( annotation1 );
        annotations.add( annotation2 );

        ConfidenceTag confidence = new ConfidenceTag( "percent", "50" );

        InteractionTag interaction = new InteractionTag( "shortlabel", "fullname",
                                                         experiments,
                                                         participants,
                                                         interactionType,
                                                         xrefs,
                                                         annotations,
                                                         confidence );

        assertNotNull( interaction );
        assertEquals( "shortlabel", interaction.getShortlabel() );
        assertEquals( "fullname", interaction.getFullname() );

        assertTrue( experiments.containsAll( interaction.getExperiments() ) );
        assertTrue( interaction.getExperiments().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( participants.containsAll( interaction.getParticipants() ) );
        assertTrue( interaction.getParticipants().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( xrefs.containsAll( interaction.getXrefs() ) );
        assertTrue( interaction.getXrefs().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( annotations.containsAll( interaction.getAnnotations() ) );
        assertTrue( interaction.getAnnotations().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertEquals( confidence, interaction.getConfidence() );
        assertEquals( interactionType, interaction.getInteractionType() );
    }

    public void testProcess_ok_minimal_param() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = new InteractionTag( null, null,
                                                         experiments,
                                                         participants,
                                                         interactionType,
                                                         null, // xrefs
                                                         null, // annotations
                                                         null ); // confidence

        assertNotNull( interaction );
        assertEquals( null, interaction.getShortlabel() );
        assertEquals( null, interaction.getFullname() );

        assertTrue( experiments.containsAll( interaction.getExperiments() ) );
        assertTrue( interaction.getExperiments().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( participants.containsAll( interaction.getParticipants() ) );
        assertTrue( interaction.getParticipants().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertNotNull( interaction.getXrefs() );
        assertTrue( interaction.getXrefs().getClass().isAssignableFrom( ReadOnlyCollection.class ) );
        assertTrue( interaction.getXrefs().isEmpty() );

        assertNotNull( interaction.getAnnotations() );
        assertTrue( interaction.getAnnotations().getClass().isAssignableFrom( ReadOnlyCollection.class ) );
        assertTrue( interaction.getAnnotations().isEmpty() );

        assertEquals( null, interaction.getConfidence() );
    }


    ///////////////////////////////////
    // Wrong parameters

    public void testProcess_error_null_experiment() {

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              null, // experiments
                                              participants,
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction without experiment." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_empty_experiment_collection() {

        Collection experiments = new ArrayList( 0 );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              participants,
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with 0 experiment." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_null_participants() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              null, // participants
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with null participant collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_empty_participant_collection() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 0 );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              participants,
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with 0 participant." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_wrong_experiemnts_content() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( new Integer( 1 ) );             // !!!!!!
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              participants,
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with object being not ExperimentDescription in the experiments collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_wrong_participant_content() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( new Integer( 2 ) );          // !!!!!!
        participants.add( getProteinParticipant2() );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        InteractionTag interaction = null; // confidence
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              participants,
                                              interactionType,
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with object being not ProteinParticipant in the participants collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }

    public void testProcess_error_interactionType_null() {

        Collection experiments = new ArrayList( 2 );
        experiments.add( getExperimentDescription1() );
        experiments.add( getExperimentDescription2() );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant1() );
        participants.add( getProteinParticipant2() );

        InteractionTag interaction = null;
        try {
            interaction = new InteractionTag( "shortlabel", "fullname",
                                              experiments,
                                              participants,
                                              null, //interactionType
                                              null, // xrefs
                                              null, // annotations
                                              null );
            fail( "Should not allow to create an Interaction with a null interactionType." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( interaction );
    }
}