package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.FeatureXref;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.util.FeatureUtils;

/**
 * Feature converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class FeatureConverter {

    public Field toCalimocho(Feature feature){
        if (feature != null){
            Field field = new DefaultField();

            if (feature.getCvFeatureType() != null && feature.getCvFeatureType().getFullName() != null){
                String name = feature.getCvFeatureType().getFullName();

                field.set( CalimochoKeys.KEY, name);
                field.set( CalimochoKeys.DB, name);
            }
            else if (feature.getCvFeatureType() != null && feature.getCvFeatureType().getShortLabel() != null){
                String name = feature.getCvFeatureType().getShortLabel();

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
                String rangeAsString = FeatureUtils.convertRangeIntoString(range);
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

            for (FeatureXref refs : feature.getXrefs()){

                if (refs.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equalsIgnoreCase(refs.getCvXrefQualifier().getIdentifier())){
                    if (refs.getPrimaryId() != null){
                        field.set( CalimochoKeys.TEXT, refs.getPrimaryId());
                    }
                }
            }
            return field;
        }

        return null;
    }
}
