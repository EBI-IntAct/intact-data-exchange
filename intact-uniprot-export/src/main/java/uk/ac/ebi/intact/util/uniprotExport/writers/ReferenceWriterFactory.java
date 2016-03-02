package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.converters.ReferenceLineConverter;
import uk.ac.ebi.intact.util.uniprotExport.writers.referencewriters.ReferenceLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.referencewriters.ReferenceLineWriter1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public class ReferenceWriterFactory {

    /**
     * @param referenceLineConverter
     * @param outputStream           : the output of the Reference line writer
     * @return a ReferenceLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public ReferenceLineWriter createReferenceLineWriterFor(ReferenceLineConverter referenceLineConverter, OutputStreamWriter outputStream) throws IOException {

        if (referenceLineConverter == null) {
            return null;
        }

        if (referenceLineConverter instanceof ReferenceLineWriter1) {
            return new ReferenceLineWriter1(outputStream);
        } else {
            return null;
        }
    }

    /**
     * @param referenceLineConverter
     * @param outputStream           : the output file name of the reference line writer
     * @return a referenceLine writer which is compatible with this converter, null otherwise
     * @throws IOException
     */
    public ReferenceLineWriter createReferenceLineWriterFor(ReferenceLineConverter referenceLineConverter, String outputStream) throws IOException {

        if (referenceLineConverter == null) {
            return null;
        }

        if (referenceLineConverter instanceof ReferenceLineWriter1) {
            return new ReferenceLineWriter1(new FileWriter(outputStream));
        } else {
            return null;
        }
    }

    /**
     * @param version
     * @param outputStream : the output of the reference line writer
     * @return a referenceLine writer which is compatible with this version of the reference line format, null otherwise
     * @throws IOException
     */
    public ReferenceLineWriter createReferenceLineWriterFor(int version, OutputStreamWriter outputStream) throws IOException {

        if (version == 1) {
            return new ReferenceLineWriter1(outputStream);
        } else {
            return null;
        }
    }

    /**
     * @param version
     * @param outputStream : the output file name of the reference line writer
     * @return a referenceLine writer which is compatible with this version of the reference line format, null otherwise
     * @throws IOException
     */
    public ReferenceLineWriter createReferenceLineWriterFor(int version, String outputStream) throws IOException {

        if (version == 1) {
            return new ReferenceLineWriter1(new FileWriter(outputStream));
        } else {
            return null;
        }
    }
}
