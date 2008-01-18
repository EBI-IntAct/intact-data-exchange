package uk.ac.ebi.intact.psimitab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import uk.ac.ebi.intact.psimitab.exception.NameNotFoundException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * InterproNameHandler Tester.
 *
 * @author Nadin Neuhauser
 * @since <pre>12/19/2007</pre>
 * @version 1.0
 */
public class InterproNameHandlerTest  extends AbstractPsimitabTestCase {

    @Test
    public void usingLocalEntryFile() throws UnsupportedEncodingException, NameNotFoundException {
        File file = getFileByResources( "/interpro-entry-local.txt", InterproNameHandler.class );
        InterproNameHandler handler = new InterproNameHandler( file );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ));
    }

    @Test
    public void usingURL() throws UnsupportedEncodingException, NameNotFoundException, MalformedURLException {
        URL url = new URL( "ftp://ftp.ebi.ac.uk/pub/databases/interpro/entry.list" );
        InterproNameHandler handler = new InterproNameHandler( url );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ));
    }

    @Test
    public void usingStream() throws IOException, NameNotFoundException {
        URL url = new URL( "ftp://ftp.ebi.ac.uk/pub/databases/interpro/entry.list" );
        InterproNameHandler handler = new InterproNameHandler( url.openStream() );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ));
    }

}
