package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

/**
 * Processor exception.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ProcessorException extends RuntimeException {

    public ProcessorException() {
        super();
    }

    public ProcessorException( String message ) {
        super( message );
    }

    public ProcessorException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ProcessorException( Throwable cause ) {
        super( cause );
    }
}
