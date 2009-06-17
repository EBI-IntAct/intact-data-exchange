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

import psidev.psi.mi.tab.model.BinaryInteraction;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.Resource;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyBinaryInteractionEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyEnricherItemProcessor implements ItemProcessor<BinaryInteraction,BinaryInteraction> {

    private Resource ontologiesSolrUrl;

    public BinaryInteraction process(BinaryInteraction item) throws Exception {
        if (ontologiesSolrUrl == null) {
            throw new NullPointerException("ontologiesSolrUrl is null");
        }
        SolrServer ontologiesSolrServer = new CommonsHttpSolrServer(ontologiesSolrUrl.getURL());
        OntologySearcher ontologySearcher = new OntologySearcher(ontologiesSolrServer);
        BinaryInteractionEnricher enricher = new OntologyBinaryInteractionEnricher(ontologySearcher);

        enricher.enrich(item);

        return item;
    }

    public void setOntologiesSolrUrl(Resource ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }
}
