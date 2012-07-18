package uk.ac.ebi.intact.task;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.junit.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Tester of mitab to solr indexing job
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/12</pre>
 */
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
@Transactional(propagation = Propagation.NEVER)
public class MitabToSolrIndexJobTest extends IntactBasicTestCase{

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private CorePersister corePersister;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SolrJettyRunner solrJettyRunner;

    @Before
    public void before() throws Exception {
        solrJettyRunner.start();
    }

    @After
    public void after() throws Exception {

        solrJettyRunner.stop();
    }

    @Test
    @DirtiesContext
    public void indexMitabSolr() throws Exception {
        FileUtils.deleteDirectory(new File("target/lala-lucene"));

        Job job = (Job) applicationContext.getBean("mitabSolrIndexJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);

        // five clustered interactions
        Assert.assertEquals(12L, solrServer.query(new SolrQuery("*:*")).getResults().getNumFound());
        Assert.assertEquals(4L, solrServer.query(new SolrQuery("intact-miscore:[0.40 TO 0.75]")).getResults().getNumFound());
    }
}
