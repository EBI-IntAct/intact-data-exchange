package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;

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

public class CCLineWriter1Test extends UniprotExportBase{

    @Test
    public void test_cc_export(){
        List<CCParameters> parameters = createCCParameters1();

        try {
            File testFile = new File("CcTest1.txt");
            FileWriter test = new FileWriter(testFile);
            CCLineWriter writer = new CCLineWriter1(test);

            writer.writeCCLines(parameters);

            writer.close();

            File template = new File(CCLineWriter2Test.class.getResource("/cc_file_test1.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
