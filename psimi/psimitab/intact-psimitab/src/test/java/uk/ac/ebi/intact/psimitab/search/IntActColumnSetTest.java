/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO comment this!
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActColumnSetTest.java  *
 */
public class IntActColumnSetTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.psimitab.search.IntActColumnSet#IntActColumnSet()}.
	 */
	@Test
	public void testIntActColumnSet() {
		IntActColumnSet columnSet = new IntActColumnSet();
		
		assertNotNull(columnSet.getPsimiTabColumns());
		assertEquals(22, columnSet.getPsimiTabColumns().size());
		
		assertEquals("experimentalRole interactor A", columnSet.getbyOrder(16).getColumnName());
		assertEquals("hostOrganism", columnSet.getbyOrder(22).getColumnName());

	}
}
