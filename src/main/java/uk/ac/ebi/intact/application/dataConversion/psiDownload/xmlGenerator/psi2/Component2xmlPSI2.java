// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.*;
import uk.ac.ebi.intact.model.*;

import java.util.Iterator;

/**
 * Implements the tranformation of an IntAct Component into PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Component2xmlPSI2 implements Component2xmlI {

    ///////////////////////
    // Constants

    public static final String TAGGED_PROTEIN_MI_REFERENCE = "MI:0507";

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Component2xmlPSI2 ourInstance = new Component2xmlPSI2();

    public static Component2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private Component2xmlPSI2() {
    }

    /////////////////////
    // Private methods

    /**
     * get the value what will be used as ID of the component.
     *
     * @param session 
     * @param component
     *
     * @return the ID of the experiment.
     */
    private String getParticipantId( UserSessionDownload session, Component component ) {

        long id = session.getParticipantIdentifier( component );
        return String.valueOf( id );
    }

    /**
     * Generate the XMl content of a proteinParticipant as well as the experimentalForm based on the given component.
     *
     * @param session   the user session.
     * @param parent    the XML Element to which we will attach the proteinParticipant.
     * @param component the component from which we will get the Protein
     *
     * @return the generated XML element
     *
     * @see uk.ac.ebi.intact.model.Interactor
     * @see uk.ac.ebi.intact.model.Protein
     * @see uk.ac.ebi.intact.model.CvBiologicalRole
     * @see uk.ac.ebi.intact.model.Feature
     */
    private Element createProteinParticipant( UserSessionDownload session, Element parent, Component component ) {

        // NOTE:
        // proteinParticipant:
        //    proteinInteractorRef proteinInteractor featureList confidenceList participantRole

        // 2. Initialising the element...
        Element element = session.createElement( PROTEIN_PARTICIPANT_TAG_NAME );
        element.setAttribute( "id", getParticipantId( session, component ) );

        // 3. Generating proteinInteractorRef and proteinInteractor...
        Protein protein = (Protein) component.getInteractor();
        if ( ! session.isAlreadyDefined( protein ) ) {

            // get the global list of proteins
            Element interactorList = session.getInteractorListElement();

            // add the protein definition to the global list of proteins
            Protein2xmlFactory.getInstance( session ).create( session, interactorList, protein );
            session.declareAlreadyDefined( protein );
        }
        // add a proteinInteractorRef
        Protein2xmlFactory.getInstance( session ).createProteinInteracorReference( session, element, protein );

        // 4. Generating featureList...
        // TODO necessary to check the Tags here ?
        boolean isTagged = false;
        if ( ! component.getBindingDomains().isEmpty() ) {
            Element featureListElement = session.createElement( "featureList" );

            for ( Iterator iterator = component.getBindingDomains().iterator(); iterator.hasNext(); ) {
                Feature feature = (Feature) iterator.next();

                Feature2xmlFactory.getInstance( session ).create( session, featureListElement, feature );

                // check if that feature has a type Tag
                if ( ! isTagged ) {
                    // check it...
                    isTagged = isTaggedFeature( feature.getCvFeatureType() );
                }
            }

            // TODO test this.
            element.appendChild( featureListElement );
        }

        // 5. Generating confidenceList...
        // TODO export confidence list here ! but from what object ? Component ? in the documentation it is meant to be the confidence of the participant detection

        // 6. Generating role...
        // TODO do we filter on CvComponentRole ? allowed: neutral, bait and prey ?
        // That will fall into places when we migrate IntAct to PSI CV v2
        if ( component.getCvBiologicalRole() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session,
                                                               element,
                                                               component.getCvBiologicalRole() );

//            CvObject2xmlPSI2 cv2xml = (CvObject2xmlPSI2) CvObject2xmlFactory.getInstance( session );
//            cv2xml.createBiologicalRole( session, element, component.getCvBiologicalRole() );
        }

        // 9. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Check that the given feature type is a tag.
     *
     * @param featureType the feature type to check against.
     *
     * @return true if the cvFeatureType or one of its parent is tagged-protein (MI:0507).
     */
    public boolean isTaggedFeature( CvFeatureType featureType ) {

        // TODO that method is now public to allow its testing. Use JUnit plugin to test provate method to fix that.

        // get the PSI MI reference of that feature.
        String mi = null;
        for ( Iterator iterator = featureType.getXrefs().iterator(); iterator.hasNext() && mi == null; ) {
            Xref xref = (Xref) iterator.next();
            if ( CvDatabase.PSI_MI.equals( xref.getCvDatabase().getShortLabel() ) ) {
                mi = xref.getPrimaryId();
            }
        }

        if ( mi == null ) {
            // that should not happen
            System.err.println( featureType + " has no MI Xref !!!" );
            return false;
        } else {

            // check if the current CvFeatureTag is taggedProtein (MI:0507), if not, check the parent.
            if ( mi.equals( TAGGED_PROTEIN_MI_REFERENCE ) ) {
                return true;
            } else {
                // check the parent
                for ( Iterator iterator = featureType.getParents().iterator(); iterator.hasNext(); ) {
                    CvFeatureType parent = (CvFeatureType) iterator.next();
                    // recursive call here !
                    if ( isTaggedFeature( parent ) ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Generate the XML content of a proteinParticipant as well as the experimentalForm based on the given component.
     *
     * @param session   the user session.
     * @param parent    the XML Element to which we will attach the proteinExperimentalForm (ie. experimentalFormList).
     * @param component the component from which we will get the Protein.
     *
     * @return the generated XML element.
     *
     * @see uk.ac.ebi.intact.model.Interactor
     * @see uk.ac.ebi.intact.model.Protein
     * @see uk.ac.ebi.intact.model.CvExperimentalRole
     * @see uk.ac.ebi.intact.model.Feature
     */
    private Element createProteinExperimentalForm( UserSessionDownload session,
                                                   Element parent,
                                                   Component component ) {
        // NOTE:
        // proteinExperimentalForm
        //    experimentRef proteinParticipantRef isTagged isOverexpressed experimentalForm experimentalRole

        // TODO we may have to loop over experiment + Feature in order to produce all experimentalForm (experiment/protein/tag)

        // to write the experimentRef we need the interaction again !
        Interaction interaction = component.getInteraction();
        for ( Iterator iterator = interaction.getExperiments().iterator(); iterator.hasNext(); ) {

            Experiment experiment = (Experiment) iterator.next();

            for ( Iterator iterator1 = component.getBindingDomains().iterator(); iterator1.hasNext(); ) {
                Feature feature = (Feature) iterator1.next();

                Element proteinExperimentalFormElement = session.createElement( "proteinExperimentalForm" );

                // 1. Generating the experimentRef...
                Experiment2xmlFactory.getInstance( session ).createReference( session, proteinExperimentalFormElement, experiment );

                // 2. generating proteinParticipantRef...
                Element proteinParticipantRef = session.createElement( "proteinParticipantRef" );
                proteinParticipantRef.setAttribute( "ref", getParticipantId( session, component ) );
                proteinExperimentalFormElement.appendChild( proteinParticipantRef );

                // 3. Generating isTagged...

                // TODO do we create that expermentalForm if this is not a tag ?
                // TODO if no, how do we do if there is no Tags
                // TODO what feature do we declare in participant/feature and in experimentalForm

                boolean isTagged = isTaggedFeature( feature.getCvFeatureType() );
                Element isTaggedElement = session.createElement( "isTagged" );
                Text isTaggedText = session.createTextNode( ( isTagged ? "true" : "false" ) );
                isTaggedElement.appendChild( isTaggedText );
                proteinExperimentalFormElement.appendChild( isTaggedElement );

                // 4. Generating isOverexpressed...
                // TODO how do I find out if the protein is over expressed ?
                //      Currently we don't.

                // 5. Generating experimentalForm...
                if ( isTagged ) {
                    CvObject2xmlFactory.getInstance( session ).create( session, proteinExperimentalFormElement, feature.getCvFeatureType() );
                }

                // 6. Generating experimentalRole...
                CvObject2xmlFactory.getInstance( session ).create( session,
                                                                   proteinExperimentalFormElement,
                                                                   component.getCvExperimentalRole() );

//                CvObject2xmlPSI2 cv2xml = (CvObject2xmlPSI2) CvObject2xmlFactory.getInstance( session );
//                cv2xml.createExperimentalRole( session,
//                                               proteinExperimentalFormElement,
//                                               component.getCvExperimentalRole() );

                // . attach it to the parent.
                parent.appendChild( proteinExperimentalFormElement );

            } // features

        } // experiments

        return null;
    }


    /**
     * Generate the XMl content of a SmallMolecule based on the given component.
     *
     * @param session   the user session.
     * @param parent    the XML Element to which we will attach the smallMoleculeParticipant.
     * @param component the component from which we will get the SmallMolecule
     *
     * @return the generated XML element
     *
     * @see uk.ac.ebi.intact.model.SmallMolecule
     */
    private Element createSmallMoleculeParticipant( UserSessionDownload session,
                                                    Element parent,
                                                    Component component ) {

        // NOTE: smallMoleculeInteractorRef [or] smallMoleculeInteractor
        //       confidenceList
        //       participantRole

        // TODO implement generation of the Small Molecule !
        throw new UnsupportedOperationException();
    }

    /////////////////////
    // Public methods

    /**
     * Generated an proteinParticipant out of an IntAct Component.
     *
     * @param session   the user session.
     * @param parent    the Element to which we will add the proteinParticipant.
     * @param component the IntAct Component that we convert to PSI.
     *
     * @return the generated proteinParticipant Element.
     *
     * @see uk.ac.ebi.intact.model.Protein
     * @see uk.ac.ebi.intact.model.Feature
     * @see uk.ac.ebi.intact.model.SmallMolecule
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           Component component ) {

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

        Element element = null;

        Interactor interactor = component.getInteractor();

        if ( interactor instanceof Protein ) {

            // generating a proteinParticipant
            element = createProteinParticipant( session, parent, component );

        } else if ( interactor instanceof Interaction ) {

            // TODO find out what do do when we have an interaction as interactor in PSI v2 !!
            // IDEA skip it and log a message in the session.
            throw new UnsupportedOperationException( "Cannot export " + interactor.getClass().getName() + " in PSI version 2." );

        } else if ( interactor instanceof SmallMolecule ) {

            // generating smallMoleculeParticipant...
            element = createSmallMoleculeParticipant( session, parent, component );

        } else {

            // RNA
            // TODO Generating rnaParticipant...

            // DNA
            // TODO Generating dnaParticipant...

            throw new UnsupportedOperationException( "Cannot export " + interactor.getClass().getName() + " in PSI version 2." );
        }

        return element;
    }


    /**
     * Create the experimentalForm corresponding to the participant we are being given the <code>participantId</code>.
     *
     * @param session       the user session.
     * @param parent        the Element to which we will add the experimentalForm.
     * @param component     the component from which we get the experimentalForm information.
     *
     * @return the created experimentalForm.
     */
    public Element createExperimentalForm( UserSessionDownload session, Element parent, Component component ) {
        Element element = null;

        Interactor interactor = component.getInteractor();

        if ( interactor instanceof Protein ) {

            // generating a proteinParticipant
            element = createProteinExperimentalForm( session, parent, component );

        } else if ( interactor instanceof Interaction ) {

            // TODO find out what do do when we have an interaction as interactor in PSI v2 !!
            // IDEA skip it and log a message in the session.
            throw new UnsupportedOperationException( "Cannot export " + interactor.getClass().getName() +
                                                     " in PSI version 2." );

        } else if ( interactor instanceof SmallMolecule ) {

            // generating smallMoleculeParticipant...
            element = createSmallMoleculeParticipant( session, parent, component );

        } else {

            // RNA
            // TODO Generating rnaParticipant...

            // DNA
            // TODO Generating dnaParticipant...

            throw new UnsupportedOperationException( "Cannot export " + interactor.getClass().getName() +
                                                     " in PSI version 2." );
        }

        return element;
    }
}