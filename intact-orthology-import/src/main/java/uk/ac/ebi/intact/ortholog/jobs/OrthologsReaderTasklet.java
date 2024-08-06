package uk.ac.ebi.intact.ortholog.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import uk.ac.ebi.intact.ortholog.OrthologsFileReader;

@RequiredArgsConstructor
public class OrthologsReaderTasklet implements Tasklet {

    private final String urlPanther;
    private final String filePath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        OrthologsFileReader.decompressGzip(urlPanther, filePath);
        return RepeatStatus.FINISHED;
    }
}
