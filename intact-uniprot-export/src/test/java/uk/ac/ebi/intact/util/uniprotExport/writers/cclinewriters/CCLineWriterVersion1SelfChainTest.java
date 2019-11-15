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

public class CCLineWriterVersion1SelfChainTest extends UniprotExportBase {

    public List<EncoreInteraction> createSelfChainInteractionsP03300(){

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism= new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "12081", "pol1m");
        organism.add(org);

        // From intact-micluster.txt
        // intact:EBI-914162	intact:EBI-914162	uniprotkb:P03300-PRO_0000040090	uniprotkb:P03300-PRO_0000040090
        // psi-mi:p03300-pro_0000040090	psi-mi:p03300-pro_0000040090	psi-mi:"MI:0030"(cross-linking study)
        // Hobson et al. (2001)	pubmed:11230138|imex:IM-18988	taxid:12081(pol1m)	taxid:12081(pol1m)
        // psi-mi:"MI:0407"(direct interaction)	psi-mi:"MI:0469"(IntAct)	intact:EBI-944619	intact-miscore:0.43887317
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1 = new HashMap<>();
        interactor1.put("uniprotkb", "P03300-PRO_0000040090");
        interactor1.put("intact", "EBI-914162");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("11230138");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0030", "MI:0407"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-944619", "11230138");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "11230138");
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

    public List<EncoreInteraction> createSelfChainInteractionsQ99IB8() {

        List<EncoreInteraction> interactions = new ArrayList<>();

        //Common organisms to all the interactions
        List<CrossReference> organism = new ArrayList<CrossReference>();
        CrossReference org = new CrossReferenceImpl("taxid", "356411", "hcvjf");
        organism.add(org);

        // From intact-micluster.txt
        // intact:EBI-6858513	intact:EBI-6858513	uniprotkb:Q99IB8-PRO_0000045592	uniprotkb:Q99IB8-PRO_0000045592
        // psi-mi:q99ib8-pro_0000045592|intact:EBI-6929338	psi-mi:q99ib8-pro_0000045592|intact:EBI-6929338
        // psi-mi:"MI:0029"(cosedimentation through density gradient)	Lee et al. (2013)	pubmed:24009866|imex:IM-25847
        // taxid:356411(hcvjf)	taxid:356411(hcvjf)	psi-mi:"MI:1126"(self interaction)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-8782754	intact-miscore:0.43887317
        EncoreInteraction interaction1 = new EncoreInteraction();
        Map<String, String> interactor1 = new HashMap<>();
        interactor1.put("uniprotkb", "Q99IB8-PRO_0000045592");
        interactor1.put("intact", "EBI-6858513");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("24009866");

        interaction1.getMethodTypePairListMap().put(new MethodTypePair("MI:0029", "MI:1126"), pubmeds1);

        Map<String, String> experimentToPubmed1 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed1.put("EBI-8782754", "24009866");

        List<CrossReference> publications1 = new ArrayList<CrossReference>(1);
        CrossReference ref1 = new CrossReferenceImpl("pubmed", "24009866");
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
        // intact:EBI-6901449	intact:EBI-6901449	uniprotkb:Q99IB8-PRO_0000045596	uniprotkb:Q99IB8-PRO_0000045596
        // psi-mi:q99ib8-pro_0000045596|intact:EBI-6927909	psi-mi:q99ib8-pro_0000045596|intact:EBI-6927909
        // psi-mi:"MI:0096"(pull down)	Stapleford et al. (2011)	pubmed:21147927|imex:IM-25913
        // taxid:356411(hcvjf)	taxid:356411(hcvjf)	psi-mi:"MI:0914"(association)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-6901477	intact-miscore:0.34505215
        EncoreInteraction interaction2 = new EncoreInteraction();
        Map<String, String> interactor2 = new HashMap<>();
        interactor2.put("uniprotkb", "Q99IB8-PRO_0000045596");
        interactor2.put("intact", "EBI-6901449");

        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("21147927");

        interaction2.getMethodTypePairListMap().put(new MethodTypePair("MI:0096", "MI:0914"), pubmeds2);

        Map<String, String> experimentToPubmed2 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed2.put("EBI-6901477", "21147927");

        List<CrossReference> publications2 = new ArrayList<CrossReference>(1);
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "21147927");
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

        // From intact-micluster.txt
        // intact:EBI-6901421	intact:EBI-6901421	uniprotkb:Q99IB8-PRO_0000045598	uniprotkb:Q99IB8-PRO_0000045598
        // psi-mi:q99ib8-pro_0000045598|intact:EBI-6928831	psi-mi:q99ib8-pro_0000045598|intact:EBI-6928831
        // psi-mi:"MI:0276"(blue native page)	Stapleford et al. (2011)	pubmed:21147927|imex:IM-25913
        // taxid:356411(hcvjf)	taxid:356411(hcvjf)	psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:1335"(HPIDb)	intact:EBI-6901505	intact-miscore:0.40116468
        EncoreInteraction interaction3 = new EncoreInteraction();
        Map<String, String> interactor3 = new HashMap<>();
        interactor3.put("uniprotkb", "Q99IB8-PRO_0000045598");
        interactor3.put("intact", "EBI-6901421");

        List<String> pubmeds3 = new ArrayList<String>();
        pubmeds3.add("21147927");

        interaction3.getMethodTypePairListMap().put(new MethodTypePair("MI:0276", "MI:0915"), pubmeds3);

        Map<String, String> experimentToPubmed3 = new HashMap<String, String>();
        //Experiment obtained by the editor
        experimentToPubmed3.put("EBI-6901505", "21147927");

        List<CrossReference> publications3 = new ArrayList<CrossReference>(1);
        CrossReference ref3 = new CrossReferenceImpl("pubmed", "21147927");
        publications3.add(ref3);

        interaction3.setId(3);
        //We ignored the original intact-miscore for the test, it gets recalculated
        interaction3.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction3.setInteractorAccsA(interactor3);
        interaction3.setInteractorAccsB(interactor3);
        interaction3.setPublicationIds(publications3);
        interaction3.setOrganismsA(organism);
        interaction3.setOrganismsB(organism);
        interaction3.setExperimentToPubmed(experimentToPubmed3);

        interactions.add(interaction1);
        interactions.add(interaction2);
        interactions.add(interaction3);

        return interactions;
    }

    public MiClusterContext createSelfChainInteractionsClusterContext(){
        MiClusterContext context = new MiClusterContext();

        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0276", "blue native page");
        context.getMiTerms().put("MI:0096", "pull down");
        context.getMiTerms().put("MI:0407", "direct interaction");
        context.getMiTerms().put("MI:0030", "cross-linking study");
        context.getMiTerms().put("MI:0029", "cosedimentation through density gradient");
        context.getMiTerms().put("MI:1126", "self interaction");

        context.getSpokeExpandedInteractions().add("EBI-6901477");

        context.getInteractionToMethod_type().put("EBI-944619", new MethodTypePair("MI:0030", "MI:0407"));
        context.getInteractionToMethod_type().put("EBI-8782754", new MethodTypePair("MI:0029", "MI:1126"));
        context.getInteractionToMethod_type().put("EBI-6901477", new MethodTypePair("MI:0096", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-6901505", new MethodTypePair("MI:0276", "MI:0915"));

        return context;
    }

    @Test
    public void testConversionAndExport(){

        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactionsP03300 = createSelfChainInteractionsP03300();
        List<EncoreInteraction> interactionsQ99IB8 = createSelfChainInteractionsQ99IB8();

        List<EncoreInteraction> negativeInteractions = Collections.emptyList();


        MiClusterContext context = createSelfChainInteractionsClusterContext();


        CCParameters<SecondCCParametersVersion1> secondParametersForP03300 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsP03300),
                new HashSet<>(negativeInteractions),
                context,
                "P03300");

        CCParameters<SecondCCParametersVersion1> secondParametersForQ99IB8 = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<>(interactionsQ99IB8),
                new HashSet<>(negativeInteractions),
                context,
                "Q99IB8");

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>();
        parameters.add(secondParametersForP03300);
        parameters.add(secondParametersForQ99IB8);

        try {
            File testFile = new File("cc_line_chain_self_test.txt");
            FileWriter test = new FileWriter(testFile);

            CCLineWriter<CCParameters<SecondCCParametersVersion1>> writer = new CCLineWriterVersion1(test);
            writer.writeCCLines(parameters);
            writer.close();

            File template = new File(CCLineWriterVersion1SelfChainTest.class.getResource("/cc_line_chain_self.txt").getFile());

            Assert.assertTrue(areFilesEqual(testFile, template));

            testFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

