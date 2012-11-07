package uk.ac.ebi.intact.task;

/**
 * A task exception.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class TaskException extends Exception {
    public TaskException() {
        super();
    }

    public TaskException( String message ) {
        super( message );
    }

    public TaskException( String message, Throwable cause ) {
        super( message, cause );
    }

    public TaskException( Throwable cause ) {
        super( cause );
    }
}
