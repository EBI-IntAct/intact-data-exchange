package uk.ac.ebi.intact.export.mutation.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class FileExportHandler {
    private static final Log log = LogFactory.getLog(FileExportHandler.class);

    private ExportWriter exportWriter;


    public FileExportHandler(File dirFile) throws IOException {
        log.info("Create report files in: " + dirFile.getPath());
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!dirFile.isDirectory()) {
            throw new IOException("The file passed to the constructor has to be a directory: " + dirFile);
        }
        this.exportWriter = new MutationExportWriter(new FileWriter(new File(dirFile, "mutation_export.tsv")));
    }

    public ExportWriter getExportWriter() {
        return exportWriter;
    }
}
