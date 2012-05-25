package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.Annotation;

/**
 * Annotation converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class AnnotationConverter {

    public Field toCalimocho(Annotation annot){
        if (annot != null && annot.getCvTopic() != null && annot.getCvTopic().getShortLabel() != null){
            Field field = new DefaultField();

            String topic = MitabEscapeUtils.escapeFieldElement(annot.getCvTopic().getShortLabel());

            field.set( CalimochoKeys.KEY, topic);
            field.set( CalimochoKeys.NAME, topic);
            field.set( CalimochoKeys.VALUE, MitabEscapeUtils.escapeFieldElement(annot.getAnnotationText()));

            return field;
        }

        return null;
    }
}
