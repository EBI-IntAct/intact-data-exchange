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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.bridges.ontologies.iterator.UniprotTaxonomyOntologyIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologyIndexer;

import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyPopulatorTasklet implements Tasklet{

    private Resource ontologiesSolrUrl;
    private List<OntologyMapping> oboOntologyMappings;
    private List<OntologyMapping> taxonomyOntologyMappings;
    private boolean indexUniprotTaxonomy;

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (ontologiesSolrUrl == null) {
            throw new NullPointerException("ontologiesSolrUrl is null");
        }
        StreamingUpdateSolrServer ontologiesSolrServer = new StreamingUpdateSolrServer(ontologiesSolrUrl.getURL().toString(), 10, 4);
        ontologiesSolrServer.setMaxRetries(1);

        OntologyIndexer ontologyIndexer = new OntologyIndexer(ontologiesSolrServer);

        if (taxonomyOntologyMappings != null) {
            indexUniprotTaxonomy = true;
        }

        if (indexUniprotTaxonomy) {
            if (taxonomyOntologyMappings == null) {
                ontologyIndexer.indexUniprotTaxonomy();
            } else {
                for (OntologyMapping om : taxonomyOntologyMappings) {
                    ontologyIndexer.indexOntology(new UniprotTaxonomyOntologyIterator(om.getUrl()));
                }
            }
        }

        ontologyIndexer.indexObo(oboOntologyMappings.toArray(new OntologyMapping[oboOntologyMappings.size()]));

        long count = countDocs(ontologiesSolrServer);
        contribution.getExitStatus().addExitDescription("Total docs in index: "+count);

        return RepeatStatus.FINISHED;
    }

    private long countDocs(SolrServer ontologiesSolrServer) throws SolrServerException {
        SolrQuery countQuery = new SolrQuery("*:*");
        countQuery.setRows(0);
        QueryResponse queryResponse = ontologiesSolrServer.query(countQuery);
        long count = queryResponse.getResults().getNumFound();
        return count;
    }

    public void setOntologiesSolrUrl(Resource ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }

    public void setOboOntologyMappings(List<OntologyMapping> oboOntologyMappings) {
        this.oboOntologyMappings = oboOntologyMappings;
    }

    public void setIndexUniprotTaxonomy(boolean indexUniprotTaxonomy) {
        this.indexUniprotTaxonomy = indexUniprotTaxonomy;
    }

    public void setTaxonomyOntologyMappings(List<OntologyMapping> taxonomyOntologyMappings) {
        this.taxonomyOntologyMappings = taxonomyOntologyMappings;
    }
}
