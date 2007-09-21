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
package uk.ac.ebi.intact.dataexchange.imex.repository;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntrySet;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryHelper {

    private static final String FILE_EXTENSION = ".xml";
    private static final String FILE_EXTENSION_ENRICHED = ".enriched.xml";
    private static final String FILE_EXTENSION_RAW = ".raw.xml";
    private static final String FILE_EXTENSION_ERROR = ".error.txt";

    private Repository repository;

    public RepositoryHelper(Repository repository) {
        this.repository = repository;
    }

    public File getEntrySetFile(RepoEntrySet entrySet) {
        return getEntrySetFile(entrySet.getName());
    }

    public File getEntrySetFile(String name) {
        return new File(repository.getOriginalEntrySetDir(), name + FILE_EXTENSION);
    }

    public File getEntrySetDir(RepoEntrySet entrySet) {
        return new File(repository.getOriginalEntrySetDir(), entrySet.getName());
    }

    public File getEntrySetDir(String name) {
        return new File(repository.getEntriesDir(), name);
    }

    public File getEntryFile(RepoEntry entry) {
        return getEntryFile(entry.getPmid(), entry.getRepoEntrySet().getName(), entry.isEnriched());
    }

    public File getEntryFile(String name, String entrySetName, boolean enriched) {
        File entryFile;
        if (enriched) {
            entryFile = new File(getEntrySetDir(entrySetName), name + FILE_EXTENSION_ENRICHED);
        } else {
            entryFile = new File(getEntrySetDir(entrySetName), name + FILE_EXTENSION_RAW);
        }
        return entryFile;
    }

    public File getEntryErrorFile(RepoEntry repoEntry) {
        return getEntryErrorFile(repoEntry.getPmid(), repoEntry.getRepoEntrySet().getName());
    }

    public File getEntryErrorFile(String name, String entrySetName) {
        return new File(getEntrySetDir(entrySetName), name + FILE_EXTENSION_ERROR);
    }
}