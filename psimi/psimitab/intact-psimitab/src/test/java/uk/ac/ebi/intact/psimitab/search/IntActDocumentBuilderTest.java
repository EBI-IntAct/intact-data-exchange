/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * @author Nadin
 *
 */
public class IntActDocumentBuilderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testCreateDocumentFromPsimiTabLine() throws Exception{
		
		String psiMiTabLine = "chebi:CHEBI:38918	uniprotkb:A2RQD6	-	uniprotkb:BCR-ABL1(gene name)	-	-	MI:0405(competition binding)	Bantscheff et al (2007)	pubmed:17721511	-	taxid:9606(human)	MI:0218(physical interaction)	MI:0469(intact)	intact:EBI-1380444	-	MI:0499(unspecified role)	MI:0498(prey)	intact:EBI-1380410	interpro:IPR000219|interpro:IPR000980|interpro:IPR001452|intact:EBI-1380551	MI:0328(small molecule)	MI:0326(protein)	in vitro:-1";
		Document doc = IntActDocumentBuilder.createDocumentFromPsimiTabLine(psiMiTabLine);
//		for (int i = 0; i < doc.getFields().size(); i++){
//			Field field = (Field)doc.getFields().get(i);
//			System.out.print(field.name() +"\t");
//		}
		Assert.assertEquals(34+14, doc.getFields().size());
	}
	
	
	
	@Test
	public void testCreateBinaryInteraction() throws Exception{
	
		String psiMiTabLine = "chebi:CHEBI:38918	uniprotkb:A2RQD6	-	uniprotkb:BCR-ABL1(gene name)	-	-	MI:0405(competition binding)	Bantscheff et al (2007)	pubmed:17721511	-	taxid:9606(human)	MI:0218(physical interaction)	MI:0469(intact)	intact:EBI-1380444	-	MI:0499(unspecified role)	MI:0498(prey)	intact:EBI-1380410	interpro:IPR000219|interpro:IPR000980|interpro:IPR001452|intact:EBI-1380551	MI:0328(small molecule)	MI:0326(protein)	in vitro:-1";
		Document doc = IntActDocumentBuilder.createDocumentFromPsimiTabLine(psiMiTabLine);

		BinaryInteraction interaction = IntActDocumentBuilder.createBinaryInteraction(doc);
		Assert.assertNotNull(interaction);

	}

}
