package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.jami.model.Annotation;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAnnotation;

/**
 * Annotation converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class AnnotationConverter {

    public Field intactToCalimocho(AbstractIntactAnnotation annot){
        if (annot != null && annot.getTopic() != null){
            Field field = new DefaultField();

            String topic = Annotation.COMMENT;

            if (annot.getTopic().getShortName() != null){
                topic = annot.getTopic().getShortName();
            }

            field.set( CalimochoKeys.KEY, topic);
            field.set( CalimochoKeys.NAME, topic);
            field.set( CalimochoKeys.VALUE, annot.getValue());

            return field;
        }

        return null;
    }
}
