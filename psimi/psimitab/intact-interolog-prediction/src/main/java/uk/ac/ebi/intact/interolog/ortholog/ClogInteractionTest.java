/**
 * 
 */
package uk.ac.ebi.intact.interolog.ortholog.clog;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;

/**
 * @author mmichaut
 * @version $Id$
 * @since 25 juin 07
 */
public class ClogInteractionTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

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
	
	public Clog buildClog1() {
		Clog clog = new Clog(1);
		
		return clog;
	}
	
	public Clog buildClog2() {
		Clog clog = new Clog(2);
		
		return clog;
	}
	
	public Interactor buildInteractor(String id) {
		Collection<CrossReference> identifiers = new ArrayList<CrossReference>();
        identifiers.add( new CrossReference( "uniprotkb", id ));
        return new Interactor( identifiers );
	}
	
	public BinaryInteraction buildInteractionAB() {
		BinaryInteraction interaction = new BinaryInteraction(buildInteractor("A"), buildInteractor("B"));
		return interaction;
	}
	
	public BinaryInteraction buildInteractionCD() {
		BinaryInteraction interaction = new BinaryInteraction(buildInteractor("C"), buildInteractor("D"));
		return interaction;
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.interolog.ortholog.clog.ClogInteraction#ClogInteraction(uk.ac.ebi.intact.interolog.ortholog.clog.Clog, uk.ac.ebi.intact.interolog.ortholog.clog.Clog)}.
	 */
	@Test
	public final void testClogInteraction() {
		ClogInteraction interaction = new ClogInteraction(buildClog1(), buildClog2());
		assertNotNull(interaction);
		assertEquals(interaction.getClogA(), buildClog1());
		assertEquals(interaction.getClogB(), buildClog2());
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		ClogInteraction interaction1 = new ClogInteraction(buildClog1(), buildClog2());
		ClogInteraction interaction2 = new ClogInteraction(buildClog1(), buildClog2());
		assertEquals(interaction1, interaction2);
		interaction1.getSourceInteractions().add(buildInteractionAB());
		interaction2.getSourceInteractions().add(buildInteractionCD());
		assertEquals(interaction1, interaction2);
		
		Map<ClogInteraction, ClogInteraction> map = new HashMap<ClogInteraction, ClogInteraction>();
		map.put(interaction1, interaction1);
		assertEquals(map.containsKey(interaction1), true);
		assertEquals(map.containsKey(interaction2), true);
	}

}
