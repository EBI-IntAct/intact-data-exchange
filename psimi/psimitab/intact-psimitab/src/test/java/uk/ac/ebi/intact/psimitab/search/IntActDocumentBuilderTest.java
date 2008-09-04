/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.junit.Test;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.Column;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.Parameter;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.util.ols.OlsUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * IntActDocumentBuilder Tester.
 * 
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActDocumentBuilderTest{

	@Test
	public void testCreateDocumentFromPsimiTabLine() throws Exception{
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        Assert.assertEquals(62, doc.getFields().size());
	}

	@Test
	public void testCreateBinaryInteraction() throws Exception{
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) builder.createData(doc);
        Assert.assertNotNull(interaction);
	}

    @Test
	public void testCreateBinaryInteraction_extension2() throws Exception{
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tcomment:commentA\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) builder.createData(doc);
        Assert.assertNotNull(interaction);

        final Annotation annotationA = interaction.getInteractorA().getAnnotations().iterator().next();
        Assert.assertEquals("comment", annotationA.getType());
        Assert.assertEquals("commentA", annotationA.getText());
	}


    @Test
	public void testCreateBinaryInteraction_extension3() throws Exception{
		String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tcomment:commentA\t-\tic50A:100(molar)\t-\tic50C:300(molar)";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine(psiMiTabLine);

        IntactBinaryInteraction interaction = (IntactBinaryInteraction) builder.createData(doc);
        Assert.assertNotNull(interaction);

        final Annotation annotationA = interaction.getInteractorA().getAnnotations().iterator().next();
        Assert.assertEquals("comment", annotationA.getType());
        Assert.assertEquals("commentA", annotationA.getText());

        final Parameter parameterA = interaction.getInteractorA().getParameters().iterator().next();
        Assert.assertEquals("ic50A", parameterA.getType());
        Assert.assertEquals("100", parameterA.getValue());
        Assert.assertEquals("molar", parameterA.getUnit());


    }

    @Test
    public void testGetColumnWithParents() throws Exception {

        //Type 1
        Field intTypeFieldType1 = new Field( "MI", "0407", "direct interaction" );
        List<Field> fieldList = new ArrayList<Field>();
        fieldList.add( intTypeFieldType1 );
        Column intTypeCol = new Column( fieldList );
        //MI:0407(direct interaction)
        Assert.assertEquals( 1, intTypeCol.toString().split( "\\|" ).length );

        IntactDocumentBuilder builder = new IntactDocumentBuilder(true);
        Column intTypeColWithParents = builder.getColumnWithParents( intTypeCol, OlsUtils.PSI_MI_ONTOLOGY );
        //MI:0407(direct interaction)|MI:0915(physical association)|MI:0190(interaction type)|MI:0914(association)
        Assert.assertEquals( 4, intTypeColWithParents.toString().split( "\\|" ).length );


        //Type2
        Field intTypeFieldType2 = new Field( "psi-mi", "MI:0407", "direct interaction" );
        List<Field> fieldList_ = new ArrayList<Field>();
        fieldList_.add( intTypeFieldType2 );
        Column intTypeCol_ = new Column( fieldList_ );
        //MI:0407(direct interaction)
        Assert.assertEquals( 1, intTypeCol_.toString().split( "\\|" ).length );


        Column intTypeColWithParents_ = builder.getColumnWithParents( intTypeCol, OlsUtils.PSI_MI_ONTOLOGY );
        //MI:0407(direct interaction)|MI:0915(physical association)|MI:0190(interaction type)|MI:0914(association)
        Assert.assertEquals( 4, intTypeColWithParents_.toString().split( "\\|" ).length );

    }


    @Test
    public void testFieldsWithParentsForMI() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder(true);

        //interaction type MI:0407(direct interaction)
        List<Field> fieldsWithParentsIntType = builder.getListOfFieldsWithParents( "MI:0407", OlsUtils.PSI_MI_ONTOLOGY,true );

        Assert.assertNotNull( fieldsWithParentsIntType );
        Assert.assertEquals( 4, fieldsWithParentsIntType.size() );

        Column colWithParentsIntType = new Column( fieldsWithParentsIntType );
        //"MI:0407(direct interaction)|MI:0190(interaction type)|MI:0914(association)|MI:0915(physical association)

        String colsIntType[] = colWithParentsIntType.toString().split( "\\|" );

        int countIntType = 0;
        for ( String col : colsIntType ) {
            if ( col.equals( "MI:0407(direct interaction)" ) ) {
                countIntType++;
            }
            if ( col.equals( "MI:0190(interaction type)" ) ) {
                countIntType++;
            }
            if ( col.equals( "MI:0914(association)" ) ) {
                countIntType++;
            }
            if ( col.equals( "MI:0915(physical association)" ) ) {
                countIntType++;
            }
        }
        Assert.assertEquals( 4, countIntType );

        //interaction detection method MI:0045 (experimental interaction detection)
        List<Field> fieldsWithParentsDetMethod = builder.getListOfFieldsWithParents( "MI:0045", OlsUtils.PSI_MI_ONTOLOGY,true );

        Assert.assertNotNull( fieldsWithParentsDetMethod );
        Assert.assertEquals( 2, fieldsWithParentsDetMethod.size() );


        Column colWithParentsDetMethod = new Column( fieldsWithParentsDetMethod );
        //"MI:0045(experimental interaction detection)|MI:0001(interaction detection method)"
        String[] colsDetMethod = colWithParentsDetMethod.toString().split( "\\|" );

        int countDetMethod = 0;
        for ( String col : colsDetMethod ) {
            if ( col.equals( "MI:0045(experimental interaction detection)" ) ) {
                countDetMethod++;
            }
            if ( col.equals( "MI:0001(interaction detection method)" ) ) {
                countDetMethod++;
            }

        }

        Assert.assertEquals( 2, countDetMethod );

    }


    @Test
    public void testFieldsWithParentsForGO() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder(true);

        //String goIdentifier = "GO:0030056";
        String goIdentifier = "GO:0005634"; //nucleus

        List<Field> fieldsWithParentsProperties = builder.getListOfFieldsWithParents( goIdentifier, OlsUtils.GO_ONTOLOGY, true );


        Assert.assertNotNull( fieldsWithParentsProperties );
        //term+8 parents excludes root == 9
        Assert.assertEquals( 9, fieldsWithParentsProperties.size() );


        Column colWithParents = new Column(fieldsWithParentsProperties);
        final String[] strings = colWithParents.toString().split( "\\|" );

         /*GO:0005634(nucleus)
         GO:0043229(intracellular organelle)
         GO:0044464(cell part)
         GO:0043226(organelle)
         GO:0044424(intracellular part)
         GO:0043227(membrane-bounded organelle)
         GO:0005622(intracellular)
         GO:0005623(cell)
         GO:0043231(intracellular membrane-bounded organelle)
        */

        int countProperty = 0;

        for ( String string : strings ) {

            if(string.equals( "GO:0005634(nucleus)" )){
              countProperty++;  
            }
            if(string.equals( "GO:0044464(cell part)" )){
              countProperty++;
            }
            if(string.equals( "GO:0043226(organelle)" )){
              countProperty++;
            }
            if(string.equals( "GO:0044424(intracellular part)" )){
              countProperty++;
            }
            if(string.equals( "GO:0043227(membrane-bounded organelle)" )){
              countProperty++;
            }
            if(string.equals( "GO:0005622(intracellular)" )){
              countProperty++;
            }
            if(string.equals( "GO:0005623(cell)" )){
              countProperty++;
            }
            if(string.equals( "GO:0043231(intracellular membrane-bounded organelle)" )){
              countProperty++;
            }
        }

       Assert.assertEquals(8,countProperty);

    }


}
