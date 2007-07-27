package uk.ac.ebi.intact.application.dataConversion;

import org.w3c.dom.Document;


/**
 * Interface defining the operations required to generate flat files. Implementing classes thus generate flat files of
 * differing formats depending upon their implementation details.
 *
 * @author Chris Lewington
 * @version $Id$
 */
public interface DataBuilder {

    /**
     * This method dumps the data created to the specified file destination.
     *
     * @param fileName   The name of the file to write to.
     * @param docToWrite an XML document to write - if the data held by the builder is not XML or its own Document
     *                   should be written, this parameter should be set to null.
     *
     * @throws DataConversionException thrown if there was a problem generating the file.
     */
    public void writeData( String fileName, Document docToWrite ) throws DataConversionException;
}
