package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension;

import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.calimocho.solr.converter.AnnotationFieldConverter;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BaseFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.FieldEnricher;

import java.util.Collection;
import java.util.Set;

/**
 * Enricher for annotation topics.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class AnnotationTopicsToEnrichConverter extends AnnotationFieldConverter {

    private FieldEnricher fieldEnricher;

    public AnnotationTopicsToEnrichConverter(FieldEnricher fieldEnricher){
        if (fieldEnricher == null){
            this.fieldEnricher = new BaseFieldEnricher();
        }
        else {
            this.fieldEnricher = fieldEnricher;
        }
    }

    public SolrInputDocument indexFieldValues(Field field, SolrFieldName fName, SolrInputDocument doc, Set<String> uniques) {

        // index the normal field first
        super.indexFieldValues(field, fName, doc, uniques);

        // enrich with synonyms and parent names plus synonyms
        enrichIndexWithParentsAndSynonyms(field, fName, doc, uniques);

        return doc;
    }

    public void enrichTopic(Field field, SolrFieldName fName, SolrInputDocument doc, Set<String> uniques, String annotationText){
        String name = field.get(CalimochoKeys.TEXT);
        String identifier = field.get(CalimochoKeys.VALUE);
        String nameField = fName.toString();

        if (name != null && !uniques.contains(name)) {
            doc.addField(nameField, name);
            uniques.add(name);

            if (annotationText != null && !uniques.contains(name+":"+annotationText)) {
                doc.addField(nameField, name+":"+annotationText);
                uniques.add(name+":"+annotationText);
            }
        }
        if (identifier != null && !uniques.contains(identifier)) {
            doc.addField(nameField, identifier);
            uniques.add(identifier);

            if (annotationText != null && !uniques.contains(identifier+":"+annotationText)) {
                doc.addField(nameField, identifier+":"+annotationText);
                uniques.add(identifier+":"+annotationText);
            }
        }
    }

    public void enrichIndexWithParentsAndSynonyms(Field field, SolrFieldName name, SolrInputDocument doc, Set<String> uniques){

        try {
            String type = field.get(CalimochoKeys.NAME);
            String value = field.get(CalimochoKeys.VALUE);

            if (type != null){
                Field exact_field = fieldEnricher.findFieldByName(type);

                if (exact_field != null){

                    Collection<Field> parentsAndSynonyms = fieldEnricher.getAllParents(exact_field, true);
                    for (Field parentField : parentsAndSynonyms) {
                        // index parents and synonyms as normal fields. Stored only so don't add the field _s
                        enrichTopic(parentField, name, doc, uniques, value);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossible to enrich " + field.toString(), e);
        }
    }
}
