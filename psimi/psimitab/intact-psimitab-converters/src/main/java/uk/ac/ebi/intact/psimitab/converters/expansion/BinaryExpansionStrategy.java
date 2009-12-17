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

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractionImpl;

import java.util.ArrayList;
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

    /**
     * Builds a new interaction object based the given interaction template.
     * <br/> Components are replaced by the two given ones.
     *
     * @param interaction the interaction template.
     * @param c1          component to add to the newly created interaction.
     * @param c2          component to add to the newly created interaction.
     * @return a new interaction having c1 and c2 as component.
     */
    protected Interaction buildInteraction( Interaction interaction, Component c1, Component c2 ) {
        String shortLabel = c1.getInteractor().getShortLabel() + "-" + c2.getInteractor().getShortLabel();

        Interaction newInteraction = new InteractionImpl( interaction.getExperiments(),
                                                          interaction.getCvInteractionType(),
                                                          interaction.getCvInteractorType(),
                                                          shortLabel,
                                                          interaction.getOwner() );
        newInteraction.setAc( interaction.getAc() );
        Collection<Component> components = new ArrayList<Component>( 2 );
        components.add( c1 );
        components.add( c2 );
        newInteraction.setComponents( components );

        newInteraction.setXrefs(interaction.getXrefs());
        newInteraction.setAliases(interaction.getAliases());
        newInteraction.setAnnotations(interaction.getAnnotations());

        return newInteraction;
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
}
