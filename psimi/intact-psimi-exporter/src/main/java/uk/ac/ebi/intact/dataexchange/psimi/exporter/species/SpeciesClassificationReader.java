package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.commons.PsiJami;
import psidev.psi.mi.jami.model.InteractionEvidence;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.PublicationFileFilter;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileUnit;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.SimpleFileIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The SmallScaleSpeciesReader is an ItemStrem and ItemReader which can read a set of files starting with a common publication id and extract interactions containing
 * species of interest.
 *
 * The reader returns a SpeciesInteractionUnit when it can gather all the files of a specific publication and species.
 *
 * Some properties can be customized :
 * - the species folder name which is the name of the parent directory which contains the index files to read (not recursively however)
 * - the pmidFolderPath is the name of the pmid folder where to find the xml files to read
 * - the extension is the extension of the species files (by default is txt)
 * - the experimentSeparator is the separator of the experiment infos in the index files (by default is :)
 * - the error log name which is the name of the file where to log the error messages
 * - the publication filter which allows to filter files in a directory using file names (initialized by default)
 * - the fileNameGenerator and fileExtension which allows to recompose file names and allows to retrieve files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class SpeciesClassificationReader implements ItemReader<SpeciesInteractionUnit>, ItemStream {
    private static final Log log = LogFactory.getLog(SpeciesClassificationReader.class);

    private String speciesFolderName;
    private String pmidFolderPath;
    private String extension = "txt";
    private String errorLogName;
    private PublicationFileFilter publicationFilter;
    private FileNameGenerator fileNameGenerator;

    private File speciesFolder;

    private File pmidFolder;

    private int currentFileNumber;
    private int currentLine;
    private final static String NUMBER_FILES = "number_files";
    private final static String CURRENT_LINE = "species_line";

    private File currentSpeciesFile;
    private Set<File> positiveXmlInputs = new HashSet<File>();
    private Set<File> negativeXmlInputs = new HashSet<File>();
    private SimpleFileIterator currentFileIterator;
    private SpeciesClassificationIterator speciesIterator;

    private FileWriter logWriter;
    private final String DEFAULT_LOG_FILE = "error_smallScale_species.log";

    private SpeciesFileUnit currentSpeciesUnit;
    private Iterator<InteractionEvidence> currentPositiveInteractionIterator;
    private Iterator<InteractionEvidence> currentNegativeInteractionIterator;
    private String taxidSeparator;

    private String currentSpecies;
    private Map<String, Object> dataSourceOptions;
    private String fileExtension;

    public SpeciesClassificationReader(){
        this.fileNameGenerator = new FileNameGenerator();
    }

    private boolean readLines() throws IOException {

        this.positiveXmlInputs.clear();
        this.negativeXmlInputs.clear();

        // the current species file still contains lines
        if ((this.currentPositiveInteractionIterator != null && this.currentPositiveInteractionIterator.hasNext())
                || (this.currentNegativeInteractionIterator != null && this.currentNegativeInteractionIterator.hasNext())){
            // nothing to do
        }
        else if (this.speciesIterator.hasNext()){
            // we have read one more line
            readNextLine();
        }
        // the current species file is finished, we can read the next species file
        else {
            boolean needToReadNextFiles = true;
            this.currentLine = 0;

            while (this.currentFileIterator.hasNext() && needToReadNextFiles){

                readNextSpeciesFile();

                if (speciesIterator.hasNext()){
                    readNextLine();
                    needToReadNextFiles = false;
                }
            }

            if (needToReadNextFiles && !this.currentFileIterator.hasNext()){
                return false;
            }
        }
        return true;
    }

    @Override
    public SpeciesInteractionUnit read() throws Exception, UnexpectedInputException, ParseException {

        if (pmidFolder == null || speciesFolder == null || logWriter == null){
            throw new ParseException("You must open the reader before reading files.");
        }

        boolean readLine = readLines();
        if (readLine){

            return createNewSpeciesInteractionUnit();
        }

        // the reader finished its job
        return null;
    }

    protected SpeciesInteractionUnit createNewSpeciesInteractionUnit() {
        SpeciesInteractionUnit interactionUnit = new SpeciesInteractionUnit();
        interactionUnit.setSpecies(this.currentSpecies);
        interactionUnit.setNegativeInteractionIterator(new InteractionEvidenceChunkIterator(null, this.currentNegativeInteractionIterator));
        interactionUnit.setPositiveInteractionIterator(new InteractionEvidenceChunkIterator(null,this.currentPositiveInteractionIterator));
        return interactionUnit;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (speciesFolderName == null){
            throw new NullPointerException("A species parent folder is needed for the writer");
        }

        speciesFolder = new File(speciesFolderName);

        if ( !speciesFolder.exists() ) {
            throw new ItemStreamException( "The species parent folder : " + speciesFolder.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!speciesFolder.isDirectory()){
            throw new ItemStreamException( speciesFolderName + " is not a directory." );
        }
        else if (!speciesFolder.canRead()){
            throw new ItemStreamException( "Impossible to read files : " + speciesFolderName );
        }

        if (pmidFolderPath == null){
            throw new NullPointerException("A pmid folder is needed for the processor");
        }

        if (fileExtension == null){
            throw new NullPointerException("A file extension is needed for the processor");
        }

        pmidFolder = new File(pmidFolderPath);

        if ( !pmidFolder.exists() ) {
            throw new ItemStreamException( "The pmid folder : " + pmidFolder.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!pmidFolder.isDirectory()){
            throw new ItemStreamException( pmidFolderPath + " is not a directory." );
        }
        else if (!pmidFolder.canRead()){
            throw new ItemStreamException( "Impossible to read files in : " + pmidFolder );
        }

        if (errorLogName == null){
            this.errorLogName = DEFAULT_LOG_FILE;
        }

        try {
            logWriter = new FileWriter(errorLogName, true);
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot create file where to log the undefined publications: " + errorLogName, e );
        }

        registerDataSources();

        this.currentFileIterator = new SimpleFileIterator(speciesFolder, new String[] {extension}, false);

        // we initialize the number of processed files and processed line sif restart
        if (executionContext.containsKey(NUMBER_FILES) && executionContext.containsKey(CURRENT_LINE) ) {
            this.currentFileNumber = executionContext.getInt(NUMBER_FILES);
            this.currentLine = executionContext.getInt(CURRENT_LINE);

            int index = 0;

            // skipp processed files
            if (currentFileNumber > 0){
                while (index < currentFileNumber - 1){
                    if (!currentFileIterator.hasNext()){
                        index ++;
                        throw new ItemStreamException("The directory " + speciesFolderName + " contained less files than the expected number. We cannot restart the reader.");
                    }
                }
            }

            if (currentFileIterator.hasNext()){
                try {
                    readNextSpeciesFile();
                } catch (IOException e) {
                    throw new ItemStreamException("Could not read the species file" + currentSpeciesFile.getAbsolutePath(), e);
                }

                positiveXmlInputs.clear();
                negativeXmlInputs.clear();

                // we open the species file and initialize the current reader
                if (currentLine > 0){
                    int line = 0;
                    // skip processed lines
                    while (line < currentLine){
                        if (!this.speciesIterator.hasNext()){
                            throw new ItemStreamException("The species file " + currentSpeciesFile.getName() + " contained " + line + " lines and it is not matching the last count of processed lines which was " + currentLine);
                        }
                        this.speciesIterator.next();
                        line ++;
                    }
                }
            }
            else if (!currentFileIterator.hasNext() && currentLine > 0){
                throw new ItemStreamException("The directory " + speciesFolderName + " contained less files than the expected number. We cannot restart the reader.");
            }
            // the reader already finished its job
            else {
                currentSpeciesFile = null;
                currentSpecies = null;
                currentLine = 0;
                currentSpeciesUnit = null;
                positiveXmlInputs.clear();
                negativeXmlInputs.clear();
            }
        }
        // we open the reader for the first time
        else if (!executionContext.containsKey(NUMBER_FILES) && !executionContext.containsKey(CURRENT_LINE)) {
            if (currentFileIterator.hasNext()){
                try {
                    readNextSpeciesFile();
                } catch (IOException e) {
                    throw new ItemStreamException("Impossible to read the next species line of " + this.currentSpeciesFile.getAbsolutePath(), e);
                }

                currentLine = 0;

                positiveXmlInputs.clear();
                negativeXmlInputs.clear();
            }
            // the reader already finished its job
            else {
                currentSpeciesFile = null;
                currentLine = 0;
                currentFileNumber = 0;
                currentSpecies = null;
                currentSpeciesUnit = null;
                positiveXmlInputs.clear();
                negativeXmlInputs.clear();
            }
        }
        else{
            throw new ItemStreamException("Impossible to restart the reader. Some important information are missing : the number of processed files, the line number and the species chunk number are necessary for restart.");
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        try {
            if (logWriter != null){
                logWriter.flush();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot flush file where to log the small scale experiment errors: " + errorLogName, e );
        }

        // we keep a trace of the number of files already read and the current line number and the current chunk
        executionContext.putInt(NUMBER_FILES, currentFileNumber);
        executionContext.putInt(CURRENT_LINE, currentLine);
    }

    @Override
    public void close() throws ItemStreamException {
        // close the bufferReader if not null
        try {
            if (logWriter != null){
                logWriter.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot close file where to log the small scale experiment errors: " + errorLogName, e );
        }

        currentFileIterator = null;
        currentSpeciesFile = null;
        currentLine = 0;
        positiveXmlInputs.clear();
        negativeXmlInputs.clear();
        currentFileNumber = 0;
        speciesIterator = null;
        currentSpecies = null;
        currentSpeciesUnit = null;

        this.pmidFolder = null;
        this.speciesFolder = null;
        this.logWriter = null;
        this.currentPositiveInteractionIterator = null;
        this.currentNegativeInteractionIterator = null;
    }

    public String getSpeciesFolderName() {
        return speciesFolderName;
    }

    public void setSpeciesFolderName(String speciesFolderName) {
        this.speciesFolderName = speciesFolderName;
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

    public String getPmidFolderPath() {
        return pmidFolderPath;
    }

    public void setPmidFolderPath(String pmidFolderPath) {
        this.pmidFolderPath = pmidFolderPath;
    }

    public String getErrorLogName() {
        return errorLogName;
    }

    public void setErrorLogName(String logForUndefinedPublications) {
        this.errorLogName = logForUndefinedPublications;
    }

    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
        this.fileNameGenerator = fileNameGenerator;
    }

    public PublicationFileFilter getPublicationFilter() {
        return publicationFilter;
    }

    public void setPublicationFilter(PublicationFileFilter publicationFilter) {
        this.publicationFilter = publicationFilter;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    protected void registerDataSources() {
        // register default MI writers
        PsiJami.initialiseAllMIDataSources();
    }

    protected Map<String, Object> getDataSourceOptions() {
        return dataSourceOptions;
    }

    protected String getCurrentSpecies() {
        return currentSpecies;
    }

    protected Iterator<InteractionEvidence> getCurrentPositiveInteractionIterator() {
        return currentPositiveInteractionIterator;
    }

    protected Iterator<InteractionEvidence> getCurrentNegativeInteractionIterator() {
        return currentNegativeInteractionIterator;
    }

    protected void readNextSpeciesFile() throws IOException {
        FileUnit fileUnit = currentFileIterator.next();
        this.currentSpecies = fileUnit.getUnitName();
        this.currentSpeciesFile = !fileUnit.getEntities().isEmpty() ? fileUnit.getEntities().iterator().next() : null;

        this.currentFileNumber ++;
        this.currentLine = 0;

        this.speciesIterator = new SpeciesClassificationIterator(this.currentSpeciesFile, this.taxidSeparator, this.currentSpecies,
                this.positiveXmlInputs, this.negativeXmlInputs, this.publicationFilter, this.fileNameGenerator, this.pmidFolder, this.fileExtension);
    }

    protected void readNextLine(){

        this.currentSpeciesUnit = this.speciesIterator.next();
        this.currentSpeciesUnit.setDataSourceOptions(getDataSourceOptions());
        this.currentPositiveInteractionIterator = this.currentSpeciesUnit.getPositiveInteractionIterator();
        this.currentNegativeInteractionIterator = this.currentSpeciesUnit.getNegativeInteractionIterator();
        this.currentLine ++;
    }

    protected SpeciesFileUnit getCurrentSpeciesUnit() {
        return currentSpeciesUnit;
    }

    protected void setCurrentPositiveInteractionIterator(Iterator<InteractionEvidence> currentPositiveInteractionIterator) {
        this.currentPositiveInteractionIterator = currentPositiveInteractionIterator;
    }

    protected void setCurrentNegativeInteractionIterator(Iterator<InteractionEvidence> currentNegativeInteractionIterator) {
        this.currentNegativeInteractionIterator = currentNegativeInteractionIterator;
    }

    protected void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
    }

    protected SpeciesClassificationIterator getSpeciesIterator() {
        return speciesIterator;
    }

    protected void setCurrentSpeciesUnit(SpeciesFileUnit currentSpeciesUnit) {
        this.currentSpeciesUnit = currentSpeciesUnit;
    }

    protected int getCurrentLine() {
        return currentLine;
    }
}
