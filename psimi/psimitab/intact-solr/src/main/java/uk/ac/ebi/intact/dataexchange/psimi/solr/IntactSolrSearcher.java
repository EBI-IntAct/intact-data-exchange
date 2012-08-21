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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to simplify searching.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrSearcher extends PsicquicSolrServer{

    public IntactSolrSearcher(SolrServer solrServer) {
        super(solrServer);
    }

    public IntactSolrSearchResult search(SolrQuery query) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.search(query, RETURN_TYPE_MITAB27);
    }

    public IntactSolrSearchResult search(SolrQuery query, String returnType) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.search(query, returnType);
    }

    public IntactSolrSearchResult search(String q, Integer firstResult, Integer maxResults, String returnType) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.searchWithFilters(q, firstResult, maxResults, returnType, null);
    }

    public IntactSolrSearchResult search(String q, String returnType) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.searchWithFilters(q, null, null, returnType, null);
    }

    public IntactSolrSearchResult search(String q, Integer firstResult, Integer maxResults) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.searchWithFilters(q, firstResult, maxResults, RETURN_TYPE_MITAB27, null);
    }

    public IntactSolrSearchResult search(String q) throws PsicquicSolrException, SolrServerException {

        return (IntactSolrSearchResult) super.searchWithFilters(q, null, null, RETURN_TYPE_MITAB27, null);
    }

    @Override
    protected IntactSolrSearchResult createMitabResultsForType(SolrDocumentList docList, String mitabType, List<FacetField> facetFields) throws PsicquicSolrException {

        String [] fieldNames = solrFields.get(mitabType);

        if (fieldNames == null){
            throw new PsicquicSolrException("The format " + mitabType + " is not a recognised MITAB format");
        }

        IntactSolrSearchResult searchResults = new IntactSolrSearchResult(docList, fieldNames);
        return searchResults;
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
