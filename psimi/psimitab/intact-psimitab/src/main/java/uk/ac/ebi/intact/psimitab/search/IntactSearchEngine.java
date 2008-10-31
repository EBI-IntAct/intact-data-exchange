/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.store.Directory;
import psidev.psi.mi.search.engine.impl.AbstractSearchEngine;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.io.File;
import java.io.IOException;

/**
 * A Search Engine based on lucene
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactSearchEngine extends AbstractSearchEngine<IntactBinaryInteraction> {

    public IntactSearchEngine(Directory indexDirectory) throws IOException {
        super(indexDirectory);
    }

    public IntactSearchEngine(File indexDirectory) throws IOException {
        super(indexDirectory);
    }

    public IntactSearchEngine(String indexDirectory) throws IOException {
        super(indexDirectory);
    }

    @Override
    protected DocumentBuilder createDocumentBuilder() {
        return new IntactDocumentBuilder();
    }

    public String[] getSearchFields() {
        DocumentDefinition docDef = new IntactDocumentDefinition();

        return new String[]{"identifier",
                            docDef.getColumnDefinition(IntactDocumentDefinition.PUB_ID).getShortName(),
                            docDef.getColumnDefinition(IntactDocumentDefinition.PUB_AUTH).getShortName(),
                            "species",
                            docDef.getColumnDefinition(IntactDocumentDefinition.INT_TYPE).getShortName(),
                            "type",
                            docDef.getColumnDefinition(IntactDocumentDefinition.INT_DET_METHOD).getShortName(),
                            "detmethod",
                            docDef.getColumnDefinition(IntactDocumentDefinition.INTERACTION_ID).getShortName(),
                            "properties",
                            docDef.getColumnDefinition(IntactDocumentDefinition.HOST_ORGANISM).getShortName(),
                            docDef.getColumnDefinition(IntactDocumentDefinition.EXPANSION_METHOD).getShortName(),
                            docDef.getColumnDefinition(IntactDocumentDefinition.DATASET).getShortName()};
    }
}
