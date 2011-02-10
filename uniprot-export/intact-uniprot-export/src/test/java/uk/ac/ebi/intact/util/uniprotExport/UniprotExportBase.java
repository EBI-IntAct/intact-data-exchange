package uk.ac.ebi.intact.util.uniprotExport;

import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.*;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParametersImpl;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParametersImpl;
import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Class with samples and utility methods for testing
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public abstract class UniprotExportBase extends IntactBasicTestCase {

    protected String interaction1 = null;
    protected String interaction2 = null;
    protected String interaction3 = null;
    protected String interaction4 = null;

    public List<GOParameters> createGOParameters(){

        List<GOParameters> parameters = new ArrayList<GOParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534";
        String uniprotAc4 = "Q17862";
        String uniprotAc5 = "P33327";

        String pmid1 = "14704431";
        String pmid2 = "18467557";

        Set<String> publications1 = new HashSet<String>(1);
        publications1.add(pmid1);

        Set<String> publications2 = new HashSet<String>(1);
        publications2.add(pmid2);

        GOParameters parameter1 = new GOParametersImpl(uniprotAc1, uniprotAc2, publications1);
        GOParameters parameter2 = new GOParametersImpl(uniprotAc1, uniprotAc4, publications1);
        GOParameters parameter3 = new GOParametersImpl(uniprotAc5, uniprotAc5, publications2);

        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);

        return parameters;
    }

    public List<CCParameters2> createCCParameters(){

        List<CCParameters2> parameters = new ArrayList<CCParameters2>(3);

        String uniprotAc1 = "P28548";
        String uniprotAc2 = "Q22534";
        String uniprotAc3 = "O17670";
        String uniprotAc4 = "Q21361";
        String uniprotAc5 = "P28548-1";
        String uniprotAc6 = "P28548-2";
        String uniprotAc7 = "P28548-PRO_0000068244";

        String intactAc1 = "EBI-327642";
        String intactAc2 = "EBI-311862";
        String intactAc3 = "EBI-311862";
        String intactAc4 = "EBI-317777";
        String intactAc5 = "EBI-317778";
        String intactAc6 = "EBI-317779";

        String geneName1 = "kin-10";
        String geneName2 = "pat-12";
        String geneName3 = "eya-1";
        String geneName4 = "atf-2";

        String organismName = "Caenorhabditis elegans";
        String taxId = "6239";

        String organismName2 = "Homo sapiens";
        String taxId2 = "9606";

        String pmid1 = "14704431";
        String pmid2 = "15199141";
        String pmid3 = "18212739";
        String pmid4 = "15115758";

        Set<String> publications1 = new TreeSet<String>();
        publications1.add(pmid1);

        Set<String> publications2 = new TreeSet<String>();
        publications2.add(pmid1);
        publications2.add(pmid2);

        Set<String> publications3 = new TreeSet<String>();
        publications3.add(pmid3);

        Set<String> publications4 = new TreeSet<String>();
        publications4.add(pmid4);
        publications4.add(pmid2);

        InteractionDetails detail1 = new DefaultInteractionDetails("physical association", "tandem affinity purification", false, publications1);
        InteractionDetails detail2 = new DefaultInteractionDetails("physical association", "two hybrid pooling", false, publications2);
        InteractionDetails detail3 = new DefaultInteractionDetails("association", "anti bait coimmunoprecipitation", true, publications3);
        InteractionDetails detail4 = new DefaultInteractionDetails("physical association", "coimmunoprecipitation", false, publications4);
        InteractionDetails detail5 = new DefaultInteractionDetails("physical association", "two hybrid pooling", false, publications1);

        SortedSet<InteractionDetails> details1 = new TreeSet<InteractionDetails>();
        details1.add(detail1);

        SortedSet<InteractionDetails> details2 = new TreeSet<InteractionDetails>();
        details2.add(detail2);

        SortedSet<InteractionDetails> details3 = new TreeSet<InteractionDetails>();
        details3.add(detail5);
        details3.add(detail3);
        details3.add(detail4);

        SecondCCParameters2 secondParameters1 = new DefaultSecondCCInteractor2(uniprotAc5, intactAc4, uniprotAc2, intactAc1, geneName2, taxId2, organismName2, details1);
        SecondCCParameters2 secondParameters2 = new DefaultSecondCCInteractor2(uniprotAc6, intactAc5, uniprotAc3, intactAc2, geneName3, taxId, organismName, details2);
        SecondCCParameters2 secondParameters3 = new DefaultSecondCCInteractor2(uniprotAc7, intactAc6, uniprotAc4, intactAc3, geneName4, taxId, organismName, details3);

        List<SecondCCParameters2> listOfSecondInteractors1 = new ArrayList<SecondCCParameters2>();
        listOfSecondInteractors1.add(secondParameters1);
        listOfSecondInteractors1.add(secondParameters2);
        listOfSecondInteractors1.add(secondParameters3);

        CCParameters2 parameters1 = new DefaultCCParameters2(uniprotAc1, geneName1, taxId, listOfSecondInteractors1);
        parameters.add(parameters1);

        List<SecondCCParameters2> listOfSecondInteractors2 = new ArrayList<SecondCCParameters2>();
        SecondCCParameters2 secondParameters4 = new DefaultSecondCCInteractor2(uniprotAc2, intactAc1, uniprotAc5, intactAc4, geneName1, taxId, organismName, details1);
        listOfSecondInteractors2.add(secondParameters4);

        CCParameters2 parameters2 = new DefaultCCParameters2(uniprotAc2, geneName2, taxId2, listOfSecondInteractors2);
        parameters.add(parameters2);

        List<SecondCCParameters2> listOfSecondInteractors3 = new ArrayList<SecondCCParameters2>();
        SecondCCParameters2 secondParameters5 = new DefaultSecondCCInteractor2(uniprotAc3, intactAc2, uniprotAc6, intactAc5, geneName1, taxId, organismName, details2);
        listOfSecondInteractors3.add(secondParameters5);

        CCParameters2 parameters3 = new DefaultCCParameters2(uniprotAc3, geneName3, taxId, listOfSecondInteractors3);
        parameters.add(parameters3);

        List<SecondCCParameters2> listOfSecondInteractors4 = new ArrayList<SecondCCParameters2>();
        SecondCCParameters2 secondParameters6 = new DefaultSecondCCInteractor2(uniprotAc4, intactAc3, uniprotAc7, intactAc6, geneName1, taxId, organismName, details3);
        listOfSecondInteractors4.add(secondParameters6);

        CCParameters2 parameters4 = new DefaultCCParameters2(uniprotAc4, geneName4, taxId, listOfSecondInteractors4);
        parameters.add(parameters4);

        return parameters;
    }

    public List<DRParameters> createDRParameters(){

        List<DRParameters> parameters = new ArrayList<DRParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534";
        String uniprotAc4 = "Q17862";
        String uniprotAc5 = "P33327";

        DRParameters parameter1 = new DRParametersImpl(uniprotAc1, 2);
        DRParameters parameter2 = new DRParametersImpl(uniprotAc2, 1);
        DRParameters parameter3 = new DRParametersImpl(uniprotAc4, 1);
        DRParameters parameter4 = new DRParametersImpl(uniprotAc5, 1);

        parameters.add(parameter1);
        parameters.add(parameter2);
        parameters.add(parameter3);
        parameters.add(parameter4);

        return parameters;
    }

    public EncoreInteraction createEncoreInteraction(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548-1");
        interactorA.put("intact", "EBI-317777");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "Q22534");
        interactorB.put("intact", "EBI-327642");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0915", pubmeds);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0676", pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx1", "14704431");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        Collection<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        publications.add(ref);

        interaction.setId(1);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.6"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public EncoreInteraction createSecondEncoreInteraction(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548-2");
        interactorA.put("intact", "EBI-317778");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "O17670");
        interactorB.put("intact", "EBI-311862");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");
        pubmeds.add("15199141");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0915", pubmeds);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0398", pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.7"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public EncoreInteraction createThirdEncoreInteraction(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548-PRO_0000068244");
        interactorA.put("intact", "EBI-317779");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "Q21361");
        interactorB.put("intact", "EBI-311862");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("14704431");
        pubmeds1.add("15199141");
        List<String> pubmeds2 = new ArrayList<String>();
        pubmeds2.add("18212739");
        List<String> pubmeds3 = new ArrayList<String>();
        pubmeds3.add("14704431");
        pubmeds3.add("15199141");
        pubmeds3.add("15115758");
        List<String> pubmeds4 = new ArrayList<String>();
        pubmeds4.add("18212739");
        List<String> pubmeds5 = new ArrayList<String>();
        pubmeds5.add("15199141");
        pubmeds5.add("15115758");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0914", pubmeds2);
        type2Pubmed.put("MI:0915", pubmeds3);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0398", pubmeds1);
        method2Pubmed.put("MI:0006", pubmeds4);
        method2Pubmed.put("MI:0019", pubmeds5);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx4", "14704431");
        experimentToPubmed.put("EBI-xxxxxx6", "15199141");
        experimentToPubmed.put("EBI-xxxxxx7", "15199141");
        experimentToPubmed.put("EBI-xxxxxx8", "18212739");
        experimentToPubmed.put("EBI-xxxxxx9", "15115758");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        Collection<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        publications.add(ref);

        interaction.setId(3);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.8"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public EncoreInteraction createEncoreInteractionLowScore(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P12345");
        interactorA.put("intact", "EBI-317780");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P12346");
        interactorB.put("intact", "EBI-311863");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("19705531");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0914", pubmeds1);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0019", pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx10", "19705531");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        Collection<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "19705531");
        publications.add(ref);

        interaction.setId(4);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.3"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public EncoreInteraction createEncoreInteractionHighScoreSpokeExpanded(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P12346");
        interactorA.put("intact", "EBI-317781");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P12347");
        interactorB.put("intact", "EBI-311864");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("19705532");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0914", pubmeds1);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0398", pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx11", "19705532");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        Collection<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "19705532");
        publications.add(ref);

        interaction.setId(5);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.5"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public EncoreInteraction createEncoreInteractionHighScoreColocalization(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P12347");
        interactorA.put("intact", "EBI-317782");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P12348");
        interactorB.put("intact", "EBI-311865");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("19705533");

        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0403", pubmeds1);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0403", pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx12", "19705533");

        Collection<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        Collection<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "19705533");
        publications.add(ref);

        interaction.setId(6);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "0.7"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);
        interaction.setTypeToPubmed(type2Pubmed);
        interaction.setMethodToPubmed(method2Pubmed);

        return interaction;
    }

    public List<EncoreInteraction> createEncoreInteractions(){
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        interactions.add(createEncoreInteraction());
        interactions.add(createSecondEncoreInteraction());
        interactions.add(createThirdEncoreInteraction());

        return interactions;
    }

    public MiClusterContext createClusterContext(){
        MiClusterContext context = new MiClusterContext();

        context.getGeneNames().put("P28548","Kin-10");
        context.getGeneNames().put("P28548-1","Kin-10");
        context.getGeneNames().put("P28548-2","Kin-10");
        context.getGeneNames().put("P28548-PRO_0000068244","Kin-10");
        context.getGeneNames().put("Q22534","pat-12");
        context.getGeneNames().put("O17670","eya-1");
        context.getGeneNames().put("Q21361","atf-2");
        context.getGeneNames().put("P12345","name-1");
        context.getGeneNames().put("P12346","name-2");
        context.getGeneNames().put("P12347","name-3");
        context.getGeneNames().put("P12348","name-4");

        context.getMiTerms().put("MI:0398", "two hybrid pooling"); // condition export = 2
        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0006", "anti bait coimmunoprecipitation");
        context.getMiTerms().put("MI:0019", "coimmunoprecipitation");
        context.getMiTerms().put("MI:0403", "colocalization");
        context.getMiTerms().put("MI:0676","tandem affinity purification");

        context.getSpokeExpandedInteractions().add("EBI-xxxxxx8");
        context.getSpokeExpandedInteractions().add("EBI-xxxxxx11");

        context.getInteractionToMethod_type().put("EBI-xxxxxx1", new MethodAndTypePair("MI:0676", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx2", new MethodAndTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx3", new MethodAndTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx4", new MethodAndTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx6", new MethodAndTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx7", new MethodAndTypePair("MI:0019", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx8", new MethodAndTypePair("MI:0006", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx9", new MethodAndTypePair("MI:0019", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx10", new MethodAndTypePair("MI:0019", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx11", new MethodAndTypePair("MI:0019", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx12", new MethodAndTypePair("MI:0403", "MI:0403"));

        return context;
    }

    public IntActInteractionClusterScore createClusterForExportBasedOnMiScore(){

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(createEncoreInteractionLowScore());
        interactions.add(createEncoreInteractionHighScoreSpokeExpanded());
        interactions.add(createEncoreInteractionHighScoreColocalization());

        return createCluster(interactions);
    }

    public IntActInteractionClusterScore createClusterForExportBasedOnDetectionMethod(){

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(createEncoreInteractionHighScoreSpokeExpanded());  // two hybrid pooling : export = conditional, 2 and doesn't pass
        interactions.add(createEncoreInteractionHighScoreColocalization()); // colocalization has an export no

        return createCluster(interactions);
    }

    private IntActInteractionClusterScore createCluster(List<EncoreInteraction> interactions){
        IntActInteractionClusterScore clusterScore = new IntActInteractionClusterScore();
        clusterScore.setInteractionMapping(new HashMap<Integer, EncoreInteraction>());
        clusterScore.setInteractorMapping(new HashMap<String, List<Integer>>());

        for (EncoreInteraction interaction : interactions){
            clusterScore.getInteractionMapping().put(interaction.getId(), interaction);

            String interactorA = FilterUtils.extractUniprotAndIntactAcFromAccs(interaction.getInteractorAccsA())[0];
            String interactorB = FilterUtils.extractUniprotAndIntactAcFromAccs(interaction.getInteractorAccsB())[0];

            if (clusterScore.getInteractorMapping().containsKey(interactorA)){
                clusterScore.getInteractorMapping().get(interactorA).add(interaction.getId());
            }
            else{
                List<Integer> interactionIds = new ArrayList<Integer>();
                interactionIds.add(interaction.getId());
                clusterScore.getInteractorMapping().put(interactorA, interactionIds);
            }

            if (clusterScore.getInteractorMapping().containsKey(interactorB)){
                clusterScore.getInteractorMapping().get(interactorB).add(interaction.getId());
            }
            else{
                List<Integer> interactionIds = new ArrayList<Integer>();
                interactionIds.add(interaction.getId());
                clusterScore.getInteractorMapping().put(interactorB, interactionIds);
            }
        }
        return clusterScore;
    }

    public MiClusterScoreResults createMiScoreResultsForMiScoreExport(){
        IntActInteractionClusterScore clsuterScore = createClusterForExportBasedOnMiScore();
        MiClusterContext context = createClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(clsuterScore, context);

        return results;
    }

    public MiClusterScoreResults createMiScoreResultsForDetectionMethodExport(){
        IntActInteractionClusterScore clsuterScore = createClusterForExportBasedOnDetectionMethod();
        MiClusterContext context = createClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(clsuterScore, context);

        return results;
    }

    public void createDatabaseContext(){
        // dr export and confidence annotation topic
        CvTopic dr_export = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT);
        getCorePersister().saveOrUpdate(dr_export);

        CvTopic confidence = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, CvTopic.AUTHOR_CONFIDENCE_MI_REF, CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        getCorePersister().saveOrUpdate(confidence);

        CvTopic accepted = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.ACCEPTED);
        getCorePersister().saveOrUpdate(accepted);

        // the different methods and their export status
        CvInteraction method = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0398", "two hybrid pooling");
        Annotation annotation1 = new Annotation(dr_export, "2");
        method.addAnnotation(annotation1);

        CvInteraction method2 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0006", "anti bait coimmunoprecipitation");
        Annotation annotation2 = new Annotation(dr_export, "yes");
        method2.addAnnotation(annotation2);

        CvInteraction method3 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0019", "coimmunoprecipitation");
        Annotation annotation3 = new Annotation(dr_export, "yes");
        method3.addAnnotation(annotation3);

        CvInteraction method4 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0403", "colocalization");
        Annotation annotation4 = new Annotation(dr_export, "no");
        method4.addAnnotation(annotation4);

        CvInteraction method5 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0676", "tandem affinity purification");
        Annotation annotation5 = new Annotation(dr_export, "yes");
        method5.addAnnotation(annotation5);

        getCorePersister().saveOrUpdate(method, method2, method3, method4, method5);

    }

    public void createExperimentContext(){
        // dr export and confidence annotation topic
        CvTopic dr_export = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT);
        getCorePersister().saveOrUpdate(dr_export);

        CvTopic confidence = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, CvTopic.AUTHOR_CONFIDENCE_MI_REF, CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        getCorePersister().saveOrUpdate(confidence);

        CvTopic accepted = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.ACCEPTED);
        getCorePersister().saveOrUpdate(accepted);

        // the different methods and their export status
        CvInteraction method = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0398", "two hybrid pooling");
        Annotation annotation1 = new Annotation(dr_export, "2");
        method.addAnnotation(annotation1);

        CvInteraction method2 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0006", "anti bait coimmunoprecipitation");
        Annotation annotation2 = new Annotation(dr_export, "yes");
        method2.addAnnotation(annotation2);

        CvInteraction method3 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0019", "coimmunoprecipitation");
        Annotation annotation3 = new Annotation(dr_export, "yes");
        method3.addAnnotation(annotation3);

        CvInteraction method4 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0403", "colocalization");
        Annotation annotation4 = new Annotation(dr_export, "no");
        method4.addAnnotation(annotation4);

        CvInteraction method5 = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvInteraction.class, "MI:0676", "tandem affinity purification");
        Annotation annotation5 = new Annotation(dr_export, "yes");
        method5.addAnnotation(annotation5);

        getCorePersister().saveOrUpdate(method, method2, method3, method4, method5);

        // several experiments and their export status
        Experiment experiment_yes = getMockBuilder().createExperimentRandom(1);
        Annotation expAnn1 = new Annotation(dr_export, "yes");
        experiment_yes.addAnnotation(expAnn1);
        Annotation expAnnAcc1 = new Annotation(accepted, null);
        experiment_yes.addAnnotation(expAnnAcc1);
        interaction1 = experiment_yes.getInteractions().iterator().next().getAc();

        Experiment experiment_no = getMockBuilder().createExperimentRandom(1);
        Annotation expAnn2 = new Annotation(dr_export, "no");
        experiment_no.addAnnotation(expAnn2);
        Annotation expAnnAcc2 = new Annotation(accepted, null);
        experiment_no.addAnnotation(expAnnAcc2);
        interaction2 = experiment_no.getInteractions().iterator().next().getAc();

        Experiment experiment_condition = getMockBuilder().createExperimentRandom(2);
        Annotation expAnn3 = new Annotation(dr_export, "high");
        experiment_condition.addAnnotation(expAnn3);
        Annotation expAnnAcc3 = new Annotation(accepted, null);
        experiment_condition.addAnnotation(expAnnAcc3);

        int index = 3;
        for (Interaction interaction : experiment_condition.getInteractions()){
            if (index == 3){
                Annotation confidence1 = new Annotation(confidence, "high");
                interaction.addAnnotation(confidence1);
                interaction3 = interaction.getAc();
            }
            else {
                Annotation confidence2 = new Annotation(confidence, "low");
                interaction.addAnnotation(confidence2);
                interaction4 = interaction.getAc();
            }
            index++;
        }

        getCorePersister().saveOrUpdate(experiment_yes, experiment_condition, experiment_no);
    }

    public boolean areFilesEqual (File file1, File file2) throws IOException {

        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
        BufferedReader reader2 = new BufferedReader(new FileReader(file2));

        String line1 = reader1.readLine();
        String line2 = reader2.readLine();

        boolean isEqual = true;

        while (line1 != null && line2 != null && isEqual){
            isEqual = line1.equals(line2);
            line1 = reader1.readLine();
            line2 = reader2.readLine();
        }

        return isEqual;
    }
}
