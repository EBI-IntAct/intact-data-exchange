package uk.ac.ebi.intact.application.dataConversion;

/**
 * Created by IntelliJ IDEA.
 * User: clewington
 * Date: 19-Nov-2003
 * Time: 11:59:34
 * To change this template use Options | File Templates.
 */
public class DataConversionException extends Exception {

    private String nestedMessage;
    private Exception rootCause;

    public DataConversionException() {
    }

    public DataConversionException( String msg ) {

        super( msg );
    }

    public DataConversionException( String msg, Exception e ) {

        super( msg );

        if ( e != null ) {
            e.fillInStackTrace();
            nestedMessage = e.getMessage();
            rootCause = e;
        }
    }

    public String getNestedMessage() {

        if ( nestedMessage != null ) {

            return nestedMessage;
        } else {

            return "No nested messages have been passed on.";
        }
    }

    public boolean rootCauseExists() {
        return ( rootCause != null );
    }

    public Exception getRootCause() {
        return rootCause;
    }

}
