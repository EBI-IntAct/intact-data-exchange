package uk.ac.ebi.intact.psimitab;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.converter.xml2tab.IsExpansionStrategyAware;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeExpansion;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;

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
    public void setGetInteractorPairCluctering() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setInteractorPairClustering( false );
        Assert.assertFalse( converter.isInteractorPairClustering() );

        converter.setInteractorPairClustering( true );
        Assert.assertTrue( converter.isInteractorPairClustering() );
    }

    @Test
    public void setGetExpansionStrategy() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        Assert.assertNull( converter.getExpansionStragegy() );

        SpokeExpansion spoke = new SpokeExpansion();
        converter.setExpansionStrategy( spoke );

        Assert.assertEquals( spoke, converter.getExpansionStragegy() );
    }

    @Test
    public void setGetOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        File file = new File( "" );
        converter.setOutputFile( file );
        Assert.assertEquals( file, converter.getOutputFile() );
    }

    @Test
    public void setGetXmlFilesToConvert() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        Collection<File> files = new ArrayList<File>();
        files.add( new File( "a" ) );
        files.add( new File( "b" ) );
        converter.setXmlFilesToConvert( files );
        Assert.assertEquals( files, converter.getXmlFilesToConvert() );
    }

    @Test
    public void setOverwriteOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setOverwriteOutputFile( false );
        Assert.assertFalse( converter.isOverwriteOutputFile() );

        converter.setOverwriteOutputFile( true );
        Assert.assertTrue( converter.isOverwriteOutputFile() );
    }

    @Test
    public void overwriteOutputCheck() throws Exception {
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
    public void convert() throws Exception {

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
    public void convert2() throws Exception {
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
    public void convert3() throws Exception {

        File intputDir = new File( ConvertXml2TabTest.class.getResource( "/xml-samples" ).getFile() );

        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setBinaryInteractionClass( IntActBinaryInteraction.class );
        converter.setColumnHandler( new IntActColumnHandler() );
        converter.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        converter.setInteractorPairClustering( true );
        converter.setOverwriteOutputFile( true );

        File outputFile = new File( getTargetDirectory(), "test.txt" );
        outputFile.createNewFile();
        converter.setOutputFile( outputFile );

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

    @Test
    public void convert4() throws Exception {
        File file = new File( ConvertXml2TabTest.class.getResource( "/psi25-testset/9971739.xml" ).getFile() );
        Assert.assertNotNull( file );

        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setBinaryInteractionClass( IntActBinaryInteraction.class );
        converter.setColumnHandler( new IntActColumnHandler() );
        converter.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        converter.setInteractorPairClustering( true );
        converter.setOverwriteOutputFile( true );

        File logFile = new File( file.getParentFile(), "9971739.log" );
        Writer logWriter = new BufferedWriter( new FileWriter( logFile ) );
        converter.setLogWriter( logWriter );

        Collection<File> inputFiles = new ArrayList<File>();
        inputFiles.add( file );
        converter.setXmlFilesToConvert( inputFiles );

        final File outputFile = new File( file.getAbsolutePath() + ".txt" );
        converter.setOutputFile( outputFile );
        converter.convert();

        logWriter.flush();
        logWriter.close();

        Assert.assertTrue( outputFile.exists() );
        System.out.println( outputFile.getAbsolutePath() );
        Assert.assertTrue( outputFile.length() > 0 );
        Assert.assertTrue( logFile.exists() );
        Assert.assertTrue( logFile.length() == 0 );
    }

    @Test
    public void expansionMethod_nary_interaction_2() throws Exception {

        File xmlFile = new File( ConvertXml2TabTest.class.getResource( "/psi25-testset/single-nary-interaction.xml" ).getFile() );
        Assert.assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntActBinaryInteraction.class );

        // this column handler IS aware of the the expansion strategy
        final IntActColumnHandler columnHandler = new IntActColumnHandler();
        Assert.assertTrue( columnHandler instanceof IsExpansionStrategyAware );
        xml2tab.setColumnHandler( columnHandler );

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        IntActBinaryInteraction interaction = ( IntActBinaryInteraction ) interactions.iterator().next();
        Assert.assertNotNull( interaction.getExpansionMethod() );
        Assert.assertEquals( SpokeExpansion.EXPANSION_NAME, interaction.getExpansionMethod() );
    }

    @Test
    public void expansionMethod_binary_interaction() throws Exception {

        File xmlFile = new File( ConvertXml2TabTest.class.getResource( "/psi25-testset/single-interaction.xml" ).getFile() );
        Assert.assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntActBinaryInteraction.class );
        xml2tab.setColumnHandler( new IntActColumnHandler() );
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        IntActBinaryInteraction interaction = ( IntActBinaryInteraction ) interactions.iterator().next();
        Assert.assertEquals( null, interaction.getExpansionMethod() );
    }
}