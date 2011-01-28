package uk.ac.ebi.intact.util.uniprotExport.miscore.writer;

import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import javax.swing.event.EventListenerList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public abstract class AbstractWriter {

    protected IntActInteractionClusterScore clusterScore;
    protected final static String PUBMED = "pubmed";
    protected final static String UNIPROT = "uniprotkb";
    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected EventListenerList listenerList = new EventListenerList();
    protected final static String TAXID = "taxid";
    protected static final char TABULATION = '\t';

    /**
     * Use to out the CC lines in a file.
     */
    protected Writer writer;

    public AbstractWriter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        this.clusterScore = clusterScore;
        writer = new FileWriter(fileName);
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    public Writer getWriter() {
        return writer;
    }
}
