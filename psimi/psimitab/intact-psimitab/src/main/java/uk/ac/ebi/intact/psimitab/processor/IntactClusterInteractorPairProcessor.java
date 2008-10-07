/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.processor;

import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactClusterInteractorPairProcessor extends ClusterInteractorPairProcessor<IntactBinaryInteraction>{

    @Override
    protected void mergeCollections(IntactBinaryInteraction source, IntactBinaryInteraction target) {
        super.mergeCollections(source, target);
        mergeCollection(source.getInteractorA().getExperimentalRoles(), target.getInteractorA().getExperimentalRoles());
        mergeCollection(source.getInteractorB().getExperimentalRoles(), target.getInteractorB().getExperimentalRoles());
        mergeCollection(source.getInteractorA().getBiologicalRoles(), target.getInteractorA().getBiologicalRoles());
        mergeCollection(source.getInteractorB().getBiologicalRoles(), target.getInteractorB().getBiologicalRoles());
        mergeCollection(source.getHostOrganism(), target.getHostOrganism());
        mergeCollection(source.getDataset(), target.getDataset());
    }
}
