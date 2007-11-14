// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.*;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Process the common behaviour of an IntAct Interaction when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Interaction2xmlPSI25 extends AnnotatedObject2xmlPSI25 implements Interaction2xmlI {

    private class InferredInteraction {

        private Feature feature1;
        private Feature feature2;

        public InferredInteraction( Feature feature1, Feature feature2 ) {

            if ( feature1 == null ) {
                throw new IllegalArgumentException( "You must give a non null Feature (1st)" );
            }

            if ( feature2 == null ) {
                throw new IllegalArgumentException( "You must give a non null Feature (2nd)" );
            }

            this.feature1 = feature1;
            this.feature2 = feature2;
        }

        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( !( o instanceof InferredInteraction ) ) {
                return false;
            }

            final InferredInteraction inferredInteraction = (InferredInteraction) o;

            // we want that method to return the same result for f1-f2 tan for f2-f1
            if ( ( feature1.equals( inferredInteraction.feature1 ) && feature2.equals( inferredInteraction.feature2 ) )
                 ||
                 ( feature2.equals( inferredInteraction.feature1 ) && feature1.equals( inferredInteraction.feature2 ) ) ) {
                return true;
            }

            return false;
        }

        public int hashCode() {
            // we want that method to return the same result for f1-f2 tan for f2-f1
            return feature1.hashCode() * feature2.hashCode();
        }


        public String toString() {
            return "InferredInteraction{" +
                   "feature1=" + feature1.getAc() +
                   ", feature2=" + feature2.getAc() +
                   "}";
        }
    }

    //////////////////////////
    // Constants

    public static final Collection attributeListFilter = new ArrayList( 2 );

    static {
        attributeListFilter.add( CvTopic.AUTHOR_CONFIDENCE );
        attributeListFilter.add( CvTopic.COPYRIGHT );
    }

    //////////////////////////
    // Singleton's methods

    private static Interaction2xmlPSI25 ourInstance = new Interaction2xmlPSI25();

    public static Interaction2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Interaction2xmlPSI25() {
    }

    ///////////////////////////
    // Encapsulated method

    /**
     * Generate the xref tag of the given protein. That content is attached to the given parent Element. <br>
     * <pre>
     *   Rules:
     *   -----
     *           primaryRef:   is the AC of the interaction object
     *           secondaryRef: any other Xrefs
     * </pre>
     *
     * @param session
     * @param parent      the interaction Element to which we will attach the Xref Element and its content.
     * @param interaction the IntAct Interaction from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    private Element createInteractionXrefs( UserSessionDownload session, Element parent, Interaction interaction ) {

        return Interaction2xmlCommons.getInstance().createInteractionXrefs( session, parent, interaction );
    }

    private Element createDissociationConstant( UserSessionDownload session, Element parent, Interaction interaction ) {

        return Interaction2xmlCommons.getInstance().createDissociationConstant( session, parent, interaction );
    }

    private Element createNegativeFlag( UserSessionDownload session, Element parent, Interaction interaction ) {

        return Interaction2xmlCommons.getInstance().createNegativeFlag( session, parent, interaction );
    }

    ///////////////////////////
    // Public Methods

    /**
     * Generated an interaction out of an IntAct Interaction.
     *
     * @param session
     * @param parent      the Element to which we will add the proteinInteractor.
     * @param interaction the IntAct Interaction that we convert to PSI.
     *
     * @return the generated interaction Element.
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           Interaction interaction ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + INTERACTION_TAG_NAME + "." );
        } else {

            if ( !"interactionList".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <interactionList> to build a " + INTERACTION_TAG_NAME + "." );
            }
        }

        if ( interaction == null ) {
            throw new IllegalArgumentException( "You must give a non null Interaction to build an " + INTERACTION_TAG_NAME + "." );
        }

        // we start participant/feature id from scratch again
        // nope we do not do that anymore in the context of PSI 2.5 release candidate
        // session.resetParticipantIdentifier();
        // session.resetFeatureIdentifier();

        // NOTE: names
        //       xref
        //       availabilityRef [or] availability
        //       experimentList
        //       participantList
        //       experimentalFormList     <----- !!!!
        //       inferredInteractionList
        //       interactionType
        //       negative
        //       confidenceList
        //       attributeList

        // NOTE: names
        //       xref
        //       availabilityRef [or] availability
        //       experimentList
        //       participantList
        //       inferredInteractionList
        //       interactionType
        //       modelled                                +++++
        //       intraMolecular                          +++++
        //       negative
        //       confidenceList
        //       attributeList
        //       parameterList                           +++++ CvKinetic

        // 2. Initialising the element...
        Element element = session.createElement( INTERACTION_TAG_NAME );
        element.setAttribute( "id", "" + session.getInteractionIdentifier( interaction ) );

        // imexId
        String imexId = null;
        for ( Iterator iterator = interaction.getXrefs().iterator(); iterator.hasNext() && imexId == null; ) {
            Xref xref = (Xref) iterator.next();

            if ( CvDatabase.IMEX.equals( xref.getCvDatabase().getShortLabel() ) ) {
                imexId = xref.getPrimaryId();
            }
        }

        if ( imexId != null ) {
            element.setAttribute( "imexId", imexId );
        }

        // 3. Generating names...
        createNames( session, element, interaction );

        // 4. Generating xref (if any) ...
        createInteractionXrefs( session, element, interaction );

        // 5. Generating availability
        createAvailability( session, element, interaction );

        // 6. Generating experimentList...
        Collection experiments = interaction.getExperiments();
        Element experimentList = session.getExperimentListElement();
        Element localExperimentList = null;
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            Experiment experiment = (Experiment) iterator.next();

            // only generate it if the definition doesn't exist.
            if ( !session.isAlreadyDefined( experiment ) ) {
                // add the experiment definition to the global list of experiments
                Experiment2xmlFactory.getInstance( session ).create( session, experimentList, experiment );
                session.declareAlreadyDefined( experiment );
            }

            // create the local list of experiment
            if ( localExperimentList == null ) {
                localExperimentList = session.createElement( "experimentList" );
                element.appendChild( localExperimentList );
            }

            // add an experimentRef to the local list.
            Experiment2xmlFactory.getInstance( session ).createReference( session, localExperimentList, experiment );
        }

        // 7. Generating participantList...
        if ( !interaction.getComponents().isEmpty() ) {

            // create the parents
            Element participantListElement = session.createElement( "participantList" );

            for ( Iterator iterator = interaction.getComponents().iterator(); iterator.hasNext(); ) {
                Component component = (Component) iterator.next();

                // create the participant
                Component2xmlFactory.getInstance( session ).create( session, participantListElement, component );
            }

            element.appendChild( participantListElement );
        }

        // 8. Generating experimentalFormList...
        //    this is actually done at the same time that participantList

        // 9. Generating inferredInteractionList...
        Element inferredInteractionListElement = null;
        HashSet inferedInteractions = new HashSet( 2 );

        for ( Iterator iterator = interaction.getComponents().iterator(); iterator.hasNext(); ) {
            Component component = (Component) iterator.next();

            Interactor interactor = component.getInteractor();

            Collection features = component.getBindingDomains();

            // todo check that we are not generating it twice !!

            for ( Iterator iterator1 = features.iterator(); iterator1.hasNext(); ) {
                Feature feature = (Feature) iterator1.next();

                Feature boundDomain = feature.getBoundDomain();
                if ( boundDomain == null ) {
                    continue; // that feature doesn't interact with an other one, skip it.
                }

                // check that we are not generating several times the same features
                // eg. f1-f2 and f2-f1.
                InferredInteraction ir = new InferredInteraction( feature, boundDomain );

                if ( inferedInteractions.contains( ir ) ) {
                    // we already have generated that inferred interaction, skip it.
                    continue;
                } else {
                    inferedInteractions.add( ir );
                }

                // that feature binds to an other one ... generate an Inferred interaction.
                Component boundComponent = boundDomain.getComponent();
                Interactor interactor2 = boundComponent.getInteractor();

                if ( inferredInteractionListElement == null ) {
                    // create the parent element
                    inferredInteractionListElement = session.createElement( "inferredInteractionList" );
                }

                // create the infered interaction
                Element inferredInteractionElement = session.createElement( "inferredInteraction" );

                // generating the first participant
                createParticipant( session, inferredInteractionElement, interactor, feature );

                // generating the second participant
                createParticipant( session, inferredInteractionElement, interactor2, boundDomain );

                // append to the parent
                inferredInteractionListElement.appendChild( inferredInteractionElement );

                // generating experiment reference
                Element experimentRefListElement = session.createElement( "experimentRefList" );
                inferredInteractionElement.appendChild( experimentRefListElement );
                for ( Iterator iterator2 = interaction.getExperiments().iterator(); iterator2.hasNext(); ) {
                    Experiment experiment = (Experiment) iterator2.next();

                    Experiment2xmlFactory.getInstance( session ).createReference( session,
                                                                                  experimentRefListElement,
                                                                                  experiment );
                }
            }
        } // components

        // add the inferredInteraction if any
        if ( inferredInteractionListElement != null ) {
            element.appendChild( inferredInteractionListElement );
        }

        // 10. Generating interactionType...
        // CvInteractionType is optional in the intact model.
        if ( interaction.getCvInteractionType() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, interaction.getCvInteractionType() );
        }

        // x. Generating modelled...

        // x. Generating intraMolecular...

        // 11. Generating negative...
        // if interaction or experiment is negative (Annotation), true, otherwise false.
        createNegativeFlag( session, element, interaction );

        // 12. Generating confidenceList...
        createConfidence( session, element, interaction );

        // 13. Generating attributeList...
        createAttributeList( session, element, interaction, attributeListFilter );

        // 14. Generating the dissociation constant (if any)...
        createDissociationConstant( session, element, interaction );

        // 15. Generating parameterList...

        // 15. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    private Element createParticipant( UserSessionDownload session, Element parent, Interactor interactor, Feature feature ) {

        Element participantElement = session.createElement( "participant" );

        Feature2xmlPSI25 f2xml = (Feature2xmlPSI25) Feature2xmlFactory.getInstance( session );
        f2xml.createReference( session, participantElement, feature );

        parent.appendChild( participantElement );

        return participantElement;
    }
}