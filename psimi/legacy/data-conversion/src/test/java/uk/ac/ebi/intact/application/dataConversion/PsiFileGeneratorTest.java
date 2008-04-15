/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
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
@Ignore
public class PsiFileGeneratorTest extends DataConversionAbstractTest {

    private static final Log log = LogFactory.getLog( PsiFileGeneratorTest.class );


    @Test
    public void generateListMahajan() throws Exception {

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

        Assert.assertNotNull( xmlDoc );

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

        Assert.assertNotNull( xmlDoc );

        // TODO check the xml output
        writer.close();

        File outFile25 = new File( target, "mahajan-2000-1.mi25.xml" );
        System.out.println( "Exporting PSI-MI XML 2.5 content to: " + outFile25.getAbsolutePath() );
        BufferedWriter out2 = new BufferedWriter( new FileWriter( outFile25  ) );
        out2.write( xmlDoc );
        out2.close();
    }

}
