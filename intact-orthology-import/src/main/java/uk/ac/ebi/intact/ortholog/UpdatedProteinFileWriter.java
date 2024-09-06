package uk.ac.ebi.intact.ortholog;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class UpdatedProteinFileWriter {
    public static void updatedProteinWriter(String toWrite){
        try {
            FileWriter fileWriter = new FileWriter("UpdatedProteins.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(toWrite);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}

