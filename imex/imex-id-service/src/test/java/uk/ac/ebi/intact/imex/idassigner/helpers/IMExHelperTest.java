package uk.ac.ebi.intact.imex.idassigner.helpers;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.intact.imex.idservice.helpers.IMExHelper;

/**
 * IMExHelper Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class IMExHelperTest {

    ////////////////////////////////
    // Compatibility with JUnit 3

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter( IMExHelperTest.class );
    }

    //////////////////////////
    // Initialisation

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    ////////////////////
    // Tests  

    @Test
    public void getKeyAssignerUrl() throws Exception {
        assertEquals( "http://www.google.co.uk", IMExHelper.getKeyAssignerUrl() );
    }

    @Test
    public void getKeyAssignerUsername() throws Exception {
        assertEquals( "user", IMExHelper.getKeyAssignerUsername() );
    }

    @Test
    public void getKeyAssignerPassword() throws Exception {
        assertEquals( "password", IMExHelper.getKeyAssignerPassword() );
    }

    @Test
    public void getKeyStoreFilename() throws Exception {
        assertEquals( "C:\\imex.kst", IMExHelper.getKeyStoreFilename() );
    }
}
