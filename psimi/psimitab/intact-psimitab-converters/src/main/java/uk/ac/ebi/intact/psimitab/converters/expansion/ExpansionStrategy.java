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
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;

import java.util.Collection;

/**
 * ExpansionStrategy
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public interface ExpansionStrategy {

    /**
     * Expand an interaction into a
     *
     * @param interaction interaction which should be expanded
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<BinaryInteraction> expand( IntactInteractionEvidence interaction ) throws NotExpandableInteractionException;

    /**
     * Gets the method of the ExpansionStrategy
     *
     * @return spoke, matrix or none
     */
    public String getName();

    public String getMI();

    /**
     * Returns true if the interaction can be expanded
     * @param interaction The interaction to check
     * @return true if it can be expanded
     */
    public boolean isExpandable( IntactInteractionEvidence interaction );

    public InteractionCategory findInteractionCategory(IntactInteractionEvidence interaction);
}
