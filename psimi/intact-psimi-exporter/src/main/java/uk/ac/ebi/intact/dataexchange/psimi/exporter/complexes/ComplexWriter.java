package uk.ac.ebi.intact.dataexchange.psimi.exporter.complexes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.*;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.IntactPsiMitab;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The ComplexWriter is an ItemStream and ItemWriter which writes for each ComplexFileEntry a psi xml file
 * of type compact.
 *
 * Several properties can be customized :
 * - parentFolderPath which is the absolute path name of the parent folder where to write the xml files
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class ComplexWriter implements ItemWriter<ComplexFileEntry>, ItemStream {

    private static final Log log = LogFactory.getLog(ComplexWriter.class);

    private String parentFolderPaths;

    private InteractionWriter<Complex> psiWriter;

    private File parentFolder;
    private File currentSpecies;

    private Map<String, Object> writerOptions;

    private String extension = null;

    public ComplexWriter(){
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

        if (this.writerOptions == null || this.writerOptions.isEmpty()){
            throw new IllegalStateException("Options to instantiate the writer must be provided");
        }

        if (this.extension == null){
            throw new IllegalStateException("The writer needs to have a non null file extension must be provided");
        }

        currentSpecies = null;

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
        currentSpecies = null;
        if (this.psiWriter != null){
            this.psiWriter.close();
        }
        this.psiWriter = null;
    }

    @Override
    public void write(List<? extends ComplexFileEntry> items) throws Exception {

        if (parentFolder == null){
            throw new WriteFailedException("You must open the writer before writing files.");
        }

        for (ComplexFileEntry species : items){

            // we extract the species name which is the folder name
            String folderName = species.getSpeciesName();

            if (currentSpecies == null){
                currentSpecies = initializeSpeciesDirectory(folderName);
            }
            else if (!currentSpecies.getName().equals(folderName)){
                currentSpecies = initializeSpeciesDirectory(folderName);
            }

            // now can write a file per species entry
            String fileName = species.getEntryName() + extension;

            log.info("write species entry : " + fileName);

            File speciesFile = new File(currentSpecies, fileName);

            // initialise writer
            initialiseObjectWriter(speciesFile);

            // write entry content
            psiWriter.start();
            psiWriter.write(species.getComplex());
            psiWriter.end();
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

    private File initializeSpeciesDirectory(String species) throws IOException {
        File directory = new File(parentFolder, species);

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
