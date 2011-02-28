package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;

/**
 * Tester of the GO line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class EncoreInteractionToGOLineConverterTest extends UniprotExportBase{

    @Test
    public void test_go_convert_ok(){
        EncoreInteractionToGoLineConverter converter = new EncoreInteractionToGoLineConverter();

        EncoreInteraction interaction = createEncoreInteraction();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1");
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548-1", parameters.getFirstProtein());
        Assert.assertEquals("Q22534", parameters.getSecondProtein());
        Assert.assertEquals(1, parameters.getPubmedIds().size());
        Assert.assertEquals("14704431", parameters.getPubmedIds().iterator().next());
    }

    @Test
    public void test_go_convert_no_first_interactor(){
        EncoreInteractionToGoLineConverter converter = new EncoreInteractionToGoLineConverter();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getInteractorAccsA().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1");
        Assert.assertNull(parameters);
    }

    @Test
    public void test_go_convert_no_second_interactor(){
        EncoreInteractionToGoLineConverter converter = new EncoreInteractionToGoLineConverter();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getInteractorAccsB().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1");
        Assert.assertNull(parameters);

    }

    @Test
    public void test_go_convert_no_publications(){
        EncoreInteractionToGoLineConverter converter = new EncoreInteractionToGoLineConverter();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getPublicationIds().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1");
        Assert.assertNull(parameters);
    }

    @Test
    public void test_go_convert_one_publication_no_pubmed(){
        EncoreInteractionToGoLineConverter converter = new EncoreInteractionToGoLineConverter();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getPublicationIds().iterator().next().setDatabase("DOI");

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1");
        Assert.assertNull(parameters);
    }
}
