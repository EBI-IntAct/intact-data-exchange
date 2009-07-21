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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class to simplify searching.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrSearcher {

    private SolrServer solrServer;

    public IntactSolrSearcher(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public SolrSearchResult search(String query, Integer firstResult, Integer maxResults) throws IntactSolrException {
        if (query == null) throw new NullPointerException("Null query");
        
        if ("*".equals(query)) query = "*:*";

        SolrQuery solrQuery = new SolrQuery(query);

        if (firstResult != null) solrQuery.setStart(firstResult);

        if (maxResults != null) {
            solrQuery.setRows(maxResults);
        } else {
            solrQuery.setRows(Integer.MAX_VALUE);
        }

        return search(solrQuery);
    }

    public SolrSearchResult search(SolrQuery originalQuery) throws IntactSolrException {
        SolrQuery query = originalQuery.getCopy();

        String[] fields = (String[]) ArrayUtils.add(FieldNames.DATA_FIELDS, "pkey");

        if(query.getFields()!=null){
            fields = (String[]) ArrayUtils.add(fields, query.getFields().split(","));
        }

        query.setFields(fields);

        // if using a wildcard query we convert to lower case
        // as of http://mail-archives.apache.org/mod_mbox/lucene-solr-user/200903.mbox/%3CFD3AFB65-AEC1-40B2-A0A4-7E14A519AB05@ehatchersolutions.com%3E
        if (query.getQuery().contains("*")) {
            query.setQuery(query.getQuery().toLowerCase());
        }

        QueryResponse queryResponse = executeQuery(query);
        return new SolrSearchResult(queryResponse);
    }

    public Collection<InteractorIdCount> searchInteractors(SolrQuery query, String interactorMi) throws IntactSolrException {
        Multimap<String,InteractorIdCount> interactors = searchInteractors(query, new String[] {interactorMi});
        return interactors.values();
    }

    public Multimap<String,InteractorIdCount> searchInteractors(SolrQuery originalQuery, String[] interactorTypeMis) throws IntactSolrException {
        SolrQuery query = originalQuery.getCopy();
        query.setRows(0);
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.setFacetLimit(Integer.MAX_VALUE);
        query.setFacetSort(FacetParams.FACET_SORT_COUNT);

        Multimap<String,InteractorIdCount> interactors = new HashMultimap<String,InteractorIdCount>();

        Map<String,String> fieldNameToTypeMap = new HashMap<String,String>(interactorTypeMis.length);

        for (String mi : interactorTypeMis) {
            final String fieldName = createFieldName(mi);

            query.addFacetField(fieldName);
            fieldNameToTypeMap.put(fieldName, mi);
        }

        QueryResponse queryResponse = executeQuery(query);

        for (Map.Entry<String,String> entry : fieldNameToTypeMap.entrySet()) {
            FacetField ff = queryResponse.getFacetField(entry.getKey());

            if (ff != null && ff.getValues() != null) {
                for (FacetField.Count c : ff.getValues()) {
                    interactors.put(entry.getValue(), new InteractorIdCount(c.getName(), c.getCount()));
                }
            }
        }

        return interactors;
    }

    private String createFieldName(String mi) {
        return FieldNames.INTACT_BY_INTERACTOR_TYPE_PREFIX +mi.replaceAll(":", "").toLowerCase();
    }

    private QueryResponse executeQuery(SolrQuery query) {
        QueryResponse queryResponse;
        try {
            queryResponse = solrServer.query(query);
        } catch (SolrServerException e) {
            throw new IntactSolrException("Problem searching with query: "+query, e);
        }
        return queryResponse;
    }
}
