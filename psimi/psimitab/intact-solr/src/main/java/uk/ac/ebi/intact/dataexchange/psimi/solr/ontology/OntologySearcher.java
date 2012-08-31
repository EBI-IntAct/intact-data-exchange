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

import org.apache.commons.collections.map.LRUMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import uk.ac.ebi.intact.bridges.ontologies.FieldName;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException;

import java.io.Serializable;
import java.util.*;

/**
 * Searches the ontology, using a SolrServer pointing to an ontology core.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologySearcher implements Serializable {

    private static final int CHILDREN_CACHE_SIZE = 10000;
    private static final int PARENTS_CACHE_SIZE = 10000;

    private final SolrServer solrServer;

    private final Map<String,List<OntologyTerm>> childSearchesMap;
    private final Map<String,List<OntologyTerm>> parentSearchesMap;
    private final Map<String,OntologyNames> synonymsSearchesMap;

    private Set<String> ontologyNames;

    public  OntologySearcher(SolrServer solrServer) {
        if ( solrServer == null ) {
            throw new IllegalArgumentException( "You must give a non null solrServer" );
        }

        this.solrServer = solrServer;

        childSearchesMap = new LRUMap( CHILDREN_CACHE_SIZE );
        parentSearchesMap = new LRUMap( PARENTS_CACHE_SIZE );
        synonymsSearchesMap = new LRUMap( PARENTS_CACHE_SIZE );
    }

    public OntologyNames findNameAndSynonyms(String termId, String termName) throws SolrServerException {
        OntologyNames names = null;

        if (termId != null){
            names = searchOntologyNamesByChildId(termId, 0, 1);
        }
        else if (termName != null){
            names = searchOntologyNamesByChildName(termName, 0, 1);
        }

        return names;
    }

    public OntologyNames searchOntologyNamesByChildId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchAndCacheOntologyNamesById(id, firstResult, maxResults, OntologyFieldNames.CHILD_ID);
    }

    public OntologyNames searchOntologyNamesByChildName(String name, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchAndCacheOntologyNamesByName(name, firstResult, maxResults, OntologyFieldNames.CHILD_NAME);
    }

    private OntologyNames searchAndCacheOntologyNamesById(String id, Integer firstResult, Integer maxResults,
                                                          final String fieldId) throws SolrServerException {
        // We need to include the maxResult in the key as we do different maxResult for
        // the same term, hence we could end up getting the wrong number of term.
        final String key = id + maxResults;

        if (synonymsSearchesMap.containsKey(key)) {
            return synonymsSearchesMap.get(key);
        }

        OntologyNames names = searchOntologyNamesById( fieldId, id, firstResult, maxResults);

        synonymsSearchesMap.put(key, names);

        return names;
    }

    private OntologyNames searchAndCacheOntologyNamesByName(String name, Integer firstResult, Integer maxResults, final String fieldId) throws SolrServerException {
        // We need to include the maxResult in the key as we do different maxResult for
        // the same term, hence we could end up getting the wrong number of term.
        final String key = name + maxResults;

        if (synonymsSearchesMap.containsKey(key)) {
            return synonymsSearchesMap.get(key);
        }

        OntologyNames names = searchOntologyNamesByName( fieldId, name, firstResult, maxResults);

        synonymsSearchesMap.put(key, names);

        return names;
    }

    private OntologyNames searchOntologyNamesById(String fieldId, String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        SolrQuery query = new SolrQuery(fieldId + ":\"" + id + "\"");

        if (firstResult != null) query.setStart(firstResult);
        if (maxResults != null) query.setRows(maxResults);

        QueryResponse response = search(query, new String[] {OntologyFieldNames.CHILD_NAME, OntologyFieldNames.CHILDREN_SYNONYMS});

        return extractNamesAndSynonymsFrom(response);
    }

    private OntologyNames searchOntologyNamesByName(String fieldId, String name, Integer firstResult, Integer maxResults) throws SolrServerException {
        SolrQuery query = new SolrQuery(fieldId + ":\"" + name + "\"");

        if (firstResult != null) query.setStart(firstResult);
        if (maxResults != null) query.setRows(maxResults);

        QueryResponse response = search(query, new String[] {OntologyFieldNames.CHILD_NAME, OntologyFieldNames.CHILDREN_SYNONYMS});

        return extractNamesAndSynonymsFrom(response);
    }

    private OntologyNames extractNamesAndSynonymsFrom(QueryResponse response) {
        if (response.getResults().getNumFound() > 0) {
            final SolrDocument solrDocument = response.getResults().iterator().next();
            String childName = (String) solrDocument.getFieldValue(OntologyFieldNames.CHILD_NAME);

            OntologyNames ontologyNames = new OntologyNames(childName, (String) solrDocument.getFieldValue(OntologyFieldNames.CHILD_ID));

            Collection<Object> fieldValues = solrDocument.getFieldValues(OntologyFieldNames.CHILDREN_SYNONYMS);

            Set<String> synonymsStr = ontologyNames.getSynonyms();
            if(fieldValues != null){
                for (Object fieldValue : fieldValues) {
                    synonymsStr.add(fieldValue.toString());
                }
            }

            return ontologyNames;
        }

        return null;
    }

    public QueryResponse search(SolrQuery query, String [] fieldNames) throws SolrServerException{

        if (fieldNames != null && fieldNames.length > 0){
            query.setFields(fieldNames);
        }

        // order by unique id
        query.addSortField(OntologyFieldNames.ID, SolrQuery.ORDER.asc);

        return solrServer.query(query);
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
    public List<OntologyTerm> searchByChildId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchAndCacheById(id, firstResult, maxResults, OntologyFieldNames.CHILD_ID, childSearchesMap);
    }

    private List<OntologyTerm> searchAndCacheById(String id, Integer firstResult, Integer maxResults,
                                                  final String fieldId, Map<String, List<OntologyTerm>> cache) throws SolrServerException {
        // We need to include the maxResult in the key as we do different maxResult for
        // the same term, hence we could end up getting the wrong number of term.
        final String key = id + maxResults;

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        List<OntologyTerm> terms = searchById( fieldId, id, firstResult, maxResults);

        cache.put(key, terms);

        return terms;
    }

    private List<OntologyTerm> searchById(String fieldId, String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        SolrQuery query = new SolrQuery(fieldId + ":\"" + id + "\"");

        if (firstResult != null) query.setStart(firstResult);
        if (maxResults != null) query.setRows(maxResults);

        if (FieldName.CHILDREN_ID.equalsIgnoreCase(fieldId)){
            return processParentsHits(search(query,
                    new String[] {OntologyFieldNames.PARENT_ID, OntologyFieldNames.PARENT_NAME, OntologyFieldNames.PARENT_SYNONYMS}), id);
        }
        else if (FieldName.PARENT_ID.equalsIgnoreCase(fieldId)) {
            return processChildrenHits(search(query,
                    new String[] {OntologyFieldNames.CHILD_ID, OntologyFieldNames.CHILD_NAME, OntologyFieldNames.CHILDREN_SYNONYMS}), id);
        }
        else {
            return processParentsHits(search(query,
                    new String[] {OntologyFieldNames.PARENT_ID, OntologyFieldNames.PARENT_NAME, OntologyFieldNames.PARENT_SYNONYMS}), id);
        }
    }

    private List<OntologyTerm> processParentsHits(QueryResponse queryResponse, String id) throws SolrServerException {
        return processOntologyTermHits( queryResponse,
                id,
                OntologyFieldNames.PARENT_ID,
                OntologyFieldNames.PARENT_NAME,
                OntologyFieldNames.PARENT_SYNONYMS);
    }

    private List<OntologyTerm> processChildrenHits(QueryResponse queryResponse, String id) throws SolrServerException {
        return processOntologyTermHits( queryResponse,
                id,
                OntologyFieldNames.CHILD_ID,
                OntologyFieldNames.CHILD_NAME,
                OntologyFieldNames.CHILDREN_SYNONYMS);
    }

    private List<OntologyTerm> processOntologyTermHits( final QueryResponse queryResponse,
                                                        final String id,
                                                        final String termIdKey,
                                                        final String termNameKey,
                                                        final String termSynonymsKey) throws SolrServerException {

        final SolrDocumentList results = queryResponse.getResults();
        List<OntologyTerm> terms = new ArrayList<OntologyTerm>( results.size() );

        List<String> processedIds = new ArrayList<String>( results.size() + 1 );
        processedIds.add(id);

        for ( SolrDocument solrDocument : results ) {

            String parentId = (String) solrDocument.getFieldValue( termIdKey );
            String parentName = (String) solrDocument.getFieldValue( termNameKey );

            Collection<Object> fieldValues = solrDocument.getFieldValues(termSynonymsKey);
            Set<OntologyTerm> synonyms = new HashSet<OntologyTerm>();

            if (parentId != null && fieldValues != null) {
                for (Object fieldValue : fieldValues) {
                    synonyms.add(new LazyLoadedOntologyTerm(this, parentId, fieldValue.toString(), Collections.EMPTY_SET));
                }
            }

            if (parentId != null && !processedIds.contains(parentId)) {
                terms.add(newInternalOntologyTerm(parentId, parentName, synonyms));
                processedIds.add(parentId);
            }
        }

        return terms;
    }

    private OntologyTerm newInternalOntologyTerm(String id,
                                                 String name, Set<OntologyTerm> synonyms) throws SolrServerException {
        return new LazyLoadedOntologyTerm( this, id, name, synonyms );
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
    public List<OntologyTerm> searchByParentId(String id, Integer firstResult, Integer maxResults) throws SolrServerException {
        return searchAndCacheById(id, firstResult, maxResults, OntologyFieldNames.PARENT_ID, parentSearchesMap);
    }

    public Set<String> getOntologyNames() throws SolrServerException {
        if (ontologyNames == null) {
            SolrQuery query = new SolrQuery("*:*");
            query.setStart(0);
            query.setRows(0);

            // prepare faceting
            query.setFacet(true);
            query.setParam(FacetParams.FACET_FIELD, "ontology");
            query.setFacetLimit(10);
            query.setFacetMinCount(1);
            query.set(FacetParams.FACET_OFFSET, 0);

            // order by unique id
            query.addSortField(OntologyFieldNames.ID, SolrQuery.ORDER.asc);

            QueryResponse response = search(query, null);
            FacetField facetField = response.getFacetField("ontology");

            if (facetField.getValues() == null) {
                return Collections.EMPTY_SET;
            }

            ontologyNames = new HashSet<String>( facetField.getValues().size() );

            for (FacetField.Count c : facetField.getValues()) {
                ontologyNames.add(c.getName());
            }
        }

        return ontologyNames;
    }

    public long countAllDocuments(){
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);

        try {
            return solrServer.query(query).getResults().getNumFound();
        } catch (SolrServerException e) {
            throw new IntactSolrException("Problem searching with query: "+query, e);
        }
    }

    public void shutdown(){

        if (this.solrServer != null && this.solrServer instanceof HttpSolrServer){
            HttpSolrServer httpsolrServer = (HttpSolrServer) solrServer;
            httpsolrServer.shutdown();
        }
    }
}
