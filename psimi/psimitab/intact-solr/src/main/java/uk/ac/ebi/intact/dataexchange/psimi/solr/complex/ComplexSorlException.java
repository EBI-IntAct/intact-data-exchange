package uk.ac.ebi.intact.dataexchange.psimi.solr.complex;

/**
 * Exception thrown by the complex solr server
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 31/07/13
 */
public class ComplexSorlException extends Exception {
    /**************************/
    /*      Constructors      */
    /**************************/
    public ComplexSorlException ( ) {
        super() ;
    }

    public ComplexSorlException ( String message ) {
        super( message ) ;
    }

    public ComplexSorlException ( String message, Throwable throwable ) {
        super( message, throwable) ;
    }

    public ComplexSorlException ( Throwable throwable ) {
        super( throwable ) ;
    }
}

