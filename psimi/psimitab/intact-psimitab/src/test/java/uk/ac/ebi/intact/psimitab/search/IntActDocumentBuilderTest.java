/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.junit.Test;
import psidev.psi.mi.search.util.DocumentBuilder;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

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
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        Assert.assertEquals(59, doc.getFields().size());
	}

	@Test
	public void testCreateBinaryInteraction() throws Exception{
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) builder.createBinaryInteraction(doc);
        Assert.assertNotNull(interaction);
	}
}
