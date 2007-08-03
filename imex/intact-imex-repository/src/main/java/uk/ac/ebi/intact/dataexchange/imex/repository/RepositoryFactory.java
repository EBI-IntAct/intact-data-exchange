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

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryFactory {

    public static Repository createFileSystemRepository(String repositoryPath, boolean createIfNotExisting) {
        if (repositoryPath == null) {
            throw new NullPointerException("repositoryPath");
        }

        File repoFile = new File(repositoryPath);

        Repository fsRepository = new Repository(repoFile);

        if (!repoFile.exists()) {
            if (createIfNotExisting) {
                createFileSystemRepositoryLayout(fsRepository);
            } else {
                throw new RepositoryException("Repository not found at: "+repositoryPath);
            }
        }

        if (repoFile.exists() && !repoFile.isDirectory()) {
            throw new RepositoryException("File exists, but it is not a directory: "+repositoryPath);
        }

        if (createIfNotExisting) {
            createFileSystemRepositoryLayout(fsRepository);
        }

        return fsRepository;
    }

    public static void createFileSystemRepositoryLayout(Repository fsRepository) {
        if (fsRepository == null) {
            throw new NullPointerException("fsRepository");
        }

        fsRepository.getRepositoryDir().mkdirs();
        fsRepository.getConfigDir().mkdir();
        fsRepository.getOriginalEntrySetDir().mkdir();
        fsRepository.getEntriesDir().mkdir();
    }
}