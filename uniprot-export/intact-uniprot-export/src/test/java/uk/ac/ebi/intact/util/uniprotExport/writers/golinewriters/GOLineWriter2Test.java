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
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/01/12</pre>
 */

public class GOLineWriter2Test extends UniprotExportBase{

    @Test
    public void test_go_export(){
        List<GOParameters> parameters = createGOParameters2();

        try {
            File testFile = new File("GoTest2.txt");
            FileWriter test = new FileWriter(testFile);
            GOLineWriter writer = new GOLineWriter2(test);

            writer.writeGOLines(parameters);

            writer.close();

            File template = new File(GOLineWriter1Test.class.getResource("/go_file_test2.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
