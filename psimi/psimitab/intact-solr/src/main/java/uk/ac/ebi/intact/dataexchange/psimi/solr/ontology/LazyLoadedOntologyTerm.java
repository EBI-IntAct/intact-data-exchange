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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * A term in an ontology, with parent and children lazy load.
 * When the parents or children are invoked, the data is loaded from the index using an <code>OntologySearcher</code>.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LazyLoadedOntologyTerm implements OntologyTerm, Serializable {

    private final OntologySearcher searcher;

    private final String id;
    private final String name;
    private Set<String> synonymsStr;

    private List<OntologyTerm> parents;
    private List<OntologyTerm> children;
    private Set<OntologyTerm> synonyms;

    public LazyLoadedOntologyTerm(OntologySearcher searcher, String id) throws SolrServerException {
        this( searcher, id, null );
    }

    public LazyLoadedOntologyTerm(OntologySearcher searcher, String id, String name) throws SolrServerException {
        this.searcher = searcher;
        this.id = id;
        this.name = findNameAndSynonyms(searcher, id, name);
    }

    public LazyLoadedOntologyTerm(OntologySearcher searcher, String id, String name, Set<OntologyTerm> synonyms) throws SolrServerException {
        this.searcher = searcher;
        this.id = id;
        this.name = name;
        this.synonyms = synonyms;
    }

    private String findNameAndSynonyms(OntologySearcher searcher, String id, String defaultValue) throws SolrServerException {
        QueryResponse queryResponse = searcher.searchByChildId(id, 0, 1);

        String childName = null;
        if (queryResponse.getResults().getNumFound() > 0) {
            final SolrDocument solrDocument = queryResponse.getResults().iterator().next();
            childName = (String) solrDocument.getFieldValue(OntologyFieldNames.CHILD_NAME);

            Collection<Object> fieldValues = solrDocument.getFieldValues(OntologyFieldNames.CHILDREN_SYNONYMS);

            synonymsStr = new HashSet<String>();
            if(fieldValues != null){
                for (Object fieldValue : fieldValues) {
                    synonymsStr.add(fieldValue.toString());
                }
            }
        }

        return childName == null ? defaultValue : childName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<OntologyTerm> getParents() {
        return getParents(false);
    }

    public List<OntologyTerm> getParents(boolean includeCyclic) {
        if (parents != null) {
            return parents;
        }

        try {
            final QueryResponse queryResponse = searcher.searchByChildId(id, 0, Integer.MAX_VALUE);
            this.parents = new ArrayList<OntologyTerm>( queryResponse.getResults().size() );
            parents.addAll(processParentsHits(queryResponse, id));
        } catch (Exception e) {
            throw new IllegalStateException("Problem getting parents for document: " + id, e);
        }

        return parents;
    }

    public List<OntologyTerm> getChildren() {
        return getChildren(false);
    }

    public List<OntologyTerm> getChildren(boolean includeCyclic) {
        if (children != null) {
            return children;
        }

        this.children = new ArrayList<OntologyTerm>();

        try {
            final QueryResponse queryResponse = searcher.searchByParentId(id, 0, Integer.MAX_VALUE);
            children.addAll(processChildrenHits(queryResponse, id));
        } catch (Exception e) {
            throw new IllegalStateException("Problem getting children for document: "+id, e);
        }

        return children;
    }

    @Override
    public Set<OntologyTerm> getSynonyms() {
        if (synonyms != null) {
            return synonyms;
        }
        
        synonyms = new HashSet<OntologyTerm>();

        if (synonymsStr != null) {
            for (String syn : synonymsStr) {
                try {
                    synonyms.add(new LazyLoadedOntologyTerm(searcher, id, syn, Collections.EMPTY_SET));
                } catch (SolrServerException e) {
                    throw new IllegalStateException("Problem loading synonym: "+syn);
                }
            }
        }
        
        return synonyms;
    }

    private QueryResponse searchQuery(String idFieldName, boolean includeCyclic) throws SolrServerException {

        SolrQuery query = new SolrQuery(idFieldName+":\""+id+"\"");
        query.setRows(Integer.MAX_VALUE);
//        query.addFilterQuery("+"+OntologyFieldNames.CYCLIC+":"+String.valueOf(includeCyclic));
//        query.addFilterQuery("+"+OntologyFieldNames.RELATIONSHIP_TYPE+":\"OBO_REL:is_a\"");
//
//        if (!includeCyclic) {
//            query.addFilterQuery("-"+OntologyFieldNames.RELATIONSHIP_TYPE+":disjoint_from");
//        }

//        query.setSortField(OntologyFieldNames.CHILD_NAME+"_s", SolrQuery.ORDER.asc);

        return searcher.search(query);
    }

    public Set<OntologyTerm> getAllParentsToRoot() {
        return getAllParentsToRoot(this, false);
    }

    @Override
    public Set<OntologyTerm> getAllParentsToRoot(boolean includeSynonyms) {
        return getAllParentsToRoot(this, includeSynonyms);
    }

    protected Set<OntologyTerm> getAllParentsToRoot(OntologyTerm ontologyTerm, boolean includeSynonyms) {
        Set<OntologyTerm> parents = new HashSet<OntologyTerm>();

        for (OntologyTerm parent : ontologyTerm.getParents()) {
            parents.add(parent);

            if (includeSynonyms) {
                Set<OntologyTerm> synonyms = parent.getSynonyms();
                parents.addAll(synonyms);
            }

            parents.addAll(getAllParentsToRoot(parent, includeSynonyms));
        }

        return parents;
    }

    public Collection<OntologyTerm> getChildrenAtDepth(int depth) {
        return getChildren(this, 0, depth).get(depth);
    }

    protected Multimap<Integer, OntologyTerm> getChildren(OntologyTerm term, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) {
            return HashMultimap.create();
        }

        Multimap<Integer,OntologyTerm> terms = HashMultimap.create();
        terms.put(currentDepth, term);

        for (OntologyTerm child : term.getChildren()) {
            terms.putAll(getChildren(child, currentDepth+1, maxDepth));
        }

        return terms;
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
            Set<OntologyTerm> synonyms = new HashSet<OntologyTerm>(fieldValues.size());

            if (parentId != null) {
                for (Object fieldValue : fieldValues) {
                    synonyms.add(new LazyLoadedOntologyTerm(searcher, parentId, fieldValue.toString(), Collections.EMPTY_SET));
                }
            }
            
            if (parentId != null && !processedIds.contains(parentId)) {
                terms.add(newInternalOntologyTerm(searcher, parentId, parentName, synonyms));
                processedIds.add(parentId);
            }
        }

        return terms;
    }

    private List<OntologyTerm> processParentsHits(QueryResponse queryResponse, String id) throws IOException,
                                                                                                 SolrServerException {
        return processOntologyTermHits( queryResponse,
                                        id,
                                        OntologyFieldNames.PARENT_ID,
                                        OntologyFieldNames.PARENT_NAME,
                                        OntologyFieldNames.PARENT_SYNONYMS);
    }

    private List<OntologyTerm> processChildrenHits(QueryResponse queryResponse, String id) throws IOException,
                                                                                                  SolrServerException {
        return processOntologyTermHits( queryResponse,
                                        id,
                                        OntologyFieldNames.CHILD_ID,
                                        OntologyFieldNames.CHILD_NAME,
                                        OntologyFieldNames.CHILDREN_SYNONYMS);
    }

    protected OntologyTerm newInternalOntologyTerm(OntologySearcher searcher,
                                                   String id,
                                                   String name, Set<OntologyTerm> synonyms) throws SolrServerException {
        return new LazyLoadedOntologyTerm( searcher, id, name, synonyms );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyLoadedOntologyTerm that = (LazyLoadedOntologyTerm) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LazyLoadedOntologyTerm");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", parents=").append((parents == null)? "[NOT LOADED]" : parents);
        sb.append(", children=").append((children == null)? "[NOT LOADED]" : children);
        sb.append('}');
        return sb.toString();
    }
}
