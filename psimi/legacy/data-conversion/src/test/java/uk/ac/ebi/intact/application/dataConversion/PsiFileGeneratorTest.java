/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.CvMapping;
import uk.ac.ebi.intact.application.dataConversion.util.DisplayXML;

import java.io.*;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09-Aug-2006</pre>
 */
public class PsiFileGeneratorTest extends DataConversionAbstractTest {

    private static final Log log = LogFactory.getLog( PsiFileGeneratorTest.class );


    public void testGenerateListMahajan() throws Exception {

        File reverseMappingFile = new File( PsiFileGeneratorTest.class.getResource( "/reverseMapping.txt" ).getFile() );

        CvMapping mapping = new CvMapping();
        mapping.loadFile( reverseMappingFile );


        ExperimentListGenerator gen = new ExperimentListGenerator( "mahajan-2000-1" );
        //gen.setLargeScaleChunkSize(150);

        List<ExperimentListItem> eliSpecies = gen.generateClassificationBySpecies();

        ExperimentListItem eli = eliSpecies.get( 0 );
        log.info( "Experiment List Item: " + eli );

        Document doc = PsiFileGenerator.generatePsiData( eliSpecies.get( 0 ), PsiVersion.getVersion1(), mapping );

        Writer writer = new StringWriter();
        DisplayXML.write( doc, writer, "   " );

        String xmlDoc = writer.toString();
        //assertEquals(59420, xmlDoc.length());

        assertNotNull( xmlDoc );

        final File target = new File( PsiFileGeneratorTest.class.getResource( "/" ).getFile() ).getParentFile();

        File outFile10 = new File( target, "mahajan-2000-1.mi10.xml" );
        System.out.println( "Exporting PSI-MI XML 2.5 content to: " + outFile10.getAbsolutePath() );
        BufferedWriter out = new BufferedWriter( new FileWriter( outFile10 ) );
        out.write( xmlDoc );
        out.close();

        // TODO check the xml output

        writer.close();

        // 2.5
        doc = PsiFileGenerator.generatePsiData( eliSpecies.get( 0 ), PsiVersion.getVersion25(), mapping );

        writer = new StringWriter();
        DisplayXML.write( doc, writer, "   " );

        xmlDoc = writer.toString();
        System.out.println( xmlDoc.length() );
        //assertEquals(128563, xmlDoc.length());

        assertNotNull( xmlDoc );

        // TODO check the xml output
        writer.close();

        File outFile25 = new File( target, "mahajan-2000-1.mi25.xml" );
        System.out.println( "Exporting PSI-MI XML 2.5 content to: " + outFile25.getAbsolutePath() );
        BufferedWriter out2 = new BufferedWriter( new FileWriter( outFile25  ) );
        out2.write( xmlDoc );
        out2.close();
    }
    /*
    public void testGenerateXmlFiles_Psi25() throws Exception
    {
        File reverseMappingFile = new File(PsiFileGeneratorTest.class.getResource("/reverseMapping.txt").getFile());

        CvMapping mapping = new CvMapping();
        mapping.loadFile(reverseMappingFile);

        ExperimentListGenerator gen = new ExperimentListGenerator("ab%");

        List<ExperimentListItem> allItems = gen.generateAllClassifications();

        for (ExperimentListItem item : allItems)
        {
            PsiFileGenerator.writePsiData(item, PsiVersion.VERSION_25, mapping, new File("target/psi25"), true);
        }

        // check if the files exist and are not empty
        for (ExperimentListItem item : allItems)
        {
            File xmlFile = new File("target/psi25", item.getFilename());

            assertTrue(xmlFile.exists());
            assertTrue(xmlFile.length() > 0);
        }
    }


    public void testGenerateXmlFilesWithSmallMolecule_Psi1() throws Exception
    {
        File reverseMappingFile = new File(PsiFileGeneratorTest.class.getResource("/reverseMapping.txt").getFile());

        CvMapping mapping = new CvMapping();
        mapping.loadFile(reverseMappingFile);

        ExperimentListGenerator gen = new ExperimentListGenerator("gonzalez-2003-1");

        List<ExperimentListItem> allItems = gen.generateAllClassifications();

        for (ExperimentListItem item : allItems)
        {
            File xmlFile = new File("target/psi1", item.getFilename());
            if (xmlFile.exists()) xmlFile.delete();

            PsiFileGenerator.writePsiData(item, PsiVersion.VERSION_1, mapping, new File("target/psi1"), false);
        }

        // check if the files exist and are not empty
        for (ExperimentListItem item : allItems)
        {
            File xmlFile = new File("target/psi1", item.getFilename());

            assertFalse(xmlFile.exists());
        }
    }

    public void testGenerateXmlFileWithSmallMolecule_FromInteractions() throws Exception
    {
        File reverseMappingFile = new File(PsiFileGeneratorTest.class.getResource("/reverseMapping.txt").getFile());

        CvMapping mapping = new CvMapping();
        mapping.loadFile(reverseMappingFile);

        ExperimentListGenerator gen = new ExperimentListGenerator("tang-1997a-2");

        List<ExperimentListItem> allItems = gen.generateAllClassifications();

        for (ExperimentListItem item : allItems)
        {
            Collection<Interaction> interactions = PsiFileGenerator.getInteractionsForExperimentListItem(item);

            File xml1 = new File("target"+ File.separator +"psi1", item.getFilename());
            if (xml1.exists()) {
                xml1.delete(); // delete if it exists already
            }
            PsiFileGenerator.writePsiData(interactions, PsiVersion.VERSION_1, mapping, xml1, true);
            assertTrue( xml1.exists() );

            PsiValidatorReport report25 = PsiFileGenerator.writePsiData(interactions,
                                                                        PsiVersion.VERSION_25,
                                                                        mapping,
                                                                        new File("target"+File.separator+"psi25"+File.separator+"test-file.xml", item.getFilename()),
                                                                        true);
            assertTrue( report25.isValid() );
        }
    }

    public void testGenerateXmlFile_WithSelfInteractions() throws Exception
    {
        File reverseMappingFile = new File(PsiFileGeneratorTest.class.getResource("/reverseMapping.txt").getFile());

        CvMapping mapping = new CvMapping();
        mapping.loadFile(reverseMappingFile);

        ExperimentListGenerator gen = new ExperimentListGenerator("tian-2006-1");

        List<ExperimentListItem> allItems = gen.generateAllClassifications();

        for (ExperimentListItem item : allItems)
        {
            Collection<Interaction> interactions = PsiFileGenerator.getInteractionsForExperimentListItem(item);

            File xml1 = new File("target/psi1", item.getFilename());
            if (xml1.exists()) xml1.delete(); // delete if it exists already
            PsiFileGenerator.writePsiData(interactions, PsiVersion.VERSION_1, mapping, xml1, true);
            assertFalse(xml1.exists());

            PsiValidatorReport report25 = PsiFileGenerator.writePsiData(interactions, PsiVersion.VERSION_25, mapping, new File("target/psi25/test-file2.xml", item.getFilename()), true);
            assertTrue(report25.isValid());
        }
    }
    */
}
