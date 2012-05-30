package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.Confidence;

/**
 * Confidence converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/05/12</pre>
 */

public class ConfidenceConverter {

    public Field toCalimocho(Confidence conf){
        if (conf != null && conf.getValue() != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (conf.getCvConfidenceType() != null && conf.getCvConfidenceType().getShortLabel() != null){
                db= conf.getCvConfidenceType().getShortLabel();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, conf.getValue());

            return field;
        }

        return null;
    }
}
