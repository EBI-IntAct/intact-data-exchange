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
package uk.ac.ebi.intact.psixml.persister.shared;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psixml.persister.PersisterException;
import uk.ac.ebi.intact.psixml.persister.service.AbstractService;
import uk.ac.ebi.intact.psixml.persister.service.InteractionService;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionPersister extends InteractorPersister<Interaction> {

    public InteractionPersister(IntactContext intactContext, boolean dryRun) {
        super(intactContext, dryRun);
    }

    @Override
    public Interaction saveOrUpdate(Interaction intactObject) throws PersisterException {
        return super.saveOrUpdate(intactObject);
    }

    @Override
    protected Interaction sync(Interaction intactObject) throws PersisterException {
        CvPersister cvPersister = new CvPersister(getIntactContext(), isDryRun());
        CvInteractionType cvIntType = (CvInteractionType) cvPersister.saveOrUpdate(intactObject.getCvInteractionType());
        intactObject.setCvInteractionType(cvIntType);
        getReport().mergeWith(cvPersister.getReport());

        saveOrUpdateComponents(intactObject);
        saveOrUpdateExperiments(intactObject);

        return super.sync(intactObject);
    }

    protected void saveOrUpdateComponents(Interaction intactObject) throws PersisterException {
        ComponentPersister compPersister = new ComponentPersister(getIntactContext(), isDryRun());

        List<Component> components = new ArrayList<Component>(intactObject.getComponents().size());

        for (Component component : intactObject.getComponents()) {
            Component c = compPersister.saveOrUpdate(component);
            components.add(c);
        }

        for (Component c : components) {
            c.setInteraction(intactObject);
        }

        intactObject.setComponents(components);

        getReport().mergeWith(compPersister.getReport());
    }

    protected void saveOrUpdateExperiments(Interaction intactObject) throws PersisterException {
        ExperimentPersister persister = new ExperimentPersister(getIntactContext(), isDryRun());

        List<Experiment> experiments = new ArrayList<Experiment>(intactObject.getExperiments().size());

        for (Experiment experiment : intactObject.getExperiments()) {
            Experiment exp;

            if (PersisterHelper.doesNotContainAc(experiment)) {
                exp = persister.saveOrUpdate(experiment);
            } else {
                exp = experiment;
            }

            experiments.add(exp);
        }

        intactObject.setExperiments(experiments);

        getReport().mergeWith(persister.getReport());
    }

    @Override
    protected AbstractService getService() {
        return new InteractionService(getIntactContext());
    }
}