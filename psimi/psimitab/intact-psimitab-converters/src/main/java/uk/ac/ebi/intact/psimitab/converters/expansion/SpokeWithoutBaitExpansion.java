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
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;

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

    @Override
    protected Collection<Interaction> processExpansionWithoutBait(Interaction interaction) {
        List<Interaction> interactions = new ArrayList<Interaction>();
        // bait was null
        if (logger.isDebugEnabled())
            logger.debug("Could not find a bait component. Pick a component arbitrarily: 1st by alphabetical order.");

        // Collect and sort participants by name
        List<Component> sortedComponents = sortComponents(interaction.getComponents());

        // Pick the first one
        Component fakeBait = sortedComponents.get(0);

        // Build interactions
        for (int i = 1; i < sortedComponents.size(); i++) {
            Component fakePrey = sortedComponents.get(i);

            Interaction spokeInteraction = buildInteraction(interaction, fakeBait, fakePrey);
            interactions.add(spokeInteraction);
        }

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
