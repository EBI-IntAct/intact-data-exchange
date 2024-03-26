package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactConfidence;

/**
 * Confidence converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/05/12</pre>
 */

public class ConfidenceConverter {

    public Field intactToCalimocho(AbstractIntactConfidence conf){
        if (conf != null && conf.getValue() != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (conf.getType() != null && conf.getType().getShortName() != null){
                db= conf.getType().getShortName();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, conf.getValue());

            return field;
        }

        return null;
    }
}
