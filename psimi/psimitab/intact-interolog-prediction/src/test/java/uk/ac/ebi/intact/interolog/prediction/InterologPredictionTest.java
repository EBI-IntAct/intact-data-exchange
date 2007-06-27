/**
 * 
 */
package uk.ac.ebi.intact.interolog.prediction;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	 * Test method for {@link uk.ac.ebi.intact.interolog.prediction.InterologPrediction#run()}.
	 */
	@Test
	public final void testRun() {
		final String clog = "/test.clog.dat";
        URL urlClog = InterologPredictionTest.class.getResource( clog );
        assertNotNull( "Could not initialize test, file " + clog + " could not be found.", urlClog );
        File inputClog = new File(urlClog.getFile());
        
        final String mitab = "/test.mitab";
        URL urlMitab = InterologPredictionTest.class.getResource( mitab );
        assertNotNull( "Could not initialize test, file " + mitab + " could not be found.", urlMitab );
        File inputMitab = new File(urlMitab.getFile());
        
        InterologPrediction prediction = new InterologPrediction(new File("."));
        assertNotNull(prediction);
        prediction.setClog(inputClog);
        prediction.setMitab(inputMitab);
        
        Collection<Long> ids = new HashSet<Long>(1);
		ids.add(41l);
		prediction.setUserProteomeIdsToDownCast(ids);
		prediction.setPredictedinteractionsFileExtension(".mitab");
		prediction.setWriteDownCastHistory(false);
		prediction.setWriteClogInteractions(false);
		prediction.setDownCastOnChildren(false);
		try {
			prediction.run();
		} catch (InterologPredictionException e) {
			System.out.println(e);
		}
        
	}

}
