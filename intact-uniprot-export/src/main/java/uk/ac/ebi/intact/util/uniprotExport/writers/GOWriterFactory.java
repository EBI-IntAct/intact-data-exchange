package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverterVersion1;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.GoLineConverterVersion2;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriter1;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriter2;
import uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters.GOLineWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * A factory which creates the proper GO writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class GOWriterFactory {

    /**
     *
     * @param goConverter
     * @param outputStream : the output of the GO line writer
     * @return a GOLine writer which is compatible with this converter, null otherwise
     * @throws java.io.IOException
     */
    public GOLineWriter createGOLineWriterFor(GoLineConverter goConverter, OutputStreamWriter outputStream) throws IOException {

        if (goConverter == null){
            return null;
        }

        if (goConverter instanceof GoLineConverterVersion1){
            return new GOLineWriter1(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param goConverter
     * @param outputStream : the output file name of the GO line writer
     * @return a GOLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public GOLineWriter createGOLineWriterFor(GoLineConverter goConverter, String outputStream) throws IOException {

        if (goConverter == null){
            return null;
        }

        if (goConverter instanceof GoLineConverterVersion1){
            return new GOLineWriter1(new FileWriter(outputStream));
        }
        else if (goConverter instanceof GoLineConverterVersion2){
            return new GOLineWriter2(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output of the DR line writer
     * @return a GOLine writer which is compatible with this version of the GO line format, null otherwise
     * @throws IOException
     */
    public GOLineWriter createGOLineWriterFor(int version, OutputStreamWriter outputStream) throws IOException {

        if (version == 1){
            return new GOLineWriter1(outputStream);
        }
        else if (version == 2){
            return new GOLineWriter2(outputStream);
        }
        else {
            return null;
        }
    }

    /**
     *
     * @param version
     * @param outputStream : the output file name of the DR line writer
     * @return a GOLine writer which is compatible with this version of the GO line format, null otherwise
     * @throws IOException
     */
    public GOLineWriter createGOLineWriterFor(int version, String outputStream) throws IOException {

        if (version == 1){
            return new GOLineWriter1(new FileWriter(outputStream));
        }
        else if (version == 2){
            return new GOLineWriter2(new FileWriter(outputStream));
        }
        else {
            return null;
        }
    }
}
