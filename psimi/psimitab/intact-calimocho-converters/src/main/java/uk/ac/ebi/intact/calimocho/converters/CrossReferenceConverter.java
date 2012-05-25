package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.Xref;

/**
 * Converter for cross references
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class CrossReferenceConverter {
    public static String DATABASE_UNKNOWN = "unknown";

    public Field toCalimocho(Xref ref, boolean addTextValue){
        if (ref != null && ref.getPrimaryId() != null){
            Field field = new DefaultField();

            String db = DATABASE_UNKNOWN;
            if (ref.getCvDatabase().getShortLabel() != null){
                db= MitabEscapeUtils.escapeFieldElement(ref.getCvDatabase().getShortLabel());
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, MitabEscapeUtils.escapeFieldElement(ref.getPrimaryId()));

            if (addTextValue) {
                if (ref.getSecondaryId() != null) {
                    field.set( CalimochoKeys.TEXT, ref.getSecondaryId());
                } else if (ref.getCvXrefQualifier() != null && ref.getCvXrefQualifier().getShortLabel() != null) {
                    field.set( CalimochoKeys.TEXT, ref.getCvXrefQualifier().getShortLabel());
                }
            }

            return field;
        }

        return null;
    }
}
