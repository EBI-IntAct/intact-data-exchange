package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameter;

import java.io.IOException;

/**
 * Interface for DRLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface DRLineWriter {

    public void writeDRLine(DRParameter parameters) throws IOException;

    public void close() throws IOException;

}
