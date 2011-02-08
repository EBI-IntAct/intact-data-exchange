package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterScoreResults;

import java.util.Set;

/**
 * Tester of the Intact filter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/02/11</pre>
 */

public class IntactFilterTest extends UniprotExportBase{

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void test_simulation() throws UniprotExportException {
        createExperimentContext();

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().getAll().size());
        Assert.assertEquals(3, getDaoFactory().getExperimentDao().getAll().size());

        IntactFilter filter = new IntactFilter(new ExporterBasedOnDetectionMethod());

        MiClusterScoreResults results = filter.exportInteractions();

        InteractionClusterScore clusterScore = results.getCluster();

        // compute score for interactions with valid dr-export
        Assert.assertNotNull(clusterScore);
        Assert.assertEquals(2, clusterScore.getInteractionMapping().size());

        boolean isValid = true;

        for (EncoreInteraction interaction : clusterScore.getInteractionMapping().values()){
             Set<String> interactionAcs = interaction.getExperimentToPubmed().keySet();

            if (interactionAcs.contains(interaction2) || interactionAcs.contains(interaction4)){
                isValid = false;
                break;
            }
        }

        Assert.assertTrue(isValid);
    }
}
