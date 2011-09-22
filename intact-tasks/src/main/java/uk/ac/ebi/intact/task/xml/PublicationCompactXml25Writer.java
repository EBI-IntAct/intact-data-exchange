package uk.ac.ebi.intact.task.xml;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.io.PsimiXmlWriter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The writer will write each publication entry in a file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class PublicationCompactXml25Writer implements ItemWriter<Collection<PublicationEntry>>, ItemStream {

    private String parentFolderPaths;
    private DateFormat dateFormat;
    private PsimiXmlWriter psiWriter;

    private File parentFolder;

    public PublicationCompactXml25Writer(){
        dateFormat = new SimpleDateFormat("yyyy");
        // version 2.5.4 and form compact
        psiWriter = new psidev.psi.mi.xml.PsimiXmlWriter(PsimiXmlVersion.VERSION_254, PsimiXmlForm.FORM_COMPACT);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
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
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // nothing to update
    }

    @Override
    public void close() throws ItemStreamException {
        // nothing to close as the psixml writer is dealing with the writting, flushing and closing. If it fails, it will
        // override the previous files already written
    }

    @Override
    public void write(List<? extends Collection<PublicationEntry>> items) throws Exception {
        for (Collection<PublicationEntry> publicationEntries : items){
            if (!publicationEntries.isEmpty()){
                // the all collection is about a same publication so we can extract created date from the first item
                PublicationEntry publication = publicationEntries.iterator().next();

                Date created = publication.getCreatedDate();

                String folderName = dateFormat.format(created);

                File directory = new File (parentFolder, folderName);

                if ( !directory.exists() ) {
                    if ( !directory.mkdirs() ) {
                        throw new IOException( "Cannot create parent directory: " + directory.getAbsolutePath() );
                    }
                }
                else if (!directory.canWrite()){
                    throw new IOException( "Impossible to write in : " + directory.getAbsolutePath() );
                }

                // now can write a file per publication entry
                for (PublicationEntry publicationEntry : publicationEntries){
                    String fileName = publicationEntry.getEntryName();

                    File publicationFile = new File(directory, fileName);

                    psiWriter.write(publicationEntry.getXmlEntry(), publicationFile);
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
}
