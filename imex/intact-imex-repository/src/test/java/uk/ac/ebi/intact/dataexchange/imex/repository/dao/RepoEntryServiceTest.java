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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.mock.RepoMockBuilder;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;

import java.io.File;
import java.util.Arrays;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepoEntryServiceTest {

    private static final File TEMP_REPO_DIR = new File(System.getProperty("java.io.tmpdir"), "imex-repo-test/");

    @Before
    public void before() throws Exception {
        FileUtils.deleteDirectory(TEMP_REPO_DIR);
        ImexRepositoryContext.openRepository(TEMP_REPO_DIR.getAbsolutePath());
    }

    @Before
    public void after() throws Exception {
        FileUtils.deleteDirectory(TEMP_REPO_DIR);
    }

    @Test
    public void findImportablesExcluding() throws Exception {
        persistRepoEntrySet();
        
        RepoEntryService repoEntryService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService();

        Assert.assertEquals(7, repoEntryService.findAllRepoEntries().size());
        Assert.assertEquals(5, repoEntryService.findImportableExcluding(Arrays.asList("1","2")).size());
    }

    private void persistRepoEntrySet() throws Exception {
        Provider provider = ImexRepositoryContext.getInstance().getImexServiceProvider().getProviderService().findByName("intact");
        RepoEntrySetService repoEntrySetService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntrySetService();

        beginTransaction();
        repoEntrySetService.saveRepoEntrySet(new RepoMockBuilder().createRepoEntrySet(provider, "anEntrySet"));
        commitTransaction();
    }

    private void beginTransaction() {
        ImexRepositoryContext.getInstance().getImexPersistence().beginTransaction();
    }

    private void commitTransaction() {
        ImexRepositoryContext.getInstance().getImexPersistence().commitTransaction();
    }
}