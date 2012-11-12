package uk.ac.ebi.intact.dataexchange.imex.idassigner.report;

import java.io.Closeable;

/**
 * Report handler interface.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public interface ImexUpdateReportHandler extends Closeable {

    public ReportWriter getImexErrorWriter();

    public ReportWriter getIntactUpdateWriter();

    public ReportWriter getNewImexAssignedWriter();
}
