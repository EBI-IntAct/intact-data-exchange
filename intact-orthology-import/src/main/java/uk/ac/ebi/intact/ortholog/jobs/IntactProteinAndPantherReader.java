package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
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
import uk.ac.ebi.intact.ortholog.UpdatedProteinFileParser;

import java.io.IOException;
import java.util.*;

@Log4j
@RequiredArgsConstructor
public class IntactProteinAndPantherReader implements ItemReader<ProteinAndPantherGroup>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final String uncompressedPantherFilePath;
    private final String proteinPantherPairDirPath;
    private final UpdatedProteinFileParser updatedProteinFileParser;

    private Iterator<IntactProtein> proteinIterator;
    private Collection<String> alreadyUpdatedProteins;

    @Override
    public ProteinAndPantherGroup read() throws Exception{
        while (proteinIterator.hasNext()) {
            IntactProtein protein = proteinIterator.next();
            Collection<String> pantherIds = OrthologsProteinAssociation
                    .associateOneProteinToPantherIds(proteinPantherPairDirPath, protein);
//            if (!pantherIds.isEmpty() && !alreadyUpdatedProteins.contains(protein.getUniprotkb())) {
            if (!pantherIds.isEmpty()) {
                return new ProteinAndPantherGroup(protein, pantherIds);
            }
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
//        alreadyUpdatedProteins = UpdatedProteinFileParser.parseFile();
//        try {
//            OrthologsFileParser.parseFileAndSave(uncompressedPantherFilePath, proteinPantherPairDirPath);
//        } catch (IOException e) {
//            throw new ItemStreamException("Error parsing the file: " + uncompressedPantherFilePath, e);
//        }

        int startAc = executionContext.getInt("startAc");
        int endAc = executionContext.getInt("endAc");
        List<IntactProtein> allProteins = orthologsProteinAssociation.fetchProteins(startAc, endAc);
        log.info("Reading " + allProteins.size() + " proteins");

        proteinIterator = allProteins.iterator();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}