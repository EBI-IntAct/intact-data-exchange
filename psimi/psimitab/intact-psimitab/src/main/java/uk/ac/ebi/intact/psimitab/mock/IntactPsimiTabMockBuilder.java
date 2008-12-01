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
package uk.ac.ebi.intact.psimitab.mock;

import psidev.psi.mi.tab.mock.PsimiTabMockBuilder;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

/**
 * Mock builder for IntAct MITAB interactions.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2
 */
public class IntactPsimiTabMockBuilder extends PsimiTabMockBuilder {

    public IntactPsimiTabMockBuilder() {
    }

    @Override
    protected Interactor buildInteractor() {
        return new ExtendedInteractor();
    }

    @Override
    protected BinaryInteraction buildInteraction( Interactor a, Interactor b ) {
        return new IntactBinaryInteraction( (ExtendedInteractor) a, (ExtendedInteractor) b );
    }
}
