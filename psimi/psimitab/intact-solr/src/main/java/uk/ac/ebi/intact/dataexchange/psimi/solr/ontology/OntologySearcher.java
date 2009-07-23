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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.apache.commons.collections.map.LRUMap;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Collections;

/**
 * Searches the ontology, using a SolrServer pointing to an ontology core.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologySearcher {

    private static final int CHILDREN_CACHE_SIZE = 10000;
    private static final int PARENTS_CACHE_SIZE = 10000;

    private final SolrServer solrServer;

    private final Map<String,QueryResponse> childSearchesMap;
    private final Map<String,QueryResponse> parentSearchesMap;

    private Set<String> ontologyNames;

    public OntologySearcher(SolrServer solrServer) {
        if ( solrServer == null ) {
            throw new IllegalArgumentException( "You must give a non null solrServer" );
        }

        this.solrServer = solrServer;

        childSearchesMap = new LRUMap( CHILDREN_CACHE_SIZE );
        parentSearchesMap = new LRUMap( PARENTS_CACHE_SIZE );
    }

    public QueryResponse search(SolrQuery query) throws SolrServerException{
        return solrServer.query(query);
    }

    private QueryResponse searchById( String id, Integer firstResult, Integer maxResults,
                                      final String fieldId, Map<String,QueryResponse> cache ) throws SolrServerException {
        // We need to include the maxResult in the key as we do different maxResult for
        // the same term, hence we could end up getting the wrong number of term.
        final String key = id + maxResults;

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        QueryResponse queryResponse = searchById( fieldId, id, firstResult, maxResults);

        cache.put(key, queryResponse);

        return queryResponse;
    }

    /**
     * Search by child id so return parent relationship.
     *
     * @param id
     * @param firstResult
     * @param maxResults
     * @return
     * @throws SolrServerException
     */
    public QueryResponse searchByChildId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchById( id, firstResult, maxResults, OntologyFieldNames.CHILD_ID, childSearchesMap );
    }

    /**
     * Search by parent Id so returns children relationship.
     *
     * @param id
     * @param firstResult
     * @param maxResults
     * @return
     * @throws SolrServerException
     */
    public QueryResponse searchByParentId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchById( id, firstResult, maxResults, OntologyFieldNames.PARENT_ID, parentSearchesMap );
    }

    public Set<String> getOntologyNames() throws SolrServerException {
        if (ontologyNames == null) {
            SolrQuery query = new SolrQuery("*:*");
            query.setStart(0);
            query.setRows(0);
            query.setFacet(true);
            query.setParam(FacetParams.FACET_FIELD, "ontology");

            QueryResponse response = search(query);
            FacetField facetField = response.getFacetField("ontology");

            if (facetField.getValues() == null) {
                return Collections.EMPTY_SET;//Ênew HashSet<String>();
            }

            ontologyNames = new HashSet<String>( facetField.getValues().size() );

            for (FacetField.Count c : facetField.getValues()) {
                ontologyNames.add(c.getName());
            }
        }

        return ontologyNames;
    }

    private QueryResponse searchById(String fieldId, String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        SolrQuery query = new SolrQuery(fieldId + ":\"" + id + "\"");

        if (firstResult != null) query.setStart(firstResult);
        if (maxResults != null) query.setRows(maxResults);

        return search(query);
    }
}
