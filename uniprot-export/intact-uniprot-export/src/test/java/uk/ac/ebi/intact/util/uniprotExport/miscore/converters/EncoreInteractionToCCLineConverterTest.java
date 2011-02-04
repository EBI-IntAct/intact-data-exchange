package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.parameters.CCParameters;

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
        EncoreInteractionToCCLineConverter converter = new EncoreInteractionToCCLineConverter();

        List<EncoreInteraction> interactions = createEncoreInteractions();

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";


        CCParameters parameters = converter.convertInteractionsIntoCCLines(interactions, context, firstInteractor);
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548", parameters.getFirstInteractor());
        Assert.assertEquals("Kin-10", parameters.getFirstGeneName());
        Assert.assertEquals("6239", parameters.getFirstTaxId());
        Assert.assertEquals(3, parameters.getSecondCCInteractors().size());

    }
}
