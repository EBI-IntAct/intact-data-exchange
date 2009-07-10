// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Component2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlFactory;
import uk.ac.ebi.intact.model.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements the tranformation of an IntAct Component into PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Component2xmlPSI25 implements Component2xmlI {

    ///////////////////////
    // Constants

    public static final String TAGGED_PROTEIN_MI_REFERENCE = "MI:0507";
    private static final String PARTICIPANT_TAG_NAME = "participant";

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Component2xmlPSI25 ourInstance = new Component2xmlPSI25();

    public static Component2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Component2xmlPSI25() {
    }

    /////////////////////
    // Private methods

    /**
     * get the value what will be used as ID of the experiment.
     *
     * @param component the component for which we need an ID.
     *
     * @return the ID of the experiment.
     */
    private String getParticipantId( UserSessionDownload session, Component component ) {

        long id = session.getParticipantIdentifier( component );
        return "" + id;
    }


    /**
     * Generate the XMl content of a participant as well as the experimentalForm based on the given component.
     *
     * @param session   the user session.
     * @param parent    the XML Element to which we will attach the proteinParticipant.
     * @param component the component from which we will get the interactor
     *
     * @return the generated XML element
     *
     * @see uk.ac.ebi.intact.model.Interactor
     * @see uk.ac.ebi.intact.model.Protein
     * @see uk.ac.ebi.intact.model.NucleicAcid
     * @see uk.ac.ebi.intact.model.SmallMolecule
     * @see uk.ac.ebi.intact.model.Polymer
     * @see uk.ac.ebi.intact.model.CvExperimentalRole
     * @see uk.ac.ebi.intact.model.CvBiologicalRole
     * @see uk.ac.ebi.intact.model.Feature
     */
    private Element createParticipant( UserSessionDownload session, Element parent, Component component ) {

        // NOTE:
        // proteinParticipant:
        //    names
        //    xref
        //    interactorRef interactor interactionRef
        //    biologicalRole
        //    experimentalRoleList
        //    experimentalFormList
        //    experimentalPreparationList
        //    experimentalInteractorList
        //    featureList
        //    hostOrganismList
        //    confidenceList

        // 2. Initialising the element...
        Element element = session.createElement( PARTICIPANT_TAG_NAME );
        element.setAttribute( "id", getParticipantId( session, component ) );

        // 3. Generating names ... ( only if we have author-name in the Alias )
        // skip it for now ...

        // 4. Generating xrefs ... ( will be done when the Component becomes an AnnotatedObject )

        // 5. Generating choice of [ interactorRef | interactor | interactionRef ]
        // currently we don't have interaction as interactor ... will be implemented later
        // we always generate here a interactorRef.
        Interactor interactor = component.getInteractor();

        if ( ! session.isAlreadyDefined( interactor ) ) {

            // get the global list of proteins
            Element interactorList = session.getInteractorListElement();

            // add the protein definition to the global list of proteins
            Interactor2xmlPSI25 interactor2xml = Interactor2xmlPSI25.getInstance();
            interactor2xml.create( session, interactorList, interactor );
            session.declareAlreadyDefined( interactor );
        }

        // add a proteinInteractorRef
        Interactor2xmlPSI25 interactor2xml = Interactor2xmlPSI25.getInstance();
        interactor2xml.createInteractorReference( session, element, interactor );

        // 6. Generating biologicalRole ...
        if ( component.getCvBiologicalRole() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session,
                                                               element,
                                                               component.getCvBiologicalRole() );
        }

        // 7. Generating experimentalRoleList ...
        if ( component.getCvExperimentalRole() != null ) {
            Element experimentalRoleList = session.createElement( "experimentalRoleList" );
            element.appendChild( experimentalRoleList );

            CvObject2xmlFactory.getInstance( session ).create( session,
                                                               experimentalRoleList,
                                                               component.getCvExperimentalRole() );
        }

        // 8. Generating experimentalFormList ...
        //    Dig into the Features and export all 'tagged-protein' Feature.
//        createExperimentalFormList(session, element, component );

        // 9. Generating experimentalPreparationList ...


        // 10. Generating experimentalInteractorList ...
        // leave it for now ... this will be used when we ge tthe facility to distinguish between the interactor
        //                      used in the experiment and the one on which we make the interpretation.


        // 11. Generating featureList...
        if ( ! component.getBindingDomains().isEmpty() ) {
            Element featureListElement = session.createElement( "featureList" );

            for ( Iterator iterator = component.getBindingDomains().iterator(); iterator.hasNext(); ) {
                Feature feature = (Feature) iterator.next();
                // TODO we output all feature here, as well as the tags !!
//                if( ! isTaggedFeature( feature.getCvFeatureType() ) ) {
                    // tags are exported under experimentalForm so far...
                    // TODO cache the result of the method : isTaggedFeature
                    Feature2xmlFactory.getInstance( session ).create( session, featureListElement, feature );
//                }
            }

            if( featureListElement.hasChildNodes() ) {
                // we have generated at least one feature that was not a tag.
                element.appendChild( featureListElement );
            }
        }

        // 12. Generating hostOrganismList ...
        if ( component.getExpressedIn() != null && component.getInteractor().getBioSource() != null) {
            Element hostOrganismList = session.createElement( "hostOrganismList" );
            element.appendChild( hostOrganismList );
            BioSource2xmlFactory.getInstance( session ).createHostOrganism( session, hostOrganismList, interactor.getBioSource() );
        }

        // 13. Generating confidenceList...
        // TODO export confidence list here ! but from what object ? Component ? in the documentation it is meant to be the confidence of the participant detection

        // 14. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Holds a cache so the method isTaggedFeature doesn't get executed twice for the same CvFeatureType.
     */
    private Map tagCache = new HashMap( 50 );

    /**
     * Check that the given feature type is a tag.<br>
     * By definition a Feature is a Tag is its CvFeatureType is a child of tagged-protein (MI:0507). <br>
     * Hence we search recursively for that specific tag.
     *
     * @param featureType the feature type to check against.
     *
     * @return true if the cvFeatureType or one of its parent is tagged-protein (MI:0507).
     */
    public boolean isTaggedFeature( CvFeatureType featureType ) {

        if( tagCache.containsKey( featureType ) ) {
            Boolean answer = (Boolean) tagCache.get( featureType );
            return answer.booleanValue();
        }

        Boolean answer = Boolean.FALSE;

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

        } else {

            // check if the current CvFeatureTag is taggedProtein (MI:0507), if not, check the parent.
            if ( mi.equals( TAGGED_PROTEIN_MI_REFERENCE ) ) {

                answer = Boolean.TRUE;

            } else {
                // check the parent
                for ( Iterator iterator = featureType.getParents().iterator(); iterator.hasNext(); ) {
                    CvFeatureType parent = (CvFeatureType) iterator.next();
                    // recursive call here !
                    if ( isTaggedFeature( parent ) ) {

                        answer = Boolean.TRUE;
                    }
                }
            }
        }

        // cache it
        tagCache.put( featureType, answer );

        // return the value
        return answer.booleanValue();
    }

    /**
     * Generate the XML content of a experimentalForm of a participant.
     *
     * @param session   the user session.
     * @param parent    the XML Element to which we will attach the experimentalForm (ie. experimentalFormList).
     * @param component the component from which we will get the Protein.
     *
     * @return the generated XML element.
     *
     * @see uk.ac.ebi.intact.model.Component
     * @see uk.ac.ebi.intact.model.Feature
     * @see uk.ac.ebi.intact.model.CvFeatureType
     * @see uk.ac.ebi.intact.model.Experiment
     */
    private Element createExperimentalFormList( UserSessionDownload session,
                                                Element parent,
                                                Component component ) {
        // NOTE:
        // experimentalForm
        //    names xref experimentRefList

        // we will have to retreive any existing tag from the parent or create a new one if necessary
        // note: not all feature are tags ! only those having CvFeatureType child of MI:0505
        Element experimentalFormList = null;

        for ( Iterator iterator1 = component.getBindingDomains().iterator(); iterator1.hasNext(); ) {

            Feature feature = (Feature) iterator1.next();
            if( isTaggedFeature( feature.getCvFeatureType() ) ) {
                // that feature is a tag - export it here...

                if( experimentalFormList == null ) {
                    // create a container
                    experimentalFormList = session.createElement( "experimentalFormList" );
                    parent.appendChild( experimentalFormList );
                }

                CvObject2xmlFactory.getInstance( session ).create( session,
                                                                   experimentalFormList,
                                                                   feature.getCvFeatureType() );

                // generate the experiment ref list
                // Note: that generates a problem as the CV representation in XML are cached and here we alter the cached value.
//                Element experimentRefList = session.createElement( "experimentRefList" );
//                experimentalForm.appendChild( experimentRefList );
//
//                Collection experiments = component.getInteraction().getExperiments();
//                for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
//                    Experiment experiment = (Experiment) iterator.next();
//
//                    Experiment2xmlFactory.getInstance( session ).createReference( session,
//                                                                                  experimentRefList,
//                                                                                  experiment );
//                }
            }
        }

        return experimentalFormList;
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

        createParticipant( session, parent, component );

        return element;
    }
}