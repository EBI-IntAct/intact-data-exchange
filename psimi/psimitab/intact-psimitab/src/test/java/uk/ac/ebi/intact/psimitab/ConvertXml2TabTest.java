package uk.ac.ebi.intact.psimitab;

import org.junit.Assert;
import static org.junit.Assert.*;
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
        assertFalse( converter.isInteractorPairClustering() );

        converter.setInteractorPairClustering( true );
        assertTrue( converter.isInteractorPairClustering() );
    }

    @Test
    public void setGetExpansionStrategy() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        assertNull( converter.getExpansionStragegy() );

        SpokeExpansion spoke = new SpokeExpansion();
        converter.setExpansionStrategy( spoke );

        assertEquals( spoke, converter.getExpansionStragegy() );
    }

    @Test
    public void setGetOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        File file = new File( "" );
        converter.setOutputFile( file );
        assertEquals( file, converter.getOutputFile() );
    }

    @Test
    public void setGetXmlFilesToConvert() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();
        Collection<File> files = new ArrayList<File>();
        files.add( new File( "a" ) );
        files.add( new File( "b" ) );
        converter.setXmlFilesToConvert( files );
        assertEquals( files, converter.getXmlFilesToConvert() );
    }

    @Test
    public void setOverwriteOutputFile() throws Exception {
        ConvertXml2Tab converter = new ConvertXml2Tab();

        converter.setOverwriteOutputFile( false );
        assertFalse( converter.isOverwriteOutputFile() );

        converter.setOverwriteOutputFile( true );
        assertTrue( converter.isOverwriteOutputFile() );
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

        File intputDir = getFileByResources("/xml-samples", ConvertXml2TabTest.class);

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

        File logFile = new File( intputDir, "mitab.logger" );
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
            Assert.fail();
        }
    }

    @Test
    public void convert2() throws Exception {
        File file = getFileByResources("/xml-samples/11230133.xml", ConvertXml2TabTest.class);

        ConvertXml2Tab x2t = new ConvertXml2Tab();
        x2t.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        x2t.setInteractorPairClustering( true );
        x2t.setOverwriteOutputFile( true );

        File logFile = new File( file.getParentFile(), "11230133.logger" );
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

    @Test
    public void convert3() throws Exception {

        File intputDir = getFileByResources("/xml-samples", ConvertXml2TabTest.class);

        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setBinaryInteractionClass( IntactBinaryInteraction.class );
        converter.setColumnHandler( new IntactColumnHandler(true, true) );
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

        File logFile = new File( intputDir, "mitab.logger" );
        Writer logWriter = new BufferedWriter( new FileWriter( logFile ) );
        converter.setLogWriter( logWriter );

        // run the conversion
        converter.convert();

        logWriter.flush();
        logWriter.close();

        assertTrue( outputFile.exists() );

        assertTrue( logFile.exists() );

        // empty as other files yielded data, so the converter doesn't output.
        assertTrue( logFile.length() == 0 );

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

            assertEquals( 4, count );

        } catch ( IOException e ) {
            Assert.fail();
        }
    }

    @Test
    public void convert4() throws Exception {

        File file = getFileByResources("/psi25-testset/9971739.xml", ConvertXml2TabTest.class);
        Assert.assertNotNull( file );

        ConvertXml2Tab converter = new ConvertXml2Tab();
        converter.setBinaryInteractionClass( IntactBinaryInteraction.class );
        converter.setColumnHandler( new IntactColumnHandler(true, true) );
        converter.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        converter.setInteractorPairClustering( true );
        converter.setOverwriteOutputFile( true );

        File logFile = new File( file.getParentFile(), "9971739.logger" );
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

        assertTrue( outputFile.exists() );
        assertTrue( outputFile.length() > 0 );
        assertTrue( logFile.exists() );
        assertTrue( logFile.length() == 0 );
    }

    @Test
    public void expansionMethod_nary_interaction_2() throws Exception {

        File xmlFile = getFileByResources("/psi25-testset/single-nary-interaction.xml", ConvertXml2TabTest.class);
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntactBinaryInteraction.class );

        // this column handler IS aware of the the expansion strategy
        final IntactColumnHandler columnHandler = new IntactColumnHandler();
        assertTrue( columnHandler instanceof IsExpansionStrategyAware );
        xml2tab.setColumnHandler( columnHandler );

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) interactions.iterator().next();
        Assert.assertNotNull( interaction.getExpansionMethod() );
        assertEquals( SpokeExpansion.EXPANSION_NAME, interaction.getExpansionMethod() );
    }

    @Test
    public void expansionMethod_binary_interaction() throws Exception {

        File xmlFile = getFileByResources("/psi25-testset/single-interaction.xml", ConvertXml2TabTest.class);
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntactBinaryInteraction.class );
        xml2tab.setColumnHandler( new IntactColumnHandler() );
        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) interactions.iterator().next();
        assertEquals( null, interaction.getExpansionMethod() );
    }

    @Test
    public void datasetName () throws Exception {

        File xmlFile = getFileByResources( "/psi25-testset/17292829.xml", ConvertXml2TabTest.class);
        assertTrue( xmlFile.canRead() );

        // convert into Tab object model
        Xml2Tab xml2tab = new Xml2Tab();

        xml2tab.setBinaryInteractionClass( IntactBinaryInteraction.class );

        // this column handler IS aware of the the expansion strategy
        final IntactColumnHandler columnHandler = new IntactColumnHandler();
        assertTrue( columnHandler instanceof IsExpansionStrategyAware );
        xml2tab.setColumnHandler( columnHandler );

        xml2tab.setExpansionStrategy( new SpokeWithoutBaitExpansion() );
        xml2tab.addOverrideSourceDatabase( CrossReferenceFactory.getInstance().build( "MI", "0469", "intact" ) );
        xml2tab.setPostProcessor( new ClusterInteractorPairProcessor() );

        Collection<BinaryInteraction> interactions = xml2tab.convert( xmlFile, false );

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) interactions.iterator().next();
        Assert.assertNotNull( interaction.getDataset() );
        assertTrue( interaction.getDataset().get( 0 ).startsWith( "Cancer" ));
    }
}