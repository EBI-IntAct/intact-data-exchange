package uk.ac.ebi.intact.task.mitab.clustering;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Test job to cluster and score mitab file into clustered mitab files
 *
 * @author Rafael Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"
        , "classpath*:/META-INF/intact.spring.xml", "classpath*:/META-INF/standalone/*-standalone.spring.xml"})
public class ClusterScoreTaskletTest{

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private ApplicationContext applicationContext;


    @After
    public void deleteGeneratedMitab(){
        File file = new File("target/mitab-clustered");
        file.delete();
    }

	@Test
	public void testLaunchJob() throws Exception {
        Job job = (Job) applicationContext.getBean("clusterScoreJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue(jobExecution.getAllFailureExceptions().isEmpty());
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        File file = new File("target/mitab-clustered.txt");
        
        Assert.assertTrue(file.exists());
	}
}
