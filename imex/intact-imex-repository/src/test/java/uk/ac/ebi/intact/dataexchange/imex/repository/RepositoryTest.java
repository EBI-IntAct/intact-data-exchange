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

import org.junit.Assert;
import org.junit.Test;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import java.io.File;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntrySet;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Message;

/**
 * Repository Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryTest extends AbstractRepositoryTestCase {

    @Test
    public void storeFile_dip() throws Exception {

        File empty = new File(RepositoryTest.class.getResource("/xml/dip_2006-11-01.xml").getFile());
        getRepository().storeEntrySet(empty, "dip");

        RepositoryHelper helper = new RepositoryHelper(getRepository());
        File expectedFile = helper.getEntrySetFile("2006-11-01", "dip");

        Assert.assertTrue(expectedFile.exists());

        System.out.println("-------------");
        for (RepoEntry re : getRepository().findRepoEntriesModifiedAfter(new DateTime(1))) {
                for (Message msg : re.getMessages()) {
                    System.out.println("["+msg.getLevel()+"] "+msg.getText()+" ["+msg.getContext()+"]");
                }
            }
    }

    @Test
    public void storeFile_mint() throws Exception {
        File empty = new File(RepositoryTest.class.getResource("/xml/mint_2006-07-18.xml").getFile());
        getRepository().storeEntrySet(empty, "mint");

        RepositoryHelper helper = new RepositoryHelper(getRepository());
        File expectedFile = helper.getEntrySetFile("2006-07-18", "mint");

        Assert.assertTrue(expectedFile.exists());
    }

    @Test
    public void storeFile_intact() throws Exception {
        File empty = new File(RepositoryTest.class.getResource("/xml/intact_2006-05-19.xml").getFile());
        getRepository().storeEntrySet(empty, "intact");

        RepositoryHelper helper = new RepositoryHelper(getRepository());
        File expectedFile = helper.getEntrySetFile("2006-05-19", "intact");

        Assert.assertTrue(expectedFile.exists());
    }
}