// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.*;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Process the common behaviour of an IntAct Interaction when exporting PSI version 1.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Interaction2xmlPSI1 extends AnnotatedObject2xmlPSI1 implements Interaction2xmlI {

    //////////////////////////
    // Constants

    public static final Collection attributeListFilter = new ArrayList( 2 );

    static {
        attributeListFilter.add( CvTopic.AUTHOR_CONFIDENCE );
        attributeListFilter.add( CvTopic.COPYRIGHT );
    }

    //////////////////////////
    // Singleton's methods

    private static Interaction2xmlPSI1 ourInstance = new Interaction2xmlPSI1();

    public static Interaction2xmlPSI1 getInstance() {
        return ourInstance;
    }

    private Interaction2xmlPSI1() {
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

        // NOTE: PSI v1.0 only supports interaction having 2 participant or more. As a consequence, we don't generate
        //       PSI content if such case occur and generate a warning message, that the user will be able to collect
        //       from the session.
        int stoichiometrySum = 0;
        Collection<Component> components = interaction.getComponents();

        for ( Component component : components ) {
            stoichiometrySum += component.getStoichiometry();
        }

        if ( stoichiometrySum < 2 && components.size() < 2) {
            // we can't generate that interaction ...
            session.addMessage( "WARNING: could not generate PSI-MI content for interaction (" +
                    interaction.getAc() + " / " + interaction.getShortLabel() + ") as PSI-MI v1.0 requires" +
                    "at least 2 participants, that interaction contains " + stoichiometrySum + "." );
            return null;
        }

        // NOTE: names availabilityRef availabilityDescription experimentList participantList interactionType
        //       confidence xref attributeList

        // 2. Initialising the element...
        Element element = session.createElement( INTERACTION_TAG_NAME );

        // 3. Generating names...
        createNames( session, element, interaction );

        // 4. availability...

        // 5. Generating experimentList...
        Collection experiments = interaction.getExperiments();
        Element experimentList = session.getExperimentListElement();
        Element localExperimentList = null;
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            Experiment experiment = (Experiment) iterator.next();

            // only generate it if the definition doesn't exist.
            if ( false == session.isAlreadyDefined( experiment ) ) {
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





        // 6. Generating participantList...
        if ( false == interaction.getComponents().isEmpty() ) {

            // create the parent
            Element participantListElement = session.createElement( "participantList" );

            for ( Iterator iterator = interaction.getComponents().iterator(); iterator.hasNext(); ) {
                Component component = (Component) iterator.next();

                // take care of the stoichiometry
                float stoichiometry = component.getStoichiometry();

                if (stoichiometry <= 1)
                {
                   Component2xmlFactory.getInstance( session ).create( session, participantListElement, component ); 
                }
                else if (stoichiometry >= 2)
                {
                    for (int i=0; i<stoichiometry; i++)
                    {
                        Component2xmlFactory.getInstance( session ).create( session, participantListElement, component );
                    }
                }
            }

            element.appendChild( participantListElement );
        }

        // 7. Generating interactionType...
        // CvInteractionType is optional in the intact model.
        if ( interaction.getCvInteractionType() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, interaction.getCvInteractionType() );
        }

        // 8. Generating confidence...
        createConfidence( session, element, interaction );

        // 9. Generating xref (if any)...
        createInteractionXrefs( session, element, interaction );

        // 10. Generating attributeList...
        createAttributeList( session, element, interaction, attributeListFilter );

        // 11. Generating dissociation constant (if any)...
        createDissociationConstant( session, element, interaction );

        // 11. Attaching the newly created element to the parent...
        parent.appendChild( element );



        return element;
    }
}