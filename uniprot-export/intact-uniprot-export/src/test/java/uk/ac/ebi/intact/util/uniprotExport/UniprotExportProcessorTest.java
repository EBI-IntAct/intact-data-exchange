package uk.ac.ebi.intact.util.uniprotExport;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.filters.IntactFilter;

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
    public void test_simulation() throws UniprotExportException {
        createExperimentContext();

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().getAll().size());
        Assert.assertEquals(3, getDaoFactory().getExperimentDao().getAll().size());

        IntactFilter filter = new IntactFilter(new ExporterBasedOnDetectionMethod());

        UniprotExportProcessor processor = new UniprotExportProcessor(filter);

        processor.runUniprotExport("test1", "test2", "test3", 1);

    }
}
