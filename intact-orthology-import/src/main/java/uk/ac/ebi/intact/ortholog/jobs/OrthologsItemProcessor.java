package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsXrefWriter;
import java.util.Map;

@RequiredArgsConstructor
public class OrthologsItemProcessor implements ItemProcessor<Map.Entry<IntactProtein, String>, IntactProtein>, ItemStream {

    private final OrthologsXrefWriter orthologsXrefWriter;

    @Override
    public IntactProtein process(Map.Entry<IntactProtein, String> item) throws Exception {
        IntactProtein protein = item.getKey();
        String pantherId = item.getValue();
        orthologsXrefWriter.addOrthologyXref(protein, pantherId);
        return protein;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }
}