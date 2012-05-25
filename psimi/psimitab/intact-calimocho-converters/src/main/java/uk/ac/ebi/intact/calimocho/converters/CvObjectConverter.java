package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.CvXrefQualifier;

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
    
    public Field toCalimocho(CvObject object){
        if (object != null){
            Field field = null;           

            Collection<CvObjectXref> refs = object.getXrefs();

            for (CvObjectXref ref : refs){
                // identity xrefs
                if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                    Field identity = xrefConverter.toCalimocho(ref, false);
                    if (identity != null){
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
}
