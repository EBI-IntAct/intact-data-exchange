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
package uk.ac.ebi.intact.task.mitab.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrCleanerTasklet implements Tasklet {

    private List<Resource> solrUrls;

    public SolrCleanerTasklet() {
        solrUrls = Collections.EMPTY_LIST;
    }

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        for (Resource solrUrl : solrUrls) {
            SolrServer ontologiesSolrServer = new CommonsHttpSolrServer(solrUrl.getURL());

            ontologiesSolrServer.deleteByQuery("*:*");
            ontologiesSolrServer.commit();
            ontologiesSolrServer.optimize();

            contribution.getExitStatus().addExitDescription("Cleared: " + solrUrl);
        }

        return RepeatStatus.FINISHED;
    }

    public void setSolrUrls(List<Resource> solrUrls) {
        this.solrUrls = solrUrls;
    }
}