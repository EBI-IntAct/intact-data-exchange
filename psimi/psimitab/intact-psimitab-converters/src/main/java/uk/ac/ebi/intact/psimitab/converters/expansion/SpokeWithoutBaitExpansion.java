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
import psidev.psi.mi.jami.model.Interactor;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Checksum;
import psidev.psi.mi.tab.model.ChecksumImpl;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;

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

    public SpokeWithoutBaitExpansion() {
        super();
    }

    public SpokeWithoutBaitExpansion(boolean processExperimentDetails, boolean processPublicationDetails) {
        super(processExperimentDetails, processPublicationDetails);
    }

    public SpokeWithoutBaitExpansion(String defaultInstitution) {
        super(defaultInstitution);
    }

    public SpokeWithoutBaitExpansion(boolean processExperimentDetails, boolean processPublicationDetails, String defaultInstitution) {
        super(processExperimentDetails, processPublicationDetails, defaultInstitution);
    }

    ///////////////////////////////////////////
    // Implements ExpansionStrategy contract

    @Override
    protected Collection<BinaryInteraction> processExpansionWithoutBait(IntactInteractionEvidence interaction, BinaryInteraction interactionTemplate) {
        List<BinaryInteraction> interactions = new ArrayList<>(interaction.getParticipants().size());
        // bait was null
        if (logger.isDebugEnabled())
            logger.debug("Could not find a bait component. Pick a component arbitrarily: 1st by alphabetical order.");

        if (interactionTemplate == null){
            return Collections.EMPTY_LIST;
        }

        // Collect and sort participants by name
        List<ParticipantEvidence> sortedComponents = sortComponents(interaction.getParticipants());

        // Pick the first one
        ParticipantEvidence fakeBait = sortedComponents.get(0);

        Set<RigDataModel> rigDataModels = new HashSet<RigDataModel>(sortedComponents.size() - 1);
        boolean isFirst = true;
        boolean onlyProtein = true;

        // Build interactions
        for (int i = 1; i < sortedComponents.size(); i++) {
            ParticipantEvidence fakePrey = sortedComponents.get(i);

            MitabExpandedInteraction spokeInteraction = buildInteraction(
                    interactionTemplate,
                    (IntactParticipantEvidence) fakeBait,
                    (IntactParticipantEvidence) fakePrey,
                    true);
            BinaryInteraction expandedBinary2 = spokeInteraction.getBinaryInteraction();
            interactions.add( expandedBinary2 );

            // count the first interactor rogid only once
            if (isFirst){
                isFirst = false;

                if (spokeInteraction.getMitabInteractorA().getRigDataModel() != null){
                    rigDataModels.add(spokeInteraction.getMitabInteractorA().getRigDataModel());
                }
                else {
                    onlyProtein = false;
                }
            }

            if (spokeInteraction.getMitabInteractorB().getRigDataModel() != null){
                rigDataModels.add(spokeInteraction.getMitabInteractorB().getRigDataModel());
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

        return interactions;
    }

    @Override
    public boolean isExpandable(IntactInteractionEvidence interaction) {
        return isExpandableBasic(interaction);
    }

    ////////////////////////////
    // Private methods

    /**
     * Sort a Collection of Components based on their shorltabel.
     *
     * @param components collection to sort.
     * @return a non null List of Participant.
     */
    protected List<ParticipantEvidence> sortComponents( Collection<ParticipantEvidence> components ) {

        List<ParticipantEvidence> sortedComponents = new ArrayList<ParticipantEvidence>( components );

        Collections.sort( sortedComponents, new Comparator<ParticipantEvidence>() {
            public int compare( ParticipantEvidence p1, ParticipantEvidence p2 ) {

                Interactor i1 = p1.getInteractor();
                if ( i1 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }
                Interactor i2 = p2.getInteractor();
                if ( i2 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }

                String name1 = i1.getShortName();
                String name2 = i2.getShortName();

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
