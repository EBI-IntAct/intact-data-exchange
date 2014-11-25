package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.InteractionEvidence;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.IntactPsiMitab;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

/**
 * The SmallScaleSpeciesWriter is an ItemStream and ItemWriter which can write the negative and positive interactions of each SpeciesFileUnit
 * to the proper directory.
 *
 * Some properties can be customized :
 * - the speciesParentFolderName which is the released directory where to copy the species files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/09/11</pre>
 */

public class SpeciesInteractionWriter implements ItemWriter<SpeciesInteractionUnit>,ItemStream {
    private static final Log log = LogFactory.getLog(SpeciesInteractionWriter.class);

    private String parentFolderPaths;
    private InteractionWriter<InteractionEvidence> psiWriter;

    private File parentFolder;

    private final static String IS_NEGATIVE = "is_negative";
    private final static String CURRENT_POSITION = "current_position";
    private final static String CURRENT_SPECIES = "current_species";

    /**
     * The chunk number
     */
    private final static String CHUNK_ID = "chunk_id";

    private Map<String, Object> writerOptions;

    private boolean isNegativeEntry=false;
    private int currentChunk = 0;
    private String currentSpecies;

    private FileNameGenerator fileNameGenerator;

    private String extension;
    private long currentPosition=0;
    private Writer outputBufferedWriter;
    private FileChannel fileChannel;
    private FileOutputStream os;

    private boolean appendToExistingFile = false;

    public SpeciesInteractionWriter(){
        super();
    }
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (parentFolderPaths == null){
            throw new ItemStreamException("A species parent folder name is needed for the writer");
        }

        parentFolder = new File(parentFolderPaths);

        if ( !parentFolder.exists() ) {
            if ( !parentFolder.mkdirs() ) {
                throw new ItemStreamException( "Cannot create parent parentFolder: " + parentFolder.getAbsolutePath() );
            }
        }
        else if (!parentFolder.canWrite()){
            throw new ItemStreamException( "Impossible to write in : " + parentFolder.getAbsolutePath() );
        }

        if (this.writerOptions == null || this.writerOptions.isEmpty()){
            throw new IllegalStateException("Options to instantiate the writer must be provided");
        }

        registerWriters();

        if (this.extension == null){
            throw new ItemStreamException("Cannot open the writer if no file extension is provided");
        }

        // we get the last id generated by this processor
        if (executionContext.containsKey(IS_NEGATIVE) && executionContext.containsKey(CHUNK_ID) && executionContext.containsKey(CURRENT_SPECIES)){
            isNegativeEntry = Boolean.valueOf(executionContext.getString(IS_NEGATIVE));
            currentChunk = executionContext.getInt(CHUNK_ID);
            currentSpecies = executionContext.getString(CURRENT_SPECIES);
        }
        else if ((executionContext.containsKey(IS_NEGATIVE) && (!executionContext.containsKey(CHUNK_ID) || !executionContext.containsKey(CURRENT_SPECIES)))
                ||((!executionContext.containsKey(IS_NEGATIVE) || !executionContext.containsKey(CURRENT_SPECIES)) && executionContext.containsKey(CHUNK_ID))
                ||((!executionContext.containsKey(IS_NEGATIVE) || !executionContext.containsKey(CHUNK_ID)) && executionContext.containsKey(CURRENT_SPECIES))){
            throw new ItemStreamException("Cannot restart the SpeciesInteractionWriter as some important execution step information are missing");
        }
        else {
            // we need to reset the pointers
            isNegativeEntry = false;
            currentChunk = 0;
            currentSpecies = null;
        }

        // we initialize the bufferWriter
        if (executionContext.containsKey(CURRENT_POSITION)) {
            this.currentPosition = executionContext.getLong(CURRENT_POSITION);
        }
        else {
            this.currentPosition = 0;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

        Assert.notNull(executionContext, "ExecutionContext must not be null");

        // update current pointers
        executionContext.put(IS_NEGATIVE, Boolean.toString(isNegativeEntry));
        executionContext.put(CHUNK_ID, this.currentChunk);
        executionContext.put(CURRENT_SPECIES, this.currentSpecies);

        try {
            currentPosition = position();
            executionContext.putLong(CURRENT_POSITION, currentPosition);

        } catch (IOException e) {
            throw new ItemStreamException( "Impossible to get the last position of the writer" );
        }
    }

