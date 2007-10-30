package uk.ac.ebi.intact.psimitab;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.expansion.SpokeExpansion;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;

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
public class ConvertXml2TabTest extends AbstractPsimitabTestCase {

    public static final String SLASH = File.separator;
    public static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

    @Test
    public void testSetGetInteractorPairCluctering() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setInteractorPairClustering( false );
        Assert.assertFalse( converter.isInteractorPairClustering() );

        converter.setInteractorPairClustering( true );
        Assert.assertTrue( converter.isInteractorPairClustering() );
    }

    @Test
    public void testSetGetExpansionStrategy() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        Assert.assertNull( converter.getExpansionStragegy() );

        SpokeExpansion spoke = new SpokeExpansion();
        converter.setExpansionStrategy( spoke );

        Assert.assertEquals( spoke, converter.getExpansionStragegy() );
    }

    @Test
    public void testSetGetOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        File file = new File( "" );
        converter.setOutputFile( file );
        Assert.assertEquals( file, converter.getOutputFile() );
    }

    @Test
    public void testSetGetXmlFilesToConvert() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        Collection<File> files = new ArrayList<File>();
        files.add( new File( "a" ) );
        files.add( new File( "b" ) );
        converter.setXmlFilesToConvert( files );
        Assert.assertEquals( files, converter.getXmlFilesToConvert() );
    }

    @Test
    public void testSetOverwriteOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setOverwriteOutputFile( false );
        Assert.assertFalse( converter.isOverwriteOutputFile() );

        converter.setOverwriteOutputFile( true );
        Assert.assertTrue( converter.isOverwriteOutputFile() );
    }

    @Test
    public void testOverwriteOutputCheck() throws Exception {
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
            Assert.fail( "Overwriting of output should have been prevented." );
        } catch ( RuntimeException rte ) {
            // ok
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testConvert() throws Exception {

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

        Assert.assertTrue( file.exists() );
        
        Assert.assertTrue( logFile.exists() );

        // empty as other files yielded data, so the converter doesn't output.
        Assert.assertTrue( logFile.length() == 0 );

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

            Assert.assertEquals( 4, count );

        } catch ( IOException e ) {
            Assert.fail();
        }
    }

    @Test
    public void testConvert2() throws Exception {
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

        Assert.assertTrue( logFile.exists() );
        Assert.assertTrue( logFile.length() > 0 );
    }

    @Test
    public void testConvert3() throws Exception {

        File intputDir = new File( ConvertXml2TabTest.class.getResource( "/xml-samples" ).getFile() );

        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setBinaryInteractionClass( IntActBinaryInteraction.class );
        converter.setColumnHandler( new IntActColumnHandler() );
        converter.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        converter.setInteractorPairClustering( true );
        converter.setOverwriteOutputFile( true );
        
        File outputFile = new File(getTargetDirectory(), "test.txt");
        outputFile.createNewFile();
        converter.setOutputFile(outputFile);

        // collect input files and directories
        Collection<File> inputFiles = new ArrayList<File>();
        inputFiles.add( intputDir );

        // configure the converter
        converter.setXmlFilesToConvert( inputFiles );

        File logFile = new File( intputDir, "mitab.log" );
        Writer logWriter = new BufferedWriter( new FileWriter( logFile ) );
        converter.setLogWriter( logWriter );
  
        // run the conversion
        converter.convert();

        logWriter.flush();
        logWriter.close();

        Assert.assertTrue( outputFile.exists() );
        
        Assert.assertTrue( logFile.exists() );

        // empty as other files yielded data, so the converter doesn't output.
        Assert.assertTrue( logFile.length() == 0 );

        // count the lines, we expect 4 of'em
        try {
            BufferedReader in = new BufferedReader( new FileReader( outputFile ) );
            int count = 0;

            String line = in.readLine(); // skip the header.
            
            while ( ( line = in.readLine() ) != null ) {
                // process line here
                count++;
            }
            in.close();

            Assert.assertEquals( 4, count );

        } catch ( IOException e ) {
            Assert.fail();
        }
    }
}