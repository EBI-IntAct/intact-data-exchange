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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AbstractRepositoryTestCase {

    private File tempDir;

    private Repository repository;

    @Before
    public final void before() throws Exception {
        tempDir = new File("target", "imex-repo-"+System.currentTimeMillis());
        try {
            FileUtils.deleteDirectory(tempDir);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(tempDir.exists());

        repository = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());
    }

    @After
    public final void after() throws Exception {
        ImexRepositoryContext.closeRepository();
        repository = null;
        System.gc();

        tempDir = null;
    }

    public Repository getRepository() {
        return repository;
    }
}
