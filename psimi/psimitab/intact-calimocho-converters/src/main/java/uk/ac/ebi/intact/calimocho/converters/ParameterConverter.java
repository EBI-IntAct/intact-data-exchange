package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.model.Parameter;

/**
 * Parameter converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class ParameterConverter {

    public Field toCalimocho(Parameter param){
        if (param != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (param.getCvParameterType() != null && param.getCvParameterType().getShortLabel() != null){
                db= param.getCvParameterType().getShortLabel();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            
            StringBuffer value = new StringBuffer();
            if (param.getFactor() != null && param.getFactor() != 0){
                value.append(param.getFactor()).append("x");
            }
            if (param.getBase() != null && param.getBase() != 0){
                value.append(param.getBase());
            }
            if (param.getExponent() != null && param.getExponent() != 0){
                value.append("exponent ").append(param.getExponent());
            }
            if (param.getUncertainty() != null && param.getUncertainty() != 0){
                value.append("(+/-").append(param.getUncertainty()).append(")");
            }

            if (value.length() == 0){
                 return null;
            }

            field.set( CalimochoKeys.VALUE, value.toString());

            if (param.getCvParameterUnit() != null && param.getCvParameterUnit().getShortLabel() != null){
                field.set( CalimochoKeys.TEXT, param.getCvParameterUnit().getShortLabel());
            }

            return field;
        }

        return null;
    }
}
