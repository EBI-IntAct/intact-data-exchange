package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters1;

import java.io.IOException;
import java.util.List;

/**
 * CCLineWriters1 are able to write CCParameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public interface CCLineWriter1 {

    /**
     * append the CCLine to a file (which can be the property of the writer)
     * @param parameters : contains parameters of the CCLine
     * @throws java.io.IOException
     */
    public void writeCCLine(CCParameters1 parameters) throws IOException;

    /**
     * Write a list of CC lines (version 1)
     * @param CCLines : a list of CC lines
     * @throws IOException
     */
    public void writeCCLines(List<CCParameters1> CCLines) throws IOException;

    /**
     * Close the current writer (version 2)
     * @throws IOException
     */
    public void close() throws IOException;
}
