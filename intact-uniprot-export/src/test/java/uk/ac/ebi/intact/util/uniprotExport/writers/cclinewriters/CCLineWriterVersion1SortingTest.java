package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/01/12</pre>
 */

public class CCLineWriterVersion1SortingTest extends UniprotExportBase {

    @Test
    public void testExport(){
        List<CCParameters<SecondCCParametersVersion1>> parameters = createCCParametersVersion1ForSorting();

        try {
            File testFile = new File("cc_file_sorting_test.txt");
            FileWriter test = new FileWriter(testFile);
            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);

            writer.writeCCLines(parameters);

            writer.close();

            File template = new File(CCLineWriterVersion1SortingTest.class.getResource("/cc_file_sorting_test.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
