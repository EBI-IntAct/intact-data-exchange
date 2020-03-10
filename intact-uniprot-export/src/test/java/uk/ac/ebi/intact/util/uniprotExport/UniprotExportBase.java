package uk.ac.ebi.intact.util.uniprotExport;

import org.junit.Assert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1Impl;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParametersImpl;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters2;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntActClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

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
    protected String interaction5 = null;
    protected String interaction6 = null;

    public List<GOParameters> createGOParametersVersion1(){

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

        GOParameters parameter1 = new GOParameters1(uniprotAc1, uniprotAc2, publications1);
        GOParameters parameter4 = new GOParameters1(uniprotAc2, uniprotAc1, publications1);
        GOParameters parameter2 = new GOParameters1(uniprotAc1, uniprotAc4, publications1);
        GOParameters parameter5 = new GOParameters1(uniprotAc4, uniprotAc1, publications1);
        GOParameters parameter3 = new GOParameters1(uniprotAc5, uniprotAc5, publications2);

        parameters.add(parameter1);
        parameters.add(parameter4);
        parameters.add(parameter2);
        parameters.add(parameter5);
        parameters.add(parameter3);

        return parameters;
    }

    public List<GOParameters> createGOParametersVersion2(){

        List<GOParameters> parameters = new ArrayList<GOParameters>(3);

        String uniprotAc1 = "Q9NET8";
        String uniprotAc2 = "Q22534-2";
        String uniprotAc4 = "Q17862-PRO_xxxxxxx";
        String uniprotAc5 = "P33327-3";

        String pmid1 = "14704431";
        String pmid2 = "18467557";
        
        Set<String> componentsXrefs = new HashSet<String>(2);
        componentsXrefs.add("GO:xxxxxx1");
        componentsXrefs.add("GO:xxxxxx2");

        GOParameters parameter1 = new GOParameters2(uniprotAc1, uniprotAc2, pmid1, uniprotAc1, Collections.EMPTY_SET);
        GOParameters parameter4 = new GOParameters2(uniprotAc2, uniprotAc1, pmid1, "Q22534", componentsXrefs);
        GOParameters parameter2 = new GOParameters2(uniprotAc1, uniprotAc4, pmid1, uniprotAc1, Collections.EMPTY_SET);
        GOParameters parameter5 = new GOParameters2(uniprotAc4, uniprotAc1, pmid1, "Q17862", Collections.EMPTY_SET);
        GOParameters parameter3 = new GOParameters2(uniprotAc5, uniprotAc5, pmid2, "P33327", Collections.EMPTY_SET);

        parameters.add(parameter1);
        parameters.add(parameter4);
        parameters.add(parameter2);
        parameters.add(parameter5);
        parameters.add(parameter3);

        return parameters;
    }

    public List<CCParameters<SecondCCParametersVersion1>> createCCParametersVersion1(){

        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>(3);

        String uniprotAc1 = "P28548";
        String uniprotAc2 = "Q22534";
        String uniprotAc3 = "O17670";
        String uniprotAc5 = "P28548-1";
        String uniprotAc6 = "P28548-2";

        String intactAc1 = "EBI-327642";
        String intactAc2 = "EBI-311862";
        String intactAc4 = "EBI-317777";
        String intactAc5 = "EBI-317778";

        String geneName1 = "kin-10";
        String geneName2 = "pat-12";
        String geneName3 = "eya-1";

        String taxId = "6239";
        String taxId2 = "9606";

        SecondCCParametersVersion1 secondParameters1 = new SecondCCParametersVersion1Impl(uniprotAc5, intactAc4, taxId, uniprotAc2, intactAc1, taxId2, geneName2, 1);
        SecondCCParametersVersion1 secondParameters2 = new SecondCCParametersVersion1Impl(uniprotAc6, intactAc5, taxId, uniprotAc3, intactAc2, taxId, geneName3, 2);

        SortedSet<SecondCCParametersVersion1> listOfSecondInteractors1 = new TreeSet<SecondCCParametersVersion1>();
        listOfSecondInteractors1.add(secondParameters1);
        listOfSecondInteractors1.add(secondParameters2);

        CCParameters<SecondCCParametersVersion1> parameters1 = new CCParametersVersion1(uniprotAc1, geneName1, taxId, listOfSecondInteractors1);
        parameters.add(parameters1);

        SortedSet<SecondCCParametersVersion1> listOfSecondInteractors2 = new TreeSet<SecondCCParametersVersion1>();
        SecondCCParametersVersion1 secondParameters4 = new SecondCCParametersVersion1Impl(uniprotAc2, intactAc1, taxId2, uniprotAc5, intactAc4, taxId, geneName1, 1);
        listOfSecondInteractors2.add(secondParameters4);

        CCParameters<SecondCCParametersVersion1> parameters2 = new CCParametersVersion1(uniprotAc2, geneName2, taxId2, listOfSecondInteractors2);
        parameters.add(parameters2);

        SortedSet<SecondCCParametersVersion1> listOfSecondInteractors3 = new TreeSet<SecondCCParametersVersion1>();
        SecondCCParametersVersion1 secondParameters5 = new SecondCCParametersVersion1Impl(uniprotAc3, intactAc2, taxId, uniprotAc6, intactAc5, taxId, geneName1, 2);
        listOfSecondInteractors3.add(secondParameters5);

        CCParameters<SecondCCParametersVersion1> parameters3 = new CCParametersVersion1(uniprotAc3, geneName3, taxId, listOfSecondInteractors3);
        parameters.add(parameters3);

        return parameters;
    }

    public List<CCParameters<SecondCCParametersVersion1>> createCCParametersVersion1ForSorting(){

        String masterUniprot = "O00499";
        String masterGeneName = "BIN1";
        String taxId = "9606";

        //All human except the ones with explicit taxid
        String uniprotAc1 = "O00499";
        String intactAc1 = "EBI-719094";

        String uniprotAc2 = "Q9UBW5";
        String intactAc2 = "EBI-2042570";
        String geneName2 = "Aaa";

        String uniprotAc3 = "Q9Y2H0";
        String intactAc3 = "EBI-722139";
        String geneName3 = "aAA";

        String uniprotAc4 = "Q8NFH8";
        String intactAc4 = "EBI-7067016";
        String geneName4 = "REPS2";

        String uniprotAc5 = "Q8NFH8-2";
        String intactAc5 = "EBI-8029141";
        String geneName5 = "REPS2";

        String uniprotAc6 = "Q13426";
        String intactAc6 = "EBI-717592";
        String geneName6 = "XRCC4";

        String uniprotAc8 = "O00499-PRO_0000090001";
        String intactAc8 = "EBI-000001";

        String uniprotAc9 = "O95219";
        String intactAc9 = "EBI-724909";
        String taxId9 = "9608";
        String geneName9 = "SNX4";

        String uniprotAc10 = "P27958-PRO_0000037576";
        String intactAc10 = "EBI-8753518";
        String taxId10 = "11108";

        String uniprotAc11 = "Q9WMX2-PRO_0000037551";
        String intactAc11 = "EBI-6863748";
        String taxId11 = "333284";

        String uniprotAc12 = "O00499-1";
        String intactAc12 = "EBI-6926280";

        String uniprotAc13 = "P10636-7";
        String intactAc13 = "EBI-6926270";
        String geneName13 = "MAPT";

        String uniprotAc14 = "P10636-8";
        String intactAc14 = "EBI-366233";
        String geneName14 = "MAPT";

        String uniprotAc15 = "O00499-10";
        String intactAc15 = "EBI-7689134";

        String uniprotAc16 = "P01106";
        String intactAc16 = "EBI-447544";
        String geneName16 = "MYC";

        String uniprotAc17 = "O00499-7";
        String intactAc17 = "EBI-8870146";

        String uniprotAc18 = "Q9UBW5";
        String intactAc18 = "EBI-2042570";
        String geneName18 = "Aaa";

        String uniprotAc19 = "O00499-1";
        String intactAc19 = "EBI-6926280";

        String uniprotAc20 = "O00499-7";
        String intactAc20 = "EBI-8870146";

        String uniprotAc21 = "O00499-PRO_0000090001";
        String intactAc21 = "EBI-000001";

        String uniprotAc22 = "Z13496";
        String intactAc22 = "EBI-2864109";
        String geneName22 = "MTM1";
        String taxId22 = "333284";

        String uniprotAc23 = "P27958-PRO_0000037576";
        String intactAc23 = "EBI-8753518";
        String taxId23 = "333284";

        String uniprotAc25 = "O00499-7";
        String intactAc25 = "EBI-8870146";

        String uniprotAc26 = "O00499-PRO_0000090002";
        String intactAc26 = "EBI-000002";


        List<CCParameters<SecondCCParametersVersion1>> parameters = new ArrayList<>(1);
        SortedSet<SecondCCParametersVersion1> listOfSecondInteractors = new TreeSet<SecondCCParametersVersion1>();

        CCParameters<SecondCCParametersVersion1> parameters1 = new CCParametersVersion1(masterUniprot, masterGeneName, taxId, listOfSecondInteractors);
        parameters.add(parameters1);

        // Molecule A = O00499
        // Molecule B: not Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc2, intactAc2, taxId, geneName2, 2));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc3, intactAc3, taxId, geneName3, 4));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc4, intactAc4, taxId, geneName4, 6));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc5, intactAc5, taxId, geneName5, 6));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc6, intactAc6, taxId, geneName6, 4));

        // Molecule B: not Xeno, without gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc1, intactAc1, taxId, null, 3));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc8, intactAc8, taxId, null, 5));

        // Molecule B: Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc9, intactAc9, taxId9, geneName9, 4));

        // Molecule B: Xeno, without gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc10, intactAc10, taxId10, null, 11));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc1, intactAc1, taxId, uniprotAc11, intactAc11, taxId11, null, 5));

        // Molecule A = O00499-1
        // Molecule B: not Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc12, intactAc12, taxId, uniprotAc13, intactAc13, taxId, geneName13, 5));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc12, intactAc12, taxId, uniprotAc14, intactAc14, taxId, geneName14, 6));

        // Molecule A = O00499-10
        // Molecule B: not Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc15, intactAc15, taxId, uniprotAc16, intactAc16, taxId, geneName16, 3));

        // Molecule A = O00499-7
        // Molecule B: not Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc18, intactAc18, taxId, geneName18, 2));

        // Molecule B: not Xeno, without gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc19, intactAc19, taxId, null, 2));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc20, intactAc20, taxId, null, 2));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc21, intactAc21, taxId, null, 5));

        // Molecule B: Xeno, with gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc22, intactAc22, taxId22, geneName22, 6));

        // Molecule B: Xeno, without gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc17, intactAc17, taxId, uniprotAc23, intactAc23, taxId23, null, 2));

        // Molecule A = PRO_0000090001
        // Molecule B: not Xeno, without gene name
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc8, intactAc8, taxId, uniprotAc25, intactAc25, taxId, null, 5));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc8, intactAc8, taxId, uniprotAc8, intactAc8, taxId, null, 5));
        listOfSecondInteractors.add(new SecondCCParametersVersion1Impl(uniprotAc8, intactAc8, taxId, uniprotAc26, intactAc26, taxId, null, 5));

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
        interactorB.put("uniprotkb", "Q22534"); // Human for testing purposes
        interactorB.put("intact", "EBI-327642");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0676", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx1", "14704431");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        publications.add(ref);

        interaction.setId(1);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "11"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public EncoreInteraction createNegativeEncoreInteraction(){
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

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);

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

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public EncoreInteraction createIsoformIsoformInteraction(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548-2");
        interactorA.put("intact", "EBI-317778");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P28548-1");
        interactorB.put("intact", "EBI-317777");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");
        pubmeds.add("15199141");

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public EncoreInteraction createEncoreInteractionWithTransIsoformAndMaster(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548");
        interactorA.put("intact", "EBI-317888");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P12347-4");
        interactorB.put("intact", "EBI-99999999");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");
        pubmeds.add("15199141");

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public EncoreInteraction createEncoreInteractionWithTransIsoform(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "O17670");
        interactorA.put("intact", "EBI-311862");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "P12347-4");
        interactorB.put("intact", "EBI-99999999");

        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("14704431");
        pubmeds.add("15199141");

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx2", "14704431");
        experimentToPubmed.put("EBI-xxxxxx3", "15199141");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "15199141");
        publications.add(ref);
        publications.add(ref2);

        interaction.setId(2);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismA);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public EncoreInteraction createThirdEncoreInteraction(){
        EncoreInteraction interaction = new EncoreInteraction();

        Map<String, String> interactorA = new HashMap<String, String>();
        interactorA.put("uniprotkb", "P28548-PRO_0000068244");
        interactorA.put("intact", "EBI-317779");
        Map<String, String> interactorB = new HashMap<String, String>();
        interactorB.put("uniprotkb", "Q21361");
        interactorB.put("intact", "EBI-317743");

        List<String> pubmeds1 = new ArrayList<String>();
        pubmeds1.add("14704431");
        List<String> pubmeds4 = new ArrayList<String>();
        pubmeds4.add("18212739");
        List<String> pubmeds5 = new ArrayList<String>();
        pubmeds5.add("15199141");
        pubmeds5.add("15115758");

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0006", "MI:0914"), pubmeds4);
        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0019", "MI:0915"), pubmeds5);
        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0915"), pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx4", "14704431");
        experimentToPubmed.put("EBI-xxxxxx6", "15199141");
        experimentToPubmed.put("EBI-xxxxxx7", "15199141");
        experimentToPubmed.put("EBI-xxxxxx8", "18212739");
        experimentToPubmed.put("EBI-xxxxxx9", "15115758");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        CrossReference ref2 = new CrossReferenceImpl("pubmed", "18212739");
        CrossReference ref3 = new CrossReferenceImpl("pubmed", "15199141");
        CrossReference ref4 = new CrossReferenceImpl("pubmed", "15115758");
        publications.add(ref);
        publications.add(ref2);
        publications.add(ref3);
        publications.add(ref4);

        interaction.setId(3);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "9"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);

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

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0019", "MI:0914"), pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx10", "19705531");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
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

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0398", "MI:0914"), pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx11", "19705532");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "19705532");
        publications.add(ref);

        interaction.setId(5);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);

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

        interaction.getMethodTypePairListMap().put(new MethodTypePair("MI:0403", "MI:0403"), pubmeds1);
        Map<String, List<String>> type2Pubmed = new HashMap<String, List<String>>();
        type2Pubmed.put("MI:0403", pubmeds1);
        Map<String, List<String>> method2Pubmed = new HashMap<String, List<String>>();
        method2Pubmed.put("MI:0403", pubmeds1);

        Map<String, String> experimentToPubmed = new HashMap<String, String>();
        experimentToPubmed.put("EBI-xxxxxx12", "19705533");

        List<CrossReference> organismA = new ArrayList<CrossReference>();
        CrossReference orgA = new CrossReferenceImpl("taxid", "6239", "Caenorhabditis elegans");
        organismA.add(orgA);

        List<CrossReference> organismB = new ArrayList<CrossReference>();
        CrossReference orgB = new CrossReferenceImpl("taxid", "9606", "Homo sapiens");
        organismB.add(orgB);

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "19705533");
        publications.add(ref);

        interaction.setId(6);
        interaction.getConfidenceValues().add(new ConfidenceImpl("intactPsiscore", "10"));
        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);
        interaction.setOrganismsA(organismA);
        interaction.setOrganismsB(organismB);
        interaction.setExperimentToPubmed(experimentToPubmed);

        return interaction;
    }

    public List<EncoreInteraction> createEncoreInteractions(){
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        interactions.add(createEncoreInteraction());
        interactions.add(createSecondEncoreInteraction());
        interactions.add(createThirdEncoreInteraction());

        return interactions;
    }

    public List<EncoreInteraction> createPositiveEncoreInteractions(){
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        interactions.add(createEncoreInteraction());
        interactions.add(createThirdEncoreInteraction());

        return interactions;
    }

    public List<EncoreInteraction> createNegativeEncoreInteractions(){
        List<EncoreInteraction> interactions = new ArrayList<EncoreInteraction>();

        interactions.add(createNegativeEncoreInteraction());

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
        context.getGeneNames().put("P12347-4","name-5");

        context.getMiTerms().put("MI:0398", "two hybrid pooling"); // condition export = 2
        context.getMiTerms().put("MI:0915", "physical association");
        context.getMiTerms().put("MI:0914", "association");
        context.getMiTerms().put("MI:0006", "anti bait coimmunoprecipitation");
        context.getMiTerms().put("MI:0019", "coimmunoprecipitation");
        context.getMiTerms().put("MI:0403", "colocalization");
        context.getMiTerms().put("MI:0676","tandem affinity purification");

        context.getSpokeExpandedInteractions().add("EBI-xxxxxx8");
        context.getSpokeExpandedInteractions().add("EBI-xxxxxx11");

        context.getInteractionToMethod_type().put("EBI-xxxxxx1", new MethodTypePair("MI:0676", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx2", new MethodTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx3", new MethodTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx4", new MethodTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx6", new MethodTypePair("MI:0398", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx7", new MethodTypePair("MI:0019", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx8", new MethodTypePair("MI:0006", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx9", new MethodTypePair("MI:0019", "MI:0915"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx10", new MethodTypePair("MI:0019", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx11", new MethodTypePair("MI:0019", "MI:0914"));
        context.getInteractionToMethod_type().put("EBI-xxxxxx12", new MethodTypePair("MI:0403", "MI:0403"));
        
        Set<IntactTransSplicedProteins> isoforms = new HashSet<IntactTransSplicedProteins>();
        isoforms.add(new IntactTransSplicedProteins("EBI-99999999", "P12347-4"));
        context.getTranscriptsWithDifferentMasterAcs().put("P28548", isoforms);

        Set<String> goRefs = new HashSet<String>();
        goRefs.add("GO:00xxxxxxxxx1");
        goRefs.add("GO:00xxxxxxxxx2");
        context.getInteractionComponentXrefs().put("EBI-xxxxxx4", goRefs);

        return context;
    }

    public IntActClusterScore createClusterForExportBasedOnMiScore(){

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(createEncoreInteractionLowScore());
        interactions.add(createEncoreInteractionHighScoreSpokeExpanded());
        interactions.add(createEncoreInteractionHighScoreColocalization());

        return createCluster(interactions);
    }

    public IntActClusterScore createNegativeCluster(){

        List<EncoreInteraction> interactions = createNegativeEncoreInteractions();

        return createCluster(interactions);
    }

    public IntActClusterScore createPositiveCluster(){

        List<EncoreInteraction> interactions = createPositiveEncoreInteractions();

        return createCluster(interactions);
    }

    public IntActClusterScore createClusterForExportBasedOnDetectionMethod(){

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(createEncoreInteractionHighScoreSpokeExpanded());  // two hybrid pooling : export = conditional, 2 and doesn't pass
        interactions.add(createEncoreInteractionHighScoreColocalization()); // colocalization has an export no

        return createCluster(interactions);
    }

    private IntActClusterScore createCluster(List<EncoreInteraction> interactions){
        IntActClusterScore clusterScore = new IntActClusterScore();
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
        IntActClusterScore clsuterScore = createClusterForExportBasedOnMiScore();
        IntActClusterScore negativeClusterScore = createNegativeCluster();
        MiClusterContext context = createClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(new ExportedClusteredInteractions(clsuterScore), new ExportedClusteredInteractions(negativeClusterScore), context);

        return results;
    }

    public MiClusterScoreResults createMiScoreResultsForDetectionMethodExport(){
        IntActClusterScore clusterScore = createClusterForExportBasedOnDetectionMethod();
        IntActClusterScore negativeClusterScore = createNegativeCluster();
        MiClusterContext context = createClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);

        return results;
    }

    public MiClusterScoreResults createMiScoreResultsForSimulation(){
        IntActClusterScore clusterScore = createPositiveCluster();
        IntActClusterScore negativeClusterScore = createNegativeCluster();
        MiClusterContext context = createClusterContext();

        MiClusterScoreResults results = new MiClusterScoreResults(new ExportedClusteredInteractions(clusterScore), new ExportedClusteredInteractions(negativeClusterScore), context);

        return results;
    }

    public void createDatabaseContext(){
        // dr export and confidence annotation topic
        CvTopic dr_export = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT);
        getCorePersister().saveOrUpdate(dr_export);

        CvConfidenceType confidence = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvConfidenceType.class, "MI:1221", "author score");
        getCorePersister().saveOrUpdate(confidence);

        CvTopic accepted = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.ACCEPTED);
        getCorePersister().saveOrUpdate(accepted);

        CvPublicationStatus released = new CvPublicationStatus(IntactContext.getCurrentInstance().getInstitution(), "released");
        CvPublicationStatus readyForRelease = new CvPublicationStatus(IntactContext.getCurrentInstance().getInstitution(), "ready for release");
        getCorePersister().saveOrUpdate(released, readyForRelease);

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

    @Transactional (propagation = Propagation.NEVER)
    public void createExperimentContext() throws IntactClonerException {
        TransactionStatus status = getDataContext().beginTransaction();

        // dr export and confidence annotation topic
        CvTopic dr_export = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.UNIPROT_DR_EXPORT);
        getCorePersister().saveOrUpdate(dr_export);

        CvConfidenceType confidence = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvConfidenceType.class, "MI:1221", "author score");
        getCorePersister().saveOrUpdate(confidence);

        CvTopic accepted = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.ACCEPTED);
        getCorePersister().saveOrUpdate(accepted);

        CvTopic negative = CvObjectUtils.createCvObject(getIntactContext().getInstitution(),
                CvTopic.class, null, CvTopic.NEGATIVE);
        getCorePersister().saveOrUpdate(negative);

        CvPublicationStatus released = new CvPublicationStatus(IntactContext.getCurrentInstance().getInstitution(), "released");
        CvPublicationStatus readyForRelease = new CvPublicationStatus(IntactContext.getCurrentInstance().getInstitution(), "ready for release");
        getCorePersister().saveOrUpdate(released, readyForRelease);

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
        experiment_yes.getPublication().setStatus(released);
        Annotation expAnn1 = new Annotation(dr_export, "yes");
        experiment_yes.addAnnotation(expAnn1);
        experiment_yes.setCvInteraction(method2);

        Experiment experiment_no = getMockBuilder().createExperimentRandom(1);
        experiment_no.getPublication().setStatus(released);
        Annotation expAnn2 = new Annotation(dr_export, "no");
        experiment_no.addAnnotation(expAnn2);

        Experiment experiment_condition = getMockBuilder().createExperimentRandom(2);
        experiment_condition.getPublication().setStatus(readyForRelease);
        Annotation expAnn3 = new Annotation(dr_export, "high");
        experiment_condition.addAnnotation(expAnn3);

        Experiment experiment_negative = getMockBuilder().createExperimentRandom(1);
        experiment_negative.getPublication().setStatus(released);
        Annotation expAnn4 = new Annotation(negative, null);
        experiment_negative.getInteractions().iterator().next().addAnnotation(expAnn4);
        Annotation expAnn5 = new Annotation(dr_export, "yes");
        experiment_negative.addAnnotation(expAnn5);

        getCorePersister().saveOrUpdate(experiment_yes, experiment_condition, experiment_no, experiment_negative);

        for (Interaction interaction : experiment_yes.getInteractions()){
            if (interaction.getAnnotations().isEmpty()){
                interaction1 = interaction.getAc();
            }
            else {
                interaction6 = interaction.getAc();
            }
        }

        interaction2 = experiment_no.getInteractions().iterator().next().getAc();
        int index = 3;

        List<Interaction> interactions = new ArrayList(experiment_condition.getInteractions());
        for (Interaction interaction : interactions){
            if (index == 3){
                Confidence confidence1 = new Confidence(confidence, "high");
                interaction.addConfidence(confidence1);
                interaction3 = interaction.getAc();
            }
            else {
                Confidence confidence2 = new Confidence(confidence, "low");
                interaction.addConfidence(confidence2);
                interaction4 = interaction.getAc();
            }
            getCorePersister().saveOrUpdate(interaction);
            index++;
        }

        interaction5 = experiment_negative.getInteractions().iterator().next().getAc();

        getDataContext().commitTransaction(status);

        Assert.assertEquals(2, getDaoFactory().getConfidenceDao().getAll().size());

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
