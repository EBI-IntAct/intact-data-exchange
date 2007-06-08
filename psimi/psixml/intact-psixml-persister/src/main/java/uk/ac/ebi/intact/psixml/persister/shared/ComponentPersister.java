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
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.psixml.persister.PersisterException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ComponentPersister extends AbstractAnnotatedObjectPersister<Component> {

    public ComponentPersister(IntactContext intactContext, boolean dryRun) {
        super(intactContext, dryRun);
    }

    @Override
    public Component saveOrUpdate(Component intactObject) throws PersisterException {

        if (intactObject.getExpressedIn() != null) {
            OrganismPersister organismPersister = new OrganismPersister(getIntactContext(), isDryRun());
            BioSource bioSource = organismPersister.saveOrUpdate(intactObject.getExpressedIn());
            intactObject.setExpressedIn(bioSource);

            getReport().mergeWith(organismPersister.getReport());
        }


        CvPersister cvPersister = new CvPersister(getIntactContext(), isDryRun());
        intactObject.setCvBiologicalRole((CvBiologicalRole) cvPersister.saveOrUpdate(intactObject.getCvBiologicalRole()));
        intactObject.setCvExperimentalRole((CvExperimentalRole) cvPersister.saveOrUpdate(intactObject.getCvExperimentalRole()));
        getReport().mergeWith(cvPersister.getReport());

        // note that to avoid cyclic invocations, do not try to save the interaction here

        InteractorPersister interactorPersister = new InteractorPersister(getIntactContext(), isDryRun());
        intactObject.setInteractor(interactorPersister.saveOrUpdate(intactObject.getInteractor()));
        getReport().mergeWith(interactorPersister.getReport());

        return intactObject;
    }
}