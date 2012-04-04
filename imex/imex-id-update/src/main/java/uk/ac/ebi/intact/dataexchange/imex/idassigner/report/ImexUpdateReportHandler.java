package uk.ac.ebi.intact.dataexchange.imex.idassigner.report;

import java.io.Closeable;
import java.io.IOException;

/**
 * Report handler interface.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public interface ImexUpdateReportHandler extends Closeable {

    public ReportWriter getProcessedWriter() throws IOException;

    public ReportWriter getProcessImexPublicationWriter();

    public ReportWriter getPublicationUpToDateWriter();

    public ReportWriter getImexIdAssignedToPublicationWriter();

    public ReportWriter getImexIdAssignedToInteractionWriter();

    public ReportWriter getImexIdMismatchFoundWriter();

    public ReportWriter getImexErrorWriter();

}
