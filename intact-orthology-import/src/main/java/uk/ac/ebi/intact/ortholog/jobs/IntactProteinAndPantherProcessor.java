package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;
import uk.ac.ebi.intact.ortholog.OrthologsProteinAssociation;
import java.util.Map;

@RequiredArgsConstructor
public class IntactProteinAndPantherProcessor implements ItemProcessor<IntactProtein, Map<IntactProtein, String>>, ItemStream {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
    private final String filePath;
    private Map<String, String> uniprotAndPanther;

    @Override
    public Map<IntactProtein, String> process(IntactProtein protein) throws Exception {
        return orthologsProteinAssociation.associateOneProteinToPantherID(uniprotAndPanther, protein);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            uniprotAndPanther = OrthologsFileParser.parseFile(filePath);
        } catch (Exception e) {
            throw new ItemStreamException("Error parsing the file: " + filePath, e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
    }
}