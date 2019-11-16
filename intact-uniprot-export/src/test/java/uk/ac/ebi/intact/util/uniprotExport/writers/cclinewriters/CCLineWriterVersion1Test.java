package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverterVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/01/12</pre>
 */

public class CCLineWriterVersion1Test extends UniprotExportBase {

    @Test
    public void testExport(){
        List<CCParameters<SecondCCParametersVersion1>> parameters = createCCParametersVersion1();

        try {
            File testFile = new File("CcTest1.txt");
            FileWriter test = new FileWriter(testFile);
            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);

            writer.writeCCLines(parameters);

            writer.close();

            File template = new File(CCLineWriterVersion1Test.class.getResource("/cc_file_test1.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConversionAndExport(){
        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(3, createIsoformIsoformInteraction()); // with rules of 2019 an isoform-isoform can be converted
        interactions.add(4, createEncoreInteractionWithTransIsoform()); // can be converted but is written twice because another isoform of the entry already interacts with same protein
        interactions.add(5, createEncoreInteractionWithTransIsoformAndMaster()); // can be converted because isoform and other uniprot entry, even if this isoform is not matching the master

        List<EncoreInteraction> negativeInteractions = new ArrayList<EncoreInteraction>();
        negativeInteractions.add(interactions.get(5));
        interactions.remove(5);

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        CCParameters<SecondCCParametersVersion1> secondParameters = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactions),
                new HashSet<>(negativeInteractions),
                context,
                firstInteractor);

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>();
        parameters.add(secondParameters);

        try {
            File testFile = new File("CcTest2.txt");
            FileWriter test = new FileWriter(testFile);
            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);

            writer.writeCCLines(parameters);

            writer.close();

            File template = new File(CCLineWriterVersion1Test.class.getResource("/cc_file_test2.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
