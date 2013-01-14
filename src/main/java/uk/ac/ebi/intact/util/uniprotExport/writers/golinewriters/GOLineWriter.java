package uk.ac.ebi.intact.util.uniprotExport.writers.golinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

import java.io.IOException;
import java.util.List;

/**
 * Interface for the GOLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface GOLineWriter<T extends GOParameters> {

    /**
     * Write a GO line
     * @param parameters : the parameters of a GO line
     * @throws IOException
     */
    public void writeGOLine(T parameters) throws IOException;

    /**
     * Write a list GO lines
     * @param GOLines : a list of GO lines
     * @throws IOException
     */
    public void writeGOLines(List<T> GOLines) throws IOException;

    /**
     * Close the current writer
     * @throws IOException
     */
    public void close() throws IOException;
}
