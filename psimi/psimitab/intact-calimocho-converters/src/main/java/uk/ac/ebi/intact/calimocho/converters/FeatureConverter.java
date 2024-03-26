package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.jami.model.Range;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

/**
 * Feature converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class FeatureConverter {

    public Field intactToCalimocho(IntactFeatureEvidence feature){
        if (feature != null){
            Field field = new DefaultField();

            if (feature.getType() != null && feature.getType().getFullName() != null){
                String name = feature.getType().getFullName();

                field.set( CalimochoKeys.KEY, name);
                field.set( CalimochoKeys.DB, name);
            }
            else if (feature.getType() != null && feature.getType().getShortName() != null){
                String name = feature.getType().getShortName();

                field.set( CalimochoKeys.KEY, name);
                field.set( CalimochoKeys.DB, name);
            }
            else {
                String name = CrossReferenceConverter.DATABASE_UNKNOWN;

                field.set( CalimochoKeys.KEY, name);
                field.set( CalimochoKeys.DB, name);
            }

            boolean hasRange = false;

            StringBuffer buffer = new StringBuffer();
            for (Range range : feature.getRanges()){
                String rangeAsString = PsimitabTools.convertRangeIntoString(range);
                if (rangeAsString != null && hasRange){
                    buffer.append(",").append(rangeAsString);
                }
                else if (rangeAsString != null && !hasRange){
                    hasRange = true;
                    buffer.append(rangeAsString);
                }
            }

            if (hasRange){
                field.set( CalimochoKeys.VALUE, buffer.toString());
            }
            else {
                field.set( CalimochoKeys.VALUE, "?-?");
            }

            for (Xref refs : feature.getXrefs()){

                if (refs.getQualifier() != null && Xref.IDENTITY_MI.equalsIgnoreCase(refs.getQualifier().getMIIdentifier())){
                    if (refs.getId() != null){
                        field.set( CalimochoKeys.TEXT, refs.getId());
                    }
                }
            }
            return field;
        }

        return null;
    }
}
