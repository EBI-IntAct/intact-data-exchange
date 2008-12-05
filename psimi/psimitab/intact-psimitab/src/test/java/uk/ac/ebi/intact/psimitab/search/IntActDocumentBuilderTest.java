/**
 *
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;
import org.apache.lucene.document.*;
import org.apache.lucene.store.Directory;
import org.junit.*;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.model.builder.*;
import psidev.psi.mi.tab.model.builder.Field;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.rsc.RelevanceScoreCalculator;
import uk.ac.ebi.intact.psimitab.rsc.RelevanceScoreCalculatorImpl;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.model.Parameter;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * IntActDocumentBuilder Tester.
 *
 * @version $Id$
 * @since 2.0.0
 */
public class IntActDocumentBuilderTest {

    private static Directory ontologyDirectory;
    private OntologyIndexSearcher ontologyIndexSearcher;

    @BeforeClass
    public static void buildIndex() throws Exception {
        ontologyDirectory = TestHelper.buildDefaultOntologiesIndex();
    }

    @AfterClass
    public static void afterMath() throws Exception {
        ontologyDirectory.close();
    }

    @Before
    public void before() throws Exception {
        this.ontologyIndexSearcher = new OntologyIndexSearcher(ontologyDirectory);
    }

    @After
    public void after() throws Exception {
        if (ontologyIndexSearcher != null) {
            this.ontologyIndexSearcher.close();
            ontologyIndexSearcher = null;
        }
    }

    @Test
    public void testCreateDocumentFromPsimiTabLine() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine( psiMiTabLine );

