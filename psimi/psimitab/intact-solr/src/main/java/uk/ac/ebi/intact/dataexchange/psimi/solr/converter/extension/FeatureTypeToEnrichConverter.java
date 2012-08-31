package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension;

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
 * Enricher for feature types which is slightly different from the other enrichers as it only enrich the db element in MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class FeatureTypeToEnrichConverter extends TextFieldConverter {

    private FieldEnricher fieldEnricher;

    public FeatureTypeToEnrichConverter(FieldEnricher fieldEnricher){
        if (fieldEnricher == null){
            this.fieldEnricher = new BaseFieldEnricher();
        }
        else {
            this.fieldEnricher = fieldEnricher;
        }
    }

    @Override
    public SolrInputDocument indexFieldValues(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques) {
        // index the normal field first
        super.indexFieldValues(field, name, doc, uniques);

        // enrich with synonyms and parent names plus synonyms
        enrichIndexWithParentsAndSynonyms(field, name, doc, uniques);

        return doc;
    }

    public void enrichIndexWithParentsAndSynonyms(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques){

        try {
            String type = field.get(CalimochoKeys.DB);

            if (type != null){
                Field exact_field = fieldEnricher.findFieldByName(type);
                String nameField = name.toString();

                if (exact_field != null){

                    Collection<Field> parentsAndSynonyms = fieldEnricher.getAllParents(exact_field, true);
                    for (Field parentField : parentsAndSynonyms) {
                        // index parents and synonyms as normal fields. Stored only so don't add the field _s
                        String text = parentField.get(CalimochoKeys.TEXT);
                        String value = parentField.get(CalimochoKeys.VALUE);

                        if (text != null && !uniques.contains(text)){
                            doc.addField(nameField, text);
                            uniques.add(text);
                        }
                        if (value != null && !uniques.contains(value)){
                            doc.addField(nameField, value);
                            uniques.add(value);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossible to enrich " + field.toString(), e);
        }
    }
}
