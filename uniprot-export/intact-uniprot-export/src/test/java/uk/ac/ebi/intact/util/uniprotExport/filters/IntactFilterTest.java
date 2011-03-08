package uk.ac.ebi.intact.util.uniprotExport.filters;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportBase;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.rules.ExporterBasedOnDetectionMethod;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterConfig;
import uk.ac.ebi.intact.util.uniprotExport.filters.config.FilterContext;
import uk.ac.ebi.intact.util.uniprotExport.results.MiClusterScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;

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
        FilterConfig config = FilterContext.getInstance().getConfig();
        // accept negative interactions
        config.setExcludeNegativeInteractions(false);
        config.setExcludeSpokeExpandedInteractions(false);

        createExperimentContext();

        Assert.assertEquals(5, getDaoFactory().getInteractionDao().getAll().size());
        Assert.assertEquals(4, getDaoFactory().getExperimentDao().getAll().size());

        IntactFilter filter = new IntactFilter(new ExporterBasedOnDetectionMethod());

        MiClusterScoreResults results = filter.exportInteractions();

        IntactCluster clusterScore = results.getPositiveClusteredInteractions().getCluster();
        IntactCluster negativeClusterScore = results.getNegativeClusteredInteractions().getCluster();

        // compute score for interactions with valid dr-export
        Assert.assertNotNull(clusterScore);
        Assert.assertNotNull(negativeClusterScore);
        Assert.assertEquals(2, clusterScore.getAllInteractionIds().size());
        Assert.assertEquals(1, negativeClusterScore.getAllInteractionIds().size());

        boolean isValid = true;

        for (EncoreInteraction interaction : clusterScore.getEncoreInteractionCluster().values()){
             Set<String> interactionAcs = interaction.getExperimentToPubmed().keySet();

            if (interactionAcs.contains(interaction2) || interactionAcs.contains(interaction4) || interactionAcs.contains(interaction5)){
                isValid = false;
                break;
            }
        }

        for (EncoreInteraction interaction : negativeClusterScore.getEncoreInteractionCluster().values()){
             Set<String> interactionAcs = interaction.getExperimentToPubmed().keySet();

            if (!interactionAcs.contains(interaction5)){
                isValid = false;
                break;
            }
        }

        Assert.assertTrue(isValid);
    }
}
