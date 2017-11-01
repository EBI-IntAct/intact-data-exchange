package uk.ac.ebi.intact.export.complex.tab.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        new FileWriter(new File(this.dirFile, this.getClass().getClassLoader().getResource("readme.htm").getFile()));
        this.dirFile = dirFile;
    }

    public ExportWriter createExportFile(int taxId, String scientificName) throws IOException {
        fileMap.put(taxId, new ComplexFlatWriter(new FileWriter(new File(dirFile, transformToFileName(scientificName) + ".tsv"))));
        return fileMap.get(taxId);
    }

    private String transformToFileName(String scientificName) {
        String [] array = scientificName.split("\\s+");
        if(array.length == 1){
            return array[0].toLowerCase();
        } else {
            return array[0].toLowerCase() + '_' + array[1].toLowerCase();
        }
    }

    public File getDirFile() {
        return dirFile;
    }

    public Map<Integer, ExportWriter> getFileMap() {
        return fileMap;
    }

    public ExportWriter getExportFile(Integer taxId) {
        return fileMap.get(taxId);
    }
}
