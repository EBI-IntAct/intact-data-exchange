package uk.ac.ebi.intact.util.uniprotExport.miscore.writer;

import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public abstract class AbstractConverter {

    protected IntActInteractionClusterScore clusterScore;

    protected String fileName;

    public AbstractConverter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        this.clusterScore = clusterScore;
        this.fileName = fileName;
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    public String getFileName() {
        return this.fileName;
    }
}
