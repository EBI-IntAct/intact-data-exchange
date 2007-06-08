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
package uk.ac.ebi.intact.psixml.persister.shared;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psixml.commons.model.IntactEntry;
import uk.ac.ebi.intact.psixml.persister.Persister;
import uk.ac.ebi.intact.psixml.persister.PersisterException;
import uk.ac.ebi.intact.psixml.persister.PersisterReport;
import uk.ac.ebi.intact.psixml.persister.util.CacheContext;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryPersister implements Persister<IntactEntry> {

    private IntactContext intactContext;
    private boolean dryRun;
    private PersisterReport report;

    public EntryPersister(IntactContext intactContext, boolean dryRun) {
        this.intactContext = intactContext;
        this.dryRun = dryRun;
        this.report = new PersisterReport();
    }

    public IntactEntry saveOrUpdate(IntactEntry objectToPersist) throws PersisterException {

        InteractionPersister intPersister = new InteractionPersister(intactContext, dryRun);

        List<Interaction> persistedInteractions = new ArrayList<Interaction>(objectToPersist.getInteractions().size());

        for (Interaction interaction : objectToPersist.getInteractions()) {
            Interaction persistedInteraction = intPersister.saveOrUpdate(interaction);
            persistedInteractions.add(persistedInteraction);
            report.mergeWith(intPersister.getReport());
        }

        objectToPersist.setInteractions(persistedInteractions);

        CacheContext.getInstance(intactContext).clearAll();

        return objectToPersist;
    }

    public PersisterReport getReport() {
        return report;
    }
}