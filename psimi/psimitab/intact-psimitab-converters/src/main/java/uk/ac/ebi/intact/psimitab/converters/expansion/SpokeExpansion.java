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
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    public static final Log logger = LogFactory.getLog(SpokeExpansion.class);

    public static final String EXPANSION_NAME = "spoke expansion";
    public static final String EXPANSION_MI = "MI:1060";

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
    public Collection<BinaryInteraction> expand(Interaction interaction) throws NotExpandableInteractionException{
        if (interaction == null) {
            throw new NotExpandableInteractionException("Interaction is not expandable: "+interaction);
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
            BinaryInteraction newInteraction = buildInteraction( binaryTemplateSelf, uniqueComponent, uniqueComponent );

            // reset stoichiometry of duplicated interactor to 0
            Interactor interactorB = newInteraction.getInteractorB();
            interactorB.getStoichiometry().clear();
            interactorB.getStoichiometry().add(0);

            interactions.add( newInteraction );
        }
        else{
            logger.debug( "Interaction was n-ary, will be expanded" );

            BinaryInteraction binaryTemplate = this.interactionConverter.processInteractionDetailsWithoutInteractors(interaction);

            if (binaryTemplate == null){
                return Collections.EMPTY_LIST;
            }

            Component baitComponent = interaction.getBait();

            if (baitComponent != null) {

                Collection<Component> preyComponents = new ArrayList<Component>(interaction.getComponents().size());
                preyComponents.addAll(interaction.getComponents());
                preyComponents.remove(baitComponent);

                for (Component preyComponent : preyComponents) {
                    BinaryInteraction newInteraction = buildInteraction(binaryTemplate, baitComponent, preyComponent);
                    interactions.add(newInteraction);
                }
            } else {
                Collection<BinaryInteraction> expandedWithoutBait = processExpansionWithoutBait(interaction, binaryTemplate);
                interactions.addAll(expandedWithoutBait);

            }

            logger.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );
        }

        return interactions;
    }

    @Override
    public boolean isExpandable(Interaction interaction) {
        if (!super.isExpandable(interaction)) {
            return false;
        }

        if (interaction.getComponents().size() > 1) {
            boolean containsBait = false;

            for (Component component : interaction.getComponents()) {
                if (containsRole(component.getExperimentalRoles(), new String[] {CvExperimentalRole.BAIT_PSI_REF})) {
                    containsBait = true;
                    break;
                }
            }

            return containsBait;
        }
        return true;
    }

    public String getName() {
        return EXPANSION_NAME;
    }

    @Override
    public String getMI() {
        return EXPANSION_MI;
    }

    protected Collection<BinaryInteraction> processExpansionWithoutBait(Interaction interaction, BinaryInteraction interactionTemplate) {
        if (logger.isDebugEnabled())
            logger.debug("Could not find a bait problem for this interaction.");
        return Collections.EMPTY_LIST;
    }


}
