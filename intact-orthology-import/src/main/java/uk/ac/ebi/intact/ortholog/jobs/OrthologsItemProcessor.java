package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;
import uk.ac.ebi.intact.ortholog.OrthologsXrefWriter;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class OrthologsItemProcessor implements ItemProcessor<Map.Entry<String, String>, Collection<IntactProtein>>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final OrthologsXrefWriter orthologsXrefWriter;

    @Override
    public Collection<IntactProtein> process(Map.Entry<String, String> item) throws Exception {
        String proteinId = item.getKey();
        String pantherId = item.getValue();
        Collection<IntactProtein> proteins = orthologsProteinAssociation.getSpecificIntactProtein(proteinId);
        for (IntactProtein protein : proteins) {
            orthologsXrefWriter.addOrthologyXref(protein, pantherId);
        }
        return proteins;
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
