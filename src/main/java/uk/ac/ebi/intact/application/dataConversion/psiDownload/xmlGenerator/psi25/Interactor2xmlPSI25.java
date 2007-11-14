// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractAnnotatedObject2xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Xref2xmlFactory;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * Process the common behaviour of an IntAct Protein when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Interactor2xmlPSI25 extends AnnotatedObject2xmlPSI25 {

    /////////////////////////////
    // Singleton's methods

    private static Interactor2xmlPSI25 ourInstance = new Interactor2xmlPSI25();

    private static final String INTERACTOR_REF_TAG_NAME = "interactorRef";
    private static final String INTERACTOR_TAG_NAME = "interactor";

    public static Interactor2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Interactor2xmlPSI25() {
    }

    ///////////////////////////////
    // Encapsulated Methods

    /**
     * get the value what will be used as ID of the protein.
     *
     * @param interactor the interactor for which we need an ID.
     *
     * @return the ID of the protein.
     */
    private String getInteractorId( UserSessionDownload session, Interactor interactor ) {

        long id = session.getInteractorIdentifier( interactor );
        return "" + id;
    }

    /**
     * Generate the xref tag of the given interactor. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent     the interactor Element to which we will attach the Xref Element and its content.
     * @param interactor the interactor from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    private Element createXrefs( UserSessionDownload session, Element parent, Interactor interactor ) {

        Element xrefElement = session.createElement( "xref" );

        // 1. get uniprot xrefs
        Collection identityXrefs = AbstractAnnotatedObject2xml.getXrefByQualifier( interactor, CvXrefQualifier.IDENTITY );

        if ( identityXrefs.isEmpty() ) {
            // if not identity were found, try primary-reference
            identityXrefs = AbstractAnnotatedObject2xml.getXrefByQualifier( interactor, CvXrefQualifier.PRIMARY_REFERENCE );
        }

        // 2. process the identities
        Xref identity = null;
        for ( Iterator iterator = identityXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( identity == null ) {
                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );

            } else {

                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // 3. generate all other xrefs
        for ( Iterator iterator = interactor.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( identity == null ) {
                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );

            } else {

                if ( !identityXrefs.contains( xref ) ) {

                    // create xref
                    Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
                }
            }
        }

        // 4. generate IntAct reference
        Xref2xmlFactory.getInstance( session ).createIntactReference( session, xrefElement, interactor );

        // 5. attach the xref to the parent node.
        if ( xrefElement.hasChildNodes() ) {
            // don't insert an empty tag.
            parent.appendChild( xrefElement );
        }

        return xrefElement;
    }

    ///////////////////////////////
    // Public methods

    /**
     * Generates an interactorRef out of an IntAct interactor.
     *
     * @param session
     * @param parent     the Element to which we will add the interactorRef.
     * @param interactor the IntAct interactor that we convert to PSI.
     *
     * @return the generated interactorRef Element.
     */
    public Element createInteractorReference( UserSessionDownload session, Element parent, Interactor interactor ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + INTERACTOR_REF_TAG_NAME + "." );
        }

        if ( interactor == null ) {
            throw new IllegalArgumentException( "You must give a non null protein to build an " + INTERACTOR_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( INTERACTOR_REF_TAG_NAME );
        Text refText = session.createTextNode( getInteractorId( session, interactor ) );
        element.appendChild( refText );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generates an participantRef out of an IntAct interactor.
     *
     * @param session
     * @param parent     the Element to which we will add the participantRef.
     * @param interactor the IntAct interactor that we convert to PSI.
     *
     * @return the generated participantRef Element.
     */
    public Element createParticipantReference( UserSessionDownload session, Element parent, Interactor interactor ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + INTERACTOR_REF_TAG_NAME + "." );
        }

        if ( interactor == null ) {
            throw new IllegalArgumentException( "You must give a non null protein to build an " + INTERACTOR_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( INTERACTOR_REF_TAG_NAME );
        element.setAttribute( "ref", getInteractorId( session, interactor ) );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generated an interactor out of an IntAct Experiment.
     *
     * @param session
     * @param parent     the Element to which we will add the interactor.
     * @param interactor the IntAct interactor that we convert to PSI.
     *
     * @return the generated interactor Element.
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           Interactor interactor ) {

        /*
             @is_a@interactor type ; MI:0313 ; synonym:participant type
              @is_a@complex ; MI:0314
               @is_a@protein complex ; MI:0315
               @is_a@protein dna complex ; MI:0233
               @is_a@ribonucleoprot compl\: ribonucleoprotein complex ; MI:0316 ; synonym:protein rna complex
              @is_a@interaction ; MI:0317
              @is_a@nucleic acid ; MI:0318
               @is_a@dna\: deoxyribonucleic acid ; MI:0319 ; synonym:\<new synonym> ; synonym:DNA ; synonym:deoxyribonucleic acid
               @is_a@rna\: ribonucleic acid ; MI:0320 ; synonym:RNA
                @is_a@crna\: catalytic rna ; MI:0321 ; synonym:catalytic RNA ; synonym:catalytic ribonucleic acid
                @is_a@grna\: guide rna ; MI:0322 ; synonym:guide RNA
                @is_a@hnrna\: heterogeneous nuclear ribonucleic acid ; MI:0323 ; synonym:heterogeneous nuclear RNA ; synonym:heterogeneous nuclear ribonucleic acid
                @is_a@mrna\: messenger ribonucleic acid ; MI:0324 ; synonym:mRNA
                @is_a@trna\: transfer ribonucleic acid ; MI:0325 ; synonym:tRNA ; synonym:transfer RNA ; synonym:transfer ribonucleic acid
              @is_a@protein ; MI:0326
               @is_a@peptide ; MI:0327 ; synonym:oligopeptide ; synonym:polypeptide
              @is_a@small molecule ; MI:0328
              @is_a@unknown participant ; MI:0329
        */

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + INTERACTOR_TAG_NAME + "." );
        } else {

            if ( !"interactorList".equals( parent.getNodeName() ) && !"proteinParticipant".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <interactorList> or a <proteinParticipant> to build a " +
                                                    INTERACTOR_TAG_NAME + "." );
            }
        }

        // names xref interactorType organism sequence attributeList

        if ( interactor == null ) {
            throw new IllegalArgumentException( "You must give a non null Protein to build an " + INTERACTOR_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( INTERACTOR_TAG_NAME );
        element.setAttribute( "id", getInteractorId( session, interactor ) );

        // 3. Generating names...
        createNames( session, element, interactor );

        // 4. Generating xref (if any)...
        createXrefs( session, element, interactor );

        // 5. Generating interactorType...
        CvObject2xmlFactory.getInstance( session ).create( session, element, interactor.getCvInteractorType() );

//        Element interactorTypeElement = session.createElement( "interactorType" );
//        // create names
//        Element namesElement = session.createElement( "names" );
//        interactorTypeElement.appendChild( namesElement );
//        Element shortlabelElement = session.createElement( "shortLabel" );
//        namesElement.appendChild( shortlabelElement );
//        Text shortlabelTextElement = session.createTextNode( "protein" );
//        shortlabelElement.appendChild( shortlabelTextElement );
//        // create xrefs
//        Element xrefElement = session.createElement( "xref" );
//        interactorTypeElement.appendChild( xrefElement );
//        Element primaryRefElement = session.createElement( "primaryRef" );
//        primaryRefElement.setAttribute( "db", "psi-mi" );
//        primaryRefElement.setAttribute( "dbAc", "MI:0488" );
//        primaryRefElement.setAttribute( "id", "MI:0326" );
//        xrefElement.appendChild( primaryRefElement );

        // 6. Generating Organism...
        BioSource organism = interactor.getBioSource();

        if (organism != null) // organism can be null for small molecules
        {
            BioSource2xmlFactory.getInstance( session ).createOrganism( session, element, interactor.getBioSource() );
        }

        // 7. Generating sequence... (for Polymers only, ie. Protein and NucleicAcid)
        if ( interactor instanceof Polymer ) {
            Polymer polymer = (Polymer) interactor;

            // Do not create a sequence element if no sequence available
            if( polymer.getSequence() != null && polymer.getSequence().length() > 0 ) {
                Element sequence = session.createElement( "sequence" );
                Text sequenceText = session.createTextNode( polymer.getSequence() );
                sequence.appendChild( sequenceText );
                element.appendChild( sequence );
            }
        }

        // 8. Generating the attributeList
        createAttributeList( session, element, interactor );

        // 9. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}