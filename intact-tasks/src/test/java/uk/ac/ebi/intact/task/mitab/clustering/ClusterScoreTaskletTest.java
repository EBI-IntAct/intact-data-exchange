package uk.ac.ebi.intact.task.mitab.clustering;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;


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

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml" })
public class ClusterScoreTaskletTest {

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void deleteGeneratedMitab(){
        File directory = new File("src/test/resources/mitabClustered");
        File[] files = directory.listFiles();
        for (File file : files){
           if (!file.delete()){
               System.out.println("Failed to delete "+file);
           }
        }
    }

	@Test
	public void testLaunchJob() throws Exception {
        Job job = (Job) applicationContext.getBean("clusterScoreJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue(jobExecution.getAllFailureExceptions().isEmpty());
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

	}
}
