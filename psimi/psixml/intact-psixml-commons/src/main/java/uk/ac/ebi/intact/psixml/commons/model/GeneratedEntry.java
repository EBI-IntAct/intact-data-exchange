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
package uk.ac.ebi.intact.psixml.commons.model;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.List;

/**
 * Wrapper of IntactEntry that allows to create entries using the intact database
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GeneratedEntry extends IntactEntry {

    private IntactContext intactContext;

    public GeneratedEntry(IntactContext intactContext) {
        this.intactContext = intactContext;
    }

    public GeneratedEntry addInteractionWithAc(String ac) {
        Interaction interaction = intactContext.getDataContext().getDaoFactory()
                .getInteractionDao().getByAc(ac);

        checkResult(interaction, ac, "interaction");

        return addInteraction(interaction);
    }

    public GeneratedEntry addInteractionWithShortLabel(String shortLabel) {
        Interaction interaction = intactContext.getDataContext().getDaoFactory()
                .getInteractionDao().getByShortLabel(shortLabel);

        checkResult(interaction, shortLabel, "interaction");

        return addInteraction(interaction);
    }

    public GeneratedEntry addExperimentWithAc(String ac) {
        Experiment experiment = intactContext.getDataContext().getDaoFactory()
                .getExperimentDao().getByAc(ac);

        checkResult(experiment, ac, "experiment");

        return addExperiment(experiment);
    }

    public GeneratedEntry addExperimentWithShortLabel(String shortLabel) {
        Experiment experiment = intactContext.getDataContext().getDaoFactory()
                .getExperimentDao().getByShortLabel(shortLabel);

        checkResult(experiment, shortLabel, "experiment");

        return addExperiment(experiment);
    }

    public GeneratedEntry addInteractorWithAc(String ac) {
        Interactor interactor = intactContext.getDataContext().getDaoFactory()
                .getInteractorDao().getByAc(ac);

        checkResult(interactor, ac, "interactor");

        return addInteractor(interactor);
    }

    public GeneratedEntry addInteractorWithShortLabel(String shortLabel) {
        Interactor interactor = intactContext.getDataContext().getDaoFactory()
                .getInteractorDao().getByShortLabel(shortLabel);

        checkResult(interactor, shortLabel, "interactor");

        return addInteractor(interactor);
    }

    public GeneratedEntry addInteractorWithUniprotId(String uniprotId) {
        List<ProteinImpl> interactors = intactContext.getDataContext().getDaoFactory()
                .getProteinDao().getByUniprotId(uniprotId);

        if (interactors.isEmpty()) {
            throw new IntactException("No interactors found with uniprot id: " + uniprotId);
        }

        for (Interactor interactor : interactors) {
            addInteractor(interactor);
        }

        return this;
    }

    public GeneratedEntry addInteraction(Interaction interaction) {
        if (interaction == null) throw new NullPointerException("interaction");

        super.getInteractions().add(interaction);

        return this;
    }

    public GeneratedEntry addExperiment(Experiment experiment) {
        if (experiment == null) throw new NullPointerException("experiment");

        for (Interaction interaction : experiment.getInteractions()) {
            addInteraction(interaction);
        }

        return this;
    }

    public GeneratedEntry addInteractor(Interactor interactor) {
        if (interactor == null) throw new NullPointerException("interactor");

        for (Component component : interactor.getActiveInstances()) {
            addInteraction(component.getInteraction());
        }

        return this;
    }

    private void checkResult(AnnotatedObject<?, ?> annotatedObject, String ac, String type) {
        if (annotatedObject == null) {
            throw new IntactException("No " + type + " found with: " + ac);
        }
    }
}