package uk.ac.ebi.intact.util.uniprotExport;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters.EncoreInteractionToCCLine2Converter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.filters.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;

import java.io.IOException;

/**
 * Tester of the uniprot export processor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/02/11</pre>
 */

public class UniprotExportProcessorTest extends UniprotExportBase{

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void test_simulation() throws UniprotExportException, IOException, IntactClonerException {
        createExperimentContext();

        Assert.assertEquals(6, getDaoFactory().getInteractionDao().getAll().size());
        Assert.assertEquals(4, getDaoFactory().getExperimentDao().getAll().size());

        IntactFilter filter = new IntactFilter(new ExporterBasedOnDetectionMethod());

        UniprotExportProcessor processor = new UniprotExportProcessor(filter);
        processor.setCcConverter(new EncoreInteractionToCCLine2Converter());

        MiClusterScoreResults results = createMiScoreResultsForSimulation();
        results.getPositiveClusteredInteractions().getInteractionsToExport().add(1);
        //results.getPositiveClusteredInteractions().getInteractionsToExport().add(3);
        //results.getNegativeClusteredInteractions().getInteractionsToExport().add(2);

        processor.exportDRAndCCLines(results, "drFile", "ccFile", "ccFileSilver");

        //processor.runUniprotExport("drFile", "ccFile", "goFile");

        //results.getPositiveClusteredInteractions().getCluster().saveClusteredInteractions("interactions", results.getPositiveClusteredInteractions().getInteractionsToExport());

    }
}
