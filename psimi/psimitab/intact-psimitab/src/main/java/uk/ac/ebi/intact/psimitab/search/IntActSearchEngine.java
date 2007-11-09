/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import psidev.psi.mi.search.engine.impl.FastSearchEngine;
import psidev.psi.mi.search.util.DocumentBuilder;

import java.io.File;
import java.io.IOException;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActSearchEngine extends FastSearchEngine {

    private static final Log log = LogFactory.getLog( IntActSearchEngine.class);

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
        DocumentBuilder builder = new IntActDocumentBuilder();
        return builder;
    }

}
