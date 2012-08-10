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
package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.util.*;

/**
 * Ontology field enricher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyFieldEnricher extends BaseFieldEnricher {

    private static final Log log = LogFactory.getLog( OntologyFieldEnricher.class );

    public final OntologySearcher ontologySearcher;

    private final Map<String, Collection<Field>> cvCache;
    private final Map<String,OntologyTerm> ontologyTermCache;

    private Set<String> expandableOntologies;
    private Set<String> ontologyTermsToIgnore;

    public OntologyFieldEnricher(OntologySearcher ontologySearcher) {
        super();
        this.ontologySearcher = ontologySearcher;

        cvCache = new LRUMap(50000);
        ontologyTermCache = new LRUMap(10000);

        ontologyTermsToIgnore = new HashSet<String>();
    }

    protected void initializeOntologyTermsToIgnore(){
        // molecular interaction is root term for psi mi
        ontologyTermsToIgnore.add("MI:0000");
        // curation content is root term for some topics of publication
        ontologyTermsToIgnore.add("MI:1045");
        // attribut name is root term for cv topic
        ontologyTermsToIgnore.add("MI:0590");
        // biological role is root term for biological role
        ontologyTermsToIgnore.add("MI:0500");
        // curation quality is root term for some topics of publication
        ontologyTermsToIgnore.add("MI:0954");
        // feature type is root term for feature type
        ontologyTermsToIgnore.add("MI:0116");
        // interaction detection method is root term for interaction detection method
        ontologyTermsToIgnore.add("MI:0001");
        // interaction type is root term for interaction type
        ontologyTermsToIgnore.add("MI:0190");
        // interactor type is root term for interactor type
        ontologyTermsToIgnore.add("MI:0313");
        // participant detection method is root term for participant detection method
        ontologyTermsToIgnore.add("MI:0002");

    }

    @Override
    public boolean isExpandableOntology( final String name ) {
        if (expandableOntologies == null) {
            if (ontologySearcher == null) {
                expandableOntologies = new HashSet<String>();
            } else {
                try {
                    expandableOntologies = ontologySearcher.getOntologyNames();
                } catch (SolrServerException e) {
                    if (log.isErrorEnabled()) log.error("Problem getting list of ontology names: " +e.getMessage(), e);
                    return false;
                }
                if (expandableOntologies.contains("uniprot taxonomy")) {
                    expandableOntologies.add("taxid");
                }
            }
        }

        return expandableOntologies.contains(name);
    }

    public Field enrich(Field field) throws Exception {
        if (field == null) return null;

        String value = field.get(CalimochoKeys.VALUE);

        if (ontologyTermsToIgnore.contains(value)){
            return null;
        }

        final OntologyTerm ontologyTerm = findOntologyTerm(field);

        if (ontologyTerm == null) return field;

        return convertTermToField( field.get(CalimochoKeys.DB), ontologyTerm );
    }

    public Field findFieldByName(String name) throws Exception{
        if (name == null) return null;

        if (ontologyTermsToIgnore.contains(name)){
            return null;
        }

        final OntologyTerm ontologyTerm = findOntologyTermByName(name);

        if (ontologyTerm == null) return null;

        return convertTermToField( name, ontologyTerm );
    }

    /**
     * @param field         the field for which we want to get the parents
     * @param includeItself if true, the passed field will be part of the collection (its description updated from the index)
     * @return list of cv terms with parents and itself
     */
    public Collection<Field> getAllParents(Field field, boolean includeItself) throws SolrServerException {
         return getAllParents(field, includeItself, true);
    }

    /**
     * @param field         the field for which we want to get the parents
     * @param includeItself if true, the passed field will be part of the collection (its description updated from the index)
     * @return list of cv terms with parents and itself
     */
    public Collection<Field> getAllParents(Field field, boolean includeItself, boolean includeSynonyms) throws SolrServerException {
        if (ontologySearcher == null) {
            return Collections.EMPTY_LIST;
        }

        List<Field> allParents = null;

        final String type = field.get(CalimochoKeys.DB);

        String identifier = field.get(CalimochoKeys.VALUE);

        if (ontologyTermsToIgnore.contains(identifier)){
            return Collections.EMPTY_LIST;
        }

        if (cvCache.containsKey(identifier)) {
            return cvCache.get(identifier);
        }

        // fetch parents and fill the field list
        final OntologyTerm ontologyTerm = findOntologyTerm(field);
        final Set<OntologyTerm> parents = ontologyTerm.getAllParentsToRoot(includeSynonyms);

        allParents = convertTermsToFieldsIncludingSynonyms(type, parents);

        if (includeItself) {
            Collection<Field> itselfAndSynonyms = convertTermToFieldIncludingSynonyms(type, ontologyTerm);
            allParents.addAll(itselfAndSynonyms);
        }

        cvCache.put(identifier, allParents);

        return (allParents != null ? allParents : Collections.EMPTY_LIST);
    }
    
    public OntologyTerm findOntologyTerm(Field field) throws SolrServerException {

        String value = field.get(CalimochoKeys.VALUE);

        return findOntologyTerm(value, field.get(CalimochoKeys.TEXT));
    }

    public OntologyTerm findOntologyTermByName(String name) throws SolrServerException {
        if (ontologySearcher == null) {
            return null;
        }

        String cacheKey = "_"+name;

        if (ontologyTermCache.containsKey(cacheKey)) {
            return ontologyTermCache.get(cacheKey);
        }

        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm(ontologySearcher, null, name);

        ontologyTermCache.put( cacheKey, term );

        return term;
    }

    public OntologyTerm findOntologyTerm(String id, String name) throws SolrServerException {
        if (ontologySearcher == null) {
            return null;
        }
        
        String cacheKey = id+"_"+name;

        if (ontologyTermCache.containsKey(cacheKey)) {
            return ontologyTermCache.get(cacheKey);
        }

        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm(ontologySearcher, id, name);

        ontologyTermCache.put( cacheKey, term );

        return term;
    }

    private List<Field> convertTermsToFieldsIncludingSynonyms(String type, Set<OntologyTerm> terms) {
        List<Field> fields = new ArrayList<Field>();

        if (terms != null) {
            for ( OntologyTerm term : terms ) {

                if (!ontologyTermsToIgnore.contains(term.getId())){
                    Collection<Field> fieldsWithSynonyms = convertTermToFieldIncludingSynonyms(type, term);
                    fields.addAll( fieldsWithSynonyms );
                }
            }
        }

        return fields;
    }

    private Field convertTermToField(String type, OntologyTerm term) {
        Field field = new DefaultField( );

        field.set(CalimochoKeys.DB, type);
        field.set(CalimochoKeys.VALUE, term.getId());
        field.set(CalimochoKeys.TEXT, term.getName());

        return field;
    }

    private Collection<Field> convertTermToFieldIncludingSynonyms(String type, OntologyTerm term) {
        Collection<Field> fields = new ArrayList<Field>();

        fields.add(convertTermToField(type, term));
        
        for (OntologyTerm synonymField : term.getSynonyms()) {
            fields.add(convertTermToField(type, synonymField));
        }

        return fields;
    }

    public Set<String> getOntologyTermsToIgnore() {
        return ontologyTermsToIgnore;
    }
}