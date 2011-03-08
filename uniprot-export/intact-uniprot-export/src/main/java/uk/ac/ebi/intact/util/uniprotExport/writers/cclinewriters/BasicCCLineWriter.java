package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import java.io.IOException;

/**
 * Interface for CC line writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public interface BasicCCLineWriter {

    /**
     * Close the current writer
     * @throws IOException
     */
    public void close() throws IOException;
}
