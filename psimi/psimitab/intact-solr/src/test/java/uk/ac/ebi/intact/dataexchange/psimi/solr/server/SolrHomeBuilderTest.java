/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr.server;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrHomeBuilderTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testInstall() throws Exception {
        File workingDir = new File("/tmp/lalasolr");

        SolrHomeBuilder solrHomeBuilder = new SolrHomeBuilder();
        solrHomeBuilder.install(new File("/tmp/lalasolr"));
        
        Assert.assertTrue(workingDir.exists());
        Assert.assertTrue(workingDir.isDirectory());
        Assert.assertTrue(new File(workingDir, "home").exists());
        Assert.assertTrue(new File(workingDir, "solr.war").exists());

        FileUtils.forceDelete(workingDir);
    }
}
