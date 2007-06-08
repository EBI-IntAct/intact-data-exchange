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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister.service;

import net.sf.ehcache.Element;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key.AnnotatedObjectKey;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObjectService<A extends AnnotatedObject, K extends AnnotatedObjectKey> extends AbstractService<A, K> {

    public AnnotatedObjectService(IntactContext intactContext) {
        super(intactContext);
    }

    public void persist(A objectToPersist) throws PersisterException {
        getDao(objectToPersist.getClass()).persist(objectToPersist);

        getCache(objectToPersist.getClass()).put(createElement(objectToPersist));
    }

    protected Element createElement(A object) {
        return new AnnotatedObjectKey(object).getElement();
    }

    protected A fetchFromDb(K key) {
        String shortLabel = (String) key.getElement().getKey();
        A annotToFetch = (A) key.getElement().getValue();

        if (shortLabel == null) {
            throw new NullPointerException("Element key must not be null: " + key.getElement());
        }

        return getDao(annotToFetch.getClass()).getByShortLabel((String) key.getElement().getKey());
    }

    protected AnnotatedObjectDao<A> getDao(Class classToFetch) {
        return (AnnotatedObjectDao<A>) getIntactContext().getDataContext().getDaoFactory().getAnnotatedObjectDao(classToFetch);
    }

}