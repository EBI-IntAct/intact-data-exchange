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
package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Solr result wrapper, which facilitates getting the interactions;
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrSearchResult {

    private SolrServer solrServer;
    private QueryResponse queryResponse;

    private Collection<String> lineList;
    private Collection<Row> rowList;
    private Collection<IntactBinaryInteraction> binaryInteractionList;

    public SolrSearchResult(SolrServer solrServer, QueryResponse queryResponse) {
        this.solrServer = solrServer;
        this.queryResponse = queryResponse;
    }

    public long getTotalCount() {
        return queryResponse.getResults().getNumFound();
    }

    public Collection<String> getLineList() {
        if (lineList != null) {
            return lineList;
        }

        lineList = new ArrayList<String>(Long.valueOf(getTotalCount()).intValue());
        SolrDocumentConverter converter = new SolrDocumentConverter(solrServer, new IntactDocumentDefinition());

        for (SolrDocument doc : queryResponse.getResults()) {
            lineList.add(converter.toMitabLine(doc));
        }

        return lineList;
    }

    public Collection<Row> getRowList() {
        if (rowList != null) {
            return rowList;
        }

        rowList = new ArrayList<Row>(Long.valueOf(getTotalCount()).intValue());
        SolrDocumentConverter converter = new SolrDocumentConverter(solrServer, new IntactDocumentDefinition());

        for (SolrDocument doc : queryResponse.getResults()) {
            rowList.add(converter.toRow(doc));
        }

        return rowList;
    }

    public Collection<IntactBinaryInteraction> getBinaryInteractionList() {
        if (binaryInteractionList != null) {
            return binaryInteractionList;
        }

        binaryInteractionList = new ArrayList<IntactBinaryInteraction>(Long.valueOf(getTotalCount()).intValue());
        SolrDocumentConverter converter = new SolrDocumentConverter(solrServer, new IntactDocumentDefinition());

        for (SolrDocument doc : queryResponse.getResults()) {
            binaryInteractionList.add((IntactBinaryInteraction) converter.toBinaryInteraction(doc));
        }

        return binaryInteractionList;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }
}
