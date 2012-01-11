package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Tester of the GOLineWriter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class GOLineWriter1Test extends UniprotExportBase{

    @Test
    public void test_go_export(){
        List<GOParameters> parameters = createGOParameters();

        try {
            File testFile = new File("GoTest.txt");
            FileWriter test = new FileWriter(testFile);
            GOLineWriter writer = new GOLineWriter1(test);

            writer.writeGOLines(parameters);

            writer.close();

            File template = new File(GOLineWriter1Test.class.getResource("/go_file_test1.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