    @Override
    public void close() throws ItemStreamException {
        // nothing to close as the psixml writer is dealing with the writing, flushing and closing. If it fails, it will
        // override the previous files already written
        this.parentFolder = null;
        this.isNegativeEntry = false;
        this.currentChunk = 0;
        this.currentSpecies = null;
        if (this.psiWriter != null){
            this.psiWriter.close();
        }
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                throw new ItemStreamException( "Impossible to close the species file", e );
            }
        }

        this.currentPosition = 0;
        this.fileChannel = null;
        this.os = null;
    }

    /**
     * Creates the buffered writer for the output file channel based on
     * configuration information.
     * @throws java.io.IOException
     */
    protected void initializeBufferedWriter(File file, boolean restarted) throws IOException {

        FileUtils.setUpOutputFile(file, restarted, isAppendToExistingFile(), !isAppendToExistingFile());

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

        initialiseObjectWriter(file);

        if (!restarted){
            this.psiWriter.start();
        }
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

    protected void initialiseObjectWriter(File file) {
        this.currentPosition = 0;
        // add mandatory options
        getWriterOptions().put(InteractionWriterOptions.OUTPUT_OPTION_KEY, file);

        addSupplementaryOptions();

        try {

            if (!file.getCanonicalFile().getParentFile().canWrite()) {
                log.warn("Cannot write to the output file " + file.getAbsolutePath());
                throw new IllegalStateException("Needs to write in output file: " + file.getAbsolutePath());
            }

            // we initialize the bufferWriter
            if (this.currentPosition > 0) {
                initializeBufferedWriter(file, true);
            }
            else {
                initializeBufferedWriter(file, false);
            }
        } catch (IOException e) {
            throw new ItemStreamException("Cannot open the output file", e);
        }

        if (this.psiWriter == null){
            InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();
            this.psiWriter = writerFactory.getInteractionWriterWith(getWriterOptions());
        }
        else{
            this.psiWriter.close();
            this.psiWriter.initialiseContext(this.writerOptions);
        }

        if (this.psiWriter == null){
            throw new IllegalStateException("We cannot find a valid interaction writer with the given options.");
        }
    }

    protected void addSupplementaryOptions() {
        // by default, nothing to do
    }

    protected void registerWriters() {
        // register default MI writers
        IntactPsiMitab.initialiseAllIntactMitabWriters();

        // override writers for Intact xml
        IntactPsiXml.initialiseAllIntactXmlWriters();
    }

    @Override
    public void write(List<? extends SpeciesInteractionUnit> items) throws Exception {
        if (parentFolder == null){
            throw new WriteFailedException("You must open the writer before writing files.");
        }

        for (SpeciesInteractionUnit speciesEntry : items){
            InteractionEvidenceChunkIterator positiveIterator = speciesEntry.getPositiveInteractionIterator();
            InteractionEvidenceChunkIterator negativeIterator = speciesEntry.getNegativeInteractionIterator();
            String species = fileNameGenerator.replaceBadCharactersFor(speciesEntry.getSpecies());

            if (this.currentSpecies == null || (this.currentSpecies != null && !this.currentSpecies.equalsIgnoreCase(species))){
                this.currentSpecies = species;
                this.currentChunk = 0;
            }
            else if (this.currentChunk > 0 && this.currentSpecies != null && this.currentSpecies.equalsIgnoreCase(species)){
                this.currentChunk++;
            }
            else{
                this.currentChunk = 0;
            }

            if (!isNegativeEntry && positiveIterator.hasNext()){

                // pre process iterator
                preProcessInteractions(positiveIterator);

                // now can write a file for positive entry
                String fileName = generatePositiveFileName(currentSpecies) + extension;
                File speciesFile = new File(fileName);

                log.info("Write species files " + fileName);

                // initialise writer
                initialiseObjectWriter(speciesFile);

                // write interactions
                writeInteractions(positiveIterator);

                // write end content
                psiWriter.end();
            }
            if (negativeIterator.hasNext()) {
                isNegativeEntry = true;

                // pre process iterator
                preProcessInteractions(negativeIterator);

                // now can write a file for negative entry
                String fileName = generateNegativeFileName(currentSpecies) + extension;
                File speciesFile = new File(fileName);

                log.info("Write negative species files " + fileName);

                // initialise writer
                initialiseObjectWriter(speciesFile);

                // write interactions
                writeInteractions(negativeIterator);

                // write end content
                psiWriter.end();
            }
        }

        this.isNegativeEntry = false;
    }

    protected void preProcessInteractions(InteractionEvidenceChunkIterator iterator) {
        // nothing to do by default
    }

    protected void writeInteractions(InteractionEvidenceChunkIterator positiveIterator) {
        while(positiveIterator.hasNext()){
            InteractionEvidence interaction = positiveIterator.next();

            if (interaction != null){
                psiWriter.write(interaction);
            }
        }
    }

    protected String generateNegativeFileName(String currentSpecies) {
        return fileNameGenerator.createPublicationName(currentSpecies, currentChunk > 0 ? currentChunk : null, true);
    }

    protected String generatePositiveFileName(String currentSpecies) {
        return fileNameGenerator.createPublicationName(currentSpecies, currentChunk > 0 ? currentChunk : null, false);
    }

    public String getParentFolderPaths() {
        return parentFolderPaths;
    }

    public void setParentFolderPaths(String parentFolderPaths) {
        this.parentFolderPaths = parentFolderPaths;
    }

    public void setWriterOptions(Map<String, Object> writerOptions) {
        this.writerOptions = writerOptions;
    }

    protected Map<String, Object> getWriterOptions() {
        return writerOptions;
    }

    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
        this.fileNameGenerator = fileNameGenerator;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isAppendToExistingFile() {
        return appendToExistingFile;
    }

    public void setAppendToExistingFile(boolean appendToExistingFile) {
        this.appendToExistingFile = appendToExistingFile;
    }

    protected Writer getOutputBufferedWriter() {
        return outputBufferedWriter;
    }

    protected FileChannel getFileChannel() {
        return fileChannel;
    }

    protected FileOutputStream getOs() {
        return os;
    }

    protected InteractionWriter<InteractionEvidence> getPsiWriter() {
        return psiWriter;
    }

    protected int getCurrentChunk() {
        return currentChunk;
    }

    protected void setCurrentChunk(int currentChunk) {
        this.currentChunk = currentChunk;
    }
}
