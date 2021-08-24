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
    private Map<Integer, ExportWriter> fileMap = new HashMap<>();

    public FileExportHandler(File dirFile) throws IOException {
        log.info("Create report files in: " + dirFile.getPath());
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!dirFile.isDirectory()) {
            throw new IOException("The file passed to the constructor has to be a directory: " + dirFile);
        }

        //Copy README to target directory
        Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("readme.htm")),
                new File(dirFile, "README.htm").toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.dirFile = dirFile;
    }

    public ExportWriter createExportFile(int taxId) throws IOException {
        fileMap.put(taxId, new ComplexFlatWriter(new FileWriter(new File(dirFile, taxId + ".tsv"))));
        return fileMap.get(taxId);
    }

    public ExportWriter getExportFile(Integer taxId) {
        return fileMap.get(taxId);
    }
}