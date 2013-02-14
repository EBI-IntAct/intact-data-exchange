package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.Field;

import java.util.Comparator;

/**
 * Comparator to compare fields
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/02/13</pre>
 */

public class FieldValueComparator implements Comparator<Field> {
    @Override
    public int compare(Field field, Field field2) {
        int EQUAL = 0;
        int BEFORE = -1;
        int AFTER = 1;

        if (field == null && field == null){
            return EQUAL;
        }
        else if (field == null){
            return AFTER;
        }
        else if (field2 == null){
            return BEFORE;
        }
        else {
            String value = field.get(CalimochoKeys.VALUE);
            String value2 = field2.get(CalimochoKeys.VALUE);

            if (value == null && value == null){
                return EQUAL;
            }
            else if (value == null){
                return AFTER;
            }
            else if (value2 == null){
                return BEFORE;
            }
            else {
                return value.compareTo(value2);
            }
        }
    }
}
