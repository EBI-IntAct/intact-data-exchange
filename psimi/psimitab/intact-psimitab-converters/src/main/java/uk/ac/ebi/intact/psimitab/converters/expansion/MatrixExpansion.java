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
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Checksum;
import psidev.psi.mi.tab.model.ChecksumImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;

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

    public MatrixExpansion() {
        super();
    }

    public MatrixExpansion(boolean processExperimentDetails, boolean processPublicationDetails) {
        super(processExperimentDetails, processPublicationDetails);
    }

    public MatrixExpansion(String defaultInstitution) {
        super(defaultInstitution);
    }

    public MatrixExpansion(boolean processExperimentDetails, boolean processPublicationDetails, String defaultInstitution) {
        super(processExperimentDetails, processPublicationDetails, defaultInstitution);
    }

    /**
     * Apply the matrix expansion to the given interaction. Essentially, an interaction is created between any two
     * components.
     *
     * @param interaction the interaction to expand.
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<BinaryInteraction> expand( IntactInteractionEvidence interaction ) throws NotExpandableInteractionException {
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
            ParticipantEvidence uniqueComponent = interaction.getParticipants().iterator().next();
            MitabExpandedInteraction newInteraction = buildInteraction(
                    binaryTemplateSelf,
                    (IntactParticipantEvidence) uniqueComponent,
                    (IntactParticipantEvidence) uniqueComponent,
                    false );

            BinaryInteraction expandedBinary = newInteraction.getBinaryInteraction();

            // reset stoichiometry of duplicated interactor to 0
            Interactor interactorB = expandedBinary.getInteractorB();
            interactorB.getStoichiometry().clear();
            interactorB.getStoichiometry().add(0);

            // process participant detection methods after setting the interactors if not done at the level of interactiors
            if (interactionConverter.isProcessExperimentDetails() && interactorB.getParticipantIdentificationMethods().isEmpty()){
                interactionConverter.processExperimentParticipantIdentificationMethods(interaction, expandedBinary.getInteractorB());
                expandedBinary.getInteractorA().setParticipantIdentificationMethods(expandedBinary.getInteractorB().getParticipantIdentificationMethods());
            }

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

            interactions.add( expandedBinary );
        }
        else{
            logger.debug( "Interaction was n-ary, will be expanded" );
            ParticipantEvidence[] components = interaction.getParticipants().toArray(new ParticipantEvidence[]{});
            logger.debug( components.length + " participant(s) found." );

            BinaryInteraction binaryTemplate = this.interactionConverter.processInteractionDetailsWithoutInteractors(interaction);

            if (binaryTemplate == null){
                return Collections.EMPTY_LIST;
            }

            Set<RigDataModel> rigDataModels = new HashSet<RigDataModel>(components.length - 1);
            boolean isFirst = true;
            boolean onlyProtein = true;

            for ( int i = 0; i < components.length; i++ ) {
                ParticipantEvidence c1 = components[i];
                for ( int j = ( i + 1 ); j < components.length; j++ ) {
                    ParticipantEvidence c2 = components[j];
                    // build a new interaction
                    MitabExpandedInteraction newInteraction2 = buildInteraction(
                            binaryTemplate,
                            (IntactParticipantEvidence) c1,
                            (IntactParticipantEvidence) c2,
                            true );

                    // process participant detection methods after setting the interactors if not done at the level of interactiors
                    if (newInteraction2.getMitabInteractorA().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                        interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction2.getMitabInteractorA().getInteractor());
                    }
                    if (newInteraction2.getMitabInteractorB().getInteractor().getParticipantIdentificationMethods().isEmpty()){
                        interactionConverter.processExperimentParticipantIdentificationMethods(interaction, newInteraction2.getMitabInteractorB().getInteractor());
                    }

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

                    // flip interactors if necessary
                    interactionConverter.flipInteractorsIfNecessary(expandedBinary2);
                }
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
