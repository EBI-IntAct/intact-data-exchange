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
package uk.ac.ebi.intact.dataexchange.psimi.xml.commons.model;

import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an entry in IntAct
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactEntry {

    private Collection<Interaction> interactions;
    private Collection<Experiment> experiments;
    private Collection<Interactor> interactors;

    public IntactEntry() {
    }

    public IntactEntry(Collection<Interaction> interactions) {
        this.interactions = interactions;
    }

    public Collection<Interaction> getInteractions() {
        return interactions;
    }

    public void setInteractions(Collection<Interaction> interactions) {
        this.interactions = interactions;
    }

    /**
     * Convenience method to get the experiments - delegates the logic to the interactions
     *
     * @return Experiments
     */
    public Collection<Experiment> getExperiments() {
        if (experiments != null) {
            return experiments;

        }

        experiments = new ArrayList<Experiment>();

        for (Interaction interaction : getInteractions()) {
            experiments.addAll(interaction.getExperiments());
        }

        return experiments;
    }

    /**
     * Convenience method to get the interactors - delegates the logic to the interactions
     *
     * @return Interactors
     */
    public Collection<Interactor> getInteractor() {
        if (interactors != null) {
            return interactors;

        }

        interactors = new ArrayList<Interactor>();

        for (Interaction interaction : getInteractions()) {
            for (Component comp : interaction.getComponents()) {
                interactors.add(comp.getInteractor());
            }

        }

        return interactors;
    }
}