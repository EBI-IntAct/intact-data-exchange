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
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PersistenceContext {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(PersistenceContext.class);

    private static ThreadLocal<PersistenceContext> instance = new ThreadLocal<PersistenceContext>() {
        @Override
        protected PersistenceContext initialValue() {
            return new PersistenceContext(IntactContext.getCurrentInstance());
        }
    };

    private IntactContext intactContext;
    private boolean dryRun;

    private Map<AnnotObjectKey, CvObject> cvObjectsToBePersisted;
    private Map<AnnotObjectKey, AnnotatedObject> annotatedObjectsToBePersisted;

    public static PersistenceContext getInstance() {
        return instance.get();
    }

    private PersistenceContext(IntactContext intactContext) {
        this.intactContext = intactContext;

        this.cvObjectsToBePersisted = new HashMap<AnnotObjectKey,CvObject>();
        this.annotatedObjectsToBePersisted = new HashMap<AnnotObjectKey,AnnotatedObject>();
    }

    public void addToPersist(AnnotatedObject ao) {
        if (ao instanceof CvObject) {
            cvObjectsToBePersisted.put(keyFor(ao),(CvObject)ao);
        } else {
            annotatedObjectsToBePersisted.put(keyFor(ao), ao);
        }
    }


    public boolean contains(AnnotatedObject ao) {
        final AnnotObjectKey key = keyFor(ao);

        if (cvObjectsToBePersisted.containsKey(key)) {
            return true;
        }
        return annotatedObjectsToBePersisted.containsKey(key);
    }

    public AnnotatedObject get(AnnotatedObject ao) {
        final AnnotObjectKey key = keyFor(ao);

        if (cvObjectsToBePersisted.containsKey(key)) {
            return cvObjectsToBePersisted.get(key);
        }

        return annotatedObjectsToBePersisted.get(key);
    }

    public void persistAll() {
        if (log.isDebugEnabled()) {
            log.debug("Persisting all"+ (isDryRun()? " - DRY RUN" : ""));
        }

        for (CvObject cv : cvObjectsToBePersisted.values()) {
            getDaoFactory().getCvObjectDao().persist(cv);
        }

        cvObjectsToBePersisted.clear();
        getIntactContext().getDataContext().flushSession();

        for (AnnotatedObject ao : annotatedObjectsToBePersisted.values()) {
            getDaoFactory().getAnnotatedObjectDao((Class<AnnotatedObject>)ao.getClass()).persist(ao);
        }

        annotatedObjectsToBePersisted.clear();
        SyncContext.getInstance().clear();
        getIntactContext().getDataContext().flushSession();
    }

    private AnnotObjectKey keyFor(AnnotatedObject ao) {
        return new AnnotObjectKey(ao);
    }

    private IntactContext getIntactContext() {
        return intactContext;
    }

    private DaoFactory getDaoFactory() {
        return getIntactContext().getDataContext().getDaoFactory();
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

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Collection<AnnotatedObject> getAnnotatedObjectsToBePersisted() {
        return annotatedObjectsToBePersisted.values();
    }

    public Collection<CvObject> getCvObjectsToBePersisted() {
        return cvObjectsToBePersisted.values();
    }
}