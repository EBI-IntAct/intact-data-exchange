package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;
import uk.ac.ebi.intact.ortholog.model.ProteinAndPantherGroup;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class IntactProteinAndPantherReader implements ItemReader<ProteinAndPantherGroup>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final String uncompressedPantherFilePath;
    private final String proteinPantherPairFilePath;

    private Iterator<IntactProtein> proteinIterator;

    @Override
    public ProteinAndPantherGroup read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        while (proteinIterator.hasNext()) {
            IntactProtein protein = proteinIterator.next();
            Collection<String> pantherIds = OrthologsProteinAssociation
                    .associateOneProteinToPantherIds(proteinPantherPairFilePath, protein);
            if (!pantherIds.isEmpty()) {
                return new ProteinAndPantherGroup(protein, pantherIds);
            }
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            OrthologsFileParser.parseFileAndSave(uncompressedPantherFilePath, proteinPantherPairFilePath);
        } catch (IOException e) {
            throw new ItemStreamException("Error parsing the file: " + uncompressedPantherFilePath, e);
        }

        Collection<IntactProtein> allProteins = orthologsProteinAssociation.getIntactProtein();
//        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath);
//        uniprotAndPantherIterator = uniprotAndPanther.entrySet().iterator();
        proteinIterator = allProteins.iterator();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}