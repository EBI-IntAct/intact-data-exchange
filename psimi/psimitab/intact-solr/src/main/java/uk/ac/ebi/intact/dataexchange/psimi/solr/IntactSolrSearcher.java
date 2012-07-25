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
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;

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

    private final static String DEFAULT_PARAM_NAME = "qf";
    private final static String QUERY_TYPE = "defType";
    private final static String DISMAX_TYPE = "edismax";
    private final static String DEFAULT_QUERY_FIELDS = SolrFieldName.identifier.toString()+" "+SolrFieldName.pubid.toString()+" "+SolrFieldName.pubauth.toString()+" "+SolrFieldName.species.toString()+" "+SolrFieldName.detmethod.toString()+" "+SolrFieldName.type.toString()+" "+SolrFieldName.interaction_id.toString();

    public IntactSolrSearcher(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public SolrSearchResult search(String query, Integer firstResult, Integer maxResults, Collection<String> filteredQueries) throws IntactSolrException {
        if (query == null) throw new NullPointerException("Null query");
        
        if ("*".equals(query)) query = "*:*";

        SolrQuery solrQuery = new SolrQuery(query);
        // use dismax parser for querying default fields
        solrQuery.setParam(DEFAULT_PARAM_NAME, DEFAULT_QUERY_FIELDS);
        solrQuery.setParam(QUERY_TYPE, DISMAX_TYPE);

        if (firstResult != null) solrQuery.setStart(firstResult);

        if (maxResults != null) {
            solrQuery.setRows(maxResults);
        } else {
            solrQuery.setRows(Integer.MAX_VALUE);
        }

        if (filteredQueries != null){
            for (String filter : filteredQueries){
                solrQuery.addFilterQuery(filter);
            }
        }

        return search(solrQuery);
    }

    public SolrSearchResult search(String query, Integer firstResult, Integer maxResults) throws IntactSolrException {
        if (query == null) throw new NullPointerException("Null query");

        if ("*".equals(query)) query = "*:*";

        SolrQuery solrQuery = new SolrQuery(query);
        // use dismax parser for querying default fields
        solrQuery.setParam(DEFAULT_PARAM_NAME, DEFAULT_QUERY_FIELDS);
        solrQuery.setParam(QUERY_TYPE, DISMAX_TYPE);

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

        String[] fields = (String[]) ArrayUtils.add(FieldNames.DATA_FIELDS, FieldNames.PKEY);

        if(query.getFields()!=null){
            fields = (String[]) ArrayUtils.add(fields, query.getFields().split(","));
        }

        query.setFields(fields);

        // if using a wildcard query we convert to lower case
        // as of http://mail-archives.apache.org/mod_mbox/lucene-solr-user/200903.mbox/%3CFD3AFB65-AEC1-40B2-A0A4-7E14A519AB05@ehatchersolutions.com%3E
        if (query.getQuery().contains("*")) {
            String[] tokens = query.getQuery().split(" ");

            StringBuilder sb = new StringBuilder(query.getQuery().length());

            for (String token : tokens) {
                if (token.contains("*")) {
                    sb.append(token.toLowerCase());
                } else {
                    sb.append(token);
                }

                sb.append(" ");
            }

            query.setQuery(sb.toString().trim());
        }
        String queryString = query.getQuery();

        // and, or and not should be upper case
        query.set(queryString.replaceAll(" and ", " AND ").replaceAll(" or ", " OR ").replaceAll(" not ", " NOT ").replaceAll("\\\"", "\"").replaceAll("\\\\","\\"));

        QueryResponse queryResponse = executeQuery(query);
        return new SolrSearchResult(solrServer, queryResponse);
    }

    public Collection<InteractorIdCount> searchInteractors(SolrQuery query, String interactorMi, int first, int maxNumberOfIdCount) throws IntactSolrException {
        Multimap<String,InteractorIdCount> interactors = searchInteractors(query, new String[] {interactorMi}, first, maxNumberOfIdCount);
        return interactors.values();
    }

    public Multimap<String,InteractorIdCount> searchInteractors(SolrQuery originalQuery, String[] interactorTypeMis, int first, int maxNumberOfIdCount) throws IntactSolrException {
        SolrQuery query = originalQuery.getCopy();
        query.setRows(0);

        // we allow faceting
        query.setFacet(true);

        // we want all the facet fields with min count = 1. The facet fields with count = 0 are not interesting
        query.setFacetMinCount(1);

        // important optimization. We don't want to return all the fields, only a certain number for pagination
        query.set(FacetParams.FACET_OFFSET, first);
        query.setFacetLimit(maxNumberOfIdCount);

        // we sort the results : the biggest count first
        query.setFacetSort(FacetParams.FACET_SORT_COUNT);

        Multimap<String,InteractorIdCount> interactors = HashMultimap.create();

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
