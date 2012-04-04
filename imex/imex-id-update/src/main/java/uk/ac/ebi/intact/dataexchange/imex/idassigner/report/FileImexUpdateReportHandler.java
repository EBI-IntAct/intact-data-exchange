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

    private ReportWriter newImexAssignedWriter;
    private ReportWriter imexErrorWriter;
    private ReportWriter intactUpdateWriter;

    public FileImexUpdateReportHandler( File rootDirectory ) throws IOException {
        if ( !rootDirectory.exists() ) {
            rootDirectory.mkdirs();
        }
        if ( !rootDirectory.isDirectory() ) {
            throw new IOException( "The file passed to the constructor has to be a directory: " + rootDirectory );
        }

        // Initialize all writers
        this.newImexAssignedWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "new-imex-assigned.csv" ) ) );
        this.imexErrorWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "imex-errors.csv" ) ) );
        this.intactUpdateWriter = new ReportWriterImpl( new FileWriter( new File( rootDirectory, "intact-update.csv" ) ) );
    }

    public ReportWriter getImexErrorWriter() {
        return imexErrorWriter;
    }

    @Override
    public ReportWriter getIntactUpdateWriter() {
        return intactUpdateWriter;
    }

    @Override
    public ReportWriter getNewImexAssignedWriter(){
        return newImexAssignedWriter;
    }

    //////////////////
    // Closeable

    public void close() throws IOException {
        newImexAssignedWriter.close();
        imexErrorWriter.close();
        intactUpdateWriter.close();
    }
}