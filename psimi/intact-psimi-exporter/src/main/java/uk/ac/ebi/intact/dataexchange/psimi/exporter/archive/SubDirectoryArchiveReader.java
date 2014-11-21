package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.springframework.batch.item.*;
import org.springframework.util.Assert;

import java.io.File;

/**
 * The SubDirectoryArchiveReader is an ItemReader and ItemStream which reads a directory containing
 * a set of subDirectories to compress and returns one ArchiveFileUnit per subDirectory.
 * This reader collect a list of subdirectories and will return an ArchiveFileUnit for each subdirectory it could read and the name of each archive will be the name of the subDirectory.
 * Some properties can be customized :
 * - the name of the directory containing all the sub directories to compress.
 *
 * The SubDirectoryArchiveReader will not read the files recursively in the initial directory.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/09/11</pre>
 */

public class SubDirectoryArchiveReader implements ItemReader<ReleaseUnit>, ItemStream {

    private File directory;

    // properties for reading the files
    private int currentFileNumber;
    private static String NUMBER_FILES = "number_files";

    private SubDirectoryIterator currentFileIterator;

    @Override
    public ReleaseUnit read() throws Exception, UnexpectedInputException, ParseException {

        if (directory == null){
            throw new ParseException("You must open the reader before reading files.");
        }

        // we finished the job
        if (!this.currentFileIterator.hasNext()){
            return null;
        }

        return currentFileIterator.next();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (directory == null){
            throw new NullPointerException("A directory name is needed for the reader.");
        }

        if ( !directory.exists() ) {
            throw new ItemStreamException( "The directory : " + directory + " does not exist and is necessary for this task." );
        }
        else if (!directory.isDirectory()){
            throw new ItemStreamException( directory + " is not a directory." );
        }
        else if (!directory.canRead()){
            throw new ItemStreamException( "Impossible to read files in the directory : " + directory);
        }

        // collect the files
        this.currentFileIterator = new SubDirectoryIterator(directory, false);

        // we initialize the number of processed files if restart
        if (executionContext.containsKey(NUMBER_FILES)) {
            this.currentFileNumber = executionContext.getInt(NUMBER_FILES);

            int index = 0;

            // skipp processed files
            if (currentFileNumber > 0){
                while (index < currentFileNumber){
                    if (!currentFileIterator.hasNext()){
                        throw new ItemStreamException("The directory " + directory + " contained less XML files than the expected number. We cannot restart the reader.");
                    }
                    else {
                        currentFileIterator.next();
                        index ++;
                    }
                }
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        executionContext.putInt(NUMBER_FILES, currentFileNumber);
    }

    @Override
    public void close() throws ItemStreamException {

        // close the variables
        this.currentFileIterator = null;
        this.currentFileNumber = 0;
        this.directory = null;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }
}
