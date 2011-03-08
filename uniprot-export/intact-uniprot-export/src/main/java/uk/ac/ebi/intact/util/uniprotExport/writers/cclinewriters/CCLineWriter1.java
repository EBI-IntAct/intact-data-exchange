package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters1;

import java.io.IOException;

/**
 * CCLineWriters1 are able to write CCParameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public interface CCLineWriter1 extends BasicCCLineWriter{

    /**
     * append the CCLine to a file (which can be the property of the writer)
     * @param parameters : contains parameters of the CCLine
     * @throws java.io.IOException
     */
    public void writeCCLine(CCParameters1 parameters) throws IOException;
}
