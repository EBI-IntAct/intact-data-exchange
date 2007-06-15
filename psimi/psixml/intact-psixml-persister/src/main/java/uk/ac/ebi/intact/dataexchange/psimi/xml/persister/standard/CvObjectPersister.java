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
import uk.ac.ebi.intact.model.CvObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectPersister extends AbstractAnnotatedObjectPersister<CvObject> {

     private static ThreadLocal<CvObjectPersister> instance = new ThreadLocal<CvObjectPersister>() {
        @Override
        protected CvObjectPersister initialValue() {
            return new CvObjectPersister(IntactContext.getCurrentInstance());
        }
    };

    public static CvObjectPersister getInstance() {
        return instance.get();
    }

    protected CvObjectPersister(IntactContext intactContext) {
        super(intactContext);
    }

    @Override
    protected CvObject fetchFromDataSource(CvObject intactObject) {
        return getIntactContext().getDataContext().getDaoFactory()
                .getCvObjectDao().getByShortLabel(intactObject.getClass(), intactObject.getShortLabel());
    }
}