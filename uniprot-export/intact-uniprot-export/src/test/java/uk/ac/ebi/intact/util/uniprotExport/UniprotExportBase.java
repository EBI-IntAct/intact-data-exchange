package uk.ac.ebi.intact.util.uniprotExport;

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class UniprotExportBase extends IntactBasicTestCase {

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

    public List<CCParameters> createCCParameters(){

        List<CCParameters> parameters = new ArrayList<CCParameters>(3);

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

        String interactionType1 = "physical association";
        String interactionType2 = "association";
        String method1 = "two hybrid pooling";
        String method2 = "anti bait coimmunoprecipitation";
        String method3 = "coimmunoprecipitation";

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

        InteractionDetails detail1 = new InteractionDetailsImpl("physical association", "two hybrid pooling", false, publications1);
        InteractionDetails detail2 = new InteractionDetailsImpl("physical association", "two hybrid pooling", false, publications2);
        InteractionDetails detail3 = new InteractionDetailsImpl("association", "anti bait coimmunoprecipitation", true, publications3);
        InteractionDetails detail4 = new InteractionDetailsImpl("physical association", "coimmunoprecipitation", false, publications4);

        SortedSet<InteractionDetails> details1 = new TreeSet<InteractionDetails>();
        details1.add(detail1);

        SortedSet<InteractionDetails> details2 = new TreeSet<InteractionDetails>();
        details2.add(detail2);

        SortedSet<InteractionDetails> details3 = new TreeSet<InteractionDetails>();
        details3.add(detail1);
        details3.add(detail3);
        details3.add(detail4);

        SecondCCInteractor secondParameters1 = new SecondCCInteractorImpl(uniprotAc5, uniprotAc2, intactAc4, intactAc1, geneName2, taxId2, organismName2, details1);
        SecondCCInteractor secondParameters2 = new SecondCCInteractorImpl(uniprotAc6, uniprotAc3, intactAc5, intactAc2, geneName3, taxId, organismName, details2);
        SecondCCInteractor secondParameters3 = new SecondCCInteractorImpl(uniprotAc7, uniprotAc4, intactAc6, intactAc3, geneName4, taxId, organismName, details3);

        List<SecondCCInteractor> listOfSecondInteractors1 = new ArrayList<SecondCCInteractor>();
        listOfSecondInteractors1.add(secondParameters1);
        listOfSecondInteractors1.add(secondParameters2);
        listOfSecondInteractors1.add(secondParameters3);

        CCParameters parameters1 = new CCParametersImpl(uniprotAc1, geneName1, taxId, listOfSecondInteractors1);
        parameters.add(parameters1);

        List<SecondCCInteractor> listOfSecondInteractors2 = new ArrayList<SecondCCInteractor>();
        SecondCCInteractor secondParameters4 = new SecondCCInteractorImpl(uniprotAc2, uniprotAc5, intactAc1, intactAc4, geneName1, taxId, organismName, details1);
        listOfSecondInteractors2.add(secondParameters4);

        CCParameters parameters2 = new CCParametersImpl(uniprotAc2, geneName2, taxId2, listOfSecondInteractors2);
        parameters.add(parameters2);

        List<SecondCCInteractor> listOfSecondInteractors3 = new ArrayList<SecondCCInteractor>();
        SecondCCInteractor secondParameters5 = new SecondCCInteractorImpl(uniprotAc3, uniprotAc6, intactAc2, intactAc5, geneName1, taxId, organismName, details2);
        listOfSecondInteractors3.add(secondParameters5);

        CCParameters parameters3 = new CCParametersImpl(uniprotAc3, geneName3, taxId, listOfSecondInteractors3);
        parameters.add(parameters3);

        List<SecondCCInteractor> listOfSecondInteractors4 = new ArrayList<SecondCCInteractor>();
        SecondCCInteractor secondParameters6 = new SecondCCInteractorImpl(uniprotAc4, uniprotAc7, intactAc3, intactAc6, geneName1, taxId, organismName, details3);
        listOfSecondInteractors4.add(secondParameters6);

        CCParameters parameters4 = new CCParametersImpl(uniprotAc4, geneName4, taxId, listOfSecondInteractors4);
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

        List<CrossReference> publications = new ArrayList<CrossReference>(1);
        CrossReference ref = new CrossReferenceImpl("pubmed", "14704431");
        publications.add(ref);

        interaction.setInteractorAccsA(interactorA);
        interaction.setInteractorAccsB(interactorB);
        interaction.setPublicationIds(publications);

        return interaction;
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
