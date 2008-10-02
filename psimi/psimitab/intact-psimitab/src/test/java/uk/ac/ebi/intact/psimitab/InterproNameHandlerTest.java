package uk.ac.ebi.intact.psimitab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

/**
 * InterproNameHandler Tester.
 *
 * @author Nadin Neuhauser
 * @version 1.0
 * @since <pre>12/19/2007</pre>
 */
public class InterproNameHandlerTest extends AbstractPsimitabTestCase {

    @Test
    public void usingLocalEntryFile() throws Exception {
        File file = getFileByResources("/META-INF/interpro-entry-local.txt", InterproNameHandler.class );
        InterproNameHandler handler = new InterproNameHandler( new FileInputStream(file) );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ) );
    }

    @Test
    public void usingURL() throws Exception {
        URL url = new URL( "ftp://ftp.ebi.ac.uk/pub/databases/interpro/entry.list" );
        InterproNameHandler handler = new InterproNameHandler( url );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ) );
    }

    @Test
    public void usingStream() throws Exception {
        URL url = new URL( "ftp://ftp.ebi.ac.uk/pub/databases/interpro/entry.list" );
        InterproNameHandler handler = new InterproNameHandler( url.openStream() );
        String interproName = handler.getNameById( "IPR008255" );

        assertNotNull( interproName );
        assertEquals( "Pyridine nucleotide-disulphide oxidoreductase, class-II, active site", interproName );
        assertEquals( null, handler.getNameById( "Active_site" ) );
    }
}