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

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.psimi.solr.CoreNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Protein;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ContextConfiguration(locations = {"/META-INF/mitab-creation.spring.xml"})
public class MitabCreationTests extends IntactBasicTestCase {

    @Resource(name = "intactBatchJobLauncher")
    private JobLauncher jobLauncher;

    @Autowired
    private PersisterHelper persisterHelper;

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

        // uncommenting this will cause the test to hang - which might be used to perform extra solr query.
       // solrJettyRunner.join();

        solrJettyRunner.stop();
    }

    @Test
    public void writeMitab() throws Exception {
        FileUtils.deleteDirectory(new File("target/lala-lucene"));
        
        Experiment exp = getMockBuilder().createExperimentRandom(3);
        persisterHelper.save(exp);

        Protein proteinA = getMockBuilder().createProtein("P12345", "protA");
        Protein proteinB = getMockBuilder().createProtein("Q00001", "protB");
        Protein proteinC = getMockBuilder().createProtein("Q00002", "protC");

        Interaction interaction = getMockBuilder().createInteraction(
                getMockBuilder().createComponentBait(proteinA),
                getMockBuilder().createComponentPrey(proteinB),
                getMockBuilder().createComponentPrey(proteinC));

        CvDatabase goDb = getMockBuilder().createCvObject(CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO);
        proteinA.addXref(getMockBuilder().createXref(proteinA, "GO:0030246", null, goDb));

        proteinA.getBioSource().setTaxId("9606");

        persisterHelper.save(interaction);

        Assert.assertEquals(4, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("createMitabJob");

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
        Assert.assertTrue( jobExecution.getAllFailureExceptions().isEmpty() );
        Assert.assertEquals( "COMPLETED", jobExecution.getExitStatus().getExitCode() );

        final SolrServer solrServer = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB);

        Assert.assertEquals(5L, solrServer.query(new SolrQuery("*:*")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("P12345")).getResults().getNumFound());
        Assert.assertEquals(1L, solrServer.query(new SolrQuery("Q00002")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("go:\"GO:0003674\"")).getResults().getNumFound());
        Assert.assertEquals(2L, solrServer.query(new SolrQuery("species:Catarrhini")).getResults().getNumFound());
    }
}