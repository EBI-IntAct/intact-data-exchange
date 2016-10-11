package uk.ac.ebi.intact.export.mutation;

import uk.ac.ebi.intact.export.mutation.helper.MutationExportDao;
import uk.ac.ebi.intact.export.mutation.helper.Consumer;
import uk.ac.ebi.intact.export.mutation.writer.FileExportHandler;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.tools.feature.shortlabel.generator.ShortlabelGenerator;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class MutationExportConfig {
    private FileExportHandler fileExportHandler;
    private IntactDao intactDao;
    private ShortlabelGenerator shortlabelGenerator;
    private MutationExportDao mutationExportDao;
    private Consumer consumer;


    public IntactDao getIntactDao() {
        return intactDao;
    }

    public void setIntactDao(IntactDao intactDao) {
        this.intactDao = intactDao;
    }

    public ShortlabelGenerator getShortlabelGenerator() {
        return shortlabelGenerator;
    }

    public void setShortlabelGenerator(ShortlabelGenerator shortlabelGenerator) {
        this.shortlabelGenerator = shortlabelGenerator;
    }

    public FileExportHandler getFileExportHandler() {
        return fileExportHandler;
    }

    public void setFileExportHandler(FileExportHandler fileExportHandler) {
        this.fileExportHandler = fileExportHandler;
    }

    public MutationExportDao getMutationExportDao() {
        return mutationExportDao;
    }

    public void setMutationExportDao(MutationExportDao mutationExportDao) {
        this.mutationExportDao = mutationExportDao;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
}
