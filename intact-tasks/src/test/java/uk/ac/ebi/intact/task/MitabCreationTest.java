/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.task;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.junit.After;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.IntactSolrJettyRunner;
import uk.ac.ebi.intact.model.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml", "/META-INF/job-tests.spring.xml"})
public class MitabCreationTest extends IntactBasicTestCase {

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IntactSolrJettyRunner solrJettyRunner;

    @Before
    public void before() throws Exception {
        solrJettyRunner.setPort(18080);
        solrJettyRunner.start();
    }

    @After
    public void after() throws Exception {

        solrJettyRunner.stop();
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void writeMitab() throws Exception {

        TransactionStatus status = getDataContext().beginTransaction();

        CvTopic hidden = getMockBuilder().createCvObject( CvTopic.class, null, "hidden" );

        CvTopic internalRemark = getMockBuilder().createCvObject( CvTopic.class, null, "internal-remark" );
        internalRemark.addAnnotation( new Annotation(getIntactContext().getInstitution(), hidden, "" ) );

        CvTopic noUniprotUpdate = getMockBuilder().createCvObject( CvTopic.class, null, "no-uniprot-update" );
        noUniprotUpdate.addAnnotation( new Annotation(getIntactContext().getInstitution(), hidden, "" ) );

        Experiment exp = getMockBuilder().createExperimentRandom(3);
        exp.addAnnotation( new Annotation(exp.getOwner(), internalRemark, "some internal information" ) );

        getCorePersister().saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");
        proteinC.getAnnotations().clear();
        proteinC.addAnnotation( new Annotation(exp.getOwner(), noUniprotUpdate, "Could not map sequence" ) );

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));

        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        proteinA.addXref(getMockBuilder().createXref(proteinA, "GO:0030246", null, goDb));

        proteinA.getBioSource().setTaxId("9606");

        getCorePersister().saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        Job job = (Job) applicationContext.getBean("createMitabJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);

        Assert.assertEquals(5L, solrServer.query(new SolrQuery("*:*")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("P12345")).getResults().getNumFound());
        Assert.assertEquals(1L, solrServer.query(new SolrQuery("Q00002")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("pxrefA:\"go:GO:0003674\"")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("species:Catarrhini")).getResults().getNumFound());
        Assert.assertEquals(0L, solrServer.query(new SolrQuery("\"Could not map sequence\"")).getResults().getNumFound());
        // checking that the hidden annotation is still there
        TransactionStatus transactionStatus = getDataContext().beginTransaction();

        proteinC = getDaoFactory().getProteinDao().getByShortLabel("protC");
        Assert.assertEquals(1, proteinC.getAnnotations().size());

        getDataContext().commitTransaction(transactionStatus);
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void writeMitabSelf() throws Exception {

        TransactionStatus status = getDataContext().beginTransaction();

        Experiment exp = getMockBuilder().createExperimentRandom(3);

        getCorePersister().saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentNeutral(proteinA));
        Assert.assertEquals(1, interaction.getComponents().size());

        // set stoichiometry
        interaction.getComponents().iterator().next().setStoichiometry(2f);

        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        proteinA.addXref(getMockBuilder().createXref(proteinA, "GO:0030246", null, goDb));

        proteinA.getBioSource().setTaxId("9606");

        getCorePersister().saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        Job job = (Job) applicationContext.getBean("createMitabJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);

        Assert.assertEquals(1L, solrServer.query(new SolrQuery("P12345")).getResults().getNumFound());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void writeMitabSelf_stoichioGreaterThan2() throws Exception {

        TransactionStatus status = getDataContext().beginTransaction();
        Experiment exp = getMockBuilder().createExperimentRandom(3);

        getCorePersister().saveOrUpdate(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentNeutral(proteinA));
        Assert.assertEquals(1, interaction.getComponents().size());

        // set stoichiometry
        interaction.getComponents().iterator().next().setStoichiometry(4f);

        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        proteinA.addXref(getMockBuilder().createXref(proteinA, "GO:0030246", null, goDb));

        proteinA.getBioSource().setTaxId("9606");

        getCorePersister().saveOrUpdate(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        Job job = (Job) applicationContext.getBean("createMitabJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);

        Assert.assertEquals(1L, solrServer.query(new SolrQuery("P12345")).getResults().getNumFound());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void writeMitab_withXrefs() throws Exception {
        TransactionStatus status = getDataContext().beginTransaction();

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));

        CvDatabase imexDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.IMEX_MI_REF, CvDatabase.IMEX);
        CvXrefQualifier imexPrimary = getMockBuilder().createCvObject(CvXrefQualifier.class, CvXrefQualifier.IMEX_PRIMARY_MI_REF, CvXrefQualifier.IMEX_PRIMARY);
        interaction.addXref(getMockBuilder().createXref(interaction, "IM-1234-1", imexPrimary, imexDb));

        getCorePersister().saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        Job job = (Job) applicationContext.getBean("createMitabJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("interaction_id:IM-1234-1")).getResults().getNumFound());
    }

    @Test
    @DirtiesContext
    @Transactional(propagation = Propagation.NEVER)
    public void writeMitab_negative() throws Exception {

        TransactionStatus status = getDataContext().beginTransaction();

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));

        interaction.addAnnotation(getMockBuilder().createAnnotation("because of this and that", null, CvTopic.NEGATIVE));

        getCorePersister().saveOrUpdate(interaction);

        Assert.assertEquals(1, getDaoFactory().getInteractionDao().countAll());

        getDataContext().commitTransaction(status);

        Job job = (Job) applicationContext.getBean("createMitabJob");

        Map<String, JobParameter> params = new HashMap<String, JobParameter>(1);
        params.put("date", new JobParameter(System.currentTimeMillis()));

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters(params));
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);
        Assert.assertEquals(0L, solrServer.query(new SolrQuery("*:*")).getResults().getNumFound());
    }
}
