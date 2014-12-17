package uk.ac.ebi.intact.dataexchange.dbimporter.listener;

/**
 * Exception thrown when the db importer does not support a parsing error
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/12/14</pre>
 */

public class IntactImporterException extends RuntimeException {

    public IntactImporterException() {
        super();
    }

    public IntactImporterException(String message) {
        super(message);
    }

    public IntactImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntactImporterException(Throwable cause) {
        super(cause);
    }

    protected IntactImporterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
