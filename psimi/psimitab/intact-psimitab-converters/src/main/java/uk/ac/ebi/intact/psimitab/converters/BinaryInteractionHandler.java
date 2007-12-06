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
package uk.ac.ebi.intact.psimitab.converters;

import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import uk.ac.ebi.intact.model.Interaction;

/**
 * Defines how to process an interaction and alter the creating of a binary interaction. If the
 * BinaryInteractionImpl given is an extension of the class, it becomes possible to populate
 * additional columns.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public interface BinaryInteractionHandler<T extends BinaryInteractionImpl> {

    /**
     * Does the extra processing on the BinaryInteractionImpl.
     *
     * @param bi          Binary interaction to be processed.
     * @param interaction Source interaction.
     */
    public void process( T bi, Interaction interaction ) throws Intact2TabException;

	/**
	 * This method merge could called in ClusterInteractorPairProssesor to write the correct format of lists.
	 *
	 * @param interaction
	 * @param target
	 */
	public void mergeCollection(T interaction, T target);    

}
