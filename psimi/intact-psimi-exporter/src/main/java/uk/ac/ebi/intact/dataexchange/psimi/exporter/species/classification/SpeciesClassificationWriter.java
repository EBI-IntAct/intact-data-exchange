package uk.ac.ebi.intact.dataexchange.psimi.exporter.species.classification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.util.Assert;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * This SpeciesClassificationWriter is an ItemStream and ItemWriter which will write a list of PublicationSpeciesUnit.
 * There will be one file per species, and each line will represents a PublicationSpeciesUnit.
 * The format of the line is : year/publicationId + taxid separator + taxid + taxid separator + number of interactions.
 *
 * Some properties can be customized :
 * - speciesParentFolderName is the folder name where to write the species index files. If it does not exist, the writer will create it
 * - extension : the extension of the species file index
 * - taxidSeparator : the taxidSeparator
 * - fileName generator (initialized by default)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class SpeciesClassificationWriter implements ItemWriter<PublicationSpeciesUnit>, ItemStream {
    private String speciesParentFolderName;
    private String extension = "txt";
    private FileNameGenerator fileNameGenerator;

    private String taxidSeparator=":";

    private File speciesParentFolder;

    private long currentPosition;
    private String currentFileName;
    private Writer outputBufferedWriter;
    private FileChannel fileChannel;
    private FileOutputStream os;

    private final static String CURRENT_FILE = "current_file";
    private final static String CURRENT_POSITION = "current_position";

    private File currentFile;
    private boolean isBufferInitialized = false;

    private static final Log log = LogFactory.getLog(SpeciesClassificationWriter.class);

    public SpeciesClassificationWriter(){
        fileNameGenerator = new FileNameGenerator();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (speciesParentFolderName == null){
            throw new ItemStreamException("A parent folder is needed for the writer");
        }

        speciesParentFolder = new File(speciesParentFolderName);

        if ( !speciesParentFolder.exists() ) {
            if ( !speciesParentFolder.mkdirs() ) {
                throw new ItemStreamException( "The species parent folder : " + speciesParentFolder.getAbsolutePath() + " does not exist, cannot be created and is necessary for this task." );
            }
        }
        else if (!speciesParentFolder.canWrite()){
            throw new ItemStreamException( "Impossible to write in : " + speciesParentFolder.getAbsolutePath() );
        }

        // we initialize the bufferWriter
        if (executionContext.containsKey(CURRENT_FILE) && executionContext.containsKey(CURRENT_POSITION)) {
            this.currentFileName = executionContext.getString(CURRENT_FILE);
            this.currentFile = new File(speciesParentFolder, currentFileName);
            this.currentPosition = executionContext.getLong(CURRENT_POSITION);
        }
        // throw a ItemStream exception if the bufferWriter cannot be initialized properly
        else if ((executionContext.containsKey(CURRENT_FILE) && !executionContext.containsKey(CURRENT_POSITION)) || (!executionContext.containsKey(CURRENT_FILE) && executionContext.containsKey(CURRENT_POSITION))) {
            throw new ItemStreamException( "Impossible to open the writer, important information for restart is missing (need both current file name and file position)");
        }
        else{
            this.currentFile = null;
            this.currentFileName = null;
            this.currentPosition = 0;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        try {
            currentPosition = position();
            executionContext.putLong(CURRENT_POSITION, currentPosition);
            executionContext.putString(CURRENT_FILE, currentFileName);

        } catch (IOException e) {
            throw new ItemStreamException( "Impossible to get the last position of the writer" );
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (outputBufferedWriter != null) {
            try {
                outputBufferedWriter.close();
            } catch (IOException e) {
                throw new ItemStreamException( "Impossible to close " + currentFileName, e );

            }
        }

        try {
            if (fileChannel != null) {
                fileChannel.close();
            }
        }
        catch (IOException ioe) {
            throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
        }
        finally {
            try {
                if (os != null) {
                    os.close();
                }
            }
            catch (IOException ioe) {
                throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
            }
        }

        this.currentFile = null;
        this.currentFileName = null;
        this.currentPosition = 0;
        this.speciesParentFolder = null;
        this.fileChannel = null;
        this.os = null;
        this.isBufferInitialized = false;
    }

    @Override
    public void write(List<? extends PublicationSpeciesUnit> publicationSpeciesUnits) throws Exception {
        if (speciesParentFolder == null){
            throw new WriteFailedException("You must open the writer before writing files.");
        }

        StringBuffer buffer = new StringBuffer();

        for (PublicationSpeciesUnit publicationSpecies : publicationSpeciesUnits){
            String species = fileNameGenerator.replaceBadCharactersFor(publicationSpecies.getSpecies()) + "." + extension;
            String publicationId = publicationSpecies.getPublicationId();
            String year = Integer.toString(publicationSpecies.getYear());
            int taxid = publicationSpecies.getTaxid();
            int numberInteractions = publicationSpecies.getNumberInteractions();

            log.info("Write species " + species + ", publication " + publicationId );

            // we start from the beginning
            if (currentFileName == null){
                currentFileName = species;
                currentFile = new File(speciesParentFolder, species);

                initializeBufferedWriter(currentFile, false, true);

                writePublicationSpeciesUnit(buffer, publicationId, year, taxid, numberInteractions, false);

            }
            // we are writing within same species
            else if (species.equals(currentFileName)){

                // we restarted the job
                if (!isBufferInitialized){
                    initializeBufferedWriter(currentFile, true, false);
                }
                writePublicationSpeciesUnit(buffer, publicationId, year, taxid, numberInteractions, true);
            }
            // we are starting a new species. We must flush the current writer and reset a new one
            else {
                // flush previous buffer
                outputBufferedWriter.write(buffer.toString());
                outputBufferedWriter.flush();
                outputBufferedWriter.close();
                fileChannel.close();
                os.close();

                // starts a new buffer
                currentFileName = species;
                currentFile = new File(speciesParentFolder, species);
                initializeBufferedWriter(currentFile, false, true);

                buffer = new StringBuffer();
                writePublicationSpeciesUnit(buffer, publicationId, year, taxid, numberInteractions, false);
            }
        }

        outputBufferedWriter.write(buffer.toString());
        outputBufferedWriter.flush();
    }

    private void writePublicationSpeciesUnit(StringBuffer buffer, String publicationId, String year,
                                             int taxid, int numberInteractions, boolean isRestarted) {
        // we append to the buffer
        if (buffer.length() > 0 || isRestarted){
            buffer.append("\n");
        }
        buffer.append(year);
        buffer.append(File.separator);
        buffer.append(publicationId);
        buffer.append(taxidSeparator+taxid);
        buffer.append(taxidSeparator + numberInteractions);
    }

    /**
     * Creates the buffered writer for the output file channel based on
     * configuration information.
     * @throws java.io.IOException
     */
    private void initializeBufferedWriter(File file, boolean restarted, boolean shouldDeleteIfExists) throws IOException {

        FileUtils.setUpOutputFile(file, restarted, !shouldDeleteIfExists, shouldDeleteIfExists);

        os = new FileOutputStream(file.getAbsolutePath(), true);
        fileChannel = os.getChannel();

        Writer writer = Channels.newWriter(fileChannel, "UTF-8");
        outputBufferedWriter = new BufferedWriter(writer);

        Assert.state(outputBufferedWriter != null);
        // in case of restarting reset position to last committed point
        if (restarted) {
            checkFileSize();
            truncate();
        }

        outputBufferedWriter.flush();

        isBufferInitialized = true;
    }

    /**
     * Checks (on setState) to make sure that the current output file's size
     * is not smaller than the last saved commit point. If it is, then the
     * file has been damaged in some way and whole task must be started over
     * again from the beginning.
     * @throws java.io.IOException if there is an IO problem
     */
    private void checkFileSize() throws IOException {
        long size = -1;

        outputBufferedWriter.flush();
        size = fileChannel.size();

        if (size < currentPosition) {
            throw new ItemStreamException("Current file size is smaller than size at last commit");
        }
    }

    /**
     * Truncate the output at the last known good point.
     *
     * @throws java.io.IOException
     */
    public void truncate() throws IOException {
        fileChannel.truncate(currentPosition);
        fileChannel.position(currentPosition);
    }

    /**
     * Return the byte offset position of the cursor in the output file as a
     * long integer.
     */
    public long position() throws IOException {
        long pos = 0;

        if (fileChannel == null) {
            return 0;
        }

        outputBufferedWriter.flush();
        pos = fileChannel.position();

        return pos;

    }

    public String getSpeciesParentFolderName() {
        return speciesParentFolderName;
    }

    public void setSpeciesParentFolderName(String speciesParentFolderName) {
        this.speciesParentFolderName = speciesParentFolderName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getTaxidSeparator() {
        return taxidSeparator;
    }

    public void setTaxidSeparator(String taxidSeparator) {
        this.taxidSeparator = taxidSeparator;
    }
}
