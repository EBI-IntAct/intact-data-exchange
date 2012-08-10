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
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyCrossReferenceEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.task.mitab.BinaryIteractionItemProcessor;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyEnricherItemProcessor implements BinaryIteractionItemProcessor, ItemStream {

    private String ontologiesSolrUrl;

    private BinaryInteractionEnricher enricher;
    private boolean processOnlyInteractors = false;

    public BinaryInteraction process(BinaryInteraction item) throws Exception {

        if (!processOnlyInteractors){
            enricher.enrich(item);
        }
        else {
            if (item.getInteractorA() != null){
                enricher.enrich(item.getInteractorA());
            }
            if (item.getInteractorB() != null){
                enricher.enrich(item.getInteractorB());
            }
        }

        return item;
    }

    public void setOntologiesSolrUrl(String ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }

    public void setEnricher(BinaryInteractionEnricher enricher) {
        this.enricher = enricher;
    }

    @Override
    public void onlyProcessInteractors(boolean onlyInteractors) {
        this.processOnlyInteractors = onlyInteractors;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (enricher == null) {

            if (ontologiesSolrUrl == null) {
                throw new ItemStreamException("ontologiesSolrUrl is null");
            }

            SolrServer ontologiesSolrServer = new HttpSolrServer(ontologiesSolrUrl);
            OntologySearcher ontologySearcher = new OntologySearcher(ontologiesSolrServer);
            enricher = new OntologyCrossReferenceEnricher(ontologySearcher);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // nothing to do
    }

    @Override
    public void close() throws ItemStreamException {
        this.enricher = null;
    }
}
