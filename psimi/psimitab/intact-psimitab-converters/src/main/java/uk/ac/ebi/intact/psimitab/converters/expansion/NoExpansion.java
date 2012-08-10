/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.expansion;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.converters.InteractionConverter;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class NoExpansion extends BinaryExpansionStrategy{

    private InteractionConverter interactionConverter;

    public NoExpansion(){
        this.interactionConverter = new InteractionConverter();
    }

    public Collection<BinaryInteraction> expand(Interaction interaction) throws NotExpandableInteractionException {

        BinaryInteraction binaryInteraction = interactionConverter.toBinaryInteraction(interaction);

        if (binaryInteraction == null){
           return Collections.EMPTY_LIST;
        }
        return Collections.singleton(binaryInteraction);
    }

    public String getName() {
        return "none";
    }

    @Override
    public String getMI() {
        return "none";
    }

    public boolean isExpandable(Interaction interaction) {
        return true;
    }
}
