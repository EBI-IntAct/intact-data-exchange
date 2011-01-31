package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;

import java.io.IOException;

/**
 * Interface for the CCLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface CCLineWriter {

    public void writeCCLine(CCParameters parameters) throws IOException;

    public void close() throws IOException;
}
