package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.InteractionEvidence;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.IntactPsiMitab;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The SinglePublicationInteractionWriter is an ItemStream and ItemWriter which writes for each PublicationFileEntry a psi file.
 *
 * Several properties can be customized :
 * - parentFolderPath which is the absolute path name of the parent folder where to write the xml files
 * - writer options: jami options for writer
 * - extension of the file (.txt, .xml, ..)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class SinglePublicationInteractionWriter implements ItemWriter<Collection<PublicationFileEntry>>, ItemStream {

    private static final Log log = LogFactory.getLog(SinglePublicationInteractionWriter.class);

    private String parentFolderPaths;

    private DateFormat dateFormat;
    private InteractionWriter<InteractionEvidence> psiWriter;

    private File parentFolder;
    private File currentYear;

    private Map<String, Object> writerOptions;

    private String fileExtension = null;

    public SinglePublicationInteractionWriter(){
        dateFormat = new SimpleDateFormat("yyyy");
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (parentFolderPaths == null){
            throw new NullPointerException("An parent folder is needed for the writer");
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

        if (fileExtension == null){
            throw new ItemStreamException( "The file extension is required " );
        }

        if (this.writerOptions == null || this.writerOptions.isEmpty()){
            throw new IllegalStateException("Options to instantiate the writer must be provided");
        }

        currentYear = null;

        registerWriters();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");
        // nothing to update
    }

    @Override
    public void close() throws ItemStreamException {
        // nothing to close as the psixml writer is dealing with the writing, flushing and closing. If it fails, it will
        // override the previous files already written

        parentFolder = null;
        currentYear = null;
        if (this.psiWriter != null){
           this.psiWriter.close();
        }
        this.psiWriter = null;
    }

    @Override
    public void write(List<? extends Collection<PublicationFileEntry>> items) throws Exception {

        if (parentFolder == null){
            throw new WriteFailedException("You must open the writer before writing files.");
        }

        for (Collection<PublicationFileEntry> publicationEntries : items){
            // we process all entries
            if (!publicationEntries.isEmpty()){
                // the all collection is about a same publication so we can extract created date from the first item
                PublicationFileEntry publication = publicationEntries.iterator().next();

                // we extract the created date
                Date created = publication.getCreatedDate();

                // the folder name is the date of creation of the publication
                String folderName = dateFormat.format(created);

                if (currentYear == null){
                    currentYear = initializeYearDirectory(folderName);
                }
                else if (!currentYear.getName().equals(folderName)){
                    currentYear = initializeYearDirectory(folderName);
                }

                // now can write a file per publication entry
                for (PublicationFileEntry publicationEntry : publicationEntries){
                    String fileName = publicationEntry.getEntryName() + fileExtension;

                    log.info("write publication entry : " + fileName);

                    File publicationFile = new File(currentYear, fileName);

                    // initialise writer
                    initialiseObjectWriter(publicationFile);

                    // write entry content
                    psiWriter.start();
                    psiWriter.write(publicationEntry.getInteractions());
                    psiWriter.end();
                }
            }
        }
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    protected void initialiseObjectWriter(File file) {
        // add mandatory options
        getWriterOptions().put(InteractionWriterOptions.OUTPUT_OPTION_KEY, file);

        addSupplementaryOptions();

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

    protected Map<String, Object> getWriterOptions() {
        return writerOptions;
    }

    private File initializeYearDirectory(String year) throws IOException {
        File directory = new File(parentFolder, year);

        if ( !directory.exists() ) {
            if ( !directory.mkdirs() ) {
                throw new IOException( "Cannot create parent directory: " + directory.getAbsolutePath() );
            }
        }
        else if (!directory.canWrite()){
            throw new IOException( "Impossible to write in : " + directory.getAbsolutePath() );
        }
        return directory;
    }

}
