package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverter1;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters.CCLineWriter1;

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
     * @param ccConverter uniprot CC converter matching the version (with 2019 specification there is only one version)
     * @param outputStream : the output of the CC line writer
     * @return a CCLine writer which is compatible with this converter, null otherwise
     * @throws IOException in case of problems writing
     */
    public CCLineWriter createCCLineWriterFor(CCLineConverter ccConverter, OutputStreamWriter outputStream) throws IOException {

        if (ccConverter == null){
            return null;
        }

        if (ccConverter instanceof CCLineConverter1){
            return new CCLineWriter1(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param ccConverter uniprot CC converter matching the version (with 2019 specification there is only one version)
     * @param outputStream : the output file name of the CC line writer
     * @return a CCLine writer which is compatible with this converter, null otherwise
     * @throws IOException in case of problems writing
     */
    public CCLineWriter createCCLineWriterFor(CCLineConverter ccConverter, String outputStream) throws IOException {

        if (ccConverter == null){
            return null;
        }

        if (ccConverter instanceof CCLineConverter1){
            return new CCLineWriter1(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version uniprot CC line version (with 2019 specification there is only one version)
     * @param outputStream : the output of the CC line writer
     * @return a CCLine writer which is compatible with this version of the CC line format, null otherwise
     * @throws IOException in case of problems writing
     */
    public CCLineWriter createCCLineWriterFor(int version, OutputStreamWriter outputStream) throws IOException {

        if (version == 1){
            return new CCLineWriter1(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version uniprot CC line version (with 2019 specification there is only one version)
     * @param outputStream : the output file name of the CC line writer
     * @return a CCLine writer which is compatible with this version of the CC line format, null otherwise
     * @throws IOException in case of problems writing
     */
    public CCLineWriter createCCLineWriterFor(int version, String outputStream) throws IOException {

        if (version == 1){
            return new CCLineWriter1(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }
}
