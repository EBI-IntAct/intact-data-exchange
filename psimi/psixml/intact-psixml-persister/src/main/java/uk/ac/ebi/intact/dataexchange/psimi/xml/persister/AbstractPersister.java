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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;


/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractPersister<T extends AnnotatedObject> implements Persister<T> {

    private static final Log log = LogFactory.getLog(AbstractPersister.class);

    private IntactContext intactContext;

    protected AbstractPersister(IntactContext intactContext) {
        this.intactContext = intactContext;
    }
    
    public final void saveOrUpdate(T intactObject) throws PersisterException {
        if (intactObject == null) {
            throw new NullPointerException("intactObject");
        }

        if (PersistenceContext.getInstance().contains(intactObject)) {
            return;
        }

        if (log.isDebugEnabled()) log.debug("Saving "+intactObject.getClass().getSimpleName()+": "+intactObject.getShortLabel());

        SyncTransientResponse<T> syncResponse = syncIfTransientResponse(intactObject);

        if (syncResponse.isAlreadyPresent()) {
            if (syncedAndCandidateAreEqual(syncResponse.getValue(), intactObject)) {
                if (log.isDebugEnabled()) log.debug("\tAlready present in a data source (synced)");
            } else {
                if (log.isDebugEnabled()) log.debug("\tData source object and object to persist are not equal - update");

                if (!isDryRun()) {
                    update(intactObject, syncResponse.getValue());
                }
            }

            // don't continue if the object already exists or has been updated
            return;
        }

        log.debug("\tNot present in a data source - Will persist");
        T newAnnotatedObject = syncResponse.getValue();
        PersistenceContext.getInstance().addToPersist(newAnnotatedObject);

        saveOrUpdateAttributes(newAnnotatedObject);
    }

    public final void commit() {
       PersistenceContext.getInstance().persistAll();
    }

    protected final SyncTransientResponse<T> syncIfTransientResponse(T intactObject) {
         T refreshedObject = syncIfTransient(intactObject);

        if (refreshedObject.getAc() != null) {
            return new SyncTransientResponse<T>(true, refreshedObject);
        }
        
        return new SyncTransientResponse<T>(false, syncAttributes(intactObject));
    }

    protected T syncIfTransient(T intactObject) {
        if (log.isDebugEnabled()) log.debug("\t\tSyncing "+intactObject.getClass().getSimpleName()+": "+intactObject.getShortLabel());

        T refreshedObject = get(intactObject);

        if (refreshedObject != null) {
            if (log.isDebugEnabled()) log.debug("\t\t\tAlready synced");
            return refreshedObject;
        }

        if (log.isDebugEnabled()) log.debug("\t\t\tNot previously synced");

        SyncContext.getInstance().addToSynced(intactObject);

        intactObject.setOwner(getIntactContext().getInstitution());

        return syncAttributes(intactObject);
    }

    protected final T get(T intactObject) {
        if (PersistenceContext.getInstance().contains(intactObject)) {
            return (T) PersistenceContext.getInstance().get(intactObject);
        }
        if (SyncContext.getInstance().isAlreadySynced(intactObject)) {
            return (T) SyncContext.getInstance().get(intactObject);
        }
        return fetchFromDataSource(intactObject);
    }

    protected abstract void saveOrUpdateAttributes(T intactObject) throws PersisterException;

    protected abstract T syncAttributes(T intactObject);

    protected abstract T fetchFromDataSource(T intactObject);

    protected abstract boolean syncedAndCandidateAreEqual(T synced, T candidate);

    protected abstract boolean update(T objectToUpdate, T existingObject);

    protected IntactContext getIntactContext() {
        return intactContext;
    }

    public boolean isDryRun() {
        return PersistenceContext.getInstance().isDryRun();
    }

    private class SyncTransientResponse<T> {
        private boolean alreadyPresent;
        private T value;

        public SyncTransientResponse(boolean alreadyPresent, T value) {
            this.alreadyPresent = alreadyPresent;
            this.value = value;
        }

        public boolean isAlreadyPresent() {
            return alreadyPresent;
        }

        public T getValue() {
            return value;
        }
    }

}