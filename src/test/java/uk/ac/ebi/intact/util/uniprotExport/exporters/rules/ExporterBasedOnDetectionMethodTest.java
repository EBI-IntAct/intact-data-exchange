package uk.ac.ebi.intact.util.uniprotExport.exporters.rules;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;

import java.util.Set;

/**
 * Tester of the Exporter based on detection method
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/02/11</pre>
 */
public class ExporterBasedOnDetectionMethodTest extends UniprotExportBase {

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void test_simulation() throws UniprotExportException {
        createDatabaseContext();

        Assert.assertEquals(5, getDaoFactory().getCvObjectDao(CvInteraction.class).getAll().size());

        ExporterBasedOnDetectionMethod exporter = new ExporterBasedOnDetectionMethod();

        MiClusterScoreResults results = createMiScoreResultsForDetectionMethodExport();

        exporter.exportInteractionsFrom(results);

        Set<Integer> interactionsExported = results.getPositiveClusteredInteractions().getInteractionsToExport();
        Set<Integer> negativeInteractionsExported = results.getNegativeClusteredInteractions().getInteractionsToExport();

        Assert.assertNotNull(interactionsExported);
        Assert.assertNotNull(negativeInteractionsExported);
        Assert.assertEquals(3, interactionsExported.size());
        Assert.assertEquals(0, negativeInteractionsExported.size());

        boolean isValid = true;

        for (Integer interactionId : interactionsExported){
             if ( interactionId == 5 || interactionId == 6){
                  isValid = false;
                 break;
             }
        }

        Assert.assertTrue(isValid);
    }
}
