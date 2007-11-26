/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.store.Directory;
import psidev.psi.mi.search.engine.impl.FastSearchEngine;
import psidev.psi.mi.search.util.DocumentBuilder;

import java.io.File;
import java.io.IOException;

/**
 * A Search Engine based on lucene
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActSearchEngine extends FastSearchEngine {

    public IntActSearchEngine(Directory indexDirectory) throws IOException {
        super(indexDirectory);
    }

    public IntActSearchEngine(File indexDirectory) throws IOException {
        super(indexDirectory);
    }

    public IntActSearchEngine(String indexDirectory) throws IOException {
        super(indexDirectory);
    }

    @Override
    protected DocumentBuilder createDocumentBuilder() {
        return new IntActDocumentBuilder();
    }

}
