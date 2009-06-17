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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;

import javax.annotation.Resource;

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
        solrJettyRunner.stop();
    }

    @Test
    public void writeMitab() throws Exception {
        Experiment exp = getMockBuilder().createExperimentRandom(5);
        persisterHelper.save(exp);

        Assert.assertEquals(5, getDaoFactory().getInteractionDao().countAll());

        Job job = (Job) applicationContext.getBean("createMitabJob");

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
        System.out.println(jobExecution);
    }
    


}
