package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1Impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/01/12</pre>
 */

public class CCLineWriterVersion1SameGeneSortingInteract2Test extends UniprotExportBase {

    public List<CCParameters<SecondCCParametersVersion1>> createCCParametersVersion1SortingSameGene(){

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>(3);

        String uniprotAc1 = "Q8VEK0";
        String uniprotAc2 = "P70704";
        String uniprotAc3 = "P05067-PRO_0000000091";
        String uniprotAc4 = "P05067-4";

        String intactAc1 = "EBI-8381028";
        String intactAc2 = "EBI-20828407";
        String intactAc3 = "EBI-3894543";
        String intactAc4 = "EBI-302641";

        String geneName1 = "Atp8a1";
        String geneName2 = "APP";

        String taxId = "10090";
        String taxId2 = "9606";

        SecondCCParametersVersion1 secondParameters1 = new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc2, intactAc2, taxId, geneName1, 2);
        SecondCCParametersVersion1 secondParameters2 = new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc3, intactAc3, taxId2, geneName2, 3);
        SecondCCParametersVersion1 secondParameters3 = new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc4, intactAc4, taxId2, geneName2, 6);

        SortedSet<SecondCCParametersVersion1> listOfSecondInteractors1 = new TreeSet<SecondCCParametersVersion1>();
        listOfSecondInteractors1.add(secondParameters1);
        listOfSecondInteractors1.add(secondParameters2);
        listOfSecondInteractors1.add(secondParameters3);

        CCParameters<SecondCCParametersVersion1> parameters1 = new CCParametersVersion1(uniprotAc1, geneName1, taxId, listOfSecondInteractors1);
        parameters.add(parameters1);

        return parameters;
    }

    @Test
    public void testExport(){
        List<CCParameters<SecondCCParametersVersion1>> parameters = createCCParametersVersion1SortingSameGene();

        try {
            File testFile = new File("cc_line_same_gene_sorting_interactant2.txt");
            FileWriter test = new FileWriter(testFile);
            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);

            writer.writeCCLines(parameters);

            writer.close();

            File template = new File(CCLineWriterVersion1SameGeneSortingInteract2Test.class.getResource("/cc_line_same_gene_sorting_interactant2.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
