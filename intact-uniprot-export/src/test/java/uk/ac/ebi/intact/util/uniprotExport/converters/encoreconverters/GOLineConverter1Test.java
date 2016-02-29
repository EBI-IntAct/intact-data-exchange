package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.HashSet;
import java.util.Iterator;
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

        EncoreInteraction interaction = createEncoreInteraction();

        List<GOParameters1> parameters1 = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertEquals(1, parameters1.size());

        GOParameters1 parameters = parameters1.iterator().next();
        Assert.assertEquals("P28548-1", parameters.getFirstProtein());
        Assert.assertEquals("Q22534", parameters.getSecondProtein());
        Assert.assertEquals(1, parameters.getPubmedIds().size());
        Assert.assertEquals("14704431", parameters.getPubmedIds().iterator().next());
    }

    @Test
    public void test_go_convert_no_first_interactor(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getInteractorAccsA().clear();

        List<GOParameters1> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_no_second_interactor(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getInteractorAccsB().clear();

        List<GOParameters1> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());

    }

    @Test
    public void test_go_convert_no_publications(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getPublicationIds().clear();

        List<GOParameters1> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_one_publication_no_pubmed(){
        GoLineConverter1 converter = new GoLineConverter1();

        EncoreInteraction interaction = createEncoreInteraction();
        interaction.getPublicationIds().iterator().next().setDatabase("DOI");

        List<GOParameters1> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_simulation(){
        GoLineConverter1 converter = new GoLineConverter1();

        List<EncoreInteraction> interactions = createEncoreInteractions();
        interactions.add(3, createIsoformIsoformInteraction()); // can be converted because isoform-isoform
        interactions.add(4, createEncoreInteractionWithTransIsoform()); // can be converted
        interactions.add(5, createEncoreInteractionWithTransIsoformAndMaster()); // can be converted because isoform and other uniprot entry, even if this isoform is not matching the master

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        List<GOParameters1> parameters = converter.convertInteractionsIntoGOParameters(new HashSet<EncoreInteraction>(interactions), firstInteractor, context);
        Assert.assertNotNull(parameters);
        Assert.assertEquals(6, parameters.size());

        for (GOParameters1 par : parameters){

            // isoform isoform first
            if (("P28548-1".equalsIgnoreCase(par.getFirstProtein()) && "P28548-2".equalsIgnoreCase(par.getSecondProtein())) || ("P28548-2".equalsIgnoreCase(par.getFirstProtein()) && "P28548-1".equalsIgnoreCase(par.getSecondProtein())) ){

                Assert.assertEquals(2, par.getPubmedIds().size());
                Iterator<String> pubIterator = par.getPubmedIds().iterator();
                Assert.assertEquals("14704431", pubIterator.next());
                Assert.assertEquals("15199141", pubIterator.next());
            }
            // feature chain
            else if ("P28548".equals(par.getFirstProtein()) && "Q21361".equals(par.getSecondProtein())){
                Assert.assertEquals(4, par.getPubmedIds().size());
                Iterator<String> pubIterator = par.getPubmedIds().iterator();
                Assert.assertEquals("15199141", pubIterator.next());
                Assert.assertEquals("15115758", pubIterator.next());
                Assert.assertEquals("18212739", pubIterator.next());
                Assert.assertEquals("14704431", pubIterator.next());
            }
            // master protein and trans variant
            else if ("P28548".equals(par.getFirstProtein()) && "P12347-4".equals(par.getSecondProtein())){
                Assert.assertEquals(2, par.getPubmedIds().size());
                Iterator<String> pubIterator = par.getPubmedIds().iterator();
                Assert.assertEquals("14704431", pubIterator.next());
                Assert.assertEquals("15199141", pubIterator.next());
            }
            // first interaction with isoform
            else if ("P28548-1".equals(par.getFirstProtein())){
                Assert.assertEquals("Q22534", par.getSecondProtein());
                Assert.assertEquals(1, par.getPubmedIds().size());
                Assert.assertEquals("14704431", par.getPubmedIds().iterator().next());
            }
            // second interaction with isoform
            else if ("P28548-2".equals(par.getFirstProtein())){
                Assert.assertEquals("O17670", par.getSecondProtein());
                Assert.assertEquals(2, par.getPubmedIds().size());
                Iterator<String> pubIterator = par.getPubmedIds().iterator();
                Assert.assertEquals("14704431", pubIterator.next());
                Assert.assertEquals("15199141", pubIterator.next());
            }
            else {
                Assert.assertFalse(true);
            }
        }
    }
}
