package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.ParameterImpl;
import uk.ac.ebi.intact.model.Parameter;

/**
 * This class allows to convert a Intact parameter to a MITAB parameter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class ParameterConverter {

    /**
     *
     * @param param : the intact parameter
     * @return a calimocho field tha represents the interaction parameter
     */
    public psidev.psi.mi.tab.model.Parameter intactToMitab(Parameter param){
        if (param != null){

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (param.getCvParameterType() != null && param.getCvParameterType().getShortLabel() != null){
                db= param.getCvParameterType().getShortLabel();
            }

            StringBuffer value = new StringBuffer();
            if (param.getFactor() != null && param.getFactor() != 0){
                value.append(param.getFactor()).append("x");
            }
            if (param.getBase() != null && param.getBase() != 0){
                value.append(param.getBase());
            }
            if (param.getExponent() != null && param.getExponent() != 0){
                value.append("^").append(param.getExponent());
            }
            if (param.getUncertainty() != null && param.getUncertainty() != 0){
                value.append(" ~").append(param.getUncertainty());
            }

            if (value.length() == 0){
                return null;
            }

            psidev.psi.mi.tab.model.Parameter mitabParameter = new ParameterImpl(db, value.toString());

            if (param.getCvParameterUnit() != null && param.getCvParameterUnit().getShortLabel() != null){
                mitabParameter.setUnit(param.getCvParameterUnit().getShortLabel());
            }

            return mitabParameter;
        }

        return null;
    }
}
