package uk.ac.ebi.intact.dataexchange.imex.idassigner.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ImexUpdateReportHandler default implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class FileImexUpdateReportHandler implements ImexUpdateReportHandler {

    private ReportWriter processedWriter;
    private ReportWriter imexPublicationWriter;
    private ReportWriter publicationUpToDateWriter;
    private ReportWriter imexIdAssignedToPublicationWriter;
    private ReportWriter imexIdAssignedToInteractionWriter;
    private ReportWriter imexIdMismatchWriter;

    public FileImexUpdateReportHandler( File rootDirectory ) throws IOException {
        if ( !rootDirectory.exists() ) {
            rootDirectory.mkdirs();
        }
        if ( !rootDirectory.isDirectory() ) {
            throw new IOException( "The file passed to the constructor has to be a directory: " + rootDirectory );
        }

        // Initialize all writers
        this.processedWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "processed.csv" ) ) );
        this.imexPublicationWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "processed-imex.csv" ) ) );
        this.publicationUpToDateWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "up-to-date.csv" ) ) );
        this.imexIdAssignedToPublicationWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "publication-assigned.csv" ) ) );
        this.imexIdAssignedToInteractionWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "interaction-assigned.csv" ) ) );
        this.imexIdMismatchWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "id-mismatch.csv" ) ) );
    }

    public ReportWriter getProcessedWriter() throws IOException {
        return processedWriter;
    }

    public ReportWriter getProcessImexPublicationWriter() {
        return imexPublicationWriter;
    }

    public ReportWriter getPublicationUpToDateWriter() {
        return publicationUpToDateWriter;
    }

    public ReportWriter getImexIdAssignedToPublicationWriter() {
        return imexIdAssignedToPublicationWriter;
    }

    public ReportWriter getImexIdAssignedToInteractionWriter() {
        return imexIdAssignedToInteractionWriter;
    }

    public ReportWriter getImexIdMismatchFoundWriter() {
        return imexIdMismatchWriter;
    }

    //////////////////
    // Closeable

    public void close() throws IOException {
        processedWriter.close();
        imexPublicationWriter.close();
        publicationUpToDateWriter.close();
        imexIdAssignedToPublicationWriter.close();
        imexIdMismatchWriter.close();
        imexIdAssignedToInteractionWriter.close();
    }
}