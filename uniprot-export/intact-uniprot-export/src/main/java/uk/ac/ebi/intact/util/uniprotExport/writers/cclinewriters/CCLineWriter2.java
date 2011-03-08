package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters2;

import java.io.IOException;

/**
 * Interface for the CCLine writers, version 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface CCLineWriter2 extends BasicCCLineWriter{

    /**
     * append the CCLine to a file (which can be the property of the writer)
     * @param parameters : contains parameters of the CCLine
     * @throws IOException
     */
    public void writeCCLine(CCParameters2 parameters) throws IOException;
}
