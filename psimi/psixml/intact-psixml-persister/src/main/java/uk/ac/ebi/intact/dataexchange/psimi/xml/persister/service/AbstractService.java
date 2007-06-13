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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key.AnnotatedObjectKey;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.key.Key;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.shared.PersisterHelper;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util.CacheContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.util.PersisterConfig;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Xref;

import java.io.Serializable;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractService<T extends AnnotatedObject, K extends Key> implements Serializable {

    private static final Log log = LogFactory.getLog(AbstractService.class);

    private IntactContext intactContext;
    private CacheContext cacheContext;
    private Institution institution;

    protected AbstractService(IntactContext intactContext) {
        this.intactContext = intactContext;
        this.cacheContext = CacheContext.getInstance(intactContext);
    }

    protected CacheContext getCacheContext() {
        return cacheContext;
    }

    protected IntactContext getIntactContext() {
        return intactContext;
    }

    public T get(K key) {
        T annotatedObject = (T) key.getElement().getObjectValue();

        Element elem = getCache(annotatedObject.getClass())
                .get(key.getElement().getObjectKey());

        T intactObject = null;
        boolean isCheckTransient = true;

        if (elem != null) {
            intactObject = (T) elem.getValue();
        } else {
            T intactObjectFromDb = fetchFromDb(key);

            if (intactObjectFromDb != null) {
                intactObject = intactObjectFromDb;
                isCheckTransient = false;

                getCache(annotatedObject.getClass())
                        .put(new Element(key.getElement().getObjectKey(), intactObject));
            }


        }

        if (isCheckTransient) {
            checkTransientValues(annotatedObject);
        }

        return intactObject;
    }

    protected void checkTransientValues(T annotatedObject) {
        if (annotatedObject.getOwner() != null &&
            !PersisterHelper.isTransient(annotatedObject.getOwner(), getIntactContext())) {
            return;
        }

        annotatedObject.setOwner(getInstitution());

        for (Xref xref : (Collection<Xref>) annotatedObject.getXrefs()) {
            checkTransientXref(xref);
        }
    }

    protected void checkTransientXref(Xref xref) {
        xref.setOwner(getInstitution());
    }

    public abstract void persist(T objectToPersist) throws PersisterException;

    protected abstract T fetchFromDb(K key);

    protected Cache getCache(Class objectType) {
        return getCacheContext().cacheFor(objectType);
    }

    protected boolean isDryRun() {
        return PersisterConfig.isDryRun(getIntactContext());
    }

    protected Institution getInstitution() {
        if (institution == null) {
            institution = getIntactContext().getDataContext().getDaoFactory()
                    .getInstitutionDao().getByAc(getIntactContext().getInstitution().getAc());
        }
        return institution;
    }

    protected Element createElement(T object) {
        return new AnnotatedObjectKey(object).getElement();
    }

    protected boolean isAlreadyInCache(T annotObject) {
        Object key = createElement(annotObject).getKey();
        Cache cache = getCache(annotObject.getClass());

        if (log.isTraceEnabled()) log.trace("\tTrying cache "+cache.getName()+" for key: "+key);

        return (cache.get(key) != null);

    }

    protected void putInCache(T annotObject) {
        Cache cache = getCache(annotObject.getClass());
        Element elemenet = createElement(annotObject);

        if (log.isTraceEnabled()) log.trace("\tPutting element in cache "+cache.getName()+" with key: "+elemenet.getKey());

        cache.put(elemenet);
    }
    
}