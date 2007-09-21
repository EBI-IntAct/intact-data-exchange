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
package uk.ac.ebi.intact.dataexchange.imex.repository.mock;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntrySet;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.UnexpectedError;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepoMockBuilder {

    public RepoMockBuilder() {

    }

    public RepoEntrySet createRepoEntrySet(String providerName, String name) {
        return createRepoEntrySet(createProvider(providerName), name);
    }

    public RepoEntrySet createRepoEntrySet(Provider provider, String name) {
        RepoEntrySet repoEntrySet = new RepoEntrySet(provider, name);

        for (int i=0; i<5; i++) {
            repoEntrySet.getRepoEntries().add(createRepoEntry(String.valueOf(i), true));
        }

        for (int i=5; i<7; i++) {
            repoEntrySet.getRepoEntries().add(createRepoEntry(String.valueOf(i), true));
        }

        return repoEntrySet;
    }

    public RepoEntry createRepoEntry(String pmid, boolean importable) {
        RepoEntry repoEntry = new RepoEntry(pmid);

        if (importable) {
            repoEntry.setValid(true);
            repoEntry.setImportable(true);
        } else {
            repoEntry.setValid(true);
            repoEntry.getErrors().add(createError("This is an error."));
        }

        return repoEntry;
    }

    public Provider createProvider(String name) {
        Provider provider = new Provider(name);
        return provider;
    }

    public UnexpectedError createError(String message) {
        UnexpectedError error = new UnexpectedError(message, new RuntimeException("anException"));
        return error;
    }
}