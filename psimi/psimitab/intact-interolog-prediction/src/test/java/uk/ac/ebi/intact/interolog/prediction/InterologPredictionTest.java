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

import org.junit.Test;
import org.junit.Assert;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionDetectionMethodImpl;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.OrganismImpl;
import uk.ac.ebi.intact.interolog.mitab.MitabException;
import uk.ac.ebi.intact.interolog.mitab.MitabUtils;

/**
 * @author mmichaut
 * @version $Id$
 * @since 27 juin 07
 */
public class InterologPredictionTest {

	/**
	 * @param proteinAc
	 * @param taxid
	 * @return
	 */
	private Interactor buildInteractor(String proteinAc, Long taxid) {
		Collection<CrossReference> identifiers = new ArrayList<CrossReference>();
        identifiers.add( new CrossReferenceImpl( InterologPrediction.getUNIPROTKB(), proteinAc ) );
        Organism o = new OrganismImpl(taxid.intValue());
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
		BinaryInteraction interaction = new BinaryInteractionImpl(a, b);
		
		List <InteractionDetectionMethod> methods = new ArrayList<InteractionDetectionMethod>(1);
		InteractionDetectionMethod interologsMapping = new InteractionDetectionMethodImpl();
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
	public final void testRunFormat1() throws MitabException {
		
		// parameters
		final String clog = "/test.clog.dat";
        URL urlClog = InterologPredictionTest.class.getResource( clog );
        assertNotNull( "Could not initialize test, file " + clog + " could not be found.", urlClog );
        File inputClog = new File(urlClog.getFile());
        
        final String mitab = "/test.mitab";
        URL urlMitab = InterologPredictionTest.class.getResource( mitab );
        assertNotNull( "Could not initialize test, file " + mitab + " could not be found.", urlMitab );
        File inputMitab = new File(urlMitab.getFile());

        File testFolder = new File("./target");
        InterologPrediction prediction = new InterologPrediction(testFolder);
        assertNotNull(prediction);
        prediction.setPorc(inputClog);
        prediction.setMitab(inputMitab);
        
        Collection<Long> ids = new HashSet<Long>(1);
		ids.add(1148l);
		prediction.setUserTaxidsToDownCast(ids);
		String extension = ".mitab";
		prediction.setMitabFileExtension(extension);
		String name = "clog.predictedInteractions";
		prediction.setPredictedInteractionsFileName(name);
		prediction.setClassicPorcFormat(false);
		prediction.setDownCastOnAllPresentSpecies(false);
		prediction.setWriteDownCastHistory(false);
		prediction.setWritePorcInteractions(false);
		prediction.setDownCastOnChildren(false);
		
		// run prediction process
		try {
			prediction.run();
		} catch (InterologPredictionException e) {
			Assert.fail(e.getMessage());
		}
		
		// check results
        File resFile = new File(testFolder, name+extension);
        assertTrue( "Could not initialize test, file " + resFile + " could not be found.", resFile.exists() );

        Collection<BinaryInteraction> interactions = MitabUtils.readMiTab(resFile);
        assertNotNull(interactions);
		assertEquals(2, interactions.size());
		
		/*BinaryInteraction interaction1 = buildInteraction("P73479", "P73723", 1148l); // pbm with interaction equals...I just check the protein ids
		assertNotNull(interaction1);
		assertTrue("interaction1 should be in the results: "+interaction1, interactions.contains(interaction1));
        
		BinaryInteraction interaction2 = buildInteraction("P73479", "Q55431", 1148l);
		assertNotNull(interaction2);
		assertTrue("interaction2 should be in the results: "+interaction2, interactions.contains(interaction2));*/
		
		final String AC1 = "P73479";
		final String AC2 = "P73723";
		final String AC3 = "Q55431";
		
		for (BinaryInteraction interaction : interactions) {
			String acA = MitabUtils.getUniprotAcA(interaction);
			String acB = MitabUtils.getUniprotAcB(interaction);
			
			boolean test = 	(acA.equals(AC1)&&acB.equals(AC2) || acA.equals(AC2)&&acB.equals(AC1) ) ||
							(acA.equals(AC1)&&acB.equals(AC3) || acA.equals(AC3)&&acB.equals(AC1) );
			assertTrue("this interaction was not supposed to be predicted", test);
		}
        
	}
	
	/**
	 * Test method for {@link uk.ac.ebi.intact.interolog.prediction.InterologPrediction#run()}.
	 * @throws MitabException 
	 */
	@Test
	public final void testRunFormat2() throws MitabException {
		
		// parameters
		final String clog = "/test.clog.protein.small.dat";
        URL urlClog = InterologPredictionTest.class.getResource( clog );
        assertNotNull( "Could not initialize test, file " + clog + " could not be found.", urlClog );
        File inputClog = new File(urlClog.getFile());
        
        final String mitab = "/test.mitab";
        URL urlMitab = InterologPredictionTest.class.getResource( mitab );
        assertNotNull( "Could not initialize test, file " + mitab + " could not be found.", urlMitab );
        File inputMitab = new File(urlMitab.getFile());

        File targetFolder = new File("./target/");
        InterologPrediction prediction = new InterologPrediction(targetFolder);
        assertNotNull(prediction);
        prediction.setPorc(inputClog);
        prediction.setMitab(inputMitab);
        
        Collection<Long> ids = new HashSet<Long>(1);
		ids.add(1148l);
		prediction.setUserTaxidsToDownCast(ids);
		String extension = ".mitab";
		prediction.setMitabFileExtension(extension);
		String name = "clog.predictedInteractions";
		prediction.setPredictedInteractionsFileName(name);
		prediction.setClassicPorcFormat(true);
		prediction.setDownCastOnAllPresentSpecies(false);
		prediction.setWriteDownCastHistory(false);
		prediction.setWritePorcInteractions(false);
		prediction.setDownCastOnChildren(false);
		
		// run prediction process
		try {
			prediction.run();
		} catch (InterologPredictionException e) {
			Assert.fail(e.getMessage());
		}
		
		// check results
        File resFile = new File(targetFolder, name+extension);
        assertTrue( "Could not initialize test, file " + resFile + " could not be found.", resFile.exists() );

        Collection<BinaryInteraction> interactions = MitabUtils.readMiTab(resFile);
        assertNotNull(interactions);
		assertEquals(2, interactions.size());
		
		/*BinaryInteraction interaction1 = buildInteraction("P73479", "P73723", 1148l);
		assertNotNull(interaction1);
		assertTrue("interaction1 should be in the results: "+interaction1, interactions.contains(interaction1));
        
		BinaryInteraction interaction2 = buildInteraction("P73479", "Q55431", 1148l);
		assertNotNull(interaction2);
		assertTrue("interaction2 should be in the results: "+interaction2, interactions.contains(interaction2));*/
		
		final String AC1 = "P73479";
		final String AC2 = "P73723";
		final String AC3 = "Q55431";
		
		for (BinaryInteraction interaction : interactions) {
			String acA = MitabUtils.getUniprotAcA(interaction);
			String acB = MitabUtils.getUniprotAcB(interaction);
			
			boolean test = 	(acA.equals(AC1)&&acB.equals(AC2) || acA.equals(AC2)&&acB.equals(AC1) ) ||
							(acA.equals(AC1)&&acB.equals(AC3) || acA.equals(AC3)&&acB.equals(AC1) );
			assertTrue("this interaction was not supposed to be predicted", test);
		}
        
	}
}