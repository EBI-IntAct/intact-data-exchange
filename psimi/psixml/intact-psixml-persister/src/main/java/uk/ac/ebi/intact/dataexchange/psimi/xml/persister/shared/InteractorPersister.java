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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.shared;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service.AbstractService;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service.InteractorService;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Interactor;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorPersister<T extends Interactor> extends AbstractAnnotatedObjectPersister<T> {

    public InteractorPersister(IntactContext intactContext, boolean dryRun) {
        super(intactContext, dryRun);
    }

    @Override
    public T saveOrUpdate(T intactObject) throws PersisterException {
        return super.saveOrUpdate(intactObject);
    }

    @Override
    protected T sync(T intactObject) throws PersisterException {
        if (intactObject.getBioSource() != null) {
            OrganismPersister organismPersister = new OrganismPersister(getIntactContext(), isDryRun());
            BioSource bioSource = organismPersister.saveOrUpdate(intactObject.getBioSource());
            intactObject.setBioSource(bioSource);

            getReport().mergeWith(organismPersister.getReport());
        }

        if (intactObject.getCvInteractorType() != null) {
            CvPersister cvPersister = new CvPersister(getIntactContext(), isDryRun());
            CvInteractorType cvIntType = (CvInteractorType) cvPersister.saveOrUpdate(intactObject.getCvInteractorType());
            intactObject.setCvInteractorType(cvIntType);

            getReport().mergeWith(cvPersister.getReport());
        }

        return super.sync(intactObject);
    }

    @Override
    protected AbstractService getService() {
        return new InteractorService(getIntactContext());
    }
}