
/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters.util;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

import java.util.Comparator;

/**
 * Tools to manage PSIMITAB files
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsimitabTools {

    /**
     * Do not instantiate PsimitabTools.
     */
    private PsimitabTools() {
    }

    /**
     * Alters the order of interactorA and interactorB of a binary interaction, according to a comparator.
     * @param binaryInteraction the binary interaction
     * @param comparator the comparator to use
     */
    public static void reorderInteractors(BinaryInteraction binaryInteraction, Comparator<Interactor> comparator) {
        if (binaryInteraction == null) throw new NullPointerException("binaryInteraction is null");
        if (comparator == null) throw new NullPointerException("comparator is null");

        Interactor interactorA = binaryInteraction.getInteractorA();
        Interactor interactorB = binaryInteraction.getInteractorB();

        if (comparator.compare(interactorA, interactorB) < 0) {
            binaryInteraction.setInteractorA(interactorB);
            binaryInteraction.setInteractorB(interactorA);
        }
    }
}
