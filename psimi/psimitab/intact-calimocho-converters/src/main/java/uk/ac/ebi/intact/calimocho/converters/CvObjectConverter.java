package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.Collection;

/**
 * CvObject converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class CvObjectConverter {

    private CrossReferenceConverter xrefConverter;
    
    public CvObjectConverter(){
        this.xrefConverter = new CrossReferenceConverter();
    }
    
    public Field intactToCalimocho(CvObject object){
        if (object != null){
            Field field = null;           

            Collection<CvObjectXref> refs = object.getXrefs();

            for (CvObjectXref ref : refs){
                // identity xrefs
                if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                    Field identity = xrefConverter.intactToCalimocho(ref, false);
                    if (identity != null){
                        field = identity;
                        break;
                    }
                }
            }

            if (field == null && object.getFullName() != null){
                field = new DefaultField();

                field.set(CalimochoKeys.KEY, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.DB, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.VALUE, "-");
                field.set(CalimochoKeys.TEXT, object.getFullName());
            }
            else if (field == null && object.getShortLabel() != null){
                field = new DefaultField();

                field.set(CalimochoKeys.KEY, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.DB, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.VALUE, "-");
                field.set(CalimochoKeys.TEXT, object.getShortLabel());
            }
            else if (field != null && object.getFullName() != null){
                field.set(CalimochoKeys.TEXT, object.getFullName());
            }
            else if (field != null && object.getShortLabel() != null){
                field.set(CalimochoKeys.TEXT, object.getShortLabel());
            }

            return field;
        }

        return null;
    }
    
    public CvObject calimochoToIntact(Field field, Class<? extends CvObject> classType) throws IllegalAccessException, InstantiationException {
        
        if (field != null && classType != null){

            String db = field.get(CalimochoKeys.DB);
            String value = field.get(CalimochoKeys.VALUE);
            String text = field.get(CalimochoKeys.TEXT);

            if (db != null && (value != null || text != null)){
                CvObject object = classType.newInstance();

                object.setShortLabel(text != null ? text : value.toLowerCase());
                object.setIdentifier( value );

                CvObjectXref identityRef = new CvObjectXref();
                CvDatabase database = new CvDatabase(IntactContext.getCurrentInstance().getInstitution(), db);
                CvXrefQualifier refQualifier = new CvXrefQualifier(IntactContext.getCurrentInstance().getInstitution(), CvXrefQualifier.IDENTITY);
                refQualifier.setIdentifier(CvXrefQualifier.IDENTITY_MI_REF);
                CvObjectXref psiRef = XrefUtils.createIdentityXrefPsiMi(refQualifier, CvXrefQualifier.IDENTITY_MI_REF);
                refQualifier.addXref(psiRef);

                identityRef.setCvDatabase(database);
                identityRef.setCvXrefQualifier(refQualifier);
                object.addXref(identityRef);

                return object;
            }
        }
        
        return null;
    }
}
