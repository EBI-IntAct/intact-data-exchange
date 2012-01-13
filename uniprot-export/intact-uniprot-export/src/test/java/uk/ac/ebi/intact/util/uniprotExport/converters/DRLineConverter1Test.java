package uk.ac.ebi.intact.util.uniprotExport.converters;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tester of the DR line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/02/11</pre>
 */

public class DRLineConverter1Test extends UniprotExportBase{

    @Test
    public void test_dr_convert_ok(){
        DRLineConverter1 converter = new DRLineConverter1();

        Set<EncoreInteractionForScoring> interactions = new HashSet<EncoreInteractionForScoring>(createEncoreInteractions()); // three interactions
        interactions.add(createIsoformIsoformInteraction()); // does not count because isoform-isoform of same uniprot entry
        interactions.add(createEncoreInteractionWithTransIsoform()); // not counted because same interactor already interacts with isoform 2
        interactions.add(createEncoreInteractionWithTransIsoformAndMaster()); // cannot be converted because interaction with isoform of same entry

        DRParameters parameters = converter.convertInteractorIntoDRLine("P28548", interactions, createClusterContext());
        Assert.assertNotNull(parameters);
        Assert.assertEquals("P28548", parameters.getUniprotAc());
        Assert.assertEquals(3, parameters.getNumberOfInteractions());
    }

    @Test
    public void test_dr_convert_null(){
        DRLineConverter1 converter = new DRLineConverter1();

        DRParameters parameters = converter.convertInteractorIntoDRLine(null, Collections.EMPTY_SET, createClusterContext());
        Assert.assertNull(parameters);
    }
}
