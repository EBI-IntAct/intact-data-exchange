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
public interface FieldEnricher extends OntologyEnricher{

    Field findFieldByName(String name) throws Exception;

    Collection<Field> getAllParents(Field field, boolean includeItself) throws SolrServerException;

    Field enrich(Field object) throws Exception;
}
