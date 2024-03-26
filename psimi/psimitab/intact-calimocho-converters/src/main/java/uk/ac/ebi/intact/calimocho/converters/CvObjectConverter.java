package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.model.extension.CvTermXref;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;

import java.util.Collection;

/**
 * CvObject converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class CvObjectConverter {

    private CrossReferenceConverter<CvTermXref> xrefConverter;
    
    public CvObjectConverter(){
        this.xrefConverter = new CrossReferenceConverter<>();
    }
    
    public Field intactToCalimocho(IntactCvTerm object){
        if (object != null){
            Field field = null;           

            Collection<Xref> refs = object.getXrefs();

            for (Xref ref : refs){
                // identity xrefs
                if (ref.getQualifier() != null && Xref.IDENTITY_MI.equals(ref.getQualifier().getMIIdentifier())){
                    Field identity = xrefConverter.intactToCalimocho((CvTermXref) ref, false);
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
            else if (field == null && object.getShortName() != null){
                field = new DefaultField();

                field.set(CalimochoKeys.KEY, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.DB, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.VALUE, "-");
                field.set(CalimochoKeys.TEXT, object.getShortName());
            }
            else if (field != null && object.getFullName() != null){
                field.set(CalimochoKeys.TEXT, object.getFullName());
            }
            else if (field != null && object.getShortName() != null){
                field.set(CalimochoKeys.TEXT, object.getShortName());
            }

            return field;
        }

        return null;
    }
}
