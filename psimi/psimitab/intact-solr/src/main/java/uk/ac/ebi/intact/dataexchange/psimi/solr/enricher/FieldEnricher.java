package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import psidev.psi.mi.tab.model.builder.Field;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;

/**
 * Definition of a field enricher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface FieldEnricher {
    Field enrich(Field field) throws Exception;

    boolean isExpandableOntology( String name );

    Collection<Field> getAllParents(Field field, boolean includeItself) throws SolrServerException;
}
