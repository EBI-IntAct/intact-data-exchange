package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/01/12</pre>
 */

public class GOLineWriterVersion2Test extends UniprotExportBase{

    @Test
    public void test_go_export(){
        List<GOParameters> parameters = createGOParametersVersion2();

        try {
            File testFile = new File("GoTest2.txt");
            FileWriter test = new FileWriter(testFile);
            GOLineWriter writer = new GOLineWriter2(test);

            writer.writeGOLines(parameters);

            writer.close();

            File template = getTemplateFileWithCurrentDate();

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();
            template.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getTemplateFileWithCurrentDate() throws IOException {
        File template = new File(GOLineWriterVersion1Test.class.getResource("/go_file_test2.txt").getFile());
        File templateWithCurrentDate = new File("go_file_test2_temp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(template));
        BufferedWriter writer = new BufferedWriter(new FileWriter(templateWithCurrentDate));

        Format dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String line = reader.readLine();
        while (line != null) {
            writer.write(line.replaceAll("<CURRENT_DATE>", dateFormat.format(new Date())));
            writer.newLine();
            line = reader.readLine();
        }

        reader.close();
        writer.close();

        return templateWithCurrentDate;
    }
}
