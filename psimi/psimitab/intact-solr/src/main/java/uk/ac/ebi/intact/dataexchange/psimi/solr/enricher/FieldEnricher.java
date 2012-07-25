package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.model.Field;

import java.util.Collection;

/**
 * Definition of a field enricher.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface FieldEnricher {

    Field findFieldByName(String name) throws Exception;
    Field enrich(Field field) throws Exception;

    boolean isExpandableOntology( String name );

    Collection<Field> getAllParents(Field field, boolean includeItself) throws SolrServerException;
}
