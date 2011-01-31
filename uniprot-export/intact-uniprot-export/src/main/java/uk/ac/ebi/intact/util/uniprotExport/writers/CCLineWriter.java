package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;

import java.io.IOException;
import java.util.List;

/**
 * Interface for the CCLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface CCLineWriter {

    /**
     * append the CCLine to a file (which can be the property of the writer)
     * @param parameters : contains parameters of the CCLine
     * @throws IOException
     */
    public void writeCCLine(CCParameters parameters) throws IOException;

    /**
     * Write a list of CC lines
     * @param CCLines : a list of CC lines
     * @throws IOException
     */
    public void writeCCLines(List<CCParameters> CCLines) throws IOException;

    /**
     * Close the current writer
     * @throws IOException
     */
    public void close() throws IOException;
}
