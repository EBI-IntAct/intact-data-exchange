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

public class CCLineWriterVersion1NonSelfChainTest extends UniprotExportBase {

    public List<EncoreInteraction> createNonSelfChainInteractions(){

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "11108", "hcvh");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "356411", "hcvjf");
        organismB.add(orgB);

        // From intact-micluster.txt
        // intact:EBI-6904269	intact:EBI-6927873	uniprotkb:P27958-PRO_0000037570	uniprotkb:Q99IB8-PRO_0000045602
        // psi-mi:p27958-pro_0000037570	psi-mi:q99ib8-pro_0000045602|intact:EBI-9232975|intact:EBI-6931023
        // psi-mi:"MI:0663"(confocal microscopy)	Ma et al. (2011)	pubmed:20962101|imex:IM-26092
        // taxid:11108(hcvh)	taxid:356411(hcvjf)	psi-mi:"MI:0403"(colocalization)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-6931073	intact-miscore:0.2677602
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1A = new HashMap<>();
        interactor1A.put("uniprotkb", "P27958-PRO_0000037570");
        interactor1A.put("intact", "EBI-6904269");
        Map<String, String> interactor1B = new HashMap<>();
        interactor1B.put("uniprotkb", "Q99IB8-PRO_0000045602");
        interactor1B.put("intact", "EBI-6927873");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("20962101");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0676", "MI:0915"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-6931073", "20962101");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "20962101");
        publications1.add(ref1);

        interaction1.setId(1);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction1.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction1.setInteractorAccsA(interactor1A);
        interaction1.setInteractorAccsB(interactor1B);
        interaction1.setPublicationIds(publications1);
        interaction1.setOrganismsA(organismA);
        interaction1.setOrganismsB(organismB);
        interaction1.setExperimentToPubmed(experimentToPubmed1);

        // From intact-micluster.txt
        // intact:EBI-6919131	intact:EBI-6858501	uniprotkb:P27958-PRO_0000037572	uniprotkb:Q99IB8-PRO_0000045599
        // psi-mi:p27958-pro_0000037572	psi-mi:q99ib8-pro_0000045599|intact:EBI-6927943	psi-mi:"MI:0007"(anti tag coimmunoprecipitation)
        // Ma et al. (2011)	pubmed:20962101|imex:IM-26092	taxid:11108(hcvh)	taxid:356411(hcvjf)
        // psi-mi:"MI:0915"(physical association)	psi-mi:"MI:1335"(HPIDb)	intact:EBI-6919128	intact-miscore:0.40116468
        EncoreInteraction interaction2 = new EncoreInteraction();
        Map<String, String> interactor2A = new HashMap<>();
        interactor2A.put("uniprotkb", "P27958-PRO_0000037572");
        interactor2A.put("intact", "EBI-6919131");
        Map<String, String> interactor2B = new HashMap<>();
        interactor2B.put("uniprotkb", "Q99IB8-PRO_0000045599");
        interactor2B.put("intact", "EBI-6858501");

        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("20962101");

        interaction2.getMethodTypePairListMap().put(new MethodTypePair("MI:0676", "MI:0915"), pubmeds2);

        Map<String, String> experimentToPubmed2 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed2.put("EBI-6919128", "20962101");

        List<CrossReference> publications2 = new ArrayList<CrossReference>(1);
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "20962101");
        publications2.add(ref2);

        interaction2.setId(2);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction2.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction2.setInteractorAccsA(interactor2A);
        interaction2.setInteractorAccsB(interactor2B);
        interaction2.setPublicationIds(publications2);
        interaction2.setOrganismsA(organismA);
        interaction2.setOrganismsB(organismB);
        interaction2.setExperimentToPubmed(experimentToPubmed2);

        // From intact-micluster.txt
        // intact:EBI-6919131	intact:EBI-6927873	uniprotkb:P27958-PRO_0000037572	uniprotkb:Q99IB8-PRO_0000045602
        // psi-mi:p27958-pro_0000037572	psi-mi:q99ib8-pro_0000045602|intact:EBI-9232975|intact:EBI-6931023
        // psi-mi:"MI:0663"(confocal microscopy)|psi-mi:"MI:0007"(anti tag coimmunoprecipitation)	Ma et al. (2011)
        // pubmed:20962101|imex:IM-26092	taxid:11108(hcvh)	taxid:356411(hcvjf)
        // psi-mi:"MI:0403"(colocalization)|psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-6931011|intact:EBI-6931100	intact-miscore:0.46269515
        EncoreInteraction interaction3 = new EncoreInteraction();
        Map<String, String> interactor3A = new HashMap<>();
        interactor3A.put("uniprotkb", "P27958-PRO_0000037572");
        interactor3A.put("intact", "EBI-6919131");
        Map<String, String> interactor3B = new HashMap<>();
        interactor3B.put("uniprotkb", "Q99IB8-PRO_0000045602");
        interactor3B.put("intact", "EBI-6927873");

        List<String> pubmeds3 = new ArrayList<String>();
        pubmeds3.add("20962101");

        interaction3.getMethodTypePairListMap().put(new MethodTypePair("MI:0663", "MI:0403"), pubmeds3);
        interaction3.getMethodTypePairListMap().put(new MethodTypePair("MI:0676", "MI:0915"), pubmeds3);

        Map<String, String> experimentToPubmed3 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed3.put("EBI-6931011", "20962101");
        experimentToPubmed3.put("EBI-6931100", "20962101");

        List<CrossReference> publications3 = new ArrayList<CrossReference>(1);
        CrossReference ref3 = new CrossReferenceImpl("pubmed", "20962101");
        publications3.add(ref3);

        interaction3.setId(3);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction3.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction3.setInteractorAccsA(interactor3A);
        interaction3.setInteractorAccsB(interactor3B);
        interaction3.setPublicationIds(publications3);
        interaction3.setOrganismsA(organismA);
        interaction3.setOrganismsB(organismB);
        interaction3.setExperimentToPubmed(experimentToPubmed3);

        // From intact-micluster.txt
        // intact:EBI-6858513	intact:EBI-8753518	uniprotkb:Q99IB8-PRO_0000045592	uniprotkb:P27958-PRO_0000037576
        // psi-mi:q99ib8-pro_0000045592|intact:EBI-6929338	psi-mi:p27958-pro_0000037576
        // psi-mi:"MI:0007"(anti tag coimmunoprecipitation)|psi-mi:"MI:0007"(anti tag coimmunoprecipitation)	Camus et al. (2013)	pubmed:23420847|imex:IM-25909
        // taxid:356411(hcvjf)	taxid:11108(hcvh)	psi-mi:"MI:0915"(physical association)|psi-mi:"MI:0914"(association)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-8753555|intact:EBI-8763465|intact:EBI-8753545	intact-miscore:0.49853906
        EncoreInteraction interaction4 = new EncoreInteraction();
        Map<String, String> interactor4A = new HashMap<>();
        interactor4A.put("uniprotkb", "Q99IB8-PRO_0000045592");
        interactor4A.put("intact", "EBI-6858513");
        Map<String, String> interactor4B = new HashMap<>();
        interactor4B.put("uniprotkb", "P27958-PRO_0000037576");
        interactor4B.put("intact", "EBI-8753518");

        List<String> pubmeds4 = new ArrayList<String>();
        pubmeds4.add("23420847");

        interaction4.getMethodTypePairListMap().put(new MethodTypePair("MI:0007", "MI:0915"), pubmeds4);
        interaction4.getMethodTypePairListMap().put(new MethodTypePair("MI:0007", "MI:0914"), pubmeds4);

        Map<String, String> experimentToPubmed4 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed4.put("EBI-8753555", "23420847");
        experimentToPubmed4.put("EBI-8763465", "23420847");
        experimentToPubmed4.put("EBI-8753545", "23420847");

        List<CrossReference> publications4 = new ArrayList<CrossReference>(1);
        CrossReference ref4 = new CrossReferenceImpl("pubmed", "23420847");
        publications4.add(ref4);

        interaction4.setId(4);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction4.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction4.setInteractorAccsA(interactor4A);
        interaction4.setInteractorAccsB(interactor4B);
        interaction4.setPublicationIds(publications4);
        interaction4.setOrganismsA(organismB);
        interaction4.setOrganismsB(organismA);
        interaction4.setExperimentToPubmed(experimentToPubmed4);

        interactions.add(interaction1);
        interactions.add(interaction2);
        interactions.add(interaction3);
        interactions.add(interaction4);

        return interactions;
    }

    public List<EncoreInteraction> createNonSelfChainInteractionsSameEntry() {

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism = new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "356411", "hcvjf");
        organism.add(org);

        // From intact-micluster.txt
        // intact:EBI-6858513	intact:EBI-6901421	uniprotkb:Q99IB8-PRO_0000045592	uniprotkb:Q99IB8-PRO_0000045598
        // psi-mi:q99ib8-pro_0000045592|intact:EBI-6929338	psi-mi:q99ib8-pro_0000045598|intact:EBI-6928831
        // psi-mi:"MI:0663"(confocal microscopy)	Popescu et al. (2011)	pubmed:21347350|imex:IM-25873
        // taxid:356411(hcvjf)	taxid:356411(hcvjf)	psi-mi:"MI:0403"(colocalization)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-8771797	intact-miscore:0.2677602
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1A = new HashMap<>();
        interactor1A.put("uniprotkb", "Q99IB8-PRO_0000045592");
        interactor1A.put("intact", "EBI-6858513");
        Map<String, String> interactor1B = new HashMap<>();
        interactor1B.put("uniprotkb", "Q99IB8-PRO_0000045598");
        interactor1B.put("intact", "EBI-6901421");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("21347350");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0663", "MI:0403"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-8771797", "21347350");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "21347350");
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

        // From intact-micluster.txt
        // intact:EBI-6927873	intact:EBI-6901421	uniprotkb:Q99IB8-PRO_0000045602	uniprotkb:Q99IB8-PRO_0000045598
        // psi-mi:q99ib8-pro_0000045602|intact:EBI-9232975|intact:EBI-6931023	psi-mi:q99ib8-pro_0000045598|intact:EBI-6928831
        // psi-mi:"MI:0663"(confocal microscopy)	Popescu et al. (2011)	pubmed:21347350|imex:IM-25873
        // taxid:356411(hcvjf)	taxid:356411(hcvjf)	psi-mi:"MI:0403"(colocalization)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-8771783	intact-miscore:0.2677602
        EncoreInteraction interaction2 = new EncoreInteraction();
        Map<String, String> interactor2A = new HashMap<>();
        interactor2A.put("uniprotkb", "Q99IB8-PRO_0000045602");
        interactor2A.put("intact", "EBI-6927873");
        Map<String, String> interactor2B = new HashMap<>();
        interactor2B.put("uniprotkb", "Q99IB8-PRO_0000045598");
        interactor2B.put("intact", "EBI-6901421");

        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("21347350");

        interaction2.getMethodTypePairListMap().put(new MethodTypePair("MI:0663", "MI:0403"), pubmeds2);

        Map<String, String> experimentToPubmed2 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed2.put("EBI-8771783", "21347350");

        List<CrossReference> publications2 = new ArrayList<CrossReference>(1);
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "21347350");
        publications2.add(ref2);

        interaction2.setId(2);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction2.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction2.setInteractorAccsA(interactor2A);
        interaction2.setInteractorAccsB(interactor2B);
        interaction2.setPublicationIds(publications2);
        interaction2.setOrganismsA(organism);
        interaction2.setOrganismsB(organism);
        interaction2.setExperimentToPubmed(experimentToPubmed2);

        interactions.add(interaction1);
        interactions.add(interaction2);

        return interactions;
    }
        public MiClusterContext createNonSelfChainInteractionsClusterContext(){
        MiClusterContext context = new MiClusterContext();

        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0403", "colocalization");
        context.getMiTerms().put("MI:0663", "confocal microscopy");
        context.getMiTerms().put("MI:0007", "anti tag coimmunoprecipitation");

        context.getSpokeExpandedInteractions().add("EBI-8753555");

        context.getInteractionToMethod_type().put("EBI-6931073", new MethodTypePair("MI:0663", "MI:0403"));
        context.getInteractionToMethod_type().put("EBI-6919128", new MethodTypePair("MI:0007", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-6931011", new MethodTypePair("MI:0007", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-6931100", new MethodTypePair("MI:0663", "MI:0403"));
        context.getInteractionToMethod_type().put("EBI-8753555", new MethodTypePair("MI:0007", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-8763465", new MethodTypePair("MI:0007", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-8753545", new MethodTypePair("MI:0007", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-8771797", new MethodTypePair("MI:0663", "MI:0403"));
        context.getInteractionToMethod_type().put("EBI-8771783", new MethodTypePair("MI:0663", "MI:0403"));

        return context;
    }

    @Test
    public void testConversionAndExport(){

        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactionsP27958 = createNonSelfChainInteractions();
        List<EncoreInteraction> interactionsQ99IB8 = createNonSelfChainInteractions();
        interactionsQ99IB8.addAll(createNonSelfChainInteractionsSameEntry());

        List<EncoreInteraction> negativeInteractions = Collections.emptyList();


        MiClusterContext context = createNonSelfChainInteractionsClusterContext();


        CCParameters<SecondCCParametersVersion1> secondParametersForP27958 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsP27958),
                new HashSet<>(negativeInteractions),
                context,
                "P27958");

        CCParameters<SecondCCParametersVersion1> secondParametersForQ99IB8 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsQ99IB8),
                new HashSet<>(negativeInteractions),
                context,
                "Q99IB8");

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>();
        parameters.add(secondParametersForP27958);
        parameters.add(secondParametersForQ99IB8);

        try {
            File testFile = new File("cc_line_chain_non_self_test.txt");
            FileWriter test = new FileWriter(testFile);

            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);
            writer.writeCCLines(parameters);
            writer.close();

            File template = new File(CCLineWriterVersion1NonSelfChainTest.class.getResource("/cc_line_chain_non_self.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
