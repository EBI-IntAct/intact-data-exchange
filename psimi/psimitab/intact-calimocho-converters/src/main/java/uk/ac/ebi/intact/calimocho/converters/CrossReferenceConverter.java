package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.XrefUtils;

/**
 * Converter for cross references
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class CrossReferenceConverter {
    public static String DATABASE_UNKNOWN = "unknown";

    public Field intactToCalimocho(Xref ref, boolean addTextValue){
        if (ref != null && ref.getPrimaryId() != null){
            Field field = new DefaultField();

            String db = DATABASE_UNKNOWN;
            if (ref.getCvDatabase().getShortLabel() != null){
                db= ref.getCvDatabase().getShortLabel();
            }

            field.set( CalimochoKeys.KEY, db);
            field.set( CalimochoKeys.DB, db);
            field.set( CalimochoKeys.VALUE, ref.getPrimaryId());

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

    public InteractorXref calimochoToIntact(Field field, boolean isIdentity){
        
        if (field != null && field.get(CalimochoKeys.DB) != null && field.get(CalimochoKeys.VALUE) != null){
            IntactContext intactContext = IntactContext.getCurrentInstance();
            
            InteractorXref ref = new InteractorXref();
            String text = field.get(CalimochoKeys.TEXT);
            
            CvDatabase database = new CvDatabase(intactContext.getInstitution(), field.get(CalimochoKeys.DB));
            CvXrefQualifier refQualifier=null;
            if (isIdentity){
                refQualifier = new CvXrefQualifier(intactContext.getInstitution(), CvXrefQualifier.IDENTITY);
                refQualifier.setIdentifier(CvXrefQualifier.IDENTITY_MI_REF);
                CvObjectXref psiRef = XrefUtils.createIdentityXrefPsiMi(refQualifier, CvXrefQualifier.IDENTITY_MI_REF);
                refQualifier.addXref(psiRef);
                
                if (text != null){
                    ref.setSecondaryId(text); 
                }
            }
            else if (text != null) {
                refQualifier = new CvXrefQualifier(intactContext.getInstitution(), text);

            }
            
            ref.setCvDatabase(database);
            ref.setCvXrefQualifier(refQualifier);
            
            return ref;
        }
        
        return null;
    }
}
