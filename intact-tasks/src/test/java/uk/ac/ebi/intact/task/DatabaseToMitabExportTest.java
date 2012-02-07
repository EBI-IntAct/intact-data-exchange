package uk.ac.ebi.intact.task;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Tester of export from Intact database to mitab job
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07/02/12</pre>
 */
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
@Transactional(propagation = Propagation.NEVER)
public class DatabaseToMitabExportTest extends IntactBasicTestCase{

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private CorePersister corePersister;

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void deleteGeneratedMitab(){
        File generatedMitab = new File("target/lala.txt");
        generatedMitab.delete();
    }

    @Test
    @DirtiesContext
    public void writeMitab() throws Exception {

        Experiment exp = getMockBuilder().createExperimentRandom(3);
        corePersister.saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));


        proteinA.getBioSource().setTaxId("9606");

        corePersister.saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("mitabExportJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );
        
        File generatedMitab = new File("target/lala.txt");
        Assert.assertTrue(generatedMitab.exists());
        
        PsimiTabReader reader = new PsimiTabReader(true);
        Collection<BinaryInteraction> binaryInteractions = reader.read(generatedMitab);
        Assert.assertEquals(5, binaryInteractions.size());
    }

    @Test
    @DirtiesContext
    public void writeMitabSelf() throws Exception {
        Experiment exp = getMockBuilder().createExperimentRandom(3);

        corePersister.saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentNeutral(proteinA));
        Assert.assertEquals(1, interaction.getComponents().size());

        // set stoichiometry
        interaction.getComponents().iterator().next().setStoichiometry(2);
        proteinA.getBioSource().setTaxId("9606");

        corePersister.saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("mitabExportJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        File generatedMitab = new File("target/lala.txt");
        Assert.assertTrue(generatedMitab.exists());

        PsimiTabReader reader = new PsimiTabReader(true);
        Collection<BinaryInteraction> binaryInteractions = reader.read(generatedMitab);
        Assert.assertEquals(4, binaryInteractions.size());
    }

    @Test
    @DirtiesContext
    public void writeMitabSelf_stoichioGreaterThan2() throws Exception {

        Experiment exp = getMockBuilder().createExperimentRandom(3);

        corePersister.saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentNeutral(proteinA));
        Assert.assertEquals(1, interaction.getComponents().size());

        // set stoichiometry
        interaction.getComponents().iterator().next().setStoichiometry(4);

        proteinA.getBioSource().setTaxId("9606");

        corePersister.saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("mitabExportJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        File generatedMitab = new File("target/lala.txt");
        Assert.assertTrue(generatedMitab.exists());

        PsimiTabReader reader = new PsimiTabReader(true);
        Collection<BinaryInteraction> binaryInteractions = reader.read(generatedMitab);
        Assert.assertEquals(4, binaryInteractions.size());
    }

    @Test
    @DirtiesContext
    public void writeMitab_negative() throws Exception {
        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));

        interaction.addAnnotation(getMockBuilder().createAnnotation("because of this and that", null, CvTopic.NEGATIVE));

        corePersister.saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("mitabExportJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        File generatedMitab = new File("target/lala.txt");
        Assert.assertTrue(generatedMitab.exists());

        PsimiTabReader reader = new PsimiTabReader(true);
        Collection<BinaryInteraction> binaryInteractions = reader.read(generatedMitab);
        Assert.assertEquals(0, binaryInteractions.size());
    }
}
