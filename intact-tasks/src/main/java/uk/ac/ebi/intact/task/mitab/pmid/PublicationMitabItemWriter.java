package uk.ac.ebi.intact.task.mitab.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Item writer that will write mitab publication files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public class PublicationMitabItemWriter implements ItemWriter<Collection<PublicationFileEntry>>, ItemStream {

    private static final Log log = LogFactory.getLog(PublicationMitabItemWriter.class);

    private String parentFolderPaths;

    private DateFormat dateFormat;
    private Writer psiWriter;

    private File parentFolder;
    private File currentYear;

    private PsimiTabVersion version = PsimiTabVersion.v2_7;

    private GlobalMitabItemWriter globalMitabItemWriter;

    public PublicationMitabItemWriter(){
        dateFormat = new SimpleDateFormat("yyyy");
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (parentFolderPaths == null){
            throw new NullPointerException("An parent folder is needed for the writer");
        }

        parentFolder = new File (parentFolderPaths);

        if ( !parentFolder.exists() ) {
            if ( !parentFolder.mkdirs() ) {
                throw new ItemStreamException( "Cannot create parent parentFolder: " + parentFolder.getAbsolutePath() );
            }
        }
        else if (!parentFolder.canWrite()){
            throw new ItemStreamException( "Impossible to write in : " + parentFolder.getAbsolutePath() );
        }

        if (this.globalMitabItemWriter == null){
            this.globalMitabItemWriter = new GlobalMitabItemWriter();
            this.globalMitabItemWriter.setLineAggregator(new SimpleLineAggregator());
            this.globalMitabItemWriter.setResource(new FileSystemResource(parentFolder + "/intact.txt"));
        }

        currentYear = null;
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

                List<String> lines = new ArrayList<String>(2);

                // now can write a file per publication entry
                for (PublicationFileEntry publicationEntry : publicationEntries){
                    String fileName = publicationEntry.getEntryName() + ".xml";

                    log.info("write publication entry : " + fileName);

                    File publicationFile = new File(currentYear, fileName);

                    this.psiWriter = new BufferedWriter(new FileWriter(publicationFile));

                    try{
                        this.psiWriter.write(MitabWriterUtils.buildHeader(this.version));

                        String line = publicationEntry.getBinaryInteractions().toString();
                        psiWriter.write(line);

                        lines.add(line);
                    }
                    finally {
                        this.psiWriter.close();
                    }
                }

                this.globalMitabItemWriter.write(lines);
            }
        }
    }

    public String getParentFolderPaths() {
        return parentFolderPaths;
    }

    public void setParentFolderPaths(String parentFolderPaths) {
        this.parentFolderPaths = parentFolderPaths;
    }

    private File initializeYearDirectory(String year) throws IOException {
        File directory = new File (parentFolder, year);

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

    public PsimiTabVersion getVersion() {
        return version;
    }

    public void setVersion(PsimiTabVersion version) {
        this.version = version;
    }

    public GlobalMitabItemWriter getGlobalMitabItemWriter() {
        return globalMitabItemWriter;
    }

    public void setGlobalMitabItemWriter(GlobalMitabItemWriter globalMitabItemWriter) {
        this.globalMitabItemWriter = globalMitabItemWriter;
    }
}
