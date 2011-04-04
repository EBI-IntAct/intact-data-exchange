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

        SecondCCParameters2 firstSecondPar = parameters.getSecondCCParameters().get(0);
        Assert.assertEquals("P28548-1", firstSecondPar.getFirstUniprotAc());
        Assert.assertEquals("Q22534", firstSecondPar.getSecondUniprotAc());
        Assert.assertTrue(firstSecondPar.doesInteract());
        Assert.assertEquals("EBI-317777", firstSecondPar.getFirstIntacAc());
        Assert.assertEquals("EBI-327642", firstSecondPar.getSecondIntactAc());
        Assert.assertEquals(1, firstSecondPar.getInteractionDetails().size());
        Assert.assertEquals("9606", firstSecondPar.getTaxId());
        Assert.assertEquals("Homo sapiens", firstSecondPar.getOrganismName());
        Assert.assertEquals("pat-12", firstSecondPar.getGeneName());

        SecondCCParameters2 secondSecondPar = parameters.getSecondCCParameters().get(1);
        Assert.assertEquals("P28548-2", secondSecondPar.getFirstUniprotAc());
        Assert.assertEquals("O17670", secondSecondPar.getSecondUniprotAc());
        Assert.assertTrue(secondSecondPar.doesInteract());
        Assert.assertEquals("EBI-317778", secondSecondPar.getFirstIntacAc());
        Assert.assertEquals("EBI-311862", secondSecondPar.getSecondIntactAc());
        Assert.assertEquals(1, secondSecondPar.getInteractionDetails().size());
        Assert.assertEquals("6239", secondSecondPar.getTaxId());
        Assert.assertEquals("Caenorhabditis elegans", secondSecondPar.getOrganismName());
        Assert.assertEquals("eya-1", secondSecondPar.getGeneName());

        SecondCCParameters2 thirdSecondPar = parameters.getSecondCCParameters().get(2);
        Assert.assertEquals("P28548-PRO_0000068244", thirdSecondPar.getFirstUniprotAc());
        Assert.assertEquals("Q21361", thirdSecondPar.getSecondUniprotAc());
        Assert.assertFalse(thirdSecondPar.doesInteract());
        Assert.assertEquals("EBI-317779", thirdSecondPar.getFirstIntacAc());
        Assert.assertEquals("EBI-311862", thirdSecondPar.getSecondIntactAc());
        Assert.assertEquals(3, thirdSecondPar.getInteractionDetails().size());
        Assert.assertEquals("9606", thirdSecondPar.getTaxId());
        Assert.assertEquals("Homo sapiens", thirdSecondPar.getOrganismName());
        Assert.assertEquals("atf-2", thirdSecondPar.getGeneName());
    }
}
