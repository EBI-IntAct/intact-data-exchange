package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.service.InteractorService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class IntactProteinAndPantherWriter implements ItemWriter<Map<IntactProtein, String>>, ItemStream {

    private final InteractorService interactorService;
    private BufferedWriter bufferedWriter;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            FileWriter fileWriter = new FileWriter("proteinAndPantherBatches2.txt", true);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            throw new ItemStreamException("Error opening file for writing", e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Error closing file writer", e);
        }
    }

    @Override
    public void write(List<? extends Map<IntactProtein, String>> items) throws Exception {
        try {
            for (Map<IntactProtein, String> item : items) {
                for (Map.Entry<IntactProtein, String> entry : item.entrySet()) {
                    String protein = entry.getKey().getUniprotkb();
                    String pantherIndex = entry.getValue();
                    bufferedWriter.write(protein + "," + pantherIndex);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new ItemStreamException("Error writing to file", e);
        }
    }
}
