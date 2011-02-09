package uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriterTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Tester of DRLine writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class DRLineWriterTest extends UniprotExportBase{

    @Test
    public void test_dr_export(){
        List<DRParameters> parameters = createDRParameters();

        try {
            File testFile = new File("DrTest.txt");
            FileWriter test = new FileWriter(testFile);
            DRLineWriter writer = new DRLineWriterImpl(test);

            writer.writeDRLines(parameters);

            writer.close();

            File template = new File(GOLineWriterTest.class.getResource("/dr_file_test.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
