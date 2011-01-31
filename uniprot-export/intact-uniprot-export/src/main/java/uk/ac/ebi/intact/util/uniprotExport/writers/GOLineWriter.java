package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;

import java.io.IOException;

/**
 * Interfae for the GOLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface GOLineWriter {

    public void writeGOLine(GOParameters parameters) throws IOException;

    public void close() throws IOException;
}
