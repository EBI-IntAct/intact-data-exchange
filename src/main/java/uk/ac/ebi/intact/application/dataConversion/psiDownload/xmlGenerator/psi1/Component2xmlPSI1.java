// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Component2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Protein2xmlFactory;
import uk.ac.ebi.intact.model.*;

import java.util.*;

/**
 * Implements the tranformation of an IntAct Component into PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Component2xmlPSI1 implements Component2xmlI {

    //////////////////////////////////////
    // Constants

    public static final String UNSPECIFIED = "unspecified";
    public static final String NEUTRAL = "neutral";
    public static final String BAIT = "bait";
    public static final String PREY = "prey";

    // Holds the label of the role allowed.
    private static Set roleAllowed = new HashSet( 4 );

    private static Map roleNameRemapping = new HashMap( 4 );

    public static final String DEFAULT_ROLE = UNSPECIFIED;

    static {

        roleAllowed.add( UNSPECIFIED );
        roleAllowed.add( NEUTRAL );
        roleAllowed.add( BAIT );
        roleAllowed.add( PREY );

        roleNameRemapping.put( CvBiologicalRole.ENZYME, BAIT );
        roleNameRemapping.put( CvBiologicalRole.ENZYME_TARGET, PREY );
        roleNameRemapping.put( CvExperimentalRole.NEUTRAL, NEUTRAL );
    }

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Component2xmlPSI1 ourInstance = new Component2xmlPSI1();

    public static Component2xmlPSI1 getInstance() {
        return ourInstance;
    }

    private Component2xmlPSI1() {
    }

    /////////////////////
    // Public methods

    /**
     * Generated an proteinParticipant out of an IntAct Component.
     *
     * @param session
     * @param parent    the Element to which we will add the proteinParticipant.
     * @param component the IntAct Component that we convert to PSI.
     *
     * @return the generated proteinParticipant Element.
     */
    public Element create( UserSessionDownload session, Element parent, Component component ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + PROTEIN_PARTICIPANT_TAG_NAME + "." );
        } else {

            if ( !PARENT_TAG_NAME.equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <" + PARENT_TAG_NAME + "> to build a " + PROTEIN_PARTICIPANT_TAG_NAME + "." );
            }
        }

        if ( component == null ) {
            throw new IllegalArgumentException( "You must give a non null Interaction to build an " + PROTEIN_PARTICIPANT_TAG_NAME + "." );
        }

        Interactor interactor = component.getInteractor();
        if ( false == ( interactor instanceof Protein ) ) {
            throw new UnsupportedOperationException( "Cannot export " + interactor.getClass().getName() +
                                                     " in PSI version 1." );
        }

        // NOTE: proteinInteractorRef proteinInteractor featureList confidence role isTaggedProtein isOverexpressedProtein

        // 2. Initialising the element...
        Element element = session.createElement( PROTEIN_PARTICIPANT_TAG_NAME );

        // 3. Generating proteinInteractorRef and proteinInteractor...
        Protein protein = ( Protein ) component.getInteractor();
        if ( false == session.isAlreadyDefined( protein ) ) {

            // get the global list of proteins
            Element interactorList = session.getInteractorListElement();

            // add the protein definition to the global list of proteins
            Protein2xmlFactory.getInstance( session ).create( session, interactorList, protein );
            session.declareAlreadyDefined( protein );
        }
        // add an proteinInteractorRef
        Protein2xmlFactory.getInstance( session ).createProteinInteracorReference( session, element, protein );

        // 4. Generating featureList...
        if ( false == component.getBindingDomains().isEmpty() ) {
            Element featureListElement = session.createElement( "featureList" );

            for ( Iterator iterator = component.getBindingDomains().iterator(); iterator.hasNext(); ) {
                Feature feature = ( Feature ) iterator.next();

                Feature2xmlFactory.getInstance( session ).create( session, featureListElement, feature );
            }
        }

        // 5. Generating confidence...
        // not for now ...

        // 6. Generating role...
        Element role = session.createElement( "role" );
        // only unspecified, neutral, bait and prey are allowed here.
        // Yet, since intact-core 1.6, CvConponentRole was split into CvExperimentalRole and CvBiologicalRole.
        String theRole = chooseRole( session, component );
        Text roleText = session.createTextNode( theRole );
        role.appendChild( roleText );
        element.appendChild( role );

        // 7. Generating isTaggedProtein...
        // ???

        // 8. Generating isOverexpressedProtein...
        // ???

        // 9. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Chooses a single role based on experimental and biological role.
     *
     * @param session   user session that we will use to store messages.
     * @param component the component holding experimental and biological roles.
     *
     * @return the role of the component.
     */
    private String chooseRole( UserSessionDownload session, Component component ) {

        String role = null;

        CvExperimentalRole exp = component.getCvExperimentalRole();
        CvBiologicalRole bio = component.getCvBiologicalRole();

        if ( exp != null ) {

            String expRole = exp.getShortLabel();

            if ( roleAllowed.contains( expRole ) && ! expRole.equals( UNSPECIFIED ) ) {
                // if it is unspecified, we try to check the biologicalRole
                role = expRole;

            } else if ( roleNameRemapping.containsKey( expRole ) ) {

                role = ( String ) roleNameRemapping.get( expRole );
                session.addMessage( "NOTE: CvExperimentalRole( '" + expRole + "' ) has been renamed '" + role + "'." +
                                    "(Component: " + component.getAc() + " - " +
                                    "Interaction: " + component.getInteraction().getAc() + ")" );

            } else if ( bio != null && roleNameRemapping.containsKey( bio.getShortLabel() ) ) {

                role = ( String ) roleNameRemapping.get( bio.getShortLabel() );
                session.addMessage( "NOTE: CvBiologicalRole( '" + bio.getShortLabel() + "' ) has been renamed '" + role + "'." +
                                    "(Component: " + component.getAc() + " - " +
                                    "Interaction: " + component.getInteraction().getAc() + ")" );

            } else if ( expRole.equals( UNSPECIFIED ) ) {

                role = expRole;
            }
        }

        if ( role == null ) {
            session.addMessage( "NOTE: Failed to select a single role when given " +
                                "CvExperimentalRole( '" + ( exp == null ? "null" : exp.getShortLabel() ) + "' ) and " +
                                "CvBiologicalRole( '" + ( bio == null ? "null" : bio.getShortLabel() ) + "' ). " +
                                "Role was set to default '" + DEFAULT_ROLE + "'. " +
                                "(Component: " + component.getAc() +
                                " - Interaction: " + component.getInteraction().getAc() + ")" );
            role = DEFAULT_ROLE;
        }

        return role;
    }
}