/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.search;

import junit.framework.TestCase;

import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.query.SearchQuery;
import psidev.psi.mi.tab.PsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActFastSearchEngineTest.java$
 */
public class IntActFastSearchEngineTest extends TestCase {

    public void testSearch() throws Exception {
        Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");

        IntActFastSearchEngine searchEngine = new IntActFastSearchEngine(indexDirectory);


        SearchQuery searchQuery = new IntActSearchQuery("properties:GO0006928");        
        SearchResult go_result = searchEngine.search(searchQuery, null, null);
        assertEquals(8, go_result.getInteractions().size());

        searchQuery = new IntActSearchQuery("GO*");
        SearchResult result = searchEngine.search(searchQuery, 50, 10);
        assertEquals(10, result.getInteractions().size());
       
        searchQuery = new IntActSearchQuery("Bantscheff");
        SearchResult author_result = searchEngine.search(searchQuery, null, null);
        assertEquals(199, author_result.getInteractions().size());
        
        searchQuery = new IntActSearchQuery("expansion:spoke");
        SearchResult expansion_result = searchEngine.search(searchQuery, null, null);
        assertEquals(199, expansion_result.getInteractions().size());
    }

    public void testSearchSort() throws Exception {
    	
        Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
        IntActFastSearchEngine searchEngine = new IntActFastSearchEngine(indexDirectory);
        
        Sort sort = new Sort(IntActColumnSet.ID_A.getSortableColumnName());
        SearchResult result = searchEngine.search(new IntActSearchQuery("id:P*"), 50, 10, sort);        
        assertEquals(10, result.getInteractions().size());
        assertEquals("P42345", result.getInteractions().iterator().next().getInteractorB().getIdentifiers().iterator().next().getIdentifier());
        
        sort = new Sort(IntActColumnSet.PROPERTIES_B.getSortableColumnName());
        result = searchEngine.search(new SearchQuery("properties:GO*"), 50, 10, sort);        
        assertEquals(10, result.getInteractions().size());
        IntActBinaryInteraction interactions = (IntActBinaryInteraction)result.getInteractions().iterator().next();
        assertEquals("P16591", interactions.getInteractorB().getIdentifiers().iterator().next().getIdentifier());
    }
    

}
