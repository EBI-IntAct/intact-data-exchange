package uk.ac.ebi.intact.util.uniprotExport.writers;

import uk.ac.ebi.intact.util.uniprotExport.CcLine;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for the CCLine writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public interface CCLineWriter {

    public void writeCCLine(String uniprotId, List<CcLine> ccLines) throws IOException;

    public CcLine createCCline(String uniprot1, String geneName1, String taxId1, String uniprot2,
                               String geneName2, String taxId2, String organismName2,
                               Map<Map.Entry<String, String>, Set<String>> trueBinaryInteractionDetails,
                               Map<Map.Entry<String, String>, Set<String>> spokeExpandedInteractionDetails);

    public void close() throws IOException;
}
