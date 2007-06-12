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
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key.CvObjectKey;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key.Key;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service.AbstractService;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service.CvService;
import uk.ac.ebi.intact.model.CvObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvPersister<T extends CvObject> extends AbstractAnnotatedObjectPersister<T> {

    public CvPersister(IntactContext intactContext, boolean dryRun) {
        super(intactContext, dryRun);
    }

    @Override
    protected Key generateKey(T intactObject) {
        if (intactObject == null) {
            throw new NullPointerException("intactObject");
        }

        return new CvObjectKey(intactObject);
    }

    @Override
    protected AbstractService getService() {
        return new CvService(getIntactContext());
    }


}
