// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.SmallMolecule2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.SmallMolecule2xmlI;
import uk.ac.ebi.intact.model.SmallMolecule;

/**
 * Process the common behaviour of an IntAct Protein when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class SmallMolecule2xmlPSI2 extends AnnotatedObject2xmlPSI2 implements SmallMolecule2xmlI {

    /////////////////////////////
    // Singleton's methods

    private static SmallMolecule2xmlPSI2 ourInstance = new SmallMolecule2xmlPSI2();

    public static SmallMolecule2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private SmallMolecule2xmlPSI2() {
    }

    ///////////////////////////////
    // Encapsulated Methods

    /**
     * get the value what will be used as ID of the smallMolecule.
     *
     * @param smallMolecule the protein for which we need an ID.
     *
     * @return the ID of the protein.
     */
    private String getSmallMoleculeId( SmallMolecule smallMolecule ) {

        return SmallMolecule2xmlCommons.getInstance().getSmallMoleculeId( smallMolecule );
    }

    /**
     * Generate the xref tag of the given smallMolecule. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent        the proteinInteractor Element to which we will attach the Xref Element and its content.
     * @param smallMolecule the smallMolecule from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    private Element createSmallMoleculeXrefs( UserSessionDownload session, Element parent, SmallMolecule smallMolecule ) {

        return SmallMolecule2xmlCommons.getInstance().createSmallMoleculeXrefs( session, parent, smallMolecule );
    }

    ///////////////////////////////
    // Public methods

    /**
     * Generated an smallMoleculeInteractorRef out of an IntAct smallMolecule.
     *
     * @param session
     * @param parent        the Element to which we will add the smallMoleculeInteractorRef.
     * @param smallMolecule the IntAct smallMolecule that we convert to PSI.
     *
     * @return the generated proteinInteractorRef Element.
     */
    public Element createReference( UserSessionDownload session, Element parent, SmallMolecule smallMolecule ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + SMALL_MOLECULE_REF_TAG_NAME + "." );
        } else {

            if ( !PARENT_TAG_NAME.equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <" + PARENT_TAG_NAME + "> to build a " + SMALL_MOLECULE_REF_TAG_NAME + ", was " + parent.getNodeName() + "." );
            }

        }

        if ( smallMolecule == null ) {
            throw new IllegalArgumentException( "You must give a non null protein to build an " + SMALL_MOLECULE_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( SMALL_MOLECULE_REF_TAG_NAME );
        element.setAttribute( "ref", getSmallMoleculeId( smallMolecule ) );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generated an smallMoleculeInteractor out of an IntAct Experiment.
     *
     * @param session
     * @param parent        the Element to which we will add the smallMoleculeInteractor.
     * @param smallMolecule the IntAct smallMolecule that we convert to PSI.
     *
     * @return the generated proteinInteractor Element.
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           SmallMolecule smallMolecule ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + SMALL_MOLECULE_NODE_NAME + "." );
        } else {

            if ( !"interactorList".equals( parent.getNodeName() ) && !"proteinParticipant".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <interactorList> or a <proteinParticipant> to build a " +
                                                    SMALL_MOLECULE_NODE_NAME + "." );
            }
        }

        if ( smallMolecule == null ) {
            throw new IllegalArgumentException( "You must give a non null Protein to build an " + SMALL_MOLECULE_NODE_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( SMALL_MOLECULE_NODE_NAME );
        element.setAttribute( "id", getSmallMoleculeId( smallMolecule ) );

        // 3. Generating names...
        createNames( session, element, smallMolecule );

        // 4. Generating xref (if any)...
        createSmallMoleculeXrefs( session, element, smallMolecule );

        // 7. Generating the attributeList
        createAttributeList( session, element, smallMolecule );

        // 11. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}