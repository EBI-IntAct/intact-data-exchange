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
public class ExperimentDescriptionTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public ExperimentDescriptionTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( ExperimentDescriptionTest.class );
    }


    public void testProcess_ok_all_param() {

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

        assertNotNull( experimentDescription );

        assertEquals( "shortlabel", experimentDescription.getShortlabel() );
        assertEquals( "fullname", experimentDescription.getFullname() );

        assertEquals( bibRef, experimentDescription.getBibRef() );

        assertTrue( otherBibRef.containsAll( experimentDescription.getAdditionalBibRef() ) );
        assertTrue( experimentDescription.getAdditionalBibRef().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( xrefs.containsAll( experimentDescription.getXrefs() ) );
        assertTrue( experimentDescription.getXrefs().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertTrue( annotations.containsAll( experimentDescription.getAnnotations() ) );
        assertTrue( experimentDescription.getAnnotations().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertEquals( hostOrganism, experimentDescription.getHostOrganism() );

        assertEquals( interactionDetection, experimentDescription.getInteractionDetection() );

        assertEquals( participantDetection, experimentDescription.getParticipantDetection() );
    }

    public void testProcess_ok_minimum_param() {

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

        assertNotNull( experimentDescription );

        assertEquals( "shortlabel", experimentDescription.getShortlabel() );
        assertEquals( "fullname", experimentDescription.getFullname() );

        assertEquals( bibRef, experimentDescription.getBibRef() );

        assertNotNull( experimentDescription.getAdditionalBibRef() );
        assertTrue( experimentDescription.getAdditionalBibRef().isEmpty() );
        assertTrue( experimentDescription.getAdditionalBibRef().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertNotNull( experimentDescription.getXrefs() );
        assertTrue( experimentDescription.getXrefs().isEmpty() );
        assertTrue( experimentDescription.getXrefs().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertNotNull( experimentDescription.getAnnotations() );
        assertTrue( experimentDescription.getAnnotations().isEmpty() );
        assertTrue( experimentDescription.getAnnotations().getClass().isAssignableFrom( ReadOnlyCollection.class ) );

        assertEquals( hostOrganism, experimentDescription.getHostOrganism() );

        assertEquals( interactionDetection, experimentDescription.getInteractionDetection() );

        assertEquals( participantDetection, experimentDescription.getParticipantDetection() );
    }


    ////////////////////////////////
    // missing parameter

    public void testProcess_error_no_shortlabel() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( null, // shortlabel
                                                                  "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );

        try {
            experimentDescription = new ExperimentDescriptionTag( "", // shortlabel
                                                                  "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_no_fullname() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel",
                                                                  null, // fullname
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNotNull( experimentDescription );

        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel",
                                                                  "", // fullname
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNotNull( experimentDescription );
    }

    public void testProcess_error_no_bibRef() {

        XrefTag bibRef = null;

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_no_organism() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = null;

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_no_interactionDetection() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        InteractionDetectionTag interactionDetection = null;

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_no_participantDetection() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        ParticipantDetectionTag participantDetection = null;

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }


    ////////////////////////////////
    // wrong parameter

    public void testProcess_error_wrong_bibRef() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "sldif", "lsgvsl" ); // not pubmed !

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_xrefs_contains_something_else_than_XrefTag() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        XrefTag xref1 = new XrefTag( XrefTag.SECONDARY_REF, "id1", "interpro" );
        XrefTag xref2 = new XrefTag( XrefTag.SECONDARY_REF, "id2", "interpro" );
        XrefTag xref3 = new XrefTag( XrefTag.SECONDARY_REF, "id3", "intact" );
        Collection xrefs = new ArrayList( 3 );
        xrefs.add( xref1 );
        xrefs.add( xref2 );
        xrefs.add( new Integer( 1 ) );
        xrefs.add( xref3 );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  xrefs,
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_annotations_contains_something_else_than_AnnotationTag() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        AnnotationTag annotation1 = new AnnotationTag( "comment", "blababla" );
        AnnotationTag annotation2 = new AnnotationTag( "remark", "bladibla" );
        Collection annotations = new ArrayList( 2 );
        annotations.add( annotation1 );
        annotations.add( new Integer( 1 ) );
        annotations.add( annotation2 );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  null, // otherBibRef
                                                                  null, // xrefs
                                                                  annotations,
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }

    public void testProcess_error_additionalBibRef_contains_something_else_than_XrefTag() {

        XrefTag bibRef = new XrefTag( XrefTag.PRIMARY_REF, "123456789", CvDatabase.PUBMED );

        HostOrganismTag hostOrganism = new HostOrganismTag( "1234" );

        XrefTag psiInteractionDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:xxxx", CvDatabase.PSI_MI );
        InteractionDetectionTag interactionDetection = new InteractionDetectionTag( psiInteractionDetection );

        XrefTag psiParticipantDetection = new XrefTag( XrefTag.PRIMARY_REF, "MI:yyyy", CvDatabase.PSI_MI );
        ParticipantDetectionTag participantDetection = new ParticipantDetectionTag( psiParticipantDetection );

        XrefTag bibRef2 = new XrefTag( XrefTag.SECONDARY_REF, "123006789", CvDatabase.PUBMED );
        XrefTag bibRef3 = new XrefTag( XrefTag.SECONDARY_REF, "123456700", CvDatabase.PUBMED );
        Collection otherBibRef = new ArrayList( 2 );
        otherBibRef.add( bibRef2 );
        otherBibRef.add( new Integer( 1 ) );
        otherBibRef.add( bibRef3 );

        ExperimentDescriptionTag experimentDescription = null;
        try {
            experimentDescription = new ExperimentDescriptionTag( "shortlabel", "fullname",
                                                                  bibRef,
                                                                  otherBibRef,
                                                                  null, // xrefs
                                                                  null, // annotations
                                                                  hostOrganism,
                                                                  interactionDetection,
                                                                  participantDetection );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( experimentDescription );
    }
}