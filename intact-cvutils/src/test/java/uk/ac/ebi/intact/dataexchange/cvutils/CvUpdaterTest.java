package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.*;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.util.SchemaUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.context.IntactContext;

import java.net.URL;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUpdaterTest extends IntactBasicTestCase {

    @Before
    public void clear() throws Exception {
        SchemaUtils.createSchema();
    }

    @Test
    public void createOrUpdateCVs() throws Exception {
        IntactOntology ontology = OboUtils.createOntologyFromOboDefault(10841);

        CvUpdater updater = new CvUpdater();
        CvUpdaterStatistics stats = updater.createOrUpdateCVs(ontology);

        int total = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals(849, stats.getCreatedCvs().size());
        Assert.assertEquals(0, stats.getUpdatedCvs().size());
        Assert.assertEquals(50, stats.getObsoleteCvs().size());
        Assert.assertEquals(9, stats.getInvalidTerms().size());
        
        Assert.assertEquals(total, stats.getCreatedCvs().size());

        // update
        CvUpdaterStatistics stats2 = updater.createOrUpdateCVs(ontology);

        Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

        Assert.assertEquals(0, stats2.getCreatedCvs().size());
        Assert.assertEquals(0, stats2.getUpdatedCvs().size());
        Assert.assertEquals(50, stats2.getObsoleteCvs().size());
        Assert.assertEquals(9, stats2.getInvalidTerms().size());
    }

    @Test
    public void createOrUpdateCVs_existingTermToMarkAsObsolete() throws Exception {
        CvInteractionType aggregation = getMockBuilder().createCvObject(CvInteractionType.class, "MI:0191", "aggregation");
        CvTopic obsolete = getMockBuilder().createCvObject(CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE);
        PersisterHelper.saveOrUpdate(aggregation, obsolete);

        IntactOntology ontology = OboUtils.createOntologyFromOboDefault(10841);

        CvUpdater updater = new CvUpdater();
        CvUpdaterStatistics stats = updater.createOrUpdateCVs(ontology);
        System.out.println(stats);

        int total = getDaoFactory().getCvObjectDao().countAll();

        Assert.assertEquals(849, total);

        Assert.assertEquals(845, stats.getCreatedCvs().size());
        Assert.assertEquals(1, stats.getUpdatedCvs().size());
        Assert.assertEquals(50, stats.getObsoleteCvs().size());
        Assert.assertEquals(10, stats.getInvalidTerms().size());

    }

    @Test
    @Ignore
    public void createOrUpdateCVs_includingNonMi() throws Exception {
        URL intactObo = CvUpdaterTest.class.getResource("/intact.20071203.obo");

        IntactOntology ontology = OboUtils.createOntologyFromObo(intactObo);

        CvUpdater updater = new CvUpdater();
        CvUpdaterStatistics stats = updater.createOrUpdateCVs(ontology);

        int total = getDaoFactory().getCvObjectDao().countAll();

        System.out.println(stats);

        // TODO adjust numbers when it works
        Assert.assertEquals(stats.getCreatedCvs().size(), 835);
        Assert.assertEquals(stats.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats.getInvalidTerms().size(), 9);

        // update
        CvUpdaterStatistics stats2 = updater.createOrUpdateCVs(ontology);

        Assert.assertEquals(total, getDaoFactory().getCvObjectDao().countAll());

        Assert.assertEquals(stats2.getCreatedCvs().size(), 0);
        Assert.assertEquals(stats2.getUpdatedCvs().size(), 0);
        Assert.assertEquals(stats2.getObsoleteCvs().size(), 50);
        Assert.assertEquals(stats2.getInvalidTerms().size(), 9);
    }


}
