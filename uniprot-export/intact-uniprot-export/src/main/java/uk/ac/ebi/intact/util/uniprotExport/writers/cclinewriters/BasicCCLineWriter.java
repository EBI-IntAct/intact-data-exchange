package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.BasicCCParameters;

import java.io.IOException;
import java.util.List;

/**
 * Interface for CC line writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface BasicCCLineWriter {

        /**
     * append the CCLine to a file (which can be the property of the writer)
     * @param parameters : contains parameters of the CCLine
     * @throws java.io.IOException
     */
    public void writeCCLine(BasicCCParameters parameters) throws IOException;

    /**
     * Write a list of CC lines (version 1)
     * @param CCLines : a list of CC lines
     * @throws IOException
     */
    public void writeCCLines(List<BasicCCParameters> CCLines) throws IOException;

    /**
     * Close the current writer
     * @throws IOException
     */
    public void close() throws IOException;
}
