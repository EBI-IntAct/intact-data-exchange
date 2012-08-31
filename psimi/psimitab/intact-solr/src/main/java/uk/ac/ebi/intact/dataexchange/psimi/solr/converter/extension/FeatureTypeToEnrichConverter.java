package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension;

import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldConverter;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;
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

public class FeatureTypeToEnrichConverter implements SolrFieldConverter{

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
        String db = field.get(CalimochoKeys.DB);
        String nameField = name.toString();

        if (db != null && !uniques.contains(db)){
            doc.addField(nameField, db);
            doc.addField(nameField+"_s", db);
            uniques.add(db);
        }

        // enrich with synonyms and parent names plus synonyms
        enrichIndexWithParentsAndSynonyms(field, name, doc, uniques);

        return doc;
    }

    public void enrichIndexWithParentsAndSynonyms(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques){

        try {
            String type = field.get(CalimochoKeys.DB);

            if (type != null){
                Field exact_field = fieldEnricher.findFieldByName(type);

                if (exact_field != null){
                    Collection<Field> parentsAndSynonyms = fieldEnricher.getAllParents(exact_field, true);
                    for (Field parentField : parentsAndSynonyms) {
                        // index parents and synonyms as normal fields. Stored only so don't add the field _s
                        String text = parentField.get(CalimochoKeys.TEXT);
                        String value = parentField.get(CalimochoKeys.VALUE);
                        String nameField = name.toString();

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
