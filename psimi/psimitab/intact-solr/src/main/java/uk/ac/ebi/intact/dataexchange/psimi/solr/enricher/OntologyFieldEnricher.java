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
import psidev.psi.mi.tab.model.builder.Field;
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

    public OntologyFieldEnricher(OntologySearcher ontologySearcher) {
        super();
        this.ontologySearcher = ontologySearcher;

        cvCache = new LRUMap(50000);
        ontologyTermCache = new LRUMap(10000);
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

        final OntologyTerm ontologyTerm = findOntologyTerm(field);

        if (ontologyTerm == null) return field;

        return convertTermToField( field.getType(), ontologyTerm );
    }

    /**
     * @param field         the field for which we want to get the parents
     * @param includeItself if true, the passed field will be part of the collection (its description updated from the index)
     * @return list of cv terms with parents and itself
     */
    public Collection<Field> getAllParents(psidev.psi.mi.tab.model.builder.Field field, boolean includeItself) throws SolrServerException {
        if (ontologySearcher == null) {
            return Collections.EMPTY_LIST;
        }

        List<psidev.psi.mi.tab.model.builder.Field> allParents = null;

        final String type = field.getType();

        String identifier = field.getValue();

        if (cvCache.containsKey(identifier)) {
            return cvCache.get(identifier);
        }

        // fetch parents and fill the field list
        final OntologyTerm ontologyTerm = findOntologyTerm(field);
        final Set<OntologyTerm> parents = ontologyTerm.getAllParentsToRoot();

        allParents = convertTermsToFields(type, parents);

        if (includeItself) {
            Field updatedItself = convertTermToField(type, ontologyTerm);
            allParents.add(updatedItself);
        }

        cvCache.put(identifier, allParents);

        return (allParents != null ? allParents : Collections.EMPTY_LIST);
    }
    
    public OntologyTerm findOntologyTerm(Field field) throws SolrServerException {
        return findOntologyTerm(field.getValue(), field.getDescription());
    }

    public OntologyTerm findOntologyTerm(String id, String name) throws SolrServerException {
        if (ontologySearcher == null) {
            return null;
        }

        if (ontologyTermCache.containsKey(id)) {
            return ontologyTermCache.get(id);
        }

        final LazyLoadedOntologyTerm term = new LazyLoadedOntologyTerm(ontologySearcher, id, name);

        ontologyTermCache.put( id, term );

        return term;
    }

    private List<psidev.psi.mi.tab.model.builder.Field> convertTermsToFields( String type, Set<OntologyTerm> terms ) {
        List<psidev.psi.mi.tab.model.builder.Field> fields =
                new ArrayList<psidev.psi.mi.tab.model.builder.Field>( terms.size());

        for ( OntologyTerm term : terms ) {
            Field field = convertTermToField(type, term);
            fields.add( field );
        }

        return fields;
    }

    private Field convertTermToField(String type, OntologyTerm term) {
        return new Field( type, term.getId(), term.getName() );
    }
}