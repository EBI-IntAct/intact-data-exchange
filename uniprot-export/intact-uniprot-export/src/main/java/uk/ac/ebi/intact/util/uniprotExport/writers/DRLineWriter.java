package uk.ac.ebi.intact.util.uniprotExport.writers;

import java.io.IOException;

/**
 * Interface for DRLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface DRLineWriter {

    public void writeDRLine(String uniprotId, int numberOfInteractions) throws IOException;

    public void close() throws IOException;

}