        Assert.assertEquals( 66, doc.getFields().size() );
    }

    @Test
    public void testCreateBinaryInteraction() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine( psiMiTabLine );

        IntactBinaryInteraction interaction = ( IntactBinaryInteraction ) builder.createData( doc );
        Assert.assertNotNull( interaction );
    }

    @Test
    public void testCreateBinaryInteraction_extension2() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tcomment:commentA\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine( psiMiTabLine );

        IntactBinaryInteraction interaction = ( IntactBinaryInteraction ) builder.createData( doc );
        Assert.assertNotNull( interaction );

        final Annotation annotationA = interaction.getInteractorA().getAnnotations().iterator().next();
        Assert.assertEquals( "comment", annotationA.getType() );
        Assert.assertEquals( "commentA", annotationA.getText() );
    }

    @Test
    public void testCreateBinaryInteraction_extension3() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tcomment:commentA\t-\tic50A:100(molar)\t-\tic50C:300(molar)";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine( psiMiTabLine );

        IntactBinaryInteraction interaction = ( IntactBinaryInteraction ) builder.createData( doc );
        Assert.assertNotNull( interaction );

        final Annotation annotationA = interaction.getInteractorA().getAnnotations().iterator().next();
        Assert.assertEquals( "comment", annotationA.getType() );
        Assert.assertEquals( "commentA", annotationA.getText() );

        final Parameter parameterA = interaction.getInteractorA().getParameters().iterator().next();
        Assert.assertEquals( "ic50A", parameterA.getType() );
        Assert.assertEquals( "100", parameterA.getValue() );
        Assert.assertEquals( "molar", parameterA.getUnit() );
    }

    @Test
    public void testGetColumnWithParents() throws Exception {

        //Type 1
        Field intTypeFieldType1 = new Field( "psi-mi", "MI:0407", "direct interaction" );
        Column intTypeCol = new Column( Arrays.asList( intTypeFieldType1 ) );

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyIndexSearcher, new String[] {"go", "interpro", "psi-mi"});
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        Column intTypeColWithParents = builder.getColumnWithParents( intTypeCol );
        //MI:0407(direct interaction)|MI:0915(physical association)|MI:0190(interaction type)|MI:0914(association)
        Assert.assertEquals( 4, intTypeColWithParents.getFields().size() );
    }

    @Test
    public void testFieldsWithParentsForMI_interactionType() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyIndexSearcher, new String[] {"go", "interpro", "psi-mi"});
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        //interaction type MI:0407(direct interaction)
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsIntType = builder.getListOfFieldsWithParents( fieldBuilder.createField( "psi-mi:\"MI:0407\"(direct interaction)" ) );

        Assert.assertNotNull( fieldsWithParentsIntType );
        Assert.assertEquals( 4, fieldsWithParentsIntType.size() );

        CrossReferenceFieldBuilder fb = new CrossReferenceFieldBuilder();
        Assert.assertTrue( fieldsWithParentsIntType.contains( fb.createField( "psi-mi:\"MI:0190\"(interaction type)" ) ) );
        Assert.assertTrue( fieldsWithParentsIntType.contains( fb.createField( "psi-mi:\"MI:0915\"(physical association)" ) ) );
        Assert.assertTrue( fieldsWithParentsIntType.contains( fb.createField( "psi-mi:\"MI:0914\"(association)" ) ) );
        Assert.assertTrue( fieldsWithParentsIntType.contains( fb.createField( "psi-mi:\"MI:0407\"(direct interaction)" ) ) );
    }

    @Test
    public void testFieldsWithParentsForMI_detectionMethod() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyIndexSearcher, new String[] {"go", "interpro", "psi-mi"});
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        //interaction detection method MI:0045 (experimental interaction detection)
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsDetMethod = builder.getListOfFieldsWithParents( fieldBuilder.createField( "psi-mi:\"MI:0045\"(experimental interaction detection)" ) );

        Assert.assertNotNull( fieldsWithParentsDetMethod );
        Assert.assertEquals( 2, fieldsWithParentsDetMethod.size() );

        CrossReferenceFieldBuilder fb = new CrossReferenceFieldBuilder();
        Assert.assertTrue( fieldsWithParentsDetMethod.contains( fb.createField( "psi-mi:\"MI:0001\"(interaction detection method)" ) ) );
        Assert.assertTrue( fieldsWithParentsDetMethod.contains( fb.createField( "psi-mi:\"MI:0045\"(experimental interaction detection)" ) ) );
    }

    @Test
    public void testFieldsWithParentsForGO() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyIndexSearcher, new String[] {"go", "interpro", "psi-mi"});
        builder.addExpandableOntology( "GO" );

        //String goIdentifier = "go:\"GO:0030056";
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsProperties = builder.getListOfFieldsWithParents( fieldBuilder.createField( "go:\"GO:0005634\"(nucleus)" ) );

        Assert.assertNotNull( fieldsWithParentsProperties );
        //term+8 parents excludes root == 10
        Assert.assertEquals( 8, fieldsWithParentsProperties.size() );

        CrossReferenceFieldBuilder fb = new CrossReferenceFieldBuilder();
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0005634\"(nucleus)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0044464\"(cell part)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0043226\"(organelle)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0044424\"(intracellular part)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0043227\"(membrane-bounded organelle)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0043231\"(intracellular membrane-bounded organelle)" ) ) );
        Assert.assertTrue( fieldsWithParentsProperties.contains( fb.createField( "go:\"GO:0005575\"(cellular_component)") ) );
    }

    @Test
    public void testCreateDocumentFromPsimiTabLineWithNameAndScore() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        final IntactDocumentDefinition documentDefinition = new IntactDocumentDefinition();
        final RowBuilder rowBuilder = documentDefinition.createRowBuilder();
        final Row row = rowBuilder.createRow( psiMiTabLine );

        RelevanceScoreCalculator rsc = new RelevanceScoreCalculatorImpl( getTestProperties());
        IntactDocumentBuilder builder = new IntactDocumentBuilder(rsc );
        Document doc = builder.createDocument( row );
        Assert.assertEquals( 68, doc.getFields().size() );

        final org.apache.lucene.document.Field nameField = doc.getField( "nameA" );
        final org.apache.lucene.document.Field nameFieldSorted = doc.getField( "nameA_s" );
        final org.apache.lucene.document.Field rscField = doc.getField( "relevancescore" );
        final org.apache.lucene.document.Field rscFieldSorted = doc.getField( "relevancescore_s" );

        Assert.assertEquals(true,nameField.isIndexed());
        Assert.assertEquals(true,nameField.isStored());

        Assert.assertEquals(true,nameFieldSorted.isIndexed());
        Assert.assertEquals(false,nameFieldSorted.isStored());
        Assert.assertEquals(false,nameFieldSorted.isTokenized());

        Assert.assertEquals(true,rscField.isIndexed());
        Assert.assertEquals(true,rscField.isStored());

        Assert.assertEquals(true,rscFieldSorted.isIndexed());
        Assert.assertEquals(false,rscFieldSorted.isStored());
        Assert.assertEquals(false,rscFieldSorted.isTokenized());

    }


    @Test
    public void testGetNameA() throws Exception{

        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tnameA\tMT-ER-BR";
        final IntactDocumentDefinition documentDefinition = new IntactDocumentDefinition();
        final RowBuilder rowBuilder = documentDefinition.createRowBuilder();
        final Row row = rowBuilder.createRow( psiMiTabLine );
        IntactDocumentBuilder builder = new IntactDocumentBuilder( );
        Assert.assertEquals("Nfh",builder.getNameA( row ));
    }


    private Properties getTestProperties(){

        Properties properties = new Properties();
        properties.put("protein-small molecule","A");
        properties.put("protein-protein","B");
        properties.put("drug-drug target","C");
        properties.put("bait-prey","D");

        return properties;
    }

}