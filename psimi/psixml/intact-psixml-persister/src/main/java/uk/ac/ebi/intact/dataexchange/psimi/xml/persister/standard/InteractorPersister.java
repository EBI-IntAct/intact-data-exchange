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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.standard;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Interactor;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorPersister<T  extends Interactor> extends AbstractAnnotatedObjectPersister<T>{

    private static ThreadLocal<InteractorPersister> instance = new ThreadLocal<InteractorPersister>() {
        @Override
        protected InteractorPersister initialValue() {
            return new InteractorPersister(IntactContext.getCurrentInstance());
        }
    };

    public static InteractorPersister getInstance() {
        return instance.get();
    }

    protected InteractorPersister(IntactContext intactContext) {
        super(intactContext);
    }

    @Override
    protected void saveOrUpdateAttributes(T intactObject) throws PersisterException {
        super.saveOrUpdateAttributes(intactObject);

        if (intactObject.getBioSource() != null) {
            BioSourcePersister.getInstance().saveOrUpdate(intactObject.getBioSource());
        }

        if (intactObject.getCvInteractorType() != null) {
            CvObjectPersister.getInstance().saveOrUpdate(intactObject.getCvInteractorType());
        }
    }

    @Override
    protected T syncAttributes(T intactObject) {
        if (intactObject.getBioSource() != null) {
            BioSource syncedBioSource = BioSourcePersister.getInstance().syncIfTransient(intactObject.getBioSource());
            intactObject.setBioSource(syncedBioSource);
        }

        if (intactObject.getCvInteractorType() != null) {
            CvInteractorType cvIntType = (CvInteractorType) CvObjectPersister.getInstance().syncIfTransient(intactObject.getCvInteractorType());
            intactObject.setCvInteractorType(cvIntType);
        }

        return super.syncAttributes(intactObject);
    }

    @Override
    protected T fetchFromDataSource(T intactObject) {
        return (T) getIntactContext().getDataContext().getDaoFactory()
                .getInteractorDao().getByShortLabel(intactObject.getShortLabel());
    }
}