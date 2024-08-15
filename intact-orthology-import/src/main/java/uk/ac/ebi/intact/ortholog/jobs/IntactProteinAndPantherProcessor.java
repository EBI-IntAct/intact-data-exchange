package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;
import uk.ac.ebi.intact.ortholog.OrthologsXrefWriter;

import java.io.IOException;

@RequiredArgsConstructor
public class IntactProteinAndPantherProcessor implements ItemProcessor<IntactProtein, IntactProtein>, ItemStream {

    private final OrthologsXrefWriter orthologsXrefWriter;
    private final String uncompressedPantherFilePath;
    private final String proteinPantherPairFilePath;

    @Override
    public IntactProtein process(IntactProtein protein) throws Exception {
        String pantherId = OrthologsProteinAssociation
                .associateOneProteinToPantherId(proteinPantherPairFilePath, protein);
        orthologsXrefWriter.addOrthologyXref(protein, pantherId);
        return protein;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            OrthologsFileParser.parseFileAndSave(uncompressedPantherFilePath, proteinPantherPairFilePath);
        } catch (IOException e) {
            throw new ItemStreamException("Error parsing the file: " + uncompressedPantherFilePath, e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
    }
}