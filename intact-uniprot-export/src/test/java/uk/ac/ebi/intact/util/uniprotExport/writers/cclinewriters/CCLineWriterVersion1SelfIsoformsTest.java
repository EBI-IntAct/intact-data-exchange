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

public class CCLineWriterVersion1SelfIsoformsTest extends UniprotExportBase {

    public List<EncoreInteraction> createSelfIsoformInteractionsQ04206(){

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism = new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "9606", "human");
        organism.add(org);

        // From intact-micluster.txt
        // intact:EBI-289947	intact:EBI-289947	uniprotkb:Q04206-2	uniprotkb:Q04206-2
        // uniprotkb:RELA|…	uniprotkb:RELA|...	psi-mi:"MI:0676"(tandem affinity purification)
        // Bouwmeester et al. (2004)	pubmed:14743216|mint:MINT-5216883
        // taxid:9606(human)	taxid:9606(human)	psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0469"(IntAct)	intact:EBI-363418	author score:low|intact-miscore:0.40116468
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1 = new HashMap<>();
        interactor1.put("uniprotkb", "Q04206-2");
        interactor1.put("intact", "EBI-289947");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("14743216");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0676", "MI:0915"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-363418", "14743216");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "14743216");
        publications1.add(ref1);

        interaction1.setId(1);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction1.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction1.setInteractorAccsA(interactor1);
        interaction1.setInteractorAccsB(interactor1);
        interaction1.setPublicationIds(publications1);
        interaction1.setOrganismsA(organism);
        interaction1.setOrganismsB(organism);
        interaction1.setExperimentToPubmed(experimentToPubmed1);

        interactions.add(interaction1);

        return interactions;
    }

    public List<EncoreInteraction> createSelfIsoformInteractionsQ14790() {

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism = new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "9606", "human");
        organism.add(org);

        // From intact-micluster.txt
        // intact:EBI-288309	intact:EBI-288309	uniprotkb:Q14790-1	uniprotkb:Q14790-1
        // uniprotkb:CASP8|…	uniprotkb:CASP8|…	psi-mi:"MI:0018"(two hybrid)	Boldin et al. (1996)
        // pubmed:8681376	taxid:9606(human)	taxid:9606(human)	psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0469"(IntAct)	intact:EBI-527187|intact:EBI-527179	author score:C|author score:B|intact-miscore:0.36784992
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1 = new HashMap<>();
        interactor1.put("uniprotkb", "Q14790-1");
        interactor1.put("intact", "EBI-288309");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("8681376");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0018", "MI:0915"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-527187", "8681376");
        experimentToPubmed1.put("EBI-527179", "8681376");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "8681376");
        publications1.add(ref1);

        interaction1.setId(1);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction1.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction1.setInteractorAccsA(interactor1);
        interaction1.setInteractorAccsB(interactor1);
        interaction1.setPublicationIds(publications1);
        interaction1.setOrganismsA(organism);
        interaction1.setOrganismsB(organism);
        interaction1.setExperimentToPubmed(experimentToPubmed1);

        // From intact-micluster.txt
        // intact:EBI-288326	intact:EBI-288326	uniprotkb:Q14790-5	uniprotkb:Q14790-5
        // uniprotkb:CASP8|…	uniprotkb:CASP8|…	psi-mi:"MI:0018"(two hybrid)	Boldin et al. (1996)
        // pubmed:8681376	taxid:9606(human)	taxid:9606(human)	psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0469"(IntAct)	intact:EBI-527114	author score:A|intact-miscore:0.36784992
        EncoreInteraction interaction2 = new EncoreInteraction();
        Map<String, String> interactor2 = new HashMap<>();
        interactor2.put("uniprotkb", "Q14790-5");
        interactor2.put("intact", "EBI-288326");

        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("8681376");

        interaction2.getMethodTypePairListMap().put(new MethodTypePair("MI:0018", "MI:0915"), pubmeds2);

        Map<String, String> experimentToPubmed2 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed2.put("EBI-527114", "8681376");

        List<CrossReference> publications2 = new ArrayList<CrossReference>(1);
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "8681376");
        publications2.add(ref2);

        interaction2.setId(2);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction2.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction2.setInteractorAccsA(interactor2);
        interaction2.setInteractorAccsB(interactor2);
        interaction2.setPublicationIds(publications2);
        interaction2.setOrganismsA(organism);
        interaction2.setOrganismsB(organism);
        interaction2.setExperimentToPubmed(experimentToPubmed2);

        interactions.add(interaction1);
        interactions.add(interaction2);

        return interactions;
    }

    public MiClusterContext createSelfChainInteractionsClusterContext() {

        MiClusterContext context = new MiClusterContext();

        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0676", "tandem affinity purification");

        context.getSpokeExpandedInteractions().add("EBI-");

        context.getInteractionToMethod_type().put("EBI-363418", new MethodTypePair("MI:0676", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-527187", new MethodTypePair("MI:0018", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-527179", new MethodTypePair("MI:0018", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-527114", new MethodTypePair("MI:0018", "MI:0915"));

        return context;
    }

    @Test
    public void testConversionAndExport(){

        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactionsQ04206 = createSelfIsoformInteractionsQ04206();
        List<EncoreInteraction> interactionsQ14790 = createSelfIsoformInteractionsQ14790();

        List<EncoreInteraction> negativeInteractions = Collections.emptyList();


        MiClusterContext context = createSelfChainInteractionsClusterContext();


        CCParameters<SecondCCParametersVersion1> secondParametersForQ04206 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsQ04206),
                new HashSet<>(negativeInteractions),
                context,
                "Q04206");

        CCParameters<SecondCCParametersVersion1> secondParametersForQ14790 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsQ14790),
                new HashSet<>(negativeInteractions),
                context,
                "Q14790");

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>();
        parameters.add(secondParametersForQ04206);
        parameters.add(secondParametersForQ14790);

        try {
            File testFile = new File("cc_line_isoforms_self.txt");
            FileWriter test = new FileWriter(testFile);

            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);
            writer.writeCCLines(parameters);
            writer.close();

            File template = new File(CCLineWriterVersion1SelfIsoformsTest.class.getResource("/cc_line_isoforms_self.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
