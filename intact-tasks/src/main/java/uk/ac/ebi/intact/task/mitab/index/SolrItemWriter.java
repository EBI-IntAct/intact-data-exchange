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
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.task.mitab.BinaryInteractionItemWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrItemWriter implements BinaryInteractionItemWriter, ItemStream{

    private Resource interactionsSolrUrl;
    private Resource ontologiesSolrUrl;
    private DocumentDefinition documentDefinition;

    private OntologySearcher ontologySearcher;
    private SolrServer interactionSolrServer;

    public void write(List<? extends BinaryInteraction> items) throws Exception {
        if (interactionsSolrUrl == null) {
            throw new NullPointerException("No 'interactionsSolrUrl' configured for SolrItemWriter");
        }
        if (documentDefinition == null) {
            throw new NullPointerException("No 'documentDefinition' configured for SolrItemWriter");
        }

        if (items.isEmpty()) {
            return;
        }

        SolrServer interactionsSolrServer = getInteractionsSolrServer();

        SolrDocumentConverter solrDocumentConverter = new SolrDocumentConverter(interactionsSolrServer, documentDefinition);

        for (BinaryInteraction binaryInteraction : items) {
            SolrInputDocument solrInputDoc = solrDocumentConverter.toSolrDocument(binaryInteraction);
            interactionsSolrServer.add(solrInputDoc);
        }
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            if (ontologiesSolrUrl != null) {
                SolrServer ontologiesSolrServer = new CommonsHttpSolrServer(ontologiesSolrUrl.getURL());
                ontologySearcher = new OntologySearcher(ontologiesSolrServer);
            }
        } catch (IOException e) {
            throw new ItemStreamException("Problem with ontology solr server: "+ontologiesSolrUrl, e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    public void close() throws ItemStreamException {
        try {
            SolrServer solrServer = getInteractionsSolrServer();
            solrServer.commit();
            solrServer.optimize();
        } catch (Exception e) {
            throw new ItemStreamException("Problem closing solr server", e);
        }
    }

    public SolrServer getInteractionsSolrServer() throws IOException {
        if (interactionSolrServer == null) {
            if (interactionsSolrUrl == null) {
                throw new NullPointerException("No 'interactionsSolrUrl' configured for SolrItemWriter");
            }

            interactionSolrServer = new CommonsHttpSolrServer(interactionsSolrUrl.getURL());
        }

        return interactionSolrServer;
    }

    public void setInteractionsSolrUrl(Resource interactionsSolrUrl) {
        this.interactionsSolrUrl = interactionsSolrUrl;
    }

    public void setOntologiesSolrUrl(Resource ontologiesSolrUrl) {
        this.ontologiesSolrUrl = ontologiesSolrUrl;
    }

    public void setDocumentDefinition(DocumentDefinition documentDefinition) {
        this.documentDefinition = documentDefinition;
    }
}
