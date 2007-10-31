/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.junit.Test;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * IntActDocumentBuilder Tester.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActDocumentBuilderTest {

	@Test
	public void testCreateDocumentFromPsimiTabLine() throws Exception{
		String psiMiTabLine = "chebi:CHEBI:38918|intact:EBI-1380410\tuniprotkb:A2RQD6|intact:EBI-1380551\t-\tunknown:BCR-ABL1(gene name)\t-\t-\tMI:0405(competition binding)\tBantscheff et al. (2007)\tpubmed:17721511\t-\ttaxid:9606(human)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-1380444\t-\tMI:0499(unspecified role)\tMI:0498(prey)\tintact:EBI-1380410\tinterpro:IPR000219|interpro:IPR000980|interpro:IPR001452|intact:EBI-1380551\tMI:0328(small molecule)\tMI:0326(protein)\tin vitro:-1\tSpoke";
        DocumentBuilder builder = new IntActDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);
		Assert.assertEquals(53, doc.getFields().size());
	}

	@Test
	public void testCreateBinaryInteraction() throws Exception{
		String psiMiTabLine = "chebi:CHEBI:38918|intact:EBI-1380410\tuniprotkb:A2RQD6|intact:EBI-1380551\t-\tunknown:BCR-ABL1(gene name)\t-\t-\tMI:0405(competition binding)\tBantscheff et al. (2007)\tpubmed:17721511\t-\ttaxid:9606(human)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-1380444\t-\tMI:0499(unspecified role)\tMI:0498(prey)\tintact:EBI-1380410\tinterpro:IPR000219|interpro:IPR000980|interpro:IPR001452|intact:EBI-1380551\tMI:0328(small molecule)\tMI:0326(protein)\tin vitro:-1\tSpoke";
        DocumentBuilder builder = new IntActDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        BinaryInteraction interaction = builder.createBinaryInteraction(doc);
		Assert.assertNotNull(interaction);
	}
}
