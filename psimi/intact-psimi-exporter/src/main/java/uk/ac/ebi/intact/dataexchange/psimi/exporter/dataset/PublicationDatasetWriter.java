package uk.ac.ebi.intact.dataexchange.psimi.exporter.dataset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.util.Assert;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.archive.NameTruncation;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.PublicationFileFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * The PublicationDatasetWriter is an ItemWriter and an ItemStream.
 *
 * It can write a list of PublicationDatasetUnit which will be used to first retrieve the publication xml files
 * using the publication year (helps to retrieve publication subDirectory) and the publication id (helps to retrieve files
 * starting with the same publication id) and copy them in the proper dataset subDirectory.
 *
 * Some properties can be customized :
 * - the datasetParentFolderName which is the name of the parent directory where to write the dataset subDirectories
 * (not initialised by default)
 * - the pmidFolderName which is the name of the directory where to find all the publication files to copy
 * (not initialised by default)
 * - the publicationFilter which will filter the publications starting with a common publication id
 * (is initialised by default)
 * - the datasetTruncation which allows to extract the name of a dataset from a String
 * (is not initialize by default!)
 * - the file name generator which allows to format a publication id so it can retrieve files starting with this publication id
 * (will be initialized by default)
 * - the log file name where to write the errors (has a default value if not set)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class PublicationDatasetWriter implements ItemWriter<PublicationDatasetUnit>, ItemStream {

    private static final Log log = LogFactory.getLog(PublicationDatasetWriter.class);

    private String datasetParentFolderName;
    private String pmidFolderName;
    private PublicationFileFilter publicationFilter;
    private NameTruncation datasetTruncation;
    private FileNameGenerator fileNameGenerator;
    private String errorLogName;

    private File datasetParentFolder;
    private File pmidFolder;

    private final String DEFAULT_LOG_FILE = "error_dataset_writer.log";
    private FileWriter logWriter;

    private File currentDatasetDirectory;
    private File currentYearDirectory;

    public PublicationDatasetWriter(){
        fileNameGenerator = new FileNameGenerator();
        publicationFilter = new PublicationFileFilter();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (datasetParentFolderName == null){
            throw new NullPointerException("A parent folder is needed for the writer");
        }

        datasetParentFolder = new File(datasetParentFolderName);

        if ( !datasetParentFolder.exists() ) {

            if ( !datasetParentFolder.mkdirs() ) {
                throw new ItemStreamException( "Cannot create dataset directory: " + datasetParentFolder.getAbsolutePath() );
            }

        }
        else if (!datasetParentFolder.canWrite()){
            throw new ItemStreamException( "Impossible to write in : " + datasetParentFolder.getAbsolutePath() );
        }

        if (pmidFolderName == null){
            throw new NullPointerException("The pmid folder is needed for the writer");
        }

        pmidFolder = new File(pmidFolderName);

        if ( !pmidFolder.exists() ) {
            throw new ItemStreamException( "The pmid datasetParentFolder: " + pmidFolder.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!pmidFolder.canRead()){
            throw new ItemStreamException( "Impossible to read files in : " + pmidFolder.getAbsolutePath() );
        }

        if (errorLogName == null){
            this.errorLogName = DEFAULT_LOG_FILE;
        }

        try {
            File logFile = new File(errorLogName);

            if (logFile.exists()){
                // we append messages to the logWriter
                logWriter = new FileWriter(logFile, true);
            }
            else {
                logWriter = new FileWriter(logFile);
            }

        } catch (IOException e) {
            throw new ItemStreamException( "Cannot create file where to log the undefined publications: " + errorLogName, e );
        }

        this.currentDatasetDirectory = null;
        this.currentYearDirectory = null;

        // if restarted, the existing files which could have been copied for the failing chunk will be overwritten
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        try {
            if (logWriter != null){
                logWriter.flush();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot flush file where to log the dataset errors: " + errorLogName, e );
        }

        // nothing else to update
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (logWriter != null){
                logWriter.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException( "Cannot close file where to log the dataset errors: " + errorLogName, e );
        }

        this.pmidFolder = null;
        this.logWriter = null;
        this.datasetParentFolder = null;
        this.currentYearDirectory = null;
        this.currentDatasetDirectory = null;
        // nothing else to close.
    }

    @Override
    public void write(List<? extends PublicationDatasetUnit> items) throws Exception {

        if (datasetParentFolder == null || pmidFolder == null || logWriter == null){
            throw new WriteFailedException("You must open the writer before writing files.");
        }

        for (PublicationDatasetUnit datasetUnit : items){
            // get the publicationId
            String publicationId = datasetUnit.getPublicationId();
            // get the year.
            String year = Integer.toString(datasetUnit.getPublicationYear());

            // extract the dataset name from the dataset annotation and remove all possible bad characters
            String dataset = fileNameGenerator.replaceBadCharactersFor(datasetTruncation.truncate(datasetUnit.getDataset()));

            log.info("Write dataset " + dataset + ", publication " + publicationId + ", year " + year);
            if (dataset != null){

                // create the dataset directory if necessary
                if (this.currentDatasetDirectory == null){
                    this.currentDatasetDirectory = initializeDatasetDirectory(dataset);
                }
                else if (!this.currentDatasetDirectory.getName().equals(dataset)){
                    this.currentDatasetDirectory = initializeDatasetDirectory(dataset);
                }

                // collect files in the year directory
                if (this.currentYearDirectory == null){
                    this.currentYearDirectory = new File(pmidFolder, year);
                }
                else if (!this.currentYearDirectory.getName().equals(year)){
                    this.currentYearDirectory = new File(pmidFolder, year);
                }
                if ( !this.currentYearDirectory.exists() ) {
                    logWriter.write(dataset);
                    logWriter.write(" : the directory ");
                    logWriter.write(year);
                    logWriter.write(" does not exist and we cannot find the xml file to copy.");
                    logWriter.write("\n");
                    logWriter.flush();
                }
                else {
                    this.publicationFilter.setPublicationId(fileNameGenerator.replaceBadCharactersFor(publicationId));
                    File[] filesToCopy = this.currentYearDirectory.listFiles(this.publicationFilter);

                    // copy the publication files
                    for (File fileCopy : filesToCopy){
                        FileUtils.copyFileToDirectory(fileCopy, this.currentDatasetDirectory);
                    }

                    if (filesToCopy.length == 0){
                        logWriter.write(dataset);
                        logWriter.write(" : the directory ");
                        logWriter.write(year);
                        logWriter.write(" does not contain any publication files for publication id = " + publicationId);
                        logWriter.write("\n");
                        logWriter.flush();
                    }
                }
            }
            else {
                logWriter.write(year);
                logWriter.write("/");
                logWriter.write(publicationId);
                logWriter.write(" does not have a valid dataset and is skipped. \n");
                logWriter.flush();
            }
        }
    }

    private File initializeDatasetDirectory(String folderName) throws IOException {
        File directory = new File(this.datasetParentFolder, folderName);
        if ( !directory.exists() ) {
            if ( !directory.mkdirs() ) {
                throw new IOException( "Cannot create directory: " + directory.getAbsolutePath() );
            }
        }
        else if (!directory.canWrite()){
            throw new IOException( "Impossible to write in : " + directory.getAbsolutePath() );
        }

        return directory;
    }

    public String getDatasetParentFolderName() {
        return datasetParentFolderName;
    }

    public void setDatasetParentFolderName(String datasetParentFolderName) {
        this.datasetParentFolderName = datasetParentFolderName;
    }

    public String getErrorLogName() {
        return errorLogName;
    }

    public void setErrorLogName(String errorLogName) {
        this.errorLogName = errorLogName;
    }

    public String getPmidFolderName() {
        return pmidFolderName;
    }

    public void setPmidFolderName(String pmidFoler) {
        this.pmidFolderName = pmidFoler;
    }

    public PublicationFileFilter getPublicationFilter() {
        return publicationFilter;
    }

    public void setPublicationFilter(PublicationFileFilter publicationFilter) {
        this.publicationFilter = publicationFilter;
    }

    public NameTruncation getDatasetTruncation() {
        return datasetTruncation;
    }

    public void setDatasetTruncation(NameTruncation datasetTruncation) {
        this.datasetTruncation = datasetTruncation;
    }
}
