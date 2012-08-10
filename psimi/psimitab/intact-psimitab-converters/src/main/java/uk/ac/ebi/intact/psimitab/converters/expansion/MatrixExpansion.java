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
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Checksum;
import psidev.psi.mi.tab.model.ChecksumImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.InteractionConverter;

import java.util.*;

/**
 * Process an interaction and expand it using the matrix model.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class MatrixExpansion extends BinaryExpansionStrategy {
    /**
     * Sets up a logger for that class.
     */
    public static final Log logger = LogFactory.getLog( MatrixExpansion.class );

    public static final String EXPANSION_NAME = "matrix expansion";
    public static final String EXPANSION_MI = "MI:1061";

    /**
     * Apply the matrix expansion to the given interaction. Essentially, an interaction is created between any two
     * components.
     *
     * @param interaction the interaction to expand.
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<BinaryInteraction> expand( Interaction interaction ) throws NotExpandableInteractionException {
        if (interaction == null){
            throw new NotExpandableInteractionException("Interaction is not expandable because is null ");
        }

        InteractionCategory category = findInteractionCategory(interaction);

        if (category == null){
            return Collections.EMPTY_LIST;
        }

        Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
        if (category.equals(InteractionCategory.binary)){
            logger.debug( "Interaction was binary, no further processing involved." );
            BinaryInteraction binary = interactionConverter.toBinaryInteraction(interaction);

            if (binary != null){
                interactions.add( binary );
            }
        }
        else if (category.equals(InteractionCategory.self_intra_molecular)){
            logger.debug( "Interaction was self/intra molecular, no further processing involved." );
            BinaryInteraction binary2 = interactionConverter.toBinaryInteraction(interaction);

            if (binary2 != null){
                interactions.add( binary2 );
            }
        }
        else if (category.equals(InteractionCategory.self_inter_molecular)){
            logger.debug( "Interaction was self/inter molecular, we duplicate interactor." );
            BinaryInteraction binaryTemplateSelf = this.interactionConverter.processInteractionDetailsWithoutInteractors(interaction);
            if (binaryTemplateSelf == null){
                return Collections.EMPTY_LIST;
            }
            Component uniqueComponent = interaction.getComponents().iterator().next();
            MitabExpandedInteraction newInteraction = buildInteraction( binaryTemplateSelf, uniqueComponent, uniqueComponent );

            BinaryInteraction expandedBinary = newInteraction.getBinaryInteraction();

            // reset stoichiometry of duplicated interactor to 0
            Interactor interactorB = expandedBinary.getInteractorB();
            interactorB.getStoichiometry().clear();
            interactorB.getStoichiometry().add(0);

            // computes Rigid if necessary
            RigDataModel rigDatamodel = newInteraction.getMitabInteractorA().getRigDataModel();

            if (rigDatamodel != null){
                String rigid = interactionConverter.calculateRigidFor(Arrays.asList(rigDatamodel));

                if (rigid != null){
                    Checksum checksum = new ChecksumImpl(InteractionConverter.RIGID, rigid);
                    expandedBinary.getInteractionChecksums().add(checksum);
                }
            }

            // flip interactors if necessary
            interactionConverter.flipInteractorsIfNecessary(expandedBinary);

            interactions.add( expandedBinary );
        }
        else{
            logger.debug( "Interaction was n-ary, will be expanded" );
            Component[] components = interaction.getComponents().toArray(new Component[]{});
            logger.debug( components.length + " participant(s) found." );

            BinaryInteraction binaryTemplate = this.interactionConverter.processInteractionDetailsWithoutInteractors(interaction);

            if (binaryTemplate == null){
                return Collections.EMPTY_LIST;
            }

            Set<RigDataModel> rigDataModels = new HashSet<RigDataModel>(components.length - 1);
            boolean isFirst = true;
            boolean onlyProtein = true;

            for ( int i = 0; i < components.length; i++ ) {
                Component c1 = components[i];
                for ( int j = ( i + 1 ); j < components.length; j++ ) {
                    Component c2 = components[j];
                    // build a new interaction
                    MitabExpandedInteraction newInteraction2 = buildInteraction( binaryTemplate, c1, c2 );

                    BinaryInteraction expandedBinary2 = newInteraction2.getBinaryInteraction();
                    interactions.add( expandedBinary2 );

                    // count the first interactor rogid only once
                    if (isFirst){
                        isFirst = false;

                        if (newInteraction2.getMitabInteractorA().getRigDataModel() != null){
                             rigDataModels.add(newInteraction2.getMitabInteractorA().getRigDataModel());
                        }
                        else {
                            onlyProtein = false;
                        }
                    }

                    if (newInteraction2.getMitabInteractorB().getRigDataModel() != null){
                        rigDataModels.add(newInteraction2.getMitabInteractorB().getRigDataModel());
                    }
                    else {
                        onlyProtein = false;
                    }
                }
            }

            // process rigid if possible
            if (onlyProtein){

                String rigid = interactionConverter.calculateRigidFor(rigDataModels);

                // add rigid to the first binary interaction because all the biary interactions are pointing to the same checksum list
                if (rigid != null){
                    Checksum checksum = new ChecksumImpl(InteractionConverter.RIGID, rigid);
                    interactions.iterator().next().getInteractionChecksums().add(checksum);
                }
            }

            logger.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );
        }

        return interactions;
    }


    public String getName() {
        return EXPANSION_NAME;
    }

    @Override
    public String getMI() {
        return EXPANSION_MI;
    }
}
