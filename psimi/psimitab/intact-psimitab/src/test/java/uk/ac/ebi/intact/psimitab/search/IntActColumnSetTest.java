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
		assertEquals(23, columnSet.getPsimiTabColumns().size());
		
		assertEquals("experimentalRole interactor A", columnSet.getbyOrder(16).getColumnName());
		assertEquals("experimentalRole interactor B", columnSet.getbyOrder(17).getColumnName());
		
		assertEquals("properties interactor A", columnSet.getbyOrder(18).getColumnName());
		assertEquals("properties interactor B", columnSet.getbyOrder(19).getColumnName());

		assertEquals("interactorType of A", columnSet.getbyOrder(20).getColumnName());
		assertEquals("interactorType of B", columnSet.getbyOrder(21).getColumnName());

		assertEquals("hostOrganism", columnSet.getbyOrder(22).getColumnName());

		assertEquals("expansion method", columnSet.getbyOrder(23).getColumnName());
	}
}
