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

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.InteractorConverter;
import uk.ac.ebi.intact.psimitab.converters.MitabInteractor;

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

    /**
     * Builds a new interaction object based the given interaction template.
     * <br/> Components are replaced by the two given ones.
     *
     * @param interaction the interaction template (no interactors, only interaction info).
     * @param c1          component to add to the newly created interaction.
     * @param c2          component to add to the newly created interaction.
     * @return a new interaction having c1 and c2 as component.
     */
    protected MitabExpandedInteraction buildInteraction( BinaryInteraction interaction, Component c1, Component c2 ) {
        MitabInteractor mitabInteractorA = interactorConverter.intactToMitab(c1);
        MitabInteractor mitabInteractorB = interactorConverter.intactToMitab(c2);
        Interactor interactorA = mitabInteractorA != null ? mitabInteractorA.getInteractor() : null;
        Interactor interactorB = mitabInteractorB != null ? mitabInteractorB.getInteractor() : null;

        BinaryInteraction expandedInteraction = new BinaryInteractionImpl(interactorA, interactorB);

        // copy the fields of the template binary interaction. It is not thread safe
        if (interaction != null){
            expandedInteraction.setAnnotations(interaction.getInteractionAnnotations());
            expandedInteraction.setAuthors(interaction.getAuthors());
            expandedInteraction.setChecksums(interaction.getInteractionChecksums());
            expandedInteraction.getComplexExpansion().add(new CrossReferenceImpl(CvDatabase.PSI_MI, getMI(), getName()));
            expandedInteraction.setConfidenceValues(interaction.getConfidenceValues());
            expandedInteraction.setCreationDate(interaction.getCreationDate());
            expandedInteraction.setDetectionMethods(interaction.getDetectionMethods());
            expandedInteraction.setHostOrganism(interaction.getHostOrganism());
            expandedInteraction.setInteractionAcs(interaction.getInteractionAcs());
            expandedInteraction.setInteractionTypes(interaction.getInteractionTypes());
            expandedInteraction.setPublications(interaction.getPublications());
            expandedInteraction.setSourceDatabases(interaction.getSourceDatabases());
            expandedInteraction.setXrefs(interaction.getInteractionXrefs());
            expandedInteraction.setParameters(interaction.getInteractionParameters());
            expandedInteraction.setUpdateDate(interaction.getUpdateDate());
            expandedInteraction.setNegativeInteraction(interaction.isNegativeInteraction());
        }

        MitabExpandedInteraction mitabExpandednResults = new MitabExpandedInteraction(expandedInteraction, mitabInteractorA, mitabInteractorB);

        return mitabExpandednResults;
    }

    public boolean isExpandable(Interaction interaction) {
        return isExpandableBasic(interaction);
    }

    protected boolean isExpandableBasic(Interaction interaction) {
        if (interaction.getComponents().size() == 1) {
            Component c = interaction.getComponents().iterator().next();

            return (c.getStoichiometry() >= 2 ||
                    containsRole(c.getExperimentalRoles(), new String[]{CvExperimentalRole.SELF_PSI_REF, PUTATIVE_SELF_PSI_REF}));

        }

        return true;
    }

    protected boolean containsRole(Collection<CvExperimentalRole> experimentalRoles, String[] rolesToFind) {
        if (experimentalRoles != null) {
            for (CvExperimentalRole expRole : experimentalRoles) {
                for (String roleToFind : rolesToFind) {
                    if (roleToFind.equals(expRole.getIdentifier())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionCategory findInteractionCategory(Interaction interaction) {
        if (interaction.getComponents().size() == 1) {
            Component c = interaction.getComponents().iterator().next();

            // we have a self interaction but inter molecular
            if (c.getStoichiometry() >= 2 || (c.getStoichiometry() == 0 && !containsRole(c.getExperimentalRoles(), new String[]{CvExperimentalRole.SELF_PSI_REF, PUTATIVE_SELF_PSI_REF}))){
                return InteractionCategory.self_inter_molecular;
            }
            else {
                return InteractionCategory.self_inter_molecular;
            }
        }
        else if (interaction.getComponents().size() == 2){
            return InteractionCategory.binary;
        }
        else if (interaction.getComponents().size() > 2){
            return InteractionCategory.n_ary;
        }

        return null;
    }
}
