/**
 *
 */
package uk.ac.ebi.intact.psimitab.search;


import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.dataadapter.OBOParseException;
import psidev.psi.mi.search.util.DocumentBuilder;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.CrossReferenceFieldBuilder;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.FieldBuilder;
import uk.ac.ebi.intact.bridges.ontologies.OntologyDocument;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexWriter;
import uk.ac.ebi.intact.bridges.ontologies.iterator.OboOntologyIterator;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.Annotation;
import uk.ac.ebi.intact.psimitab.model.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * IntActDocumentBuilder Tester.
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActDocumentBuilderTest {

    private static File getTargetDirectory() {
        String outputDirPath = IntActDocumentBuilderTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    private static Directory ontologyDirectory;

    @BeforeClass
    public static void buildIndex() throws Exception {

//        final URL goUrl = new URL( "http://www.geneontology.org/ontology/gene_ontology_edit.obo" );
//        final URL goUrl = new URL( "file:C:\\Documents and Settings\\Samuel\\Desktop\\gene_ontology_edit.obo" );
        final URL psimiUrl = new URL( "http://psidev.sourceforge.net/mi/rel25/data/psi-mi25.obo" );
        //IntActDocumentBuilderTest.class.getResource( "/ontologies/go_slim.obo" ).toURI();
        File f = new File( getTargetDirectory(), "ontologyIndex" );
        ontologyDirectory = FSDirectory.getDirectory( f );
        OntologyIndexWriter writer = new OntologyIndexWriter( ontologyDirectory, true );

//        addOntologyToIndex( goUrl, "GO",writer );
        addOntologyToIndex( psimiUrl, "MI", writer );

        writer.flush();
        writer.optimize();
        writer.close();
    }

    private static void addOntologyToIndex( URL goUrl, String ontology, OntologyIndexWriter writer ) throws OBOParseException,
                                                                                                            IOException {
        OboOntologyIterator iterator = new OboOntologyIterator( ontology, goUrl );
        while ( iterator.hasNext() ) {
            final OntologyDocument ontologyDocument = iterator.next();
            writer.addDocument( ontologyDocument );
        }
    }

    @Test
    public void testCreateDocumentFromPsimiTabLine() throws Exception {
        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-";
        DocumentBuilder builder = new IntactDocumentBuilder();
        Document doc = builder.createDocumentFromPsimiTabLine( psiMiTabLine );

        Assert.assertEquals( 62, doc.getFields().size() );
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

        List<Field> fieldList = new ArrayList<Field>();
        fieldList.add( intTypeFieldType1 );

        Column intTypeCol = new Column( fieldList );
        //MI:0407(direct interaction)
        Assert.assertEquals( 1, intTypeCol.toString().split( "\\|" ).length );

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyDirectory );
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        Column intTypeColWithParents = builder.getColumnWithParents( intTypeCol );
        //MI:0407(direct interaction)|MI:0915(physical association)|MI:0190(interaction type)|MI:0914(association)
        Assert.assertEquals( 4, intTypeColWithParents.toString().split( "\\|" ).length );

        //Type2
        Field intTypeFieldType2 = new Field( "psi-mi", "MI:0407", "direct interaction" );

        List<Field> fieldList_ = new ArrayList<Field>();
        fieldList_.add( intTypeFieldType2 );

        Column intTypeCol_ = new Column( fieldList_ );
        //MI:0407(direct interaction)
        Assert.assertEquals( 1, intTypeCol_.toString().split( "\\|" ).length );

        Column intTypeColWithParents_ = builder.getColumnWithParents( intTypeCol );
        //MI:0407(direct interaction)|MI:0915(physical association)|MI:0190(interaction type)|MI:0914(association)
        Assert.assertEquals( 4, intTypeColWithParents_.toString().split( "\\|" ).length );
    }

    @Test
    public void testFieldsWithParentsForMI_interactionType() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyDirectory );
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        //interaction type MI:0407(direct interaction)
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsIntType = builder.getListOfFieldsWithParents( fieldBuilder.createField( "psi-mi:\"MI:0407\"(direct interaction)" ) );

        Assert.assertNotNull( fieldsWithParentsIntType );
        Assert.assertEquals( 5, fieldsWithParentsIntType.size() );

        Column colWithParentsIntType = new Column( fieldsWithParentsIntType );
        //"MI:0407(direct interaction)|MI:0190(interaction type)|MI:0914(association)|MI:0915(physical association)

        String colsIntType[] = colWithParentsIntType.toString().split( "\\|" );

        Assert.assertEquals( 5, colsIntType.length );

        Collection<String> results = Arrays.asList( colsIntType );
        Assert.assertTrue( results.contains( "psi-mi:\"MI:0000\"(molecular interaction)" ) );
        Assert.assertTrue( results.contains( "psi-mi:\"MI:0190\"(interaction type)" ) );
        Assert.assertTrue( results.contains( "psi-mi:\"MI:0915\"(physical association)" ) );
        Assert.assertTrue( results.contains( "psi-mi:\"MI:0914\"(association)" ) );
        Assert.assertTrue( results.contains( "psi-mi:\"MI:0407\"(direct interaction)" ) );


    }

    @Test
    public void testFieldsWithParentsForMI_detectionMethod() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyDirectory );
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        //interaction detection method MI:0045 (experimental interaction detection)
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsDetMethod = builder.getListOfFieldsWithParents( fieldBuilder.createField( "MI:0045(experimental interaction detection)" ) );

        Assert.assertNotNull( fieldsWithParentsDetMethod );
        Assert.assertEquals( 2, fieldsWithParentsDetMethod.size() );

        Column colWithParentsDetMethod = new Column( fieldsWithParentsDetMethod );
        //"MI:0045(experimental interaction detection)|MI:0001(interaction detection method)"
        String[] colsDetMethod = colWithParentsDetMethod.toString().split( "\\|" );

        int countDetMethod = 0;
        for ( String col : colsDetMethod ) {
            if ( col.equals( "psi-mi:\"MI:0045\"(experimental interaction detection)" ) ) {
                countDetMethod++;
            }
            if ( col.equals( "psi-mi:\"MI:0001\"(interaction detection method)" ) ) {
                countDetMethod++;
            }
        }

        Assert.assertEquals( 2, countDetMethod );
    }

    @Test
    public void testFieldsWithParentsForGO() throws Exception {

        IntactDocumentBuilder builder = new IntactDocumentBuilder( ontologyDirectory );
        builder.addExpandableOntology( "GO" );
        builder.addExpandableOntology( "MI" );
        builder.addExpandableOntology( "psi-mi" );

        //String goIdentifier = "go:\"GO:0030056";
        FieldBuilder fieldBuilder = new CrossReferenceFieldBuilder();
        List<Field> fieldsWithParentsProperties = builder.getListOfFieldsWithParents( fieldBuilder.createField( "go:\"GO:0005634\"(nucleus)" ) );

        Assert.assertNotNull( fieldsWithParentsProperties );
        //term+8 parents excludes root == 9
        Assert.assertEquals( 9, fieldsWithParentsProperties.size() );

        Column colWithParents = new Column( fieldsWithParentsProperties );
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
            if ( string.equals( "go:\"GO:0005634\"(nucleus)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0044464\"(cell part)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0043226\"(organelle)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0044424\"(intracellular part)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0043227\"(membrane-bounded organelle)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0005622\"(intracellular)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0005623\"(cell)" ) ) {
                countProperty++;
            }
            if ( string.equals( "go:\"GO:0043231\"(intracellular membrane-bounded organelle)" ) ) {
                countProperty++;
            }
        }
        Assert.assertEquals( 8, countProperty );
    }
}