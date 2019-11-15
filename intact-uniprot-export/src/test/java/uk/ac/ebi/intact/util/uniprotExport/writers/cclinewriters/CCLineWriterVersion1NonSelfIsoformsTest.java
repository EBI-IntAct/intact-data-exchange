package uk.ac.ebi.intact.util.uniprotExport.writers.cclinewriters;

import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.CCLineConverterVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CCLineWriterVersion1NonSelfIsoformsTest extends UniprotExportBase {

    public List<EncoreInteraction> createNonSelfIsoformInteractions(){

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> mouse = new ArrayList<CrossReference>();
        CrossReference orgMouse = new CrossReferenceImpl("taxid", "10090", "mouse");
        mouse.add(orgMouse);

        List<CrossReference> human = new ArrayList<CrossReference>();
        CrossReference orgHuman = new CrossReferenceImpl("taxid", "9606", "human");
        human.add(orgHuman);

        EncoreInteraction interaction1 = new EncoreInteraction();

        // From intact-micluster.txt
        // intact:EBI-288461	intact:EBI-286828
        // uniprotkb:Q9WVI9-1	uniprotkb:P12023-2
        // uniprotkb:Mapk8ip1|...	uniprotkb:App
        // psi-mi:"MI:0019"(coimmunoprecipitation)|psi-mi:"MI:0019"(coimmunoprecipitation)
        // Matsuda et al. (2001)	pubmed:11517249|mint:MINT-5214436	taxid:10090(mouse)	taxid:10090(mouse)
        // psi-mi:"MI:0915"(physical association)|psi-mi:"MI:0914"(association)	psi-mi:"MI:0471"(MINT)
        // intact:EBI-82010|intact:EBI-81994	intact-miscore:0.49853906
        Map<String, String> interactor1A = new HashMap<>();
        interactor1A.put("uniprotkb", "Q9WVI9-1");
        interactor1A.put("intact", "EBI-288461");
        Map<String, String> interactor1B = new HashMap<>();
        interactor1B.put("uniprotkb", "P12023-2");
        interactor1B.put("intact", "EBI-286828");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("11517249");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0019", "MI:0915"), pubmeds1);
        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0019", "MI:0914"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-82010", "11517249");
        experimentToPubmed1.put("EBI-81994", "11517249");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "11517249");
        publications1.add(ref1);

        interaction1.setId(1);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction1.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction1.setInteractorAccsA(interactor1A);
        interaction1.setInteractorAccsB(interactor1B);
        interaction1.setPublicationIds(publications1);
        interaction1.setOrganismsA(mouse);
        interaction1.setOrganismsB(mouse);
        interaction1.setExperimentToPubmed(experimentToPubmed1);

        // From intact-micluster.txt
        // intact:EBI-288464	intact:EBI-1207633	uniprotkb:Q9WVI9-2	uniprotkb:Q86Y07-1
        // uniprotkb:Mapk8ip1|... uniprotkb:VRK2|...
        // psi-mi:"MI:0096"(pull down)	Blanco et al. (2008)	pubmed:18286207	taxid:10090(mouse)	taxid:9606(human)
        // psi-mi:"MI:0915"(physical association)	psi-mi:"MI:0469"(IntAct)	intact:EBI-1779763	intact-miscore:0.40116468
        EncoreInteraction interaction2 = new EncoreInteraction();
        Map<String, String> interactor2A = new HashMap<>();
        interactor2A.put("uniprotkb", "Q9WVI9-2");
        interactor2A.put("intact", "EBI-288464");
        Map<String, String> interactor2B = new HashMap<>();
        interactor2B.put("uniprotkb", "Q86Y07-1");
        interactor2B.put("intact", "EBI-1207633");

        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("18286207");

        interaction2.getMethodTypePairListMap().put(new MethodTypePair("MI:0096", "MI:0915"), pubmeds2);

        Map<String, String> experimentToPubmed2 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed2.put("EBI-1779763", "18286207");

        List<CrossReference> publications2 = new ArrayList<CrossReference>(1);
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "18286207");
        publications2.add(ref2);

        interaction2.setId(2);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction2.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction2.setInteractorAccsA(interactor2A);
        interaction2.setInteractorAccsB(interactor2B);
        interaction2.setPublicationIds(publications2);
        interaction2.setOrganismsA(mouse);
        interaction2.setOrganismsB(human);
        interaction2.setExperimentToPubmed(experimentToPubmed2);

        // From intact-micluster.txt
        // intact:EBI-1207636	intact:EBI-288464	uniprotkb:Q86Y07-2	uniprotkb:Q9WVI9-2
        // uniprotkb:VRK2|...	uniprotkb:Mapk8ip1	psi-mi:"MI:0096"(pull down)|psi-mi:"MI:0096"(pull down)
        // Blanco et al. (2008)	pubmed:18286207	taxid:9606(human)	taxid:10090(mouse)
        // psi-mi:"MI:0407"(direct interaction)|psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0469"(IntAct)	intact:EBI-2121669|intact:EBI-1779789	intact-miscore:0.54351985
        EncoreInteraction interaction3 = new EncoreInteraction();
        Map<String, String> interactor3A = new HashMap<>();
        interactor3A.put("uniprotkb", "Q86Y07-2");
        interactor3A.put("intact", "EBI-1207636");
        Map<String, String> interactor3B = new HashMap<>();
        interactor3B.put("uniprotkb", "Q9WVI9-2");
        interactor3B.put("intact", "EBI-288464");

        List<String> pubmeds3 = new ArrayList<String>();
        pubmeds3.add("18286207");

        interaction3.getMethodTypePairListMap().put(new MethodTypePair("MI:0096", "MI:0407"), pubmeds3);
        interaction3.getMethodTypePairListMap().put(new MethodTypePair("MI:0096", "MI:0915"), pubmeds3);


        Map<String, String> experimentToPubmed3 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed3.put("EBI-2121669", "18286207");
        experimentToPubmed3.put("EBI-1779789", "18286207");

        List<CrossReference> publications3 = new ArrayList<CrossReference>(1);
        CrossReference ref3 = new CrossReferenceImpl("pubmed", "18286207");
        publications3.add(ref3);

        interaction3.setId(3);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction3.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction3.setInteractorAccsA(interactor3A);
        interaction3.setInteractorAccsB(interactor3B);
        interaction3.setPublicationIds(publications3);
        interaction3.setOrganismsA(human);
        interaction3.setOrganismsB(mouse);
        interaction3.setExperimentToPubmed(experimentToPubmed3);

        interactions.add(interaction1);
        interactions.add(interaction2);
        interactions.add(interaction3);

        return interactions;
    }

    public List<EncoreInteraction> createNonSelfIsoformInteractionsSameEntry() {

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism = new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "9606", "human");
        organism.add(org);

        EncoreInteraction interaction1 = new EncoreInteraction();

        // From intact-micluster.txt
        // intact:EBI-288326	intact:EBI-288309	uniprotkb:Q14790-5	uniprotkb:Q14790-1
        // uniprotkb:CASP8|... uniprotkb:CASP8|...
        // psi-mi:"MI:0018"(two hybrid)	Boldin et al. (1996)	pubmed:8681376
        // taxid:9606(human)	taxid:9606(human)	psi-mi:"MI:0915"(physical association)	psi-mi:"MI:0469"(IntAct)
        // intact:EBI-527125|intact:EBI-527153	author score:B|author score:The point mutation on casp8 alfa-1 upgrade de interaction from B to A|intact-miscore:0.36784992
        Map<String, String> interactor1A = new HashMap<>();
        interactor1A.put("uniprotkb", "Q14790-5");
        interactor1A.put("intact", "EBI-288326");
        Map<String, String> interactor1B = new HashMap<>();
        interactor1B.put("uniprotkb", "Q14790-1");
        interactor1B.put("intact", "EBI-288309");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("8681376");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0018", "MI:0915"), pubmeds1);
        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0018", "MI:0915"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-527125", "8681376");
        experimentToPubmed1.put("EBI-527153", "8681376");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "8681376");
        publications1.add(ref1);

        interaction1.setId(1);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction1.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction1.setInteractorAccsA(interactor1A);
        interaction1.setInteractorAccsB(interactor1B);
        interaction1.setPublicationIds(publications1);
        interaction1.setOrganismsA(organism);
        interaction1.setOrganismsB(organism);
        interaction1.setExperimentToPubmed(experimentToPubmed1);

        interactions.add(interaction1);

        return interactions;
    }
    public MiClusterContext createNonSelfChainInteractionsClusterContext(){

        MiClusterContext context = new MiClusterContext();

        context.getGeneNames().put("Q9WVI9-1","Mapk8ip1");
        context.getGeneNames().put("Q9WVI9-2","Mapk8ip1");
        context.getGeneNames().put("Q86Y07-1","VRK2");
        context.getGeneNames().put("Q86Y07-2","VRK2");
        context.getGeneNames().put("P12023-2","App");
        context.getGeneNames().put("Q14790-1", "CASP8");
        context.getGeneNames().put("Q14790-5", "CASP8");

        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0407", "direct interaction");
        context.getMiTerms().put("MI:0018", "two hybrid");
        context.getMiTerms().put("MI:0096", "pull down");

        context.getSpokeExpandedInteractions().add("EBI-81994");

        context.getInteractionToMethod_type().put("EBI-82010", new MethodTypePair("MI:0019", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-81994", new MethodTypePair("MI:0019", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-1779763", new MethodTypePair("MI:0096", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-2121669", new MethodTypePair("MI:0096", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-1779789", new MethodTypePair("MI:0096", "MI:0407"));
        context.getInteractionToMethod_type().put("EBI-527125", new MethodTypePair("MI:0018", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-527153", new MethodTypePair("MI:0018", "MI:0915"));

        return context;
    }

    @Test
    public void testConversionAndExport(){

        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactions = createNonSelfIsoformInteractions();
        List<EncoreInteraction> interactionsSameEntry = createNonSelfIsoformInteractionsSameEntry();

        List<EncoreInteraction> negativeInteractions = Collections.emptyList();


        MiClusterContext context = createNonSelfChainInteractionsClusterContext();


        CCParameters<SecondCCParametersVersion1> secondParametersForQ9WVI9 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactions),
                new HashSet<>(negativeInteractions),
                context,
                "Q9WVI9");

        CCParameters<SecondCCParametersVersion1> secondParametersForP12023 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactions),
                new HashSet<>(negativeInteractions),
                context,
                "P12023");

        CCParameters<SecondCCParametersVersion1> secondParametersForQ86Y07 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactions),
                new HashSet<>(negativeInteractions),
                context,
                "Q86Y07");

        CCParameters<SecondCCParametersVersion1> secondParametersForQ14790 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsSameEntry),
                new HashSet<>(negativeInteractions),
                context,
                "Q14790");

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>();
        parameters.add(secondParametersForQ9WVI9);
        parameters.add(secondParametersForP12023);
        parameters.add(secondParametersForQ86Y07);
        parameters.add(secondParametersForQ14790);

        try {
            File testFile = new File("cc_line_isoforms_non_self_test.txt");
            FileWriter test = new FileWriter(testFile);

            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);
            writer.writeCCLines(parameters);
            writer.close();

            File template = new File(CCLineWriterVersion1NonSelfIsoformsTest.class.getResource("/cc_line_isoforms_non_self.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
