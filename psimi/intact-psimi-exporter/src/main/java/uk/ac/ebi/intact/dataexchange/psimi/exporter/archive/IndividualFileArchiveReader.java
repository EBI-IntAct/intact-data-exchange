package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.springframework.batch.item.*;
import org.springframework.util.Assert;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileUnit;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.NameTruncation;

import java.io.File;

/**
 * The IndividualFileArchiveReader is an ItemReader and ItemStream which reads a directory containing
 * files having a specific extension and returns one to several ArchiveFileUnit for all files matching a common prefix name.
 * This reader collect an ordered list of files having a specific extension and will return an ArchiveFileUnit when it could regroup several files starting with a common name
 * Some properties can be customized :
 * - the name of the directory where are the files.
 * - the fileNameTruncation which allows to extract the common name of the files to archive together
 * - the extensions of the files we want to extract together
 *
 * The files which will be compressed together are not deleted and the IndividualFileArchiveReader will read the files recursively in the initial directory
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/09/11</pre>
 */

public class IndividualFileArchiveReader implements ItemReader<FileUnit>, ItemStream {

    private File directory;
    private NameTruncation fileNameTruncation;
    private String[] extensions;

    // properties necessary for reading files

    private int currentFileNumber;
    private static String NUMBER_FILES = "number_files";

    private FileWithCommonAbstractPathIterator fileIterator;

    @Override
    public FileUnit read() throws Exception, UnexpectedInputException, ParseException {
        if (directory == null){
            throw new ParseException("You must open the reader before reading files.");
        }

        // we finished the job
        if (!fileIterator.hasNext()){
            return null;
        }

        FileUnit fileUnit = fileIterator.next();
        currentFileNumber++;

        while (fileUnit.getEntities().isEmpty() && fileIterator.hasNext()){
            fileUnit = fileIterator.next();
            currentFileNumber ++;
        }

        if (!fileUnit.getEntities().isEmpty()){
            // create and return the archive file name
            return fileUnit;
        }

        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        // we initialise the directory containing the xml files and do some checking
        if (extensions == null){
            throw new ItemStreamException("An array of file extensions is needed for the reader.");
        }

        if (fileNameTruncation == null){
            throw new ItemStreamException("A fileNameTruncation object is needed for the reader.");
        }

        if (directory == null){
            throw new ItemStreamException("A directory name is needed for the reader.");
        }

        if ( !directory.exists() ) {
            throw new ItemStreamException( "The directory : " + directory + " does not exist and is necessary for this task." );
        } else if (!directory.isDirectory()) {
            throw new ItemStreamException( directory + " is not a directory." );
        } else if (!directory.canRead()){
            throw new ItemStreamException( "Impossible to read files in the directory: " + directory );
        }

        // initialises the iterator
        this.fileIterator = new FileWithCommonAbstractPathIterator(directory, fileNameTruncation, extensions, true);

        // we initialize the number of processed files if restart
        if (executionContext.containsKey(NUMBER_FILES)) {
            this.currentFileNumber = executionContext.getInt(NUMBER_FILES);

            int index = 0;

            // skipp processed files
            if (currentFileNumber > 0){
                while (index < currentFileNumber - 1){
                    if (!fileIterator.hasNext()){
                        throw new ItemStreamException("The directory " + directory + " contained less XML files than the expected number. We cannot restart the reader.");
                    }
                    else {
                        fileIterator.next();
                        index ++;
                    }
                }
            }
        }
        else {
            this.currentFileNumber = 0;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        executionContext.putInt(NUMBER_FILES, currentFileNumber);
    }

    @Override
    public void close() throws ItemStreamException {
        // reset the variables
        this.currentFileNumber = 0;
        this.fileIterator = null;
        this.directory = null;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    public NameTruncation getFileNameTruncation() {
        return fileNameTruncation;
    }

    public void setFileNameTruncation(NameTruncation fileNameTruncation) {
        this.fileNameTruncation = fileNameTruncation;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }
}
