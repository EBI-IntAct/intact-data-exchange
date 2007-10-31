/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import psidev.psi.mi.search.index.impl.PsimiTabIndexWriter;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0 
 */
public class IntActPsimiTabIndexWriter extends PsimiTabIndexWriter
{

	private static final Log log = LogFactory.getLog(IntActPsimiTabIndexWriter.class);
	
    public IntActPsimiTabIndexWriter()
    {
    }

    @Override
	public Document createDocument(String interaction) throws MitabLineException {
        DocumentBuilder builder = new IntActDocumentBuilder();
        return builder.createDocumentFromPsimiTabLine(interaction);
	}

}
