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

import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import static org.junit.Assert.*;
import org.junit.Test;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.query.SearchQuery;

/**
 * IntActSearchEngine Tester.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActSearchEngineTest {

	@Test 
	public void testExperimentalRole() throws Exception {

		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);
		
		SearchQuery searchQuery = new IntActSearchQuery("roleA:bait");        
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals(186, result.getInteractions().size());
        
		searchQuery = new IntActSearchQuery("roles:\"unspecified role\"");  
        result = searchEngine.search(searchQuery, null, null);
        assertEquals(10+4, result.getInteractions().size());

        Sort sort = new Sort(IntActColumnSet.EXPERIMENTAL_ROLE_B.getSortableColumnName());
        result = searchEngine.search(new IntActSearchQuery("roleB:prey"), 20, 10, sort);        
        assertEquals(10, result.getInteractions().size());
        assertEquals("P41240", result.getInteractions().get(0).getInteractorB().getIdentifiers().iterator().next().getIdentifier());     
	}
	
	@Test 
	public void testProperties() throws Exception {
			
		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);
		
		SearchQuery searchQuery = new IntActSearchQuery("GO:0006928");        
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals(8, result.getInteractions().size());
        
		searchQuery = new IntActSearchQuery("GO*");        
        result = searchEngine.search(searchQuery, null, 10);
        assertEquals(10, result.getInteractions().size());
        
		searchQuery = new IntActSearchQuery("propertiesB:interpro");        
        result = searchEngine.search(searchQuery, null, 10);
        assertEquals(10, result.getInteractions().size());

        Sort sort = new Sort(IntActColumnSet.PROPERTIES_B.getSortableColumnName());
        result = searchEngine.search(new IntActSearchQuery("properties:IPR008271"), 20, 10, sort);        
        assertEquals(10, result.getInteractions().size());
        assertEquals("Q9Y6R4", result.getInteractions().get(0).getInteractorB().getIdentifiers().iterator().next().getIdentifier());     
	}
	
	@Test 
	public void testInteractorType() throws Exception {
			
		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);
		
		SearchQuery searchQuery = new IntActSearchQuery("typeA:\"small molecule\"");        
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals(196, result.getInteractions().size());
        
		searchQuery = new IntActSearchQuery("typeB:protein");        
        result = searchEngine.search(searchQuery, null, 10);
        assertEquals(10, result.getInteractions().size());

        Sort sort = new Sort(IntActColumnSet.INTERACTOR_TYPE_B.getSortableColumnName());
        result = searchEngine.search(new IntActSearchQuery("interactor_types:\"small molecule\""), 10, 20, sort);        
        assertEquals(20, result.getInteractions().size());
        assertEquals("Q13535", result.getInteractions().get(0).getInteractorB().getIdentifiers().iterator().next().getIdentifier());     
	}
	
	@Test 
	public void testHostorganism() throws Exception {
			
		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);
		
		SearchQuery searchQuery = new IntActSearchQuery("hostOrganism:\"in vitro\"");        
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals(14, result.getInteractions().size());
        
        Sort sort = new Sort(IntActColumnSet.HOSTORGANISM.getSortableColumnName());
        result = searchEngine.search(new IntActSearchQuery("in*"), 50, 20, sort);
        assertEquals(20, result.getInteractions().size());
        assertEquals("P78527", result.getInteractions().get(0).getInteractorB().getIdentifiers().iterator().next().getIdentifier());     
	}
	
	@Test 
	public void testExpansionMethod() throws Exception {
			
		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);
		
		SearchQuery searchQuery = new IntActSearchQuery("expansion:spoke");        
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals(186, result.getInteractions().size());
        
        Sort sort = new Sort(IntActColumnSet.EXPANSION_METHOD.getSortableColumnName());
        result = searchEngine.search(new IntActSearchQuery("spoke"), 50, 20, sort);        
        assertEquals(20, result.getInteractions().size());
        assertEquals("P78527", result.getInteractions().get(0).getInteractorB().getIdentifiers().iterator().next().getIdentifier());     
	}

	@Test
	public void testDataSet() throws Exception {

		Directory indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/intact.sample-extra.xls");
		IntActSearchEngine searchEngine = new IntActSearchEngine(indexDirectory);

		SearchQuery searchQuery = new IntActSearchQuery("dataset:Cancer");
        SearchResult result = searchEngine.search(searchQuery, null, null);
        assertEquals( 0, result.getInteractions().size() );

 		indexDirectory = TestHelper.createIndexFromResource("/mitab_samples/17292829.txt");
		searchEngine = new IntActSearchEngine(indexDirectory);

        //searchQuery = new IntActSearchQuery("dataset:\"Cancer - Interactions investigated in the context of cancer\"");
        searchQuery = new IntActSearchQuery("dataset:Cancer");
        result = searchEngine.search(searchQuery, null, null);
        assertEquals(1, result.getInteractions().size());
    }
}
