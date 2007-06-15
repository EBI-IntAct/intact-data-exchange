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
import uk.ac.ebi.intact.model.CvObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SyncContext {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(SyncContext.class);

    private static ThreadLocal<SyncContext> instance = new ThreadLocal<SyncContext>() {
        @Override
        protected SyncContext initialValue() {
            return new SyncContext(IntactContext.getCurrentInstance());
        }
    };

    private IntactContext intactContext;

    private Map<AnnotObjectKey, CvObject> syncedCvObjects;
    private Map<AnnotObjectKey, AnnotatedObject> syncedAnnotatedObjects;

    public static SyncContext getInstance() {
        return instance.get();
    }

    private SyncContext(IntactContext intactContext) {
        this.intactContext = intactContext;

        this.syncedCvObjects = new HashMap<AnnotObjectKey, CvObject>();
        this.syncedAnnotatedObjects = new HashMap<AnnotObjectKey, AnnotatedObject>();
    }

    public void addToSynced(AnnotatedObject ao) {
        if (ao instanceof CvObject) {
            syncedCvObjects.put(keyFor(ao), (CvObject) ao);
        } else {
            syncedAnnotatedObjects.put(keyFor(ao), ao);
        }
    }

    public boolean isAlreadySynced(AnnotatedObject ao) {
        if (syncedCvObjects.containsKey(keyFor(ao))) {
            return true;
        }
        return syncedAnnotatedObjects.containsKey(keyFor(ao));
    }

    public AnnotatedObject get(AnnotatedObject ao) {
        final AnnotObjectKey key = keyFor(ao);

        if (syncedCvObjects.containsKey(key)) {
            return syncedCvObjects.get(key);
        }
        return syncedAnnotatedObjects.get(key);
    }

    private AnnotObjectKey keyFor(AnnotatedObject ao) {
        return new AnnotObjectKey(ao);
    }

    private class AnnotObjectKey {

        private Class annotatedObjectClass;
        private String shortLabel;

        public AnnotObjectKey(AnnotatedObject annotObject) {
            this.annotatedObjectClass = annotObject.getClass();
            this.shortLabel = annotObject.getShortLabel();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnnotObjectKey that = (AnnotObjectKey) o;

            if (annotatedObjectClass != null ? !annotatedObjectClass.equals(that.annotatedObjectClass) : that.annotatedObjectClass != null)
                return false;
            if (shortLabel != null ? !shortLabel.equals(that.shortLabel) : that.shortLabel != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (annotatedObjectClass != null ? annotatedObjectClass.hashCode() : 0);
            result = 31 * result + (shortLabel != null ? shortLabel.hashCode() : 0);
            return result;
        }
    }

    public Collection<AnnotatedObject> getSyncedAnnotatedObjects() {
        return syncedAnnotatedObjects.values();
    }

    public Collection<CvObject> getSyncedCvObjects() {
        return syncedCvObjects.values();
    }

    public void clear() {
        syncedCvObjects.clear();
    }
}