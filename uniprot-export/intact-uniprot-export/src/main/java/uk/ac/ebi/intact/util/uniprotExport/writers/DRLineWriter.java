package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;

import java.io.IOException;
import java.util.List;

/**
 * Interface for DRLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface DRLineWriter {

    /**
     * append the DR line to a file (which can be the property of the writer)
     * @param parameters
     * @throws IOException
     */
    public void writeDRLine(DRParameters parameters) throws IOException;

    /**
     * Write all the DR lines in a file which is closed at the end
     * @param DRLines : a list of DR lines
     * @param fileName : the name of the output file
     * @throws IOException
     */
    public void writeDRLines(List<DRParameters> DRLines, String fileName) throws IOException;

    /**
     * Close the current writer
     * @throws IOException
     */
    public void close() throws IOException;

}
