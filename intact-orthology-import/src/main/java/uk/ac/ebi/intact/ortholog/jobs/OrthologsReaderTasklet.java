package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.repeat.RepeatStatus;
import uk.ac.ebi.intact.ortholog.OrthologsFileReader;
import uk.ac.ebi.intact.ortholog.OrthologsFileParser;

import java.io.IOException;

@RequiredArgsConstructor
public class OrthologsReaderTasklet implements Tasklet {

    private final String urlPanther;
    private final String filePath;
    private final String proteinPantherPairDirPath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext ) throws Exception {
        OrthologsFileReader.decompressGzip(urlPanther, filePath);
        try {
            OrthologsFileParser.parseFileAndSave(filePath, proteinPantherPairDirPath);
        } catch (IOException e) {
            throw new ItemStreamException("Error parsing the file: " + filePath, e);
        }
        return RepeatStatus.FINISHED;
    }
}
