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

        Assert.assertEquals( 68, doc.getFields().size() );
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
        //String psiMiTabLine = "uniprotkb:P04141|drugbank:DB00020|intact:DGI-294002\tuniprotkb:P15509|intact:DGI-294033|uniprotkb:P13727|intact:DGI-294039\tuniprotkb:GMCSF(gene name synonym)|uniprotkb:Sargramostim(gene name synonym)|uniprotkb:Molgramostin(gene name synonym)|intact:Molgramostin(drug brand name)|intact:Sargramostim(commercial name)|intact:Immunex(drug brand name)|intact:\"Leucomax (Novartis)\"(drug brand name)|intact:\"Leukine (Berlex Laboratories Inc)\"(drug brand name)|uniprotkb:csf2_human\tuniprotkb:CSF2R(gene name synonym)|uniprotkb:CSF2RY(gene name synonym)|uniprotkb:CDw116(gene name synonym)|uniprotkb:csf2r_human|uniprotkb:MBP(gene name synonym)|uniprotkb:Proteoglycan 2(gene name synonym)|uniprotkb:prg2_human\tuniprotkb:CSF2\tuniprotkb:CSF2RA|uniprotkb:PRG2\tpsi-mi:\"MI:0045\"(experimental interac)|psi-mi:\"MI:0045\"(experimental interac)\t-\tpubmed:18048412|pubmed:18048412\ttaxid:9606(human)\ttaxid:9606(human)|taxid:9606(human)\tpsi-mi:\"MI:0407\"(direct interaction)|psi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:1002\"(DrugBank)|psi-mi:\"MI:1002\"(DrugBank)\tintact:DGI-294032|intact:DGI-294043\t-\tpsi-mi:\"MI:1094\"(drug)\tpsi-mi:\"MI:1095\"(drug target)|psi-mi:\"MI:1095\"(drug target)\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)|psi-mi:\"MI:0499\"(unspecified role)\tdrugbank:BIOD00035(secondary-ac)|drugbank:BTD00035(secondary-ac)|rcsb pdb:2GMF|go:\"GO:0005129\"(granulocyte macrophage colony-stimulating factor receptor binding)|go:\"GO:0045740\"(positive regulation of DNA replication)|go:\"GO:0042523\"(positive regulation of tyrosine phosphorylation of Stat5 protein)|ensembl:ENSG00000164400|rcsb pdb:1CSG|refseq:NP_000749.2|interpro:IPR012351(Four-helical cytokine, core)|interpro:IPR000773(Granulocyte-macrophage colony-stimulating factor)|uniprotkb:Q2VPI8(secondary-ac)|uniprotkb:Q8NFI6(secondary-ac)|uniprotkb:P04141(identity)|drugbank:DB00020(identity)\tgo:\"GO:0005887\"(integral to plasma membrane)|go:\"GO:0004872\"(receptor activity)|ensembl:ENSG00000198223|refseq:NP_006131.2|refseq:NP_758448.1|refseq:NP_758449.1|refseq:NP_758450.1|refseq:NP_758452.1|interpro:IPR008957(Fibronectin, type III-like fold)|interpro:IPR003961(Fibronectin, type III)|interpro:IPR003532(Short hematopoietin receptor, family 2, conserved site)|uniprotkb:O00207(secondary-ac)|uniprotkb:Q14429(secondary-ac)|uniprotkb:Q14430(secondary-ac)|uniprotkb:Q14431(secondary-ac)|uniprotkb:Q16564(secondary-ac)|uniprotkb:P15509(identity)|go:\"GO:0005576\"(extracellular region)|go:\"GO:0005529\"(sugar binding)|ensembl:ENSG00000186652|rcsb pdb:1H8U|rcsb pdb:2BRS|refseq:NP_002719.3|interpro:IPR001304(C-type lectin)|interpro:IPR016186(C-type lectin-like)|interpro:IPR002352(Eosinophil major basic protein)|uniprotkb:P81448(secondary-ac)|uniprotkb:Q14227(secondary-ac)|uniprotkb:Q6ICT2(secondary-ac)|uniprotkb:P13727(identity)\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0326\"(protein)\ttaxid:-3(unknown)|taxid:-3(unknown)\t-\tDrugBank - a knowledgebase for drugs, drug actions and drug targets.|DrugBank - a knowledgebase for drugs, drug actions and drug targets.\tbiotech prep:Human, recombinant GM-CSF, expressed in yeast. Glycoprotein that is 127 residues. Substitution of Leu23 leads to a difference from native protein.|drug type:Biotech; Approved; Investigational|drug category:Immunomodulatory Agents; Antineoplastic Agents; Anti-Infective Agents|disease indication:For the treatment of cancer and bone marrow transplant|pharmacology:\"Sargramostim is used in the treatment of bone marrow transplant recipients or those exposed to chemotherapy an recovering from acut myelogenous leukemia, Leukine or GM-CSF is a hematopoietic growth factor which stimulates the survival, clonal expansion (proliferation) and differentiation of hematopoietic progenitor cells. GM-CSF is also capable of activating mature granulocytes and macrophages. After a bone marrow transplant or chemotherapy, patients have a reduced capacity to produce red and white blood cells. Supplementing them with external sources of GM-CSF helps bring the level of neutrophils back to normal so that they can better fight infections.\"|mechanism of action:\"Sargramostim binds to the Granulocyte-macrophage colony stimulating factor receptor (GM-CSF-R-alpha or CSF2R) which stimulates a JAK2 STAT1/STAT3 signal transduction pathway. This leads to the production of hemopoietic cells and neutrophils\"|dosage form:Injection, solution Subcutaneous|dosage form:Injection, powder, for solution Intravenous drip|dosage form:Injection, solution Intravenous drip|dosage form:Injection, powder, for solution Subcutaneous|organisms affected:Humans and other mammals|url:\"http://www.rxlist.com/cgi/generic/sargramostim.htm\"|comment:\"isoelectric point (MI:2030): 5.05\"|comment:\"average molecular weight (MI:2155): 14434.5000\"\t-\t-\t-\t-";
        //String psiMiTabLine = "uniprotkb:P15104|intact:DGI-294111\tchebi:\"CHEBI:16015\"|drugbank:DB00142|intact:DGI-301615|chebi:\"CHEBI:16811\"|drugbank:DB00134|intact:DGI-300873\tuniprotkb:GLNS(gene name synonym)|uniprotkb:Glutamate--ammonia ligase(gene name synonym)|uniprotkb:glna_human\tintact:\"(2S)-2-Aminopentanedioic acid\"(drug brand name)|intact:\"(S)-(+)-Glutamic acid\"(drug brand name)|intact:\"(S)-2-Aminopentanedioic acid\"(drug brand name)|intact:\"(S)-Glutamic acid\"(drug brand name)|intact:1-Aminopropane-1,3-dicarboxylic acid(drug brand name)|intact:2-Aminoglutaric acid(drug brand name)|intact:2-Aminopentanedioic acid(drug brand name)|intact:a-Glutamic acid(drug brand name)|intact:a-Aminoglutaric acid(drug brand name)|intact:\"L-(+)-Glutamic acid\"(drug brand name)|intact:L-a-Aminoglutaric acid(drug brand name)|intact:L-Glutamate(drug brand name)|intact:L-Glutaminic acid(drug brand name)|intact:Glutaminic acid(drug brand name)|intact:Glutamic acid(drug brand name)|intact:L-Glutamic Acid(commercial name)|intact:Aciglut(drug brand name)|intact:Glusate(drug brand name)|intact:Glutacid(drug brand name)|intact:Glutamicol(drug brand name)|intact:Glutamidex(drug brand name)|intact:Glutaminol(drug brand name)|intact:Glutaton(drug brand name)|intact:\"(S)-2-Amino-4-(methylthio)butanoic acid\"(drug brand name)|intact:\"2-Amino-4-(methylthio)butyric acid\"(drug brand name)|intact:a-Amino-g-methylmercaptobutyric acid(drug brand name)|intact:g-Methylthio-a-aminobutyric acid(drug brand name)|intact:\"L-(-)-Methionine\"(drug brand name)|intact:L-a-Amino-g-methylthiobutyric acid(drug brand name)|intact:Methionine(drug brand name)|intact:L-Methionine(commercial name)|intact:Acimethin(drug brand name)|intact:Cymethion(drug brand name)\tuniprotkb:GLUL\t-\tpsi-mi:\"MI:0045\"(experimental interac)|psi-mi:\"MI:0045\"(experimental interac)\t-\tpubmed:18048412|pubmed:18048412\ttaxid:9606(human)\ttaxid:-3(unknown)|taxid:-3(unknown)\tpsi-mi:\"MI:0407\"(direct interaction)|psi-mi:\"MI:0407\"(direct interaction)\tpsi-mi:\"MI:1002\"(DrugBank)|psi-mi:\"MI:1002\"(DrugBank)\tintact:DGI-301864|intact:DGI-300980\t-\tpsi-mi:\"MI:1095\"(drug target)\tpsi-mi:\"MI:1094\"(drug)|psi-mi:\"MI:1094\"(drug)\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)|psi-mi:\"MI:0502\"(enzyme target)\tensembl:ENSG00000135821|rcsb pdb:2OJW|rcsb pdb:2QC8|refseq:NP_001028216.1|refseq:NP_001028228.1|refseq:NP_002056.2|interpro:IPR008147(Glutamine synthetase, beta-Grasp)|interpro:IPR014746(Glutamine synthetase/guanido kinase, catalytic region)|interpro:IPR008146(Glutamine synthetase, catalytic region)|reactome:REACT_13|uniprotkb:Q499Y9(secondary-ac)|uniprotkb:Q5T9Z1(secondary-ac)|uniprotkb:Q7Z3W4(secondary-ac)|uniprotkb:Q8IZ17(secondary-ac)|uniprotkb:P15104(identity)\tdrugbank:NUTR00027(secondary-ac)|pubmed:10736373|pubmed:15939876|pubmed:10736365|pubmed:7901400|pubmed:17202478|rcsb pdb:1BGV|chebi:\"CHEBI:16015\"(L-glutamic acid)|drugbank:DB00142(identity)|drugbank:NUTR00038(secondary-ac)|rcsb pdb:1WKM|chebi:\"CHEBI:16811\"(methionine)|drugbank:DB00134(identity)\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0328\"(small molecule)\ttaxid:-3(unknown)|taxid:-3(unknown)\t-\tDrugBank - a knowledgebase for drugs, drug actions and drug targets.|DrugBank - a knowledgebase for drugs, drug actions and drug targets.\t-\tbiotech prep:A peptide that is a homopolymer of glutamic acid. [PubChem]|drug type:Small Molecule; Nutraceutical; Approved|drug category:Dietary supplement; Micronutrient; Non-Essential Amino Acids|disease indication:\"Considered to be nature's \\\"Brain food\\\" by improving mental capacities; helps speed the healing of ulcers; gives a \\\"lift\\\" from fatigue; helps control alcoholism, schizophrenia and the craving for sugar.\"|pharmacology:In addition to being one of the building blocks in protein synthesis, it is the most widespread neurotransmitter in brain function, as an excitatory neurotransmitter and as a precursor for the synthesis of GABA in GABAergic neurons.|mechanism of action:\"Glutamate activates both ionotropic and metabotropic glutamate receptors. The ionotropic ones being non-NMDA (AMPA and kainate) and NMDA receptors. Free glutamic acid cannot cross the blood-brain barrier in appreciable quantities; instead it is converted into L-glutamine, which the brain uses for fuel and protein synthesis. It is conjectured that glutamate is involved in cognitive functions like learning and memory in the brain, though excessive amounts may cause neuronal damage associated in diseases like amyotrophic lateral sclerosis, lathyrism, and Alzheimer's disease. Also, the drug phencyclidine (more commonly known as PCP) antagonizes glutamate at the NMDA receptor, causing behavior reminiscent of schizophrenia. Glutamate in action is extremely difficult to study due to its transient nature.\"|comment:\"drug absorption (MI:2045): Absorbed from the lumen of the small intestine into the enterocytes.Absorption is efficient and occurs by an active transport mechanism.\"|toxicity attribute name:Glutamate causes neuronal damage and eventual cell death, particularly when NMDA receptors are activated, High dosages of glutamic acid may include symptoms such as headaches and neurological problems.|drug metabolism:Hepatic|dosage form:Capsule Oral|organisms affected:Humans and other mammals|inchi id:\"InChI=1/C5H9NO4/c6-3(5(9)10)1-2-4(7)8/h3H,1-2,6H2,(H,7,8)(H,9,10)/t3-/m0/s1/f/h7,9H\"|comment:\"melting point (MI:2026): 224 oC\"|comment:\"isoelectric point (MI:2030): 2.23\"|comment:\"average molecular weight (MI:2155): 147.1293\"|comment:\"monoisotopic molecular weight (MI:2156): 147.0532\"|comment:\"experimental h2o solubility (MI:2157): 8.57 mg/mL at 25 oC [BULL,HB et al. (1978)]\"|comment:\"predicted h2o solubility (MI:2158): 8.06e+01 mg/mL [ALOGPS]\"|biotech prep:A sulfur containing essential amino acid that is important in many body functions. It is a chelating agent for heavy metals. [PubChem]|drug type:Small Molecule; Nutraceutical; Approved|drug category:Dietary supplement; Micronutrient; Essential Amino Acids|disease indication:Used for protein synthesis including the formation of SAMe, L-homocysteine, L-cysteine, taurine, and sulfate.|pharmacology:L-Methionine is a principle supplier of sulfur which prevents disorders of the hair, skin and nails; helps lower cholesterol levels by increasing the liver's production of lecithin; reduces liver fat and protects the kidneys; a natural chelating agent for heavy metals; regulates the formation of ammonia and creates ammonia-free urine which reduces bladder irritation; influences hair follicles and promotes hair growth. L-methionine may protect against the toxic effects of hepatotoxins, such as acetaminophen. Methionine may have antioxidant activity.|mechanism of action:The mechanism of the possible anti-hepatotoxic activity of L-methionine is not entirely clear. It is thought that metabolism of high doses of acetaminophen in the liver lead to decreased levels of hepatic glutathione and increased oxidative stress. L-methionine is a precursor to L-cysteine. L-cysteine itself may have antioxidant activity. L-cysteine is also a precursor to the antioxidant glutathione. Antioxidant activity of L-methionine and metabolites of L-methionine appear to account for its possible anti-hepatotoxic activity. Recent research suggests that methionine itself has free-radical scavenging activity by virtue of its sulfur, as well as its chelating ability.|comment:\"drug absorption (MI:2045): Absorbed from the lumen of the small intestine into the enterocytes by an active transport process.\"|toxicity attribute name:Doses of L-methionine of up to 250 mg daily are generally well tolerated. Higher doses may cause nausea, vomiting and headache. Healthy adults taking 8 grams of L-methionine daily for four days were found to have reduced serum folate levels and leucocytosis. Healthy adults taking 13.9 grams of L-methionine daily for five days were found to have changes in serum pH and potassium and increased urinary calcium excretion. Schizophrenic patients given 10 to 20 grams of L-methionine daily for two weeks developed functional psychoses. Single doses of 8 grams precipitated encephalopathy in patients with cirrhosis.|drug metabolism:Hepatic|dosage form:Capsule Oral|dosage form:Tablet Oral|dosage form:Powder Oral|organisms affected:Humans and other mammals|food interaction:Take with food.|inchi id:\"InChI=1/C5H11NO2S/c1-9-3-2-4(6)5(7)8/h4H,2-3,6H2,1H3,(H,7,8)/t4-/m0/s1/f/h7H\"|comment:\"melting point (MI:2026): 276-279 oC\"|comment:\"average molecular weight (MI:2155): 149.2113\"|comment:\"monoisotopic molecular weight (MI:2156): 149.0510\"|comment:\"experimental h2o solubility (MI:2157): 56.6 mg/mL at 25 oC [YALKOWSKY,SH & DANNENFELSER,RM (1992)]\"|comment:\"predicted h2o solubility (MI:2158): 2.39e+01 mg/mL [ALOGPS]\"|comment:\"experimental logs (MI:2161): -0.42 [ADME Research, USCD]\"\t-\t-\t-";

        final IntactDocumentDefinition documentDefinition = new IntactDocumentDefinition();
        final RowBuilder rowBuilder = documentDefinition.createRowBuilder();
        final Row row = rowBuilder.createRow( psiMiTabLine );

        RelevanceScoreCalculator rsc = new RelevanceScoreCalculatorImpl( getTestProperties());
        IntactDocumentBuilder builder = new IntactDocumentBuilder(rsc );
        //IntactDocumentBuilder builder = new IntactDocumentBuilder(null, new String[]{"chebi"} );
        Document doc = builder.createDocument( row );

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
    public void testGetName() throws Exception{

        String psiMiTabLine = "uniprotkb:P16884|intact:EBI-446344\tuniprotkb:Q60824|intact:EBI-446159\tuniprotkb:Nefh(gene name)\tuniprotkb:Dst(gene name synonym)\tintact:Nfh\tintact:Bpag1\tMI:0018(2 hybrid)\tLeung et al. (1999)\tpubmed:9971739\ttaxid:10116(rat)\ttaxid:10090(mouse)\tMI:0218(physical interaction)\tMI:0469(intact)\tintact:EBI-446356\t-\tMI:0498(prey)\tMI:0496(bait)\tMI:0499(unspecified role)\tMI:0499(unspecified role)\tinterpro:IPR004829|interpro:IPR010790|interpro:IPR001664|uniprotkb:O35482|rgd:3159|ensembl:ENSRNOG00000008716|uniprotkb:Q540Z7|uniprotkb:Q63368\tgo:0005737|go:0030056|go:0005200|go:0045104|interpro:IPR001589|interpro:IPR001715|interpro:IPR002048|interpro:IPR001101|uniprotkb:Q60845|uniprotkb:Q9WU50|go:0008090|go:0015629|go:0015630|go:0060053|go:0008017|go:0031122|go:0031110|ensembl:ENSMUSG00000026131\tMI:0326(protein)\tMI:0326(protein)\tyeast:4932\t-\t-\tnameA\tMT-ER-BR";
        final IntactDocumentDefinition documentDefinition = new IntactDocumentDefinition();
        final RowBuilder rowBuilder = documentDefinition.createRowBuilder();
        final Row row = rowBuilder.createRow( psiMiTabLine );
        IntactDocumentBuilder builder = new IntactDocumentBuilder( );
        String nameA = builder.getName( row,MitabDocumentDefinition.ALIAS_INTERACTOR_A,  MitabDocumentDefinition.ALTID_INTERACTOR_A );
        String nameB = builder.getName( row,MitabDocumentDefinition.ALIAS_INTERACTOR_B,  MitabDocumentDefinition.ALTID_INTERACTOR_B );
        
        Assert.assertEquals("Nfh",nameA);
        Assert.assertEquals("Bpag1",nameB);
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