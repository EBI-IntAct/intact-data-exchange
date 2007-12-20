/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * IntActColumnSet Tester.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActColumnSetTest {

	@Test
	public void testIntActColumnSet() {
		IntActColumnSet columnSet = new IntActColumnSet();
		
		assertNotNull(columnSet.getPsimiTabColumns());
		assertEquals(26, columnSet.getPsimiTabColumns().size());
		
		assertEquals("experimentalRole interactor A", columnSet.getByOrder(15).getColumnName());
		assertEquals("experimentalRole interactor B", columnSet.getByOrder(16).getColumnName());

		assertEquals("biologicalRole interactor A", columnSet.getByOrder(17).getColumnName());
		assertEquals("biologicalRole interactor B", columnSet.getByOrder(18).getColumnName());

        assertEquals("properties interactor A", columnSet.getByOrder(19).getColumnName());
		assertEquals("properties interactor B", columnSet.getByOrder(20).getColumnName());

		assertEquals("interactorType of A", columnSet.getByOrder(21).getColumnName());
		assertEquals("interactorType of B", columnSet.getByOrder(22).getColumnName());

		assertEquals("hostOrganism", columnSet.getByOrder(23).getColumnName());

		assertEquals("expansion method", columnSet.getByOrder(24).getColumnName());

        assertEquals("dataset", columnSet.getByOrder(25).getColumnName());
    }
}
