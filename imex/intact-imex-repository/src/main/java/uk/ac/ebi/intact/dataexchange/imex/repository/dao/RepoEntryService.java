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
package uk.ac.ebi.intact.dataexchange.imex.repository.dao;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;

import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: RepoEntryService.java 9304 2007-08-06 11:10:23Z baranda $
 */
public interface RepoEntryService
{
    static final String NAME = "repoEntryService";

    void saveRepoEntry(RepoEntry repoEntry);

    //void removeProvider(Provider provider);

    //void updateProvider(Provider provider);

    List<RepoEntry> findAllRepoEntries();

    RepoEntry findByPmid(String name);

    List<RepoEntry> findImportableExcluding(List<String> pmids);
}