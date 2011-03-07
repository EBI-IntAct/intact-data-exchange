package uk.ac.ebi.intact.util.uniprotExport;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @Ignore
    public void test_simulation() throws UniprotExportException, IOException {
        createExperimentContext();

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().getAll().size());
        Assert.assertEquals(3, getDaoFactory().getExperimentDao().getAll().size());

        IntactFilter filter = new IntactFilter(new ExporterBasedOnDetectionMethod());

        UniprotExportProcessor processor = new UniprotExportProcessor(filter);
        MiClusterScoreResults results = createMiScoreResultsForDetectionMethodExport();
        results.getPositiveClusteredInteractions().getInteractionsToExport().add(1);
        results.getPositiveClusteredInteractions().getInteractionsToExport().add(2);
        results.getPositiveClusteredInteractions().getInteractionsToExport().add(3);
        processor.exportDRAndCCLines(results, "drFile", "ccFile", 1);

        results.getPositiveClusteredInteractions().getCluster().saveClusteredInteractions("interactions", results.getPositiveClusteredInteractions().getInteractionsToExport());

    }
}
