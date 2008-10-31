/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.search;

import psidev.psi.mi.search.index.impl.InteractorIndexWriter;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import java.io.InputStream;

/**
 * IntactInteractorIndexWriter Tester.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactInteractorIndexWriterTest {

    private IntactInteractorIndexWriter indexWriter;
    private Directory directory;

    @Before
    public void before() throws Exception {
        indexWriter = new IntactInteractorIndexWriter();
        directory = new RAMDirectory();
//        directory = FSDirectory.getDirectory( "C:\\testIndex" );
    }

    @After
    public void after() throws Exception {
        indexWriter = null;
        directory.close();
        directory = null;
    }

    @Test
    public void index3() throws Exception {
        InputStream is = IntactInteractorIndexWriterTest.class.getResourceAsStream("/mitab_samples/imatinib_small.txt");
        indexWriter.index(directory, is, true, true);

        assertSearchResultCount(3, "*");
    }

    @Test
    public void index4() throws Exception {
        InputStream is = IntactInteractorIndexWriterTest.class.getResourceAsStream("/mitab_samples/aspirin.tsv");
        indexWriter.index(directory, is, true, true);

        assertSearchResultCount(17, "*");

        assertSearchResultCount(1, "aspirine");

        // check column by column

        // protein identifier A
        assertSearchResultCount(1, "idA:P23219");
        assertSearchResultCount(2, "P23219");
        // compound identifier A
        assertSearchResultCount(1, "DB00371");

        // protein identifier B
        assertSearchResultCount(2, "P60045");
        // compound identifier B
        assertSearchResultCount(1, "DB00497");

        // alternative identifier A
        assertSearchResultCount(1, "Cirpon");

        // alternative identifier B
        assertSearchResultCount(1, "Acetophen");

        // aliases A
        assertSearchResultCount(1, "GABRA6");

        // aliases B
        assertSearchResultCount(1, "GABRA1");

        // properties A
        assertSearchResultCount(0, "1PXX_A");

        // properties B
        assertSearchResultCount(0, "APRD00387");

        // properties of interactions
        assertSearchResultCount(17, "18048412");
        assertSearchResultCount(16, "9606");
    }

    private void assertSearchResultCount( final int expectedCount, String searchQuery ){
        Assert.assertEquals( expectedCount, Searcher.search(searchQuery, directory).getTotalCount());
    }
}
