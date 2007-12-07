package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;

import java.net.URL;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUpdaterTest extends IntactBasicTestCase {

    @Test
    public void createOrUpdateCVs() throws Exception {
        IntactOntology ontology = OboUtils.createOntologyFromOboDefault(8938);

        CvUpdater updater = new CvUpdater(ontology);
        CvUpdaterStatistics stats = updater.createOrUpdateCVs();

        int total = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals(stats.getCreatedCvs().size(), 835);
        Assert.assertEquals(stats.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats.getInvalidTerms().size(), 9);

        // update
        CvUpdaterStatistics stats2 = updater.createOrUpdateCVs();

        Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

        Assert.assertEquals(stats2.getCreatedCvs().size(), 0);
        Assert.assertEquals(stats2.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats2.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats2.getInvalidTerms().size(), 9);
    }

    @Test
    @Ignore
    public void createOrUpdateCVs_includingNonMi() throws Exception {
        URL intactObo = CvUpdaterTest.class.getResource("/intact.20071203.obo");

        IntactOntology ontology = OboUtils.createOntologyFromObo(intactObo);

        CvUpdater updater = new CvUpdater(ontology);
        CvUpdaterStatistics stats = updater.createOrUpdateCVs();

        int total = getDaoFactory().getCvObjectDao().countAll();

        System.out.println(stats);

        // TODO adjust numbers when it works
        Assert.assertEquals(stats.getCreatedCvs().size(), 835);
        Assert.assertEquals(stats.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats.getInvalidTerms().size(), 9);

        // update
        CvUpdaterStatistics stats2 = updater.createOrUpdateCVs();

        Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

        Assert.assertEquals(stats2.getCreatedCvs().size(), 0);
        Assert.assertEquals(stats2.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats2.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats2.getInvalidTerms().size(), 9);
    }


}
