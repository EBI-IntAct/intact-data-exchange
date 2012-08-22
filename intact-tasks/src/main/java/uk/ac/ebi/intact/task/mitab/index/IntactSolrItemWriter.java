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

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.hupo.psi.mi.psicquic.indexing.batch.writer.SolrItemWriter;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrItemWriter extends SolrItemWriter {

    private String ontologiesSolrUrl;
    private OntologySearcher ontologySearcher;

    public IntactSolrItemWriter(){
        super();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);

        // create ontologySearcher
        if (ontologiesSolrUrl != null) {
            HttpSolrServer ontologiesSolrServer = createOntologySolrServer();

            ontologySearcher = new OntologySearcher(ontologiesSolrServer);
        }

        // create new SolrDocumentConverter
        this.solrConverter = new SolrDocumentConverter(solrServer, ontologySearcher);
    }

    private HttpSolrServer createOntologySolrServer() {
        HttpSolrServer ontologiesSolrServer = new HttpSolrServer(ontologiesSolrUrl, createHttpClient());

        return ontologiesSolrServer;
    }

    @Override
    public void close() throws ItemStreamException {
        super.close();

        if (this.ontologySearcher != null){
            this.ontologySearcher.shutdown();
        }
    }

    public void setOntologiesSolrUrl(String ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }
}
