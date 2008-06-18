/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;
import psidev.psi.mi.search.index.impl.PsimiTabIndexWriter;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;

/**
 * Creates an index from a psi mi tab file
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0 
 */
public class IntactPsimiTabIndexWriter extends PsimiTabIndexWriter
{

    public IntactPsimiTabIndexWriter()
    {
    }

    @Override
	public Document createDocument(String interaction) throws MitabLineException {
        DocumentBuilder builder = new IntactDocumentBuilder();
        return builder.createDocumentFromPsimiTabLine(interaction);
	}

}
