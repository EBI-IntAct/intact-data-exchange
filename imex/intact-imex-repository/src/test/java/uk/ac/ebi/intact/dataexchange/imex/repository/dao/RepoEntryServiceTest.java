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

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.AbstractRepositoryTestCase;
import uk.ac.ebi.intact.dataexchange.imex.repository.mock.RepoMockBuilder;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;

import java.util.Arrays;
import java.util.List;

/**
 * RepoEntryService Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepoEntryServiceTest extends AbstractRepositoryTestCase {
    
    @Test
    public void findImportablesExcluding() throws Exception {
        persistRepoEntrySet();
        
        RepoEntryService repoEntryService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService();

        Assert.assertEquals(7, repoEntryService.findAllRepoEntries().size());
        Assert.assertEquals(7, repoEntryService.countAll());
        Assert.assertEquals(5, repoEntryService.findImportableExcluding(Arrays.asList("1","2")).size());
    }

    @Test
    public void findModifiedAfter() throws Exception {
        persistRepoEntrySet();

        RepoEntryService repoEntryService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService();

        final List<RepoEntry> entries = repoEntryService.findAllRepoEntries();
        Assert.assertEquals(7, entries.size());

        DateTime dateTime = new DateTime();

        // Sleeping for 1 second to make sure the timestamp shows a big enough difference since data insert.
        Thread.sleep( 1*1000 );

        // now let's update a single record 
        beginTransaction();

        final RepoEntry entry = entries.iterator().next();
        entry.setPmid("555");
        commitTransaction();

        Assert.assertEquals(1, repoEntryService.findImportableModifiedAfter(dateTime).size());
        Assert.assertEquals("555", repoEntryService.findImportableModifiedAfter(dateTime).iterator().next().getPmid());
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