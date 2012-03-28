package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

/**
 * Exception to throw when cannot update an IMEx record
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/03/12</pre>
 */

public class PublicationImexUpdaterException extends Exception {

    public PublicationImexUpdaterException() {
        super();
    }

    public PublicationImexUpdaterException(String s) {
        super(s);
    }

    public PublicationImexUpdaterException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PublicationImexUpdaterException(Throwable throwable) {
        super(throwable);
    }
}
