package uk.ac.ebi.intact.util.uniprotExport.writers;

import java.io.IOException;
import java.util.Set;

/**
 * Interfae for the GOLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface GOLineWriter {

    public void writeGOLine(String uniprot1, String uniprot2, Set<String> pubmedIds) throws IOException;

    public void close() throws IOException;
}
