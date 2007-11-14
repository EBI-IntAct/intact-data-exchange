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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyHashMap;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class EntryTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public EntryTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( EntryTest.class );
    }

    ///////////////////////////
    // Utility methods

    private ExperimentDescriptionTag getExperimentDescription( String id, String pudmed ) {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, pudmed, CvDatabase.PUBMED );

        XrefTag xref1 = new XrefTag( XrefTag.SECONDARY_REF, "id1" + id, "interpro" );
        XrefTag xref3 = new XrefTag( XrefTag.SECONDARY_REF, "id3" + id, "intact" );
        Collection xrefs = new ArrayList( 2 );
        xrefs.add( xref1 );
        xrefs.add( xref3 );

        AnnotationTag annotation1 = new AnnotationTag( "comment", "blababla" + id );
        AnnotationTag annotation2 = new AnnotationTag( "remark", "bladibla" + id );
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
                                                                                       null, // otherBibRef,
                                                                                       xrefs,
                                                                                       annotations,
                                                                                       hostOrganism,
                                                                                       interactionDetection,
                                                                                       participantDetection );
        return experimentDescription;
    }

    private ProteinInteractorTag getProteinInteractor( String id ) {

        XrefTag xrefUniprot = new XrefTag( XrefTag.PRIMARY_REF, id, "uniprot" );
        ProteinInteractorTag proteinInteractor = new ProteinInteractorTag( xrefUniprot, null );
        return proteinInteractor;
    }

    private ProteinParticipantTag getProteinParticipant( String id, String role ) {

        ProteinInteractorTag proteinInteractor = getProteinInteractor( id );
        ProteinParticipantTag proteinParticipant = new ProteinParticipantTag( proteinInteractor, role, null, null, null, null );

        return proteinParticipant;
    }

    private InteractionTag getInteraction( String shortlabel ) {

        Collection experiments = new ArrayList( 1 );
        experiments.add( getExperimentDescription( "1", "3284583725" ) );

        Collection participants = new ArrayList( 2 );
        participants.add( getProteinParticipant( "P12345", "bait" ) );
        participants.add( getProteinParticipant( "P87F87", "prey" ) );

        XrefTag xref = new XrefTag( XrefTag.PRIMARY_REF, "id", CvDatabase.PSI_MI );
        InteractionTypeTag interactionType = new InteractionTypeTag( xref );

        Collection xrefs = new ArrayList( 2 );
        XrefTag xref1 = new XrefTag( XrefTag.PRIMARY_REF, "id1", "db1" + shortlabel );
        XrefTag xref2 = new XrefTag( XrefTag.PRIMARY_REF, "id2", "db1" );
        xrefs.add( xref1 );
        xrefs.add( xref2 );

        Collection annotations = new ArrayList( 1 );
        AnnotationTag annotation1 = new AnnotationTag( "comment", "sakbvksbv" + shortlabel );
        annotations.add( annotation1 );

        ConfidenceTag confidence = new ConfidenceTag( "percent", "50" );

        InteractionTag interaction = new InteractionTag( shortlabel, "fullname",
                                                         experiments,
                                                         participants,
                                                         interactionType,
                                                         xrefs,
                                                         annotations,
                                                         confidence );
        return interaction;
    }


    ///////////////////////////////
    // Tests creation

    public void testProcess_ok() {

        Map experimentDescriptions = new HashMap( 2 );
        ExperimentDescriptionTag experiment1 = getExperimentDescription( "1", "1234578650" );
        ExperimentDescriptionTag experiment2 = getExperimentDescription( "2", "8785765485" );
        experimentDescriptions.put( "E1", experiment1 );
        experimentDescriptions.put( "E2", experiment2 );

        Map proteinInteractors = new HashMap( 4 );
        ProteinInteractorTag protein1 = getProteinInteractor( "a" );
        ProteinInteractorTag protein2 = getProteinInteractor( "b" );
        ProteinInteractorTag protein3 = getProteinInteractor( "c" );
        ProteinInteractorTag protein4 = getProteinInteractor( "d" );
        proteinInteractors.put( "P1", protein1 );
        proteinInteractors.put( "P2", protein2 );
        proteinInteractors.put( "P3", protein3 );
        proteinInteractors.put( "P4", protein4 );

        Collection interactions = new ArrayList( 3 );
        interactions.add( getInteraction( "I1" ) );
        interactions.add( getInteraction( "I2" ) );
        interactions.add( getInteraction( "I3" ) );

        EntryTag entry = new EntryTag( experimentDescriptions,
                                       proteinInteractors,
                                       interactions );

        assertNotNull( entry );

        assertTrue( experimentDescriptions.values().containsAll( entry.getExperimentDescriptions().values() ) );
        assertTrue( entry.getExperimentDescriptions().getClass().isAssignableFrom( ReadOnlyHashMap.class ) );
        assertEquals( experiment1, entry.getExperimentDescriptions( "E1" ) );
        assertEquals( experiment2, entry.getExperimentDescriptions( "E2" ) );

        assertTrue( proteinInteractors.values().containsAll( entry.getProteinInteractors().values() ) );
        assertTrue( entry.getProteinInteractors().getClass().isAssignableFrom( ReadOnlyHashMap.class ) );
        assertEquals( protein1, entry.getProteinInteractors( "P1" ) );
        assertEquals( protein2, entry.getProteinInteractors( "P2" ) );
        assertEquals( protein3, entry.getProteinInteractors( "P3" ) );
        assertEquals( protein4, entry.getProteinInteractors( "P4" ) );

        assertTrue( entry.getInteractions().getClass().isAssignableFrom( ReadOnlyCollection.class ) );
        assertTrue( interactions.containsAll( entry.getInteractions() ) );
    }

    public void testProcess_ok_minimum_param() {

        Collection interactions = new ArrayList( 3 );
        interactions.add( getInteraction( "I1" ) );
        interactions.add( getInteraction( "I2" ) );
        interactions.add( getInteraction( "I3" ) );

        EntryTag entry = new EntryTag( null, // experimentDescriptions
                                       null, // proteinInteractors
                                       interactions );

        assertNotNull( entry );

        assertTrue( entry.getExperimentDescriptions().isEmpty() );
        assertTrue( entry.getExperimentDescriptions().getClass().isAssignableFrom( ReadOnlyHashMap.class ) );

        assertTrue( entry.getProteinInteractors().isEmpty() );
        assertTrue( entry.getProteinInteractors().getClass().isAssignableFrom( ReadOnlyHashMap.class ) );

        assertTrue( entry.getInteractions().getClass().isAssignableFrom( ReadOnlyCollection.class ) );
        assertTrue( interactions.containsAll( entry.getInteractions() ) );
    }


    ////////////////////////////////
    // Test Wrong collection content
    public void testProcess_error_wrong_experiments_content() {

        Map experimentDescriptions = new HashMap( 2 );
        experimentDescriptions.put( "E1", getExperimentDescription( "1", "1234578650" ) );
        experimentDescriptions.put( "E2", new Integer( 1 ) ); //// !!!!!

        Map proteinInteractors = new HashMap( 1 );
        proteinInteractors.put( "P1", getProteinInteractor( "a" ) );

        Collection interactions = new ArrayList( 3 );
        interactions.add( getInteraction( "I1" ) );

        EntryTag entry = null;
        try {
            entry = new EntryTag( experimentDescriptions,
                                  proteinInteractors,
                                  interactions );
            fail( "Should not create an Entry having something else than ExperimentDescription in its experiment collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( entry );
    }

    public void testProcess_error_wrong_proteinInteractor_content() {

        Map experimentDescriptions = new HashMap( 1 );
        experimentDescriptions.put( "E1", getExperimentDescription( "1", "1234578650" ) );

        Map proteinInteractors = new HashMap( 2 );
        proteinInteractors.put( "P1", getProteinInteractor( "a" ) );
        proteinInteractors.put( "P2", new Integer( 1 ) );

        Collection interactions = new ArrayList( 3 );
        interactions.add( getInteraction( "I1" ) );

        EntryTag entry = null;
        try {
            entry = new EntryTag( experimentDescriptions,
                                  proteinInteractors,
                                  interactions );
            fail( "Should not create an Entry having something else than ProteinInteractorTag in its proteinInteractor collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( entry );
    }

    public void testProcess_error_wrong_interactions_content() {

        Map experimentDescriptions = new HashMap( 1 );
        experimentDescriptions.put( "E1", getExperimentDescription( "1", "1234578650" ) );

        Map proteinInteractors = new HashMap( 1 );
        proteinInteractors.put( "P1", getProteinInteractor( "a" ) );

        Collection interactions = new ArrayList( 3 );
        interactions.add( getInteraction( "I1" ) );
        interactions.add( new Integer( 1 ) );

        EntryTag entry = null;
        try {
            entry = new EntryTag( experimentDescriptions,
                                  proteinInteractors,
                                  interactions );
            fail( "Should not create an Entry having something else than InteractionTag in its interaction collection." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( entry );
    }

    public void testProcess_error_no_interactions() {

        Collection interactions = new ArrayList( 1 );

        EntryTag entry = null;
        try {
            entry = new EntryTag( null, // experimentDescriptions
                                  null, // proteinInteractors
                                  interactions );
            fail( "Should not create an Entry having an empty collection of interaction." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( entry );
    }

    public void testProcess_error_interactions_is_null() {

        Collection interactions = null;

        EntryTag entry = null;
        try {
            entry = new EntryTag( null, // experimentDescriptions
                                  null, // proteinInteractors
                                  interactions );
            fail( "Should not create an Entry having a collection of interaction being null." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( entry );
    }
}