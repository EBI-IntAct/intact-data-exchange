package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsXrefWriter;
import uk.ac.ebi.intact.ortholog.model.ProteinAndPantherGroup;

@RequiredArgsConstructor
public class IntactProteinAndPantherProcessor implements ItemProcessor<ProteinAndPantherGroup, IntactProtein>, ItemStream {

    private final OrthologsXrefWriter orthologsXrefWriter;

    @Override
    public IntactProtein process(ProteinAndPantherGroup proteinAndPantherGroup) throws Exception {
        orthologsXrefWriter.addOrthologyXrefs(proteinAndPantherGroup.getProtein(), proteinAndPantherGroup.getPantherIds());
        return proteinAndPantherGroup.getProtein();
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