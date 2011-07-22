package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters2;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Tester of the CC line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class EncoreInteractionToCCLineConverterTest extends UniprotExportBase{

    @Test
    public void test_cc_convert_ok(){
        EncoreInteractionToCCLine2Converter converter = new EncoreInteractionToCCLine2Converter();

        List<EncoreInteractionForScoring> interactions = createEncoreInteractions();

        List<EncoreInteractionForScoring> negativeInteractions = new ArrayList<EncoreInteractionForScoring>();
        negativeInteractions.add(interactions.get(2));
        interactions.remove(2);

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        /*MethodAndTypePair mp1 = new MethodAndTypePair("MI:0398", "MI:0915");
        List<String> pubmeds = new ArrayList<String>();
        pubmeds.add("123456789");

        Map<MethodAndTypePair, List<String>> map = new HashMap<MethodAndTypePair, List<String>> ();
        map.put(mp1, pubmeds);

        MethodAndTypePair mp2 = new MethodAndTypePair("MI:0398", "MI:0915");
        boolean contain = map.containsKey(mp2);

        List<String> ps = map.get(mp2);*/

        CCParameters<SecondCCParameters2> parameters = converter.convertPositiveAndNegativeInteractionsIntoCCLines(interactions, negativeInteractions, context, firstInteractor);
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548", parameters.getMasterUniprotAc());
        Assert.assertEquals("Kin-10", parameters.getGeneName());
        Assert.assertEquals("6239", parameters.getTaxId());
        Assert.assertEquals(3, parameters.getSecondCCParameters().size());

        int i = 0;

        for (SecondCCParameters2 secondPar : parameters.getSecondCCParameters()){
            i++;

            if (i == 2){
                Assert.assertEquals("P28548-1", secondPar.getFirstUniprotAc());
                Assert.assertEquals("Q22534", secondPar.getSecondUniprotAc());
                Assert.assertTrue(secondPar.doesInteract());
                Assert.assertEquals("EBI-317777", secondPar.getFirstIntacAc());
                Assert.assertEquals("EBI-327642", secondPar.getSecondIntactAc());
                Assert.assertEquals(1, secondPar.getInteractionDetails().size());
                Assert.assertEquals("9606", secondPar.getTaxId());
                Assert.assertEquals("Homo sapiens", secondPar.getOrganismName());
                Assert.assertEquals("pat-12", secondPar.getGeneName());
            }

            else if (i == 1){
                Assert.assertEquals("P28548-2", secondPar.getFirstUniprotAc());
                Assert.assertEquals("O17670", secondPar.getSecondUniprotAc());
                Assert.assertTrue(secondPar.doesInteract());
                Assert.assertEquals("EBI-317778", secondPar.getFirstIntacAc());
                Assert.assertEquals("EBI-311862", secondPar.getSecondIntactAc());
                Assert.assertEquals(1, secondPar.getInteractionDetails().size());
                Assert.assertEquals("6239", secondPar.getTaxId());
                Assert.assertEquals("Caenorhabditis elegans", secondPar.getOrganismName());
                Assert.assertEquals("eya-1", secondPar.getGeneName());
            }
            else if (i == 3){
                Assert.assertEquals("P28548-PRO_0000068244", secondPar.getFirstUniprotAc());
                Assert.assertEquals("Q21361", secondPar.getSecondUniprotAc());
                Assert.assertFalse(secondPar.doesInteract());
                Assert.assertEquals("EBI-317779", secondPar.getFirstIntacAc());
                Assert.assertEquals("EBI-311862", secondPar.getSecondIntactAc());
                Assert.assertEquals(3, secondPar.getInteractionDetails().size());
                Assert.assertEquals("9606", secondPar.getTaxId());
                Assert.assertEquals("Homo sapiens", secondPar.getOrganismName());
                Assert.assertEquals("atf-2", secondPar.getGeneName());
            }
        }
    }
}
