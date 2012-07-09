package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;

/**
 * Annotation converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class AnnotationConverter {

    public Field intactToCalimocho(Annotation annot){
        if (annot != null && annot.getCvTopic() != null){
            Field field = new DefaultField();

            String topic = CvTopic.COMMENT;

            if (annot.getCvTopic().getShortLabel() != null){
                topic = annot.getCvTopic().getShortLabel();
            }

            field.set( CalimochoKeys.KEY, topic);
            field.set( CalimochoKeys.NAME, topic);
            field.set( CalimochoKeys.VALUE, annot.getAnnotationText());

            return field;
        }

        return null;
    }
}
