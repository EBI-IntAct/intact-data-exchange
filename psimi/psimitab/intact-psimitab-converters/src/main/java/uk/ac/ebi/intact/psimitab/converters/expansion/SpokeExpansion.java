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
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;

import java.util.*;

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

    public SpokeExpansion() {
        super();
    }

    public SpokeExpansion(boolean processExperimentDetails, boolean processPublicationDetails) {
        super(processExperimentDetails, processPublicationDetails);
    }

    public SpokeExpansion(String defaultInstitution) {
        super(defaultInstitution);
    }

    public SpokeExpansion(boolean processExperimentDetails, boolean processPublicationDetails, String defaultInstitution) {
        super(processExperimentDetails, processPublicationDetails, defaultInstitution);
    }

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
            MitabExpandedInteraction newInteraction = buildInteraction( binaryTemplateSelf, uniqueComponent, uniqueComponent, false );

            BinaryInteraction expandedBinary = newInteraction.getBinaryInteraction();
            interactions.add(expandedBinary);

            // process participant detection methods after setting the interactors if not done at the level of interactiors
            if (interactionConverter.isProcessExperimentDetails() && newInteraction.getMitabInteractorA().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction.getMitabInteractorA().getInteractor());
            }
            if (interactionConverter.isProcessExperimentDetails() && newInteraction.getMitabInteractorB().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction.getMitabInteractorB().getInteractor());
            }

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
                    expandedBinary.getChecksums().add(checksum);
                }
            }

            // flip interactors if necessary
            interactionConverter.flipInteractorsIfNecessary(expandedBinary);
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

                Set<RigDataModel> rigDataModels = new HashSet<RigDataModel>(preyComponents.size());
                boolean isFirst = true;
                boolean onlyProtein = true;

                for (Component preyComponent : preyComponents) {
                    MitabExpandedInteraction newInteraction = buildInteraction(binaryTemplate, baitComponent, preyComponent, true);

                    BinaryInteraction expandedBinary2 = newInteraction.getBinaryInteraction();
                    interactions.add( expandedBinary2 );

                    // process participant detection methods after setting the interactors if not done at the level of interactiors
                    if (newInteraction.getMitabInteractorA().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                        interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction.getMitabInteractorA().getInteractor());
                    }
                    if (newInteraction.getMitabInteractorB().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                        interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction.getMitabInteractorB().getInteractor());
                    }

                    // count the first interactor rogid only once
                    if (isFirst){
                        isFirst = false;

                        if (newInteraction.getMitabInteractorA().getRigDataModel() != null){
                            rigDataModels.add(newInteraction.getMitabInteractorA().getRigDataModel());
                        }
                        else {
                            onlyProtein = false;
                        }
                    }

                    if (newInteraction.getMitabInteractorB().getRigDataModel() != null){
                        rigDataModels.add(newInteraction.getMitabInteractorB().getRigDataModel());
                    }
                    else {
                        onlyProtein = false;
                    }

                    // flip interactors if necessary
                    interactionConverter.flipInteractorsIfNecessary(expandedBinary2);
                }
                // process rigid if possible
                if (onlyProtein){

                    String rigid = interactionConverter.calculateRigidFor(rigDataModels);

                    // add rigid to the first binary interaction because all the biary interactions are pointing to the same checksum list
                    if (rigid != null){
                        Checksum checksum = new ChecksumImpl(InteractionConverter.RIGID, rigid);
                        interactions.iterator().next().getChecksums().add(checksum);
                    }
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

    protected Collection<BinaryInteraction> processExpansionWithoutBait(Interaction interaction, BinaryInteraction interactionTemplate) throws NotExpandableInteractionException {
            throw new NotExpandableInteractionException("Could not find a bait problem for this interaction.");
    }


}
