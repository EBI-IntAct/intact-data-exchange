package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverterVersion1;
import uk.ac.ebi.intact.util.uniprotExport.converters.DRLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.drlinewriters.DRLineWriter1;

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
    public DRLineWriter createDRLineWriterFor(DRLineConverter drConverter, OutputStreamWriter outputStream) throws IOException {

        if (drConverter == null){
            return null;
        }

        if (drConverter instanceof DRLineConverterVersion1){
            return new DRLineWriter1(outputStream);
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
    public DRLineWriter createDRLineWriterFor(DRLineConverter drConverter, String outputStream) throws IOException {

        if (drConverter == null){
            return null;
        }

        if (drConverter instanceof DRLineConverterVersion1){
            return new DRLineWriter1(new FileWriter(outputStream));
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
            return new DRLineWriter1(outputStream);
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
            return new DRLineWriter1(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }
}
