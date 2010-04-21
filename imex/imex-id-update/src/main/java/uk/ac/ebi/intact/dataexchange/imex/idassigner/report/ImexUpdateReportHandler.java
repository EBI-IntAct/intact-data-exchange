package uk.ac.ebi.intact.dataexchange.imex.idassigner.report;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.ReportWriter;

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

    ReportWriter getProcessedWriter() throws IOException;

    ReportWriter getProcessImexPublicationWriter();

    ReportWriter getPublicationUpToDateWriter();

    ReportWriter getImexIdAssignedToPublicationWriter();

    ReportWriter getImexIdAssignedToInteractionWriter();

    ReportWriter getImexIdMismatchFoundWriter();
}
