package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class for OntologyEnricher
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/11/13</pre>
 */

public abstract class AbstractOntologyEnricher implements OntologyEnricher {
    private static final Log log = LogFactory.getLog(AbstractOntologyEnricher.class);

    public OntologySearcher ontologySearcher;

    private Map<String, Collection<Field>> cvCache;
    private Map<String,OntologyTerm> ontologyTermCache;

    private Set<String> expandableOntologies;
    private Set<String> ontologyTermsToIgnore;

    public AbstractOntologyEnricher(OntologySearcher ontologySearcher) {
        super();
        this.ontologySearcher = ontologySearcher;

        cvCache = new LRUMap(50000);
        ontologyTermCache = new LRUMap(10000);

        ontologyTermsToIgnore = new HashSet<String>();
    }

    protected void initializeOntologyTermsToIgnore(){
        // molecular interaction is root term for psi mi
        ontologyTermsToIgnore.add("MI:0000");
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

    protected OntologySearcher getOntologySearcher() {
        return ontologySearcher;
    }

    protected Map<String, Collection<Field>> getCvCache() {
        return cvCache;
    }

    protected Map<String, OntologyTerm> getOntologyTermCache() {
        return ontologyTermCache;
    }

    protected Set<String> getExpandableOntologies() {
        return expandableOntologies;
    }

    protected Set<String> getOntologyTermsToIgnore() {
        return ontologyTermsToIgnore;
    }
}
