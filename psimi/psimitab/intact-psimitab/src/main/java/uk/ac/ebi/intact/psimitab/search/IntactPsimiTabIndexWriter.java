/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;
import psidev.psi.mi.search.index.PsimiIndexWriter;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;

import java.io.IOException;

/**
 * Creates an extended index.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0 
 */
public class IntactPsimiTabIndexWriter extends PsimiIndexWriter {

    public IntactPsimiTabIndexWriter() {
        super(new IntactDocumentBuilder());
    }

    public Document createDocument(String line) throws MitabLineException {
        return getDocumentBuilder().createDocumentFromPsimiTabLine( line );
    }

    public IntactPsimiTabIndexWriter( OntologyIndexSearcher ontologyIndexSearcher, String[] ontologiesToExpand ) throws IOException {
        super( new IntactDocumentBuilder(ontologyIndexSearcher, ontologiesToExpand) );
    }
}
