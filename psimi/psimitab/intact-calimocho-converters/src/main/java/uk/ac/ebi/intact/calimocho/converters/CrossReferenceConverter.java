package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactXref;

/**
 * Converter for cross references
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class CrossReferenceConverter<T extends AbstractIntactXref> {
    public static String DATABASE_UNKNOWN = "unknown";

    public Field intactToCalimocho(T ref, boolean addTextValue){
        if (ref != null && ref.getId() != null){
            Field field = new DefaultField();

            String db = DATABASE_UNKNOWN;
            if (ref.getDatabase().getShortName() != null){
                db= ref.getDatabase().getShortName();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, ref.getId());

            if (addTextValue) {
                if (ref.getSecondaryId() != null) {
                    field.set( CalimochoKeys.TEXT, ref.getSecondaryId());
                } else if (ref.getQualifier() != null && ref.getQualifier().getShortName() != null) {
                    field.set( CalimochoKeys.TEXT, ref.getQualifier().getShortName());
                }
            }

            return field;
        }

        return null;
    }
}
