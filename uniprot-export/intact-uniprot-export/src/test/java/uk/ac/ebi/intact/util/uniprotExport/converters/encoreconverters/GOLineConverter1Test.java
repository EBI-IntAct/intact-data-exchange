package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.HashSet;
import java.util.List;

/**
 * Tester of the GO line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class GOLineConverter1Test extends UniprotExportBase{

    @Test
    public void test_go_convert_ok(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteractionForScoring interaction = createEncoreInteraction();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548-1", parameters.getFirstProtein());
        Assert.assertEquals("Q22534", parameters.getSecondProtein());
        Assert.assertEquals(1, parameters.getPubmedIds().size());
        Assert.assertEquals("14704431", parameters.getPubmedIds().iterator().next());
    }

    @Test
    public void test_go_convert_no_first_interactor(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getInteractorAccsA().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertNull(parameters);
    }

    @Test
    public void test_go_convert_no_second_interactor(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getInteractorAccsB().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertNull(parameters);

    }

    @Test
    public void test_go_convert_no_publications(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getPublicationIds().clear();

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertNull(parameters);
    }

    @Test
    public void test_go_convert_one_publication_no_pubmed(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getPublicationIds().iterator().next().setDatabase("DOI");

        GOParameters parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertNull(parameters);
    }

    @Test
    public void test_go_convert_simulation(){
        GoLineConverter1 converter = new GoLineConverter1();

        List<EncoreInteractionForScoring> interactions = createEncoreInteractions();
        interactions.add(3, createIsoformIsoformInteraction()); // can be converted because isoform-isoform
        interactions.add(4, createEncoreInteractionWithTransIsoform()); // can be converted
        interactions.add(5, createEncoreInteractionWithTransIsoformAndMaster()); // can be converted because isoform and other uniprot entry, even if this isoform is not matching the master

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        List<GOParameters1> parameters = converter.convertInteractionsIntoGOParameters(new HashSet<EncoreInteractionForScoring>(interactions), firstInteractor, context);
        Assert.assertNotNull(parameters);
        //Assert.assertEquals(12, parameters.size());
    }
}
