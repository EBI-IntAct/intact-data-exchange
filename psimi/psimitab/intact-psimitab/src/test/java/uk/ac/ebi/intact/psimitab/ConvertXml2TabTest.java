package uk.ac.ebi.intact.psimitab;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import psidev.psi.mi.tab.converter.xml2tab.TabConvertionException;
import psidev.psi.mi.tab.expansion.SpokeExpansion;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * ConvertXml2Tab Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/04/2007</pre>
 */
public class ConvertXml2TabTest extends TestCase {

    public static final String SLASH = File.separator;
    public static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

    public ConvertXml2TabTest( String name ) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite( ConvertXml2TabTest.class );
    }

    ////////////////////
    // Tests

    public void testSetGetInteractorPairCluctering() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setInteractorPairClustering( false );
        assertFalse( converter.isInteractorPairClustering() );

        converter.setInteractorPairClustering( true );
        assertTrue( converter.isInteractorPairClustering() );
    }

    public void testSetGetExpansionStrategy() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        assertNull( converter.getExpansionStragegy() );

        SpokeExpansion spoke = new SpokeExpansion();
        converter.setExpansionStrategy( spoke );

        assertEquals( spoke, converter.getExpansionStragegy() );
    }

    public void testSetGetOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        File file = new File( "" );
        converter.setOutputFile( file );
        assertEquals( file, converter.getOutputFile() );
    }

    public void testSetGetXmlFilesToConvert() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        Collection<File> files = new ArrayList<File>();
        files.add( new File( "a" ) );
        files.add( new File( "b" ) );
        converter.setXmlFilesToConvert( files );
        assertEquals( files, converter.getXmlFilesToConvert() );
    }

    public void testSetOverwriteOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setOverwriteOutputFile( false );
        assertFalse( converter.isOverwriteOutputFile() );

        converter.setOverwriteOutputFile( true );
        assertTrue( converter.isOverwriteOutputFile() );
    }

    public void testOverwriteOutputCheck() throws IOException {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setOverwriteOutputFile( false );
        File file = new File( TMP_DIR + SLASH + "testOutputPsimitab.csv" );
        file.createNewFile();
        file.deleteOnExit();

        converter.setOutputFile( file );
        converter.setOverwriteOutputFile( false );

        Collection<File> files = new ArrayList<File>();
        files.add( new File( "a.xml" ) );
        files.add( new File( "b.xml" ) );

        converter.setXmlFilesToConvert( files );

        try {
            converter.convert();
            fail( "Overwriting of output should have been prevented." );
        } catch ( RuntimeException rte ) {
            // ok
        } catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
    }

    public void testConvert() throws IOException, ConverterException, TabConvertionException {

        File intputDir = new File( ConvertXml2TabTest.class.getResource( "/xml-samples" ).getFile() );

        ConvertXml2Tab converter = new ConvertXml2Tab();

        // collect input files and directories
        Collection<File> inputFiles = new ArrayList<File>();
        inputFiles.add( intputDir );

        // configure the converter
        converter.setXmlFilesToConvert( inputFiles );

        File file = new File( "target/xml-samples.xls" );

        converter.setOutputFile( file );
        converter.setOverwriteOutputFile( true );

        converter.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        converter.setInteractorPairClustering( true );

        File logFile = new File( intputDir, "mitab.log" );
        Writer logWriter = new BufferedWriter( new FileWriter( logFile ) );
        converter.setLogWriter( logWriter );

        // run the conversion
        converter.convert();

        logWriter.flush();
        logWriter.close();

        assertTrue( file.exists() );
        
        assertTrue( logFile.exists() );

        // empty as other files yielded data, so the converter doesn't output.
        assertTrue( logFile.length() == 0 );

        // count the lines, we expect 4 of'em
        try {
            BufferedReader in = new BufferedReader( new FileReader( file ) );
            int count = 0;

            String line = in.readLine(); // skip the header.
            while ( ( line = in.readLine() ) != null ) {
                // process line here
                count++;
            }
            in.close();

            assertEquals( 4, count );

        } catch ( IOException e ) {
            fail();
        }
    }

    public void testConvert2() throws ConverterException, IOException, TabConvertionException {
        File file = new File( ConvertXml2TabTest.class.getResource( "/xml-samples/11230133.xml" ).getFile() );
        ConvertXml2Tab x2t = new ConvertXml2Tab();
        x2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        x2t.setInteractorPairClustering( true );
        x2t.setOverwriteOutputFile( true );

        File logFile = new File( file.getParentFile(), "11230133.log" );
        Writer logWriter = new BufferedWriter( new FileWriter( logFile ) );
        x2t.setLogWriter( logWriter );

        Collection<File> inputFiles = new ArrayList<File>();
        inputFiles.add( file );
        x2t.setXmlFilesToConvert( inputFiles );

        x2t.setOutputFile( new File( file.getAbsolutePath() + ".xls" ) );

        x2t.convert();

        logWriter.flush();
        logWriter.close();

        assertTrue( logFile.exists() );
        assertTrue( logFile.length() > 0 );
    }
}
