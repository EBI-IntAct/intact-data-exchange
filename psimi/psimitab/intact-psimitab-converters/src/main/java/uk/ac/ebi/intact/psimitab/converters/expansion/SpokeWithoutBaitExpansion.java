/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.expansion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.InteractionUtils;

import java.util.*;

/**
 * Process an interaction and expand it using the spoke model. Whenever no bait can be found we select an arbitrary
 * bait (1st one by alphabetical order based on the interactor shortlabel) and build the spoke interactions based on
 * that fake bait.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class SpokeWithoutBaitExpansion extends SpokeExpansion {

    /**
     * Sets up a logger for that class.
     */
    public static final Log logger = LogFactory.getLog( SpokeWithoutBaitExpansion.class );

    ///////////////////////////////////////////
    // Implements ExpansionStrategy contract

    /**
     * Interaction having more than 2 components get split following the spoke model expansion. That is, we build
     * pairs of components following bait-prey and enzyme-target associations.
     *
     * @param interaction a non null interaction.
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<Interaction> expand( Interaction interaction ) {
        Collection<Interaction> interactions = new ArrayList<Interaction>();
        Collection<Component> components = interaction.getComponents();

        if ( InteractionUtils.isBinaryInteraction( interaction ) ) {

            logger.debug( "Interaction was binary, no further processing involved." );
            if ( interaction.getComponents().size() == 1 ) {
                // single Interaction
                Component singleComponent = components.iterator().next();
                CvObjectXref idRef = CvObjectUtils.getPsiMiIdentityXref( singleComponent.getCvExperimentalRole() );
                if ( idRef.getPrimaryId().equals( CvExperimentalRole.SELF_PSI_REF ) ) {
                    Interaction newSelfInteraction = buildSingleInteraction( interaction, singleComponent );
                    interactions.add( newSelfInteraction );
                }
            } else {
                interactions.add( interaction );
            }

        } else {
            logger.debug( components.size() + " component(s) found." );

            Component baitComponent = interaction.getBait();

            if ( baitComponent != null ) {

                Collection<Component> preyComponents = new ArrayList<Component>();
                preyComponents.addAll( components );
                preyComponents.remove( baitComponent );

                for ( Component preyComponent : preyComponents ) {
                    Interaction newInteraction = buildInteraction( interaction, baitComponent, preyComponent );
                    interactions.add( newInteraction );
                }
            } else {

                // bait was null
                logger.debug( "Could not find a bait component. Pick a component arbitrarily: 1st by alphabetical order." );

                // Collect and sort participants by name
                List<Component> sortedComponents = sortComponents( components );

                // Pick the first one
                Component fakeBait = sortedComponents.get( 0 );

                // Build interactions
                for ( int i = 1; i < sortedComponents.size(); i++ ) {
                    Component fakePrey = sortedComponents.get( i );

                    Interaction spokeInteraction = buildInteraction( interaction, fakeBait, fakePrey );
                    interactions.add( spokeInteraction );
                }
            }
        }
        logger.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );


        return interactions;
    }

    ////////////////////////////
    // Private methods

    /**
     * Sort a Collection of Components based on their shorltabel.
     *
     * @param components collection to sort.
     * @return a non null List of Participant.
     */
    protected List<Component> sortComponents( Collection<Component> components ) {

        List<Component> sortedComponents = new ArrayList<Component>( components );

        Collections.sort( sortedComponents, new Comparator<Component>() {
            public int compare( Component p1, Component p2 ) {

                Interactor i1 = p1.getInteractor();
                if ( i1 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }
                Interactor i2 = p2.getInteractor();
                if ( i2 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }

                String name1 = i1.getShortLabel();
                String name2 = i2.getShortLabel();

                int result;
                if ( name1 == null ) {
                    result = -1;
                } else if ( name2 == null ) {
                    result = 1;
                } else {
                    result = name1.compareTo( name2 );
                }

                return result;
            }
        } );

        return sortedComponents;
    }
}
