package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.service.InteractorService;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class ProteinCollectionWriter implements ItemWriter<Collection<IntactProtein>>, ItemStream {

    private final InteractorService interactorService;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }

    @Override
    public void write(List<? extends Collection<IntactProtein>> items) throws Exception {
        for (Collection<IntactProtein> proteins : items) {
            interactorService.saveOrUpdate(proteins);
        }
    }
}
