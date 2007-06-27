/**
 * 
 */
package uk.ac.ebi.intact.interolog.prediction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.interolog.mitab.MitabException;
import uk.ac.ebi.intact.interolog.mitab.MitabUtils;

/**
 * @author mmichaut
 * @version $Id$
 * @since 27 juin 07
 */
public class InterologPredictionTest {

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
	
	
	/**
	 * @param proteinAc
	 * @param taxid
	 * @return
	 */
	private Interactor buildInteractor(String proteinAc, Long taxid) {
		Collection<CrossReference> identifiers = new ArrayList<CrossReference>();
        identifiers.add( new CrossReference( InterologPrediction.getUNIPROTKB(), proteinAc ));
        Organism o = new Organism(taxid.intValue());
		Interactor interactor = new Interactor( identifiers );
		interactor.setOrganism(o);
		return interactor;
	}
	
	/**
	 * @param proteinAcA
	 * @param proteinAcB
	 * @param taxid
	 * @return
	 */
	private BinaryInteraction buildInteraction(String proteinAcA, String proteinAcB, Long taxid) {
		
		Interactor a = buildInteractor(proteinAcA, taxid);
		Interactor b = buildInteractor(proteinAcB, taxid);
		BinaryInteraction interaction = new BinaryInteraction(a, b);
		
		List <InteractionDetectionMethod> methods = new ArrayList<InteractionDetectionMethod>(1);
		InteractionDetectionMethod interologsMapping = new InteractionDetectionMethod();
		interologsMapping.setDatabase("MI");
		interologsMapping.setIdentifier("0064");
		interologsMapping.setText("interologs mapping");
		methods.add(interologsMapping);
		interaction.setDetectionMethods(methods);
		
		
		interaction.setInteractionAcs(new ArrayList<CrossReference>());
		interaction.setInteractionTypes(new ArrayList<InteractionType>());
		interaction.setPublications(new ArrayList<CrossReference>());//necessary otherwise interactions are different
		interaction.setSourceDatabases(new ArrayList<CrossReference>());
		interaction.setConfidenceValues(new ArrayList<Confidence>());
		
		
		return interaction;
	}

	/**
	 * Test method for {@link uk.ac.ebi.intact.interolog.prediction.InterologPrediction#run()}.
	 * @throws MitabException 
	 */
	@Test
	@Ignore
	public final void testRun() throws MitabException {
		
		// parameters
		final String clog = "/test.clog.dat";
        URL urlClog = InterologPredictionTest.class.getResource( clog );
        assertNotNull( "Could not initialize test, file " + clog + " could not be found.", urlClog );
        File inputClog = new File(urlClog.getFile());
        
        final String mitab = "/test.mitab";
        URL urlMitab = InterologPredictionTest.class.getResource( mitab );
        assertNotNull( "Could not initialize test, file " + mitab + " could not be found.", urlMitab );
        File inputMitab = new File(urlMitab.getFile());
        
        InterologPrediction prediction = new InterologPrediction(new File("./src/test/resources/"));
        assertNotNull(prediction);
        prediction.setClog(inputClog);
        prediction.setMitab(inputMitab);
        
        Collection<Long> ids = new HashSet<Long>(1);
		ids.add(1148l);
		prediction.setUserTaxidsToDownCast(ids);
		String extension = ".mitab";
		prediction.setPredictedinteractionsFileExtension(extension);
		String name = "clog.predictedInteractions";
		prediction.setPredictedinteractionsFileName(name);
		prediction.setDownCastOnAllPresentSpecies(false);
		prediction.setWriteDownCastHistory(false);
		prediction.setWriteClogInteractions(false);
		prediction.setDownCastOnChildren(false);
		
		// run prediction process
		try {
			prediction.run();
		} catch (InterologPredictionException e) {
			System.out.println(e);
		}
		
		// check results
		final String res = "/"+name+extension;
        URL url = InterologPredictionTest.class.getResource( res );
        assertNotNull( "Could not initialize test, file " + res + " could not be found.", url );
        File resFile = new File(url.getFile());
        
        Collection<BinaryInteraction> interactions = MitabUtils.readMiTab(resFile);
        assertNotNull(interactions);
		assertEquals(interactions.size(), 2);
		
		BinaryInteraction interaction1 = buildInteraction("P73479", "P73723", 1148l);
		assertNotNull(interaction1);
		assertTrue("interaction1 should be in the results: "+interaction1, interactions.contains(interaction1));
        
		BinaryInteraction interaction2 = buildInteraction("P73479", "Q55431", 1148l);
		assertNotNull(interaction2);
		assertTrue("interaction2 should be in the results: "+interaction2, interactions.contains(interaction2));
        
	}

}
