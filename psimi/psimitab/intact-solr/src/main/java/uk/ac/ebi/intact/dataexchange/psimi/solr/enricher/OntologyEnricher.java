package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;

/**
 * Interface for ontology enrichers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/11/13</pre>
 */

public interface OntologyEnricher {

    boolean isExpandableOntology( String name );

    OntologyTerm findOntologyTerm(String id, String name) throws SolrServerException;

    OntologyTerm findOntologyTermByName(String name) throws SolrServerException;
}
