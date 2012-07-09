package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.util.InstitutionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.ebi.intact.model.CvAliasType.*;

/**
 * Converter for aliases
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class AliasConverter {

    private static final List<String> uniprotKeys;
    public static final String UNIPROT = "uniprotkb";
    public static final String INTACT = "intact";

    static {
        uniprotKeys = new ArrayList<String>( Arrays.asList(GENE_NAME_MI_REF, GENE_NAME_SYNONYM_MI_REF,
                ISOFORM_SYNONYM_MI_REF, LOCUS_NAME_MI_REF,
                ORF_NAME_MI_REF) );
    }

    public Field intactToCalimocho(Alias alias){
        if (alias != null && alias.getName() != null){
            Field field = new DefaultField();

            if (alias.getCvAliasType() != null && alias.getCvAliasType().getShortLabel() != null){
                String type = alias.getCvAliasType().getShortLabel();
                String typeMI = alias.getCvAliasType().getIdentifier();

                if (uniprotKeys.contains(typeMI)){
                    field.set(CalimochoKeys.KEY, UNIPROT);
                    field.set(CalimochoKeys.DB, UNIPROT);
                }
                else {
                    String institutionName = INTACT;
                    IntactContext context = IntactContext.getCurrentInstance();

                    if (context.getInstitution() != null){
                        Institution institution = context.getInstitution();

                        CvDatabase database = InstitutionUtils.retrieveCvDatabase(context, institution);

                        if (database != null && database.getShortLabel() != null){
                            institutionName = database.getShortLabel();
                        }
                    }

                    field.set(CalimochoKeys.KEY, institutionName);
                    field.set(CalimochoKeys.DB, institutionName);
                }

                field.set(CalimochoKeys.TEXT, type);
            }
            else {
                field.set(CalimochoKeys.KEY, CrossReferenceConverter.DATABASE_UNKNOWN);
                field.set(CalimochoKeys.DB, CrossReferenceConverter.DATABASE_UNKNOWN);
            }

            field.set(CalimochoKeys.VALUE, alias.getName());

            return field;
        }

        return null;
    }
}
