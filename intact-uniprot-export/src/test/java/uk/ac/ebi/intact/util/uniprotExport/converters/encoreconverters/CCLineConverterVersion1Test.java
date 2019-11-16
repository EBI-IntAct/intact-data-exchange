package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Unit tester for CC line converter 1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/01/12</pre>
 */

public class CCLineConverterVersion1Test extends UniprotExportBase {

    @Test
    public void testConvertOk(){
        CCLineConverterVersion1 converter = new CCLineConverterVersion1();

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(3, createIsoformIsoformInteraction()); // with rules of 2019 an isoform-isoform can be converted
        interactions.add(4, createEncoreInteractionWithTransIsoform()); // can be converted but is written twice because another isoform of the entry already interacts with same protein
        interactions.add(5, createEncoreInteractionWithTransIsoformAndMaster()); // can be converted because isoform and other uniprot entry, even if this isoform is not matching the master

        List<EncoreInteraction> negativeInteractions = new ArrayList<EncoreInteraction>();
        negativeInteractions.add(interactions.get(5));
        interactions.remove(5);

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        CCParameters<SecondCCParametersVersion1> parameters = converter.convertPositiveAndNegativeInteractionsIntoCCLines(
                new HashSet<EncoreInteraction>(interactions),
                new HashSet<EncoreInteraction>(negativeInteractions),
                context,
                firstInteractor);
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548", parameters.getMasterUniprotAc());
        Assert.assertEquals("Kin-10", parameters.getGeneName());
        Assert.assertEquals("6239", parameters.getTaxId());
        Assert.assertEquals(5, parameters.getSecondCCParameters().size());

        int i = 0;

        for (SecondCCParametersVersion1 secondPar : parameters.getSecondCCParameters()){

            if (i == 0){
                Assert.assertEquals("P28548-PRO_0000068244", secondPar.getFirstUniprotAc());
                Assert.assertEquals("Q21361", secondPar.getSecondUniprotAc());
                Assert.assertEquals("EBI-317779", secondPar.getFirstIntactAc());
                Assert.assertEquals("EBI-317743", secondPar.getSecondIntactAc());
                Assert.assertEquals("atf-2", secondPar.getGeneName());
                Assert.assertEquals(5, secondPar.getNumberOfInteractionEvidences());
                Assert.assertEquals("9606", secondPar.getTaxId());
            }
            else if (i == 1){
                Assert.assertEquals("P12347-4", secondPar.getFirstUniprotAc());
                Assert.assertEquals("O17670", secondPar.getSecondUniprotAc());
                Assert.assertEquals("EBI-99999999", secondPar.getFirstIntactAc());
                Assert.assertEquals("EBI-311862", secondPar.getSecondIntactAc());
                Assert.assertEquals("eya-1", secondPar.getGeneName());
                Assert.assertEquals(2, secondPar.getNumberOfInteractionEvidences());
                Assert.assertEquals("6239", secondPar.getTaxId());
            }
            else if (i == 2){
                Assert.assertEquals("P28548-2", secondPar.getFirstUniprotAc());
                Assert.assertEquals("O17670", secondPar.getSecondUniprotAc());
                Assert.assertEquals("EBI-317778", secondPar.getFirstIntactAc());
                Assert.assertEquals("EBI-311862", secondPar.getSecondIntactAc());
                Assert.assertEquals("eya-1", secondPar.getGeneName());
                Assert.assertEquals(2, secondPar.getNumberOfInteractionEvidences());
                Assert.assertEquals("6239", secondPar.getTaxId());
            }
            else if (i == 3){
                Assert.assertEquals("P28548-2", secondPar.getFirstUniprotAc());
                Assert.assertEquals("P28548-1", secondPar.getSecondUniprotAc());
                Assert.assertEquals("EBI-317778", secondPar.getFirstIntactAc());
                Assert.assertEquals("EBI-317777", secondPar.getSecondIntactAc());
                Assert.assertEquals("Kin-10", secondPar.getGeneName());
                Assert.assertEquals(2, secondPar.getNumberOfInteractionEvidences());
                Assert.assertEquals("6239", secondPar.getTaxId());
            }
            else if (i == 4){
                Assert.assertEquals("P28548-1", secondPar.getFirstUniprotAc());
                Assert.assertEquals("Q22534", secondPar.getSecondUniprotAc());
                Assert.assertEquals("EBI-317777", secondPar.getFirstIntactAc());
                Assert.assertEquals("EBI-327642", secondPar.getSecondIntactAc());
                Assert.assertEquals("pat-12", secondPar.getGeneName());
                Assert.assertEquals(1, secondPar.getNumberOfInteractionEvidences());
                Assert.assertEquals("9606", secondPar.getTaxId());
            }
            i++;
        }
    }
}
