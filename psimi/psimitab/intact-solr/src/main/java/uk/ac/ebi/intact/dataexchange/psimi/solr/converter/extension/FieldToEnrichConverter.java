package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;
import psidev.psi.mi.calimocho.solr.converter.TextFieldConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BaseFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.FieldEnricher;

import java.util.Collection;
import java.util.Set;

/**
 * xref field converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/07/12</pre>
 */

public class FieldToEnrichConverter extends TextFieldConverter {

    private FieldEnricher fieldEnricher;

    public FieldToEnrichConverter(FieldEnricher fieldEnricher){
        if (fieldEnricher == null){
            this.fieldEnricher = new BaseFieldEnricher();
        }
        else {
            this.fieldEnricher = fieldEnricher;
        }
    }

    @Override
    public void indexFieldValues(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques) {
        // index the normal field first
        super.indexFieldValues(field, name, doc, uniques);

        // enrich with synonyms and parent names plus synonyms
        enrichIndexWithParentsAndSynonyms(field, name, doc, uniques);
    }

    public void indexEnrichedFieldValues(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques) {

        String db = field.get(CalimochoKeys.DB);
        String value = field.get(CalimochoKeys.VALUE);
        String text = field.get(CalimochoKeys.TEXT);
        String nameField = name.toString();

        if (db != null && !uniques.contains(db)){
            doc.addField(nameField, db);
            uniques.add(db);
        }
        if (value != null && !uniques.contains(value)){
            doc.addField(nameField, value);
            uniques.add(value);
        }
        if (db != null && value != null && !uniques.contains(db+":"+value)) {
            doc.addField(nameField, db+":"+value);
            uniques.add(db+":"+value);
        }
        if (text != null && !uniques.contains(text)){
            doc.addField(nameField, text);
            uniques.add(text);
        }
    }

    public void enrichIndexWithParentsAndSynonyms(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques){
        String type = field.get(CalimochoKeys.DB);
        if (fieldEnricher.isExpandableOntology(type)) {

            try {
                Collection<Field> parentsAndSynonyms = fieldEnricher.getAllParents(field, true);
                for (Field parentField : parentsAndSynonyms) {
                    // index parents and synonyms as normal fields. Stored only so don't add the field _s
                    indexEnrichedFieldValues(parentField, name, doc, uniques);
                }
            } catch (SolrServerException e) {
                e.printStackTrace();
            }
        }
    }
}
