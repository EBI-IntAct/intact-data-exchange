package uk.ac.ebi.intact.psimitab;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.UnsupportedEncodingException;

import uk.ac.ebi.intact.psimitab.exception.NameNotFoundException;

/**
 * InterproNameHandler Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>12/19/2007</pre>
 * @version 1.0
 */
public class InterproNameHandlerTest  extends AbstractPsimitabTestCase {

    @Test
    public void getNameByIdTest() throws UnsupportedEncodingException, NameNotFoundException {
        File file = getFileByResources( "/interpro-entry-local.txt", InterproNameHandler.class );
        InterproNameHandler handler = new InterproNameHandler(file);
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ));
    }

}
