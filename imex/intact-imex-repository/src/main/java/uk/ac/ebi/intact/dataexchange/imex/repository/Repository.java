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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.EntrySet;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntityNotFoundException;

import java.io.File;
import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Repository {

    private static final Log log = LogFactory.getLog(Repository.class);

    private static final String CONFIG_DIR_NAME = ".config";
    private static final String ORIGINAL_DIR_NAME = "original";
    private static final String ENTRIES_DIR_NAME = "entries";

    private File repositoryDir;

    public Repository(File repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    public void storeEntrySet(File entryXml, String providerName) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Adding entry: "+entryXml+" (Provider: "+providerName+")");
        }

        ImexRepositoryContext context = ImexRepositoryContext.getInstance();
        Provider provider = context.getImexServiceProvider().getProviderService().findByName(providerName);

        if (provider == null) {
            throw new RepoEntityNotFoundException("No provider found with name: " + providerName);
        }

        String entryName = entryXml.getName();
        String name = FilenameUtils.removeExtension(entryName);

        EntrySet entrySet = new EntrySet(provider, name);

        RepositoryHelper repoHelper = new RepositoryHelper(this);
        File newFile = repoHelper.getEntrySetFile(entrySet);

        // copy the physical file
        if (log.isDebugEnabled()) {
            log.debug("Copying file to: "+newFile);
        }
        FileUtils.copyFile(entryXml, newFile);

        // create the record in the database
        context.getImexPersistence().beginTransaction();
        context.getImexServiceProvider().getEntrySetService().saveEntrySet(entrySet);
        context.getImexPersistence().commitTransaction();

        // TODO init the split and enrichment
    }

    public EntrySet retrieveEntrySet(String name) {
        throw new UnsupportedOperationException();
    }


    public File getRepositoryDir() {
        return repositoryDir;
    }

    public File getConfigDir() {
        return new File(getRepositoryDir(), CONFIG_DIR_NAME);
    }

    public File getOriginalEntrySetDir() {
        return new File(getRepositoryDir(), ORIGINAL_DIR_NAME);
    }

    public File getEntriesDir() {
        return new File(getRepositoryDir(), ENTRIES_DIR_NAME);
    }
}