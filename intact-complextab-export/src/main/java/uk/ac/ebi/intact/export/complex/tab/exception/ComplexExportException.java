package uk.ac.ebi.intact.export.complex.flat.exception;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ComplexExportException extends Exception {
    public ComplexExportException (String message) {
        super (message);
    }

    public ComplexExportException (Throwable cause) {
        super (cause);
    }

    public ComplexExportException (String message, Throwable cause) {
        super (message, cause);
    }
}
