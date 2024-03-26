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

import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractorConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.MitabInteractor;

import java.util.Collection;

/**
 * Abstraction of an expansion strategy.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public abstract class BinaryExpansionStrategy implements ExpansionStrategy {

    protected static final String PUTATIVE_SELF_PSI_REF = "MI:0898";

    protected InteractionConverter interactionConverter;
    protected InteractorConverter interactorConverter;


    public BinaryExpansionStrategy(){
        this.interactionConverter = new InteractionConverter();
        this.interactorConverter = new InteractorConverter();
    }

    public BinaryExpansionStrategy(boolean processExperimentDetails, boolean processPublicationDetails){
        this.interactionConverter = new InteractionConverter(processExperimentDetails, processPublicationDetails);
        this.interactorConverter = new InteractorConverter();
    }

    public BinaryExpansionStrategy(String defaultInstitution){
        this.interactionConverter = new InteractionConverter(defaultInstitution);
        this.interactorConverter = new InteractorConverter(defaultInstitution);
    }

    public BinaryExpansionStrategy(boolean processExperimentDetails, boolean processPublicationDetails, String defaultInstitution){
        this.interactionConverter = new InteractionConverter(processExperimentDetails, processPublicationDetails, defaultInstitution);
        this.interactorConverter = new InteractorConverter(defaultInstitution);
    }

    /**
     * Builds a new interaction object based the given interaction template.
     * <br/> Components are replaced by the two given ones.
     *
     * @param interaction the interaction template (no interactors, only interaction info).
     * @param c1          component to add to the newly created interaction.
     * @param c2          component to add to the newly created interaction.
     * @return a new interaction having c1 and c2 as component.
     */
    protected MitabExpandedInteraction buildInteraction(BinaryInteraction interaction, IntactParticipantEvidence c1, IntactParticipantEvidence c2, boolean isExpanded ) {
        MitabInteractor mitabInteractorA = interactorConverter.intactToMitab(c1);
        MitabInteractor mitabInteractorB = interactorConverter.intactToMitab(c2);
        Interactor interactorA = mitabInteractorA != null ? mitabInteractorA.getInteractor() : null;
        Interactor interactorB = mitabInteractorB != null ? mitabInteractorB.getInteractor() : null;

        BinaryInteraction expandedInteraction = new BinaryInteractionImpl(interactorA, interactorB);

        // copy the fields of the template binary interaction. It is not thread safe
        if (interaction != null){
            expandedInteraction.setAnnotations(interaction.getAnnotations());
            expandedInteraction.setAuthors(interaction.getAuthors());
            expandedInteraction.setChecksums(interaction.getChecksums());
            if (isExpanded){
                expandedInteraction.getComplexExpansion().add(new CrossReferenceImpl(CvTerm.PSI_MI, getMI(), getName()));
            }
            expandedInteraction.setConfidenceValues(interaction.getConfidenceValues());
            expandedInteraction.setCreationDate(interaction.getCreationDate());
            expandedInteraction.setDetectionMethods(interaction.getDetectionMethods());
            expandedInteraction.setHostOrganism(interaction.getHostOrganism());
            expandedInteraction.setInteractionAcs(interaction.getInteractionAcs());
            expandedInteraction.setInteractionTypes(interaction.getInteractionTypes());
            expandedInteraction.setPublications(interaction.getPublications());
            expandedInteraction.setSourceDatabases(interaction.getSourceDatabases());
            expandedInteraction.setXrefs(interaction.getXrefs());
            expandedInteraction.setParameters(interaction.getParameters());
            expandedInteraction.setUpdateDate(interaction.getUpdateDate());
            expandedInteraction.setNegativeInteraction(interaction.isNegativeInteraction());
        }

        MitabExpandedInteraction mitabExpandednResults = new MitabExpandedInteraction(expandedInteraction, mitabInteractorA, mitabInteractorB);

        return mitabExpandednResults;
    }

    public boolean isExpandable(IntactInteractionEvidence interaction) {
        return isExpandableBasic(interaction);
    }

    protected boolean isExpandableBasic(IntactInteractionEvidence interaction) {
        if (interaction.getParticipants().isEmpty()) {
            return false;
        }

        return true;
    }

    protected boolean roleIsAnyOf(CvTerm experimentalRole, String[] rolesToFind) {
        if (experimentalRole != null) {
            for (String roleToFind : rolesToFind) {
                if (roleToFind.equals(experimentalRole.getMIIdentifier())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionCategory findInteractionCategory(IntactInteractionEvidence interaction) {
        if (interaction.getParticipants().size() == 1) {
            ParticipantEvidence c = interaction.getParticipants().iterator().next();

            // we have a self interaction but inter molecular
            if (c.getStoichiometry() != null &&
                    (c.getStoichiometry().getMinValue() >= 2 ||
                            (c.getStoichiometry().getMinValue() == 0 && !roleIsAnyOf(c.getExperimentalRole(), new String[]{Participant.SELF_ROLE_MI, Participant.PUTATIVE_SELF_ROLE_MI})))) {
                return InteractionCategory.self_inter_molecular;
            }
            else {
                return InteractionCategory.self_intra_molecular;
            }
        }
        else if (interaction.getParticipants().size() == 2){
            return InteractionCategory.binary;
        }
        else if (interaction.getParticipants().size() > 2){
            return InteractionCategory.n_ary;
        }

        return null;
    }
}
