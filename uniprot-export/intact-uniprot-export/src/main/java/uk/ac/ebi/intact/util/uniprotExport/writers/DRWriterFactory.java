package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.DefaultInteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.InteractorToDRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DefaultDRLineWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A factory which returns the proper DR line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class DRWriterFactory {

    /**
     *
     * @param drConverter
     * @param outputStream : the output of the DR line writer
     * @return a DRLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public DRLineWriter createDRLineWriterFor(InteractorToDRLineConverter drConverter, OutputStreamWriter outputStream) throws IOException {

        if (drConverter == null){
            return null;
        }

        if (drConverter instanceof DefaultInteractorToDRLineConverter){
            return new DefaultDRLineWriter(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param drConverter
     * @param outputStream : the output file name of the DR line writer
     * @return a DRLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public DRLineWriter createDRLineWriterFor(InteractorToDRLineConverter drConverter, String outputStream) throws IOException {

        if (drConverter == null){
            return null;
        }

        if (drConverter instanceof DefaultInteractorToDRLineConverter){
            return new DefaultDRLineWriter(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output of the DR line writer
     * @return a DRLine writer which is compatible with this version of the DR line format, null otherwise
     * @throws IOException
     */
    public DRLineWriter createDRLineWriterFor(int version, OutputStreamWriter outputStream) throws IOException {

        if (version == 1){
            return new DefaultDRLineWriter(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output file name of the DR line writer
     * @return a DRLine writer which is compatible with this version of the DR line format, null otherwise
     * @throws IOException
     */
    public DRLineWriter createDRLineWriterFor(int version, String outputStream) throws IOException {

        if (version == 1){
            return new DefaultDRLineWriter(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }
}
