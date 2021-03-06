package uk.ac.ebi.intact.util.uniprotExport;

/**
 * TException thrown when the uniprot export fails
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Oct-2010</pre>
 */

public class UniprotExportException extends Exception{

    public UniprotExportException() {
        super();  
    }

    public UniprotExportException(String message) {
        super(message);
    }

    public UniprotExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniprotExportException(Throwable cause) {
        super(cause);
    }
}
