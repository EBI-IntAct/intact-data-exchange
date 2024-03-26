package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactParameter;

/**
 * Parameter converter : converts interaction parameters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class ParameterConverter {

    /**
     *
     * @param param : the intact parameter
     * @return a calimocho field tha represents the interaction parameter
     */
    public Field intactToCalimocho(AbstractIntactParameter param){
        if (param != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (param.getType() != null && param.getType().getShortName() != null){
                db= param.getType().getShortName();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            
            StringBuffer value = new StringBuffer();
            if (param.getFactor() != 0){
                value.append(param.getFactor()).append("x");
            }
            if (param.getBase() != 0){
                value.append(param.getBase());
            }
            if (param.getExponent() != 0){
                value.append("^").append(param.getExponent());
            }
            if (param.getUncertainty() != null && param.getUncertainty().intValue() != 0){
                value.append(" ~").append(param.getUncertainty());
            }

            if (value.length() == 0){
                 return null;
            }

            field.set( CalimochoKeys.VALUE, value.toString());

            if (param.getUnit() != null && param.getUnit().getShortName() != null){
                field.set( CalimochoKeys.TEXT, param.getUnit().getShortName());
            }

            return field;
        }

        return null;
    }
}
