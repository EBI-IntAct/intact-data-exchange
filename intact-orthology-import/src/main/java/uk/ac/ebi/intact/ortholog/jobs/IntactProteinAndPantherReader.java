package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;
import uk.ac.ebi.intact.ortholog.model.ProteinAndPantherGroup;
import java.util.*;

@Log4j
@RequiredArgsConstructor
public class IntactProteinAndPantherReader implements ItemReader<ProteinAndPantherGroup>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final String proteinPantherPairDirPath;
    private Iterator<IntactProtein> proteinIterator;

    @Override
    public ProteinAndPantherGroup read() throws Exception{
        while (proteinIterator.hasNext()) {
            IntactProtein protein = proteinIterator.next();
            Collection<String> pantherIds = OrthologsProteinAssociation
                    .associateOneProteinToPantherIds(proteinPantherPairDirPath, protein);
            if (!pantherIds.isEmpty()) {
                return new ProteinAndPantherGroup(protein, pantherIds);
            }
        }
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
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