package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * The ArchiveFileWriter will write a list of ArchiveFileUnit.
 * Only ArchiveFileUnits having an archiveName and a non empty list of files will be written, the others are skipped.
 * This objects contains some properties that we can customize :
 * - a compressor compressor which will archive the files of an ArchiveFileUnit
 * - an errorLogName which is the name of the file where the errors are logged
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/09/11</pre>
 */

public class ArchiveFileWriter implements ItemWriter<FileUnit>, ItemStream {
    private final String DEFAULT_LOG_FILE = "archive_file_error.log";
    private String errorLogName = DEFAULT_LOG_FILE;

    private FileWriter logWriter;
    private Compressor compressor;

    public ArchiveFileWriter(){
        // by default, zip compressor
        this.compressor = new Compressor();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            // we append messages to the logWriter
            logWriter = new FileWriter(errorLogName, true);
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot create file where to log the archive compression errors: " + errorLogName, e );
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        try {
            if (logWriter != null){
                logWriter.flush();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot flush file where to log the archive compressor errors: " + errorLogName, e );
        }
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (logWriter != null){
                logWriter.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot close file where to log the archive compressor errors: " + errorLogName, e );
        }
    }

    @Override
    public void write(List<? extends FileUnit> items) throws Exception {

        for (FileUnit unit : items){

            if (!unit.getEntities().isEmpty() && unit.getUnitName() != null){
                final File outputFile = new File(unit.getUnitName() + "." + compressor.getCompression());
                compressor.compress(outputFile, unit.getEntities(), false);
            }
            else {
                logWriter.write("Cannot write the archive fileUnit " + unit.toString() + " because does not have a proper name or a list of files to archive");
                logWriter.write("\n");
                logWriter.flush();
            }
        }
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public void setCompressor(Compressor compressor) {
        this.compressor = compressor;
    }

    public String getErrorLogName() {
        return errorLogName;
    }

    public void setErrorLogName(String errorLogName) {
        this.errorLogName = errorLogName;
    }
}
