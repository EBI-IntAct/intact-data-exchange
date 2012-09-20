package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl;

import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;

import java.util.Collection;

/**
 * This will select only a few confidence scores to index.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/07/12</pre>
 */

public class ConfidenceScoreSelectiveAdder implements RowDataSelectiveAdder {

    private String confidenceName = "intact-miscore";

    public ConfidenceScoreSelectiveAdder(String name){
        if (name != null){
            this.confidenceName = name;
        }
    }

    public boolean addToDoc(SolrInputDocument doc, Row row) {
        Collection<Field> confidences = row.getFields(InteractionKeys.KEY_CONFIDENCE);

        for (Field field : confidences) {
            String type = field.get(CalimochoKeys.DB);

            if (type != null && confidenceName.equalsIgnoreCase(type)){
                try{
                    // only one score is indexed
                    Double value = Double.parseDouble(field.get(CalimochoKeys.VALUE));
                    doc.addField(FieldNames.INTACT_SCORE_NAME, value);
                    return true;
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return false;
    }
}
