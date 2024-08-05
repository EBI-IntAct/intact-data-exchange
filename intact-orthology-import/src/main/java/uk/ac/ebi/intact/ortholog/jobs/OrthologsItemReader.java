package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class OrthologsItemReader implements ItemReader<Map.Entry<IntactProtein, String>>, ItemStream {

    private final IntactDao intactDao;
    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final String filePath;

    private Iterator<Map.Entry<IntactProtein, String>> uniprotAndPantherIterator;

    @Override
    public Map.Entry<IntactProtein, String> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return uniprotAndPantherIterator.hasNext() ? uniprotAndPantherIterator.next() : null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Collection<IntactProtein> allProteins = intactDao.getProteinDao().getAll();
        Map<String, String> uniprotAndPanther = OrthologsFileParser.parseFile(filePath);
        Map<IntactProtein, String> uniprotAndPantherMap = orthologsProteinAssociation
                .associateAllProteinsToPantherId(uniprotAndPanther, allProteins);
        uniprotAndPantherIterator = uniprotAndPantherMap.entrySet().iterator();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}
