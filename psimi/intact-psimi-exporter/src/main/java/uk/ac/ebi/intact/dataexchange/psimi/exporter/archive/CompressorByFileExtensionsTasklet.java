package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The CompressorByFileExtensionsTasklet is a compressor tasklet which reads a directory containing
 * files having specific extension and compresses all the files having the specific extension(s) together. The CompressorByFileExtensionsTasklet
 * will read all the files recursively in the directory.
 * Some properties can be customized :
 * - the name of the directory where are the files to compress.
 * - the outputFileName which will be the name of the archive file
 * - the extensions which lists all the extensions of the files we want to compress together
 *
 * By default, the files which will be compressed together are not deleted and the compressor is a zip compressor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/10/11</pre>
 */

public class CompressorByFileExtensionsTasklet extends CompressorTasklet {

    // customized variables
    private String directoryName;
    private String[] extensions;

    private File directory;

    public CompressorByFileExtensionsTasklet() {

        // initialises variables by default
        super.setDeleteSourceFiles(false);
        super.setCompressor(new Compressor());
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        if (directory == null){
            initializeVariables();
        }

        if (super.getOutputFile() == null) {
            throw new IllegalArgumentException("An output file is needed");
        }

        if (extensions == null){
            throw new IllegalArgumentException("the extensions of the file to compress is mandatory");
        }

        List<File> files = new ArrayList<File>(FileUtils.listFiles(directory, extensions, true));
        Collections.sort(files);

        getCompressor().compress(getOutputFile(), files, false);

        return RepeatStatus.FINISHED;
    }

    private void initializeVariables() throws ItemStreamException {

        if (directory == null){
            throw new NullPointerException("A directory name is needed for the reader.");
        }

        if ( !directory.exists() ) {
            throw new ItemStreamException( "The directory : " + directory + " does not exist and is necessary for this task." );
        }
        else if (!directory.isDirectory()){
            throw new ItemStreamException( directoryName + " is not a directory." );
        }
        else if (!directory.canRead()){
            throw new ItemStreamException( "Impossible to read files in the directory : " + directoryName );
        }
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }
}
