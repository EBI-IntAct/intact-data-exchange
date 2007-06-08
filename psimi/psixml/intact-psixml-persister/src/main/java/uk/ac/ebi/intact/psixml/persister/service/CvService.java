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
package uk.ac.ebi.intact.psixml.persister.service;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;
import uk.ac.ebi.intact.psixml.persister.key.CvObjectKey;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvService<T extends CvObject> extends AnnotatedObjectService<T, CvObjectKey> {

    public CvService(IntactContext intactContext) {
        super(intactContext);
    }

    @Override
    protected Element createElement(T object) {
        return new CvObjectKey(object).getElement();
    }

    @Override
    protected T fetchFromDb(CvObjectKey key) {
        CvObject cvObjToFetch = (CvObject) key.getElement().getValue();

        String miRef = (String) key.getElement().getKey();
        Class clazz = CgLibUtil.removeCglibEnhanced(cvObjToFetch.getClass());

        if (miRef == null) {
            throw new NullPointerException("Trying to fetch a CV with a null PSI MI: " + key.getElement().getValue());
        }

        T cv = (T) getIntactContext().getCvContext().getByMiRef(clazz, miRef);

        return cv;
    }

    @Override
    protected AnnotatedObjectDao getDao(Class objectType) {
        return getIntactContext().getDataContext().getDaoFactory().getCvObjectDao(objectType);
    }

    @Override
    protected void checkTransientValues(T annotatedObject) {
        super.checkTransientValues(annotatedObject);

        CvObject cv = null;

        Cache cache = getCache(CvObject.class);
        Element elem = cache.get(new CvObjectKey(annotatedObject).getElement().getKey());

        if (elem != null) {
            cv = (CvObject) elem.getValue();
        }

        if (cv == null) {
            cv = getIntactContext().getCvContext().getByLabel(annotatedObject.getClass(), annotatedObject.getShortLabel());

            if (cv == null) {
                super.checkTransientValues(annotatedObject);
                cache.put(new CvObjectKey(annotatedObject).getElement());
            }
        }
    }
}