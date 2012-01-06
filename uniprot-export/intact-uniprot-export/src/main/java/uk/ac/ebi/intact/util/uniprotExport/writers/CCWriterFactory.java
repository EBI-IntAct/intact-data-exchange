package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter2;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter1;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A factory which returns the valid CCWriter depending on the give cc converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class CCWriterFactory {

    /**
     *
     * @param ccConverter
     * @param outputStream : the output of the CC line writer
     * @return a CCLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public CCLineWriter createCCLineWriterFor(CCLineConverter ccConverter, OutputStreamWriter outputStream) throws IOException {

        if (ccConverter == null){
            return null;
        }

        if (ccConverter instanceof CCLineConverter1){
            return new CCLineWriter1(outputStream);
        }
        else if (ccConverter instanceof CCLineConverter2){
            return new CCLineWriter2(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param ccConverter
     * @param outputStream : the output file name of the CC line writer
     * @return a CCLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public CCLineWriter createCCLineWriterFor(CCLineConverter ccConverter, String outputStream) throws IOException {

        if (ccConverter == null){
            return null;
        }

        if (ccConverter instanceof CCLineConverter1){
            return new CCLineWriter1(new FileWriter(outputStream));
        }
        else if (ccConverter instanceof CCLineConverter2){
            return new CCLineWriter2(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output of the CC line writer
     * @return a CCLine writer which is compatible with this version of the CC line format, null otherwise
     * @throws IOException
     */
    public CCLineWriter createCCLineWriterFor(int version, OutputStreamWriter outputStream) throws IOException {

        if (version == 1){
            return new CCLineWriter1(outputStream);
        }
        else if (version == 2){
            return new CCLineWriter2(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output file name of the CC line writer
     * @return a CCLine writer which is compatible with this version of the CC line format, null otherwise
     * @throws IOException
     */
    public CCLineWriter createCCLineWriterFor(int version, String outputStream) throws IOException {

        if (version == 1){
            return new CCLineWriter1(new FileWriter(outputStream));
        }
        else if (version == 2){
            return new CCLineWriter2(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }
}
