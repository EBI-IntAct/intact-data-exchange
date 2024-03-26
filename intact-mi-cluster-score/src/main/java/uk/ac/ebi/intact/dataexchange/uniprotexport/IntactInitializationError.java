package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntactInitializationError extends Error {

    private static final Log log = LogFactory.getLog( IntactInitializationError.class );


    public IntactInitializationError() {
        super();
    }

    public IntactInitializationError( String message ) {
        super( message );
    }

    public IntactInitializationError( String message, Throwable cause ) {
        super( message, cause );
    }

    public IntactInitializationError( Throwable cause ) {
        super( cause );
    }
}
