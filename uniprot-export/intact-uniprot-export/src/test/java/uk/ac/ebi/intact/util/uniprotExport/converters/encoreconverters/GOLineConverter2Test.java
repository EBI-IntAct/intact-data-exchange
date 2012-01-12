package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters2;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Unit tester for GO line converter 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/01/12</pre>
 */

public class GOLineConverter2Test extends UniprotExportBase {

    @Test
    public void test_go_convert_ok(){
        GoLineConverter2 converter = new GoLineConverter2();

        EncoreInteractionForScoring interaction = createEncoreInteraction();

        List<GOParameters2> parameters1 = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertEquals(1, parameters1.size());

        GOParameters2 parameters = parameters1.iterator().next();
        Assert.assertEquals("P28548-1", parameters.getFirstProtein());
        Assert.assertEquals("Q22534", parameters.getSecondProtein());
        Assert.assertEquals("14704431", parameters.getPubmedId());
    }

    @Test
    public void test_go_convert_no_first_interactor(){
        GoLineConverter2 converter = new GoLineConverter2();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getInteractorAccsA().clear();

        List<GOParameters2> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_no_second_interactor(){
        GoLineConverter2 converter = new GoLineConverter2();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getInteractorAccsB().clear();

        List<GOParameters2> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());

    }

    @Test
    public void test_go_convert_no_publications(){
        GoLineConverter2 converter = new GoLineConverter2();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getPublicationIds().clear();

        List<GOParameters2> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_one_publication_no_pubmed(){
        GoLineConverter2 converter = new GoLineConverter2();

        EncoreInteractionForScoring interaction = createEncoreInteraction();
        interaction.getPublicationIds().iterator().next().setDatabase("DOI");

        List<GOParameters2> parameters = converter.convertInteractionIntoGOParameters(interaction, "P28548-1", createClusterContext());
        Assert.assertTrue(parameters.isEmpty());
    }

    @Test
    public void test_go_convert_simulation(){
        GoLineConverter2 converter = new GoLineConverter2();

        List<EncoreInteractionForScoring> interactions = createEncoreInteractions();
        interactions.add(3, createIsoformIsoformInteraction()); // can be converted because isoform-isoform
        interactions.add(4, createEncoreInteractionWithTransIsoform()); // can be converted
        interactions.add(5, createEncoreInteractionWithTransIsoformAndMaster()); // can be converted because isoform and other uniprot entry, even if this isoform is not matching the master

        MiClusterContext context = createClusterContext();

        String firstInteractor = "P28548";

        List<GOParameters2> parameters = converter.convertInteractionsIntoGOParameters(new HashSet<EncoreInteractionForScoring>(interactions), firstInteractor, context);
        Assert.assertNotNull(parameters);
        Assert.assertEquals(17, parameters.size());

        for (GOParameters2 par : parameters){

            // isoform isoform first
            if (("P28548-1".equalsIgnoreCase(par.getFirstProtein()) && "P28548-2".equalsIgnoreCase(par.getSecondProtein())) || ("P28548-2".equalsIgnoreCase(par.getFirstProtein()) && "P28548-1".equalsIgnoreCase(par.getSecondProtein())) ){

                Assert.assertEquals("P28548", par.getMasterProtein());
                if (!"14704431".equals(par.getPubmedId()) && !"15199141".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }
            }
            // feature chain
            else if ("P28548-PRO_0000068244".equals(par.getFirstProtein())){

                Assert.assertEquals("Q21361", par.getSecondProtein());
                Assert.assertEquals("P28548", par.getMasterProtein());
                if (!"15199141".equals(par.getPubmedId()) && !"18212739".equals(par.getPubmedId()) && !"15115758".equals(par.getPubmedId()) && !"14704431".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }

                Assert.assertEquals(2, par.getComponentXrefs().size());
                Iterator<String> compIterator = par.getComponentXrefs().iterator();
                Assert.assertEquals("GO:00xxxxxxxxx1", compIterator.next());
                Assert.assertEquals("GO:00xxxxxxxxx2", compIterator.next());
            }
            // master protein and trans variant
            else if ("P28548".equals(par.getFirstProtein())){

                Assert.assertEquals("P28548", par.getMasterProtein());
                Assert.assertEquals("P12347-4", par.getSecondProtein());
                if (!"15199141".equals(par.getPubmedId()) && !"14704431".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }
            }
            // trans variant and master protein
            else if ("P12347-4".equals(par.getFirstProtein()) && "P28548".equals(par.getSecondProtein())){

                Assert.assertEquals("P28548", par.getMasterProtein());
                if (!"15199141".equals(par.getPubmedId()) && !"14704431".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }
            }
            // trans variant and another uniprot entry
            else if ("P12347-4".equals(par.getFirstProtein()) && "O17670".equals(par.getSecondProtein())){

                Assert.assertEquals("P28548", par.getMasterProtein());
                if (!"15199141".equals(par.getPubmedId()) && !"14704431".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }
            }
            // first interaction with isoform
            else if ("P28548-1".equals(par.getFirstProtein())){

                Assert.assertEquals("P28548", par.getMasterProtein());
                Assert.assertEquals("Q22534", par.getSecondProtein());
                Assert.assertEquals("14704431", par.getPubmedId());
            }
            // second interaction with isoform
            else if ("P28548-2".equals(par.getFirstProtein())){

                Assert.assertEquals("P28548", par.getMasterProtein());
                Assert.assertEquals("O17670", par.getSecondProtein());
                if (!"15199141".equals(par.getPubmedId()) && !"14704431".equals(par.getPubmedId())){
                    Assert.assertTrue(false);
                }
            }
            else {
                Assert.assertFalse(true);
            }
        }
    }
}
