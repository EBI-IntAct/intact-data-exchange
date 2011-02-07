package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;

import java.util.Set;

/**
 * Tester of the exporter based on MiScore
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/02/11</pre>
 */

public class ExporterBasedOnClusterScoreTest extends UniprotExportBase{

    @Test
    public void test_simulation() throws UniprotExportException {
        ExporterBasedOnClusterScore exporter = new ExporterBasedOnClusterScore();

        MiScoreResults results = createMiScoreResultsForMiScoreExport();

        exporter.exportInteractionsFrom(results);

        Set<Integer> interactionsExported = results.getInteractionsToExport();

        Assert.assertNotNull(interactionsExported);
        Assert.assertEquals(3, interactionsExported.size());

        boolean isValid = true;

        for (Integer interactionId : interactionsExported){
             if ( interactionId == 4 || interactionId == 5 || interactionId == 6){
                  isValid = false;
                 break;
             }
        }

        Assert.assertTrue(isValid);
    }
}
