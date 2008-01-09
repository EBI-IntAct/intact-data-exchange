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
public class IntActPsimiTabIndexWriter extends PsimiTabIndexWriter
{

    public IntActPsimiTabIndexWriter()
    {
    }

    @Override
	public Document createDocument(String interaction) throws MitabLineException {
        DocumentBuilder builder = new IntActDocumentBuilder();
        return builder.createDocumentFromPsimiTabLine(interaction);
	}

}
