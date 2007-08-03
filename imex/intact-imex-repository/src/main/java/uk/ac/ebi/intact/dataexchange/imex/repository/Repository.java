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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Repository {

   private final static String CONFIG_DIR_NAME = ".config";
    private final static String ORIGINAL_DIR_NAME = "original";
    private final static String ENTRIES_DIR_NAME = "entries";

    private File repositoryDir;

    public Repository(File repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    public void addEntry(File entryXml) {
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