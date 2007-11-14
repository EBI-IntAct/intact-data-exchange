// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Protein2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Protein2xmlI;
import uk.ac.ebi.intact.model.Protein;

/**
 * Process the common behaviour of an IntAct Protein when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Protein2xmlPSI2 extends AnnotatedObject2xmlPSI2 implements Protein2xmlI {

    /////////////////////////////
    // Singleton's methods

    private static Protein2xmlPSI2 ourInstance = new Protein2xmlPSI2();

    public static Protein2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private Protein2xmlPSI2() {
    }

    ///////////////////////////////
    // Encapsulated Methods

    /**
     * get the value what will be used as ID of the protein.
     *
     * @param protein the protein for which we need an ID.
     *
     * @return the ID of the protein.
     */
    private String getProteinId( UserSessionDownload session, Protein protein ) {

        long id = session.getInteractorIdentifier( protein );
        return "" + id;
    }

    /**
     * Generate the xref tag of the given protein. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent  the proteinInteractor Element to which we will attach the Xref Element and its content.
     * @param protein the protein from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    private Element createProteinXrefs( UserSessionDownload session, Element parent, Protein protein ) {

        return Protein2xmlCommons.getInstance().createProteinXrefs( session, parent, protein );
    }

    ///////////////////////////////
    // Public methods

    /**
     * Generates an proteinInteractorRef out of an IntAct Protein.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinInteractorRef.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinInteractorRef Element.
     */
    public Element createProteinInteracorReference( UserSessionDownload session, Element parent, Protein protein ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + PROTEIN_INTERACTOR_REF_TAG_NAME + "." );
        }

        if ( protein == null ) {
            throw new IllegalArgumentException( "You must give a non null protein to build an " + PROTEIN_INTERACTOR_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( PROTEIN_INTERACTOR_REF_TAG_NAME );
        element.setAttribute( "ref", getProteinId( session, protein ) );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generates an proteinParticipantRef out of an IntAct Protein.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinParticipantRef.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinParticipantRef Element.
     */
    public Element createParticipantReference( UserSessionDownload session, Element parent, Protein protein ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + PROTEIN_PARTICIPANT_REF_TAG_NAME + "." );
        }

        if ( protein == null ) {
            throw new IllegalArgumentException( "You must give a non null protein to build an " + PROTEIN_PARTICIPANT_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( PROTEIN_PARTICIPANT_REF_TAG_NAME );
        element.setAttribute( "ref", getProteinId( session, protein ) );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generated an proteinInteractor out of an IntAct Experiment.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinInteractor.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinInteractor Element.
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           Protein protein ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + PROTEIN_INTERACTOR_TAG_NAME + "." );
        } else {

            if ( !"interactorList".equals( parent.getNodeName() ) && !"proteinParticipant".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <interactorList> or a <proteinParticipant> to build a " +
                                                    PROTEIN_INTERACTOR_TAG_NAME + "." );
            }
        }

        if ( protein == null ) {
            throw new IllegalArgumentException( "You must give a non null Protein to build an " + PROTEIN_INTERACTOR_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( PROTEIN_INTERACTOR_TAG_NAME );
        element.setAttribute( "id", getProteinId( session, protein ) );

        // 3. Generating names...
        createNames( session, element, protein );

        // 4. Generating xref (if any)...
        createProteinXrefs( session, element, protein );

        // 5. Generating Organism...
        BioSource2xmlFactory.getInstance( session ).createOrganism( session, element, protein.getBioSource() );

        // 6. Generating sequence...
        Element sequence = session.createElement( "sequence" );
        Text sequenceText = session.createTextNode( protein.getSequence() );
        sequence.appendChild( sequenceText );
        element.appendChild( sequence );

        // 7. Generating the attributeList
        createAttributeList( session, element, protein );

        // 11. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}