package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;

import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class OrthologsItemReader implements ItemReader<Map.Entry<String, String>>, ItemStream {

    private final String filePath;

    private Map<String, String> uniprotAndPanther;

    private Iterator<Map.Entry<String, String>> uniprotAndPantherIterator;

    @Override
    public Map.Entry<String, String> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return uniprotAndPantherIterator.hasNext() ? uniprotAndPantherIterator.next() : null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        uniprotAndPanther = OrthologsFileParser.parseFile(filePath);
        uniprotAndPantherIterator = uniprotAndPanther.entrySet().iterator();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}
