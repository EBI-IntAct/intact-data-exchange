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
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.InteractionUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Process an interaction and expand it using the spoke model.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class SpokeExpansion extends BinaryExpansionStrategy {

    /**
     * Sets up a logger for that class.
     */
    public static final Log logger = LogFactory.getLog( SpokeExpansion.class );

    public static final String EXPANSION_NAME = "Spoke";

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
                // create a Collection of only prey components
                Collection<Component> preyComponents = new ArrayList<Component>();
                preyComponents.addAll( components );
                preyComponents.remove( baitComponent );

                // create a new Interaction for each bait-prey pair
                for ( Component preyComponent : preyComponents ) {
                    Interaction newInteraction = buildInteraction( interaction, baitComponent, preyComponent );
                    interactions.add( newInteraction );
                }
            } else {
                // bait was null
                logger.debug( "Could not find a bait component. No further processing involved." );
            }


            logger.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );
        }

        return interactions;
    }

    public String getName() {
        return EXPANSION_NAME;
    }


}
