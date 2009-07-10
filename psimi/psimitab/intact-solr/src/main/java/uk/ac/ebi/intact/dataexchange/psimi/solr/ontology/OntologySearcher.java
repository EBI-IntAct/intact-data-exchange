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
package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Searches the ontology, using a SolrServer pointing to an ontology core.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologySearcher {

    private SolrServer solrServer;

    public OntologySearcher(SolrServer solrServer) {
        this.solrServer = solrServer;
    }


    public QueryResponse search(SolrQuery query) throws SolrServerException{
        return solrServer.query(query);
    }

    public QueryResponse searchByChildId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchById(OntologyFieldNames.CHILD_ID, id, firstResult, maxResults);
    }

    public QueryResponse searchByParentId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchById(OntologyFieldNames.PARENT_ID, id, firstResult, maxResults);
    }

    private QueryResponse searchById(String fieldId, String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        SolrQuery query = new SolrQuery(fieldId + ":\"" + id + "\"");

        if (firstResult != null) query.setStart(firstResult);
        if (maxResults != null) query.setRows(maxResults);

        return search(query);
    }
}
