package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Confidence;
import uk.ac.ebi.intact.model.CvConfidenceType;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.util.XrefUtils;

/**
 * Confidence converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/05/12</pre>
 */

public class ConfidenceConverter {

    public final static String CONFIDENCE = "confidence";
    public final static String CONFIDENCE_MI = "MI:1064";

    public Field intactToCalimocho(Confidence conf){
        if (conf != null && conf.getValue() != null){
            Field field = new DefaultField();

            String db = CrossReferenceConverter.DATABASE_UNKNOWN;
            if (conf.getCvConfidenceType() != null && conf.getCvConfidenceType().getShortLabel() != null){
                db= conf.getCvConfidenceType().getShortLabel();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, conf.getValue());

            return field;
        }

        return null;
    }

    /**
     *
     * @param field
     * @return the converted interaction confidence
     */
    public Confidence calimochoToIntact(Field field){

        if (field != null && field.get(CalimochoKeys.VALUE) != null){

            Confidence confidence = new Confidence(field.get(CalimochoKeys.VALUE));
            
            String confType = field.get(CalimochoKeys.DB);
            CvConfidenceType type;

            if (confType != null){
                type = new CvConfidenceType(IntactContext.getCurrentInstance().getInstitution(), confType);
            }
            else {
                type = new CvConfidenceType(IntactContext.getCurrentInstance().getInstitution(), CONFIDENCE);

                type.setIdentifier(CONFIDENCE_MI);
                CvObjectXref psiRef = XrefUtils.createIdentityXrefPsiMi(type, CONFIDENCE_MI);
                type.addXref(psiRef);
            }

            confidence.setCvConfidenceType(type);
            return confidence;
        }

        return null;
    }
}
