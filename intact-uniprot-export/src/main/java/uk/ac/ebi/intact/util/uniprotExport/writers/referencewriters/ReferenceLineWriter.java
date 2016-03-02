package uk.ac.ebi.intact.util.uniprotExport.writers.referencewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.referencelineparameters.ReferenceParameters;

import java.io.IOException;
import java.util.List;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
public interface ReferenceLineWriter {

    /**
     * append the Reference line to a file (which can be the property of the writer)
     *
     * @param parameters
     * @throws IOException
     */
    public void writeReferenceLine(ReferenceParameters parameters) throws IOException;

    /**
     * Write a list Reference lines
     *
     * @param ReferenceLines : a list of Reference lines
     * @throws IOException
     */
    public void writeReferenceLines(List<ReferenceParameters> ReferenceLines) throws IOException;

    /**
     * Close the current writer
     *
     * @throws IOException
     */
    public void close() throws IOException;

}
