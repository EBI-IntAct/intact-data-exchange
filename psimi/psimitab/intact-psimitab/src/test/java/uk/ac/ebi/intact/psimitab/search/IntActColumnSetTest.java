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
		IntactColumnSet columnSet = new IntactColumnSet();
		
		assertNotNull(columnSet.getPsimiTabColumns());
		assertEquals(26, columnSet.getPsimiTabColumns().size());
		
		assertEquals("Experimental role(s) interactor A", columnSet.getByOrder(15).getColumnName());
		assertEquals("Experimental role(s) interactor B", columnSet.getByOrder(16).getColumnName());

		assertEquals("Biological role(s) interactor A", columnSet.getByOrder(17).getColumnName());
		assertEquals("Biological role(s) interactor B", columnSet.getByOrder(18).getColumnName());

        assertEquals("Properties interactor A", columnSet.getByOrder(19).getColumnName());
		assertEquals("Properties interactor B", columnSet.getByOrder(20).getColumnName());

		assertEquals("Type(s) interactor A", columnSet.getByOrder(21).getColumnName());
		assertEquals("Type(s) interactor B", columnSet.getByOrder(22).getColumnName());

		assertEquals("HostOrganism(s)", columnSet.getByOrder(23).getColumnName());

		assertEquals("Expansion method(s)", columnSet.getByOrder(24).getColumnName());

        assertEquals("Dataset name(s)", columnSet.getByOrder(25).getColumnName());
    }
}
