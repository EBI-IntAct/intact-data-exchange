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
package uk.ac.ebi.intact.psimitab.util;

import psidev.psi.mi.tab.utils.AbstractBinaryInteractionHandler;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

import java.util.Collection;

import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactBinaryInteractionHandler extends AbstractBinaryInteractionHandler<IntactBinaryInteraction> {

    @Override
    public IntactBinaryInteraction newBinaryInteraction(Interactor i1, Interactor i2) {
        if (!(i1 instanceof ExtendedInteractor && i2 instanceof ExtendedInteractor)) {
            throw new IllegalArgumentException("To create an intact binary interaction, we need interactors of type ExtendedInteractor and not: "+i1.getClass().getName());
        }
        return new IntactBinaryInteraction((ExtendedInteractor)i1, (ExtendedInteractor)i2);
    }

    @Override
    public Interactor newInteractor(Collection identifiers) {
        return new ExtendedInteractor(identifiers);
    }
    
    @Override
    protected void populateBinaryInteraction(IntactBinaryInteraction source, IntactBinaryInteraction target) {
        super.populateBinaryInteraction(source, target);

        target.getHostOrganism().addAll(source.getHostOrganism());
        target.getDataset().addAll(source.getDataset());
        target.getExpansionMethods().addAll(source.getExpansionMethods());
        target.getParameters().addAll(source.getParameters());
    }

    @Override
    protected void populateInteractor(Interactor source, Interactor target) {
        super.populateInteractor(source, target);
        
        ExtendedInteractor extTarget = (ExtendedInteractor) target;
        ExtendedInteractor extSource = (ExtendedInteractor) source;

        extTarget.setInteractorType(extSource.getInteractorType());
        extTarget.getExperimentalRoles().addAll(extSource.getExperimentalRoles());
        extTarget.getBiologicalRoles().addAll(extSource.getBiologicalRoles());
        extTarget.getProperties().addAll(extSource.getProperties());
        extTarget.getParameters().addAll(extSource.getParameters());
    }
}
