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
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ProviderService;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryTest {

    @Test
    public void storeFile_default() throws Exception{
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "myRepo/");
        FileUtils.deleteDirectory(tempDir);

        Repository repo = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());

        File empty = new File(RepositoryTest.class.getResource("/xml/dip_2006-11-01.xml").getFile());
        repo.storeEntrySet(empty, "dip");

        RepositoryHelper helper = new RepositoryHelper(repo);
        File expectedFile = helper.getEntrySetFile("dip_2006-11-01");

        Assert.assertTrue(expectedFile.exists());
    }
}