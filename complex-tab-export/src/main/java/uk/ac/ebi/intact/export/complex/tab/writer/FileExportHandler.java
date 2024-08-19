package uk.ac.ebi.intact.export.complex.tab.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class FileExportHandler {
    private static final Log log = LogFactory.getLog(FileExportHandler.class);

    private File dirFile;
    private Map<Integer, ExportWriter> curatedFileMap = new HashMap<>();
    private Map<Integer, ExportWriter> predictedFileMap = new HashMap<>();

    public FileExportHandler(File dirFile) throws IOException {
        log.info("Create report files in: " + dirFile.getPath());
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!dirFile.isDirectory()) {
            throw new IOException("The file passed to the constructor has to be a directory: " + dirFile);
        }

        //Copy README to target directory
        Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("readme.html")),
                new File(dirFile, "README.html").toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.dirFile = dirFile;
    }

    public ExportWriter createExportFile(int taxId, boolean predicted) throws IOException {
        String filename = String.valueOf(taxId);
        if (predicted) {
            filename += "_predicted";
        }

        getFileMap(predicted).put(taxId, new ComplexFlatWriter(new FileWriter(new File(dirFile, filename + ".tsv"))));
        return getFileMap(predicted).get(taxId);
    }

    public ExportWriter getExportFile(Integer taxId, boolean predicted) {
        return getFileMap(predicted).get(taxId);
    }

    private Map<Integer, ExportWriter> getFileMap(boolean predicted) {
        if (predicted) {
            return predictedFileMap;
        } else {
            return curatedFileMap;
        }
    }
}