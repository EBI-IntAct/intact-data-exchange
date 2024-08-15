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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class IntactProteinAndPantherReader implements ItemReader<IntactProtein>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
//    private final String filePath;

    private Iterator<IntactProtein> proteinIterator;
//    private Iterator<Map.Entry<String, String>> uniprotAndPantherIterator;

    @Override
    public IntactProtein read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return proteinIterator.hasNext() ? proteinIterator.next() : null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
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