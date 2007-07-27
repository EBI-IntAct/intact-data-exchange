package uk.ac.ebi.intact.application.dataConversion;

import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * PSI-specific implementation of DataBuilder interface. This class will generate a PSI-format file from the data it is
 * supplied with. The implementation is based on the <code>Graph2MIF</code> application written by Henning Mersch -
 * currently much of this code remains 'from the original', and needs to be refactored/fixed when time allows. Many
 * methods have not yet been exercised but will be fixed on demand.
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class PsiDataBuilder implements DataBuilder {

    /**
     * @see uk.ac.ebi.intact.application.dataConversion.DataBuilder
     */
    public void writeData( String fileName, Document docToWrite ) throws DataConversionException {

        if ( docToWrite == null ) {
            throw new IllegalArgumentException( "You must give a non null document." );
        }

        try {
            File f = new File( fileName );
            System.out.println( "writing output to " + f.getAbsolutePath() );
            DOMSource source = null;

            //decide what is to be written
            source = new DOMSource( docToWrite );

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

            StreamResult result = new StreamResult( f );
            transformer.transform( source, result );

        } catch ( TransformerConfigurationException tce ) {
            throw new DataConversionException( "Could not generate file - Transformer config error", tce );

        } catch ( TransformerException te ) {
            throw new DataConversionException( "Could not generate file - Transformer error", te );
        }
    }
}
