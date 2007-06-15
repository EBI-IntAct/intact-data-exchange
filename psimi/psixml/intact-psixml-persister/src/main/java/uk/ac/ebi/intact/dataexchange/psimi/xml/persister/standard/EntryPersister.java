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

import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersistenceContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.Persister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.persister.PersisterException;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;


/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryPersister implements Persister<IntactEntry> {

    private static ThreadLocal<EntryPersister> instance = new ThreadLocal<EntryPersister>() {
        @Override
        protected EntryPersister initialValue() {
            return new EntryPersister();
        }
    };

    public static EntryPersister getInstance() {
        return instance.get();
    }

    public static EntryPersister getInstance(boolean isDryRun) {
        PersistenceContext.getInstance().setDryRun(isDryRun);

        return instance.get();
    }

    private EntryPersister() {
    }

    public void saveOrUpdate(IntactEntry intactObject) throws PersisterException {

        InteractionPersister intPersister = InteractionPersister.getInstance();

        for (Interaction interaction : intactObject.getInteractions()) {
            intPersister.saveOrUpdate(interaction);
        }

    }

    public void commit() {
        PersistenceContext.getInstance().persistAll();
    }
}