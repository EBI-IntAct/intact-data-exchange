package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;

import java.util.Arrays;
import java.util.List;

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
        uniprotKeys = Arrays.asList(
                psidev.psi.mi.jami.model.Alias.GENE_NAME_MI,
                psidev.psi.mi.jami.model.Alias.GENE_NAME_SYNONYM_MI,
                psidev.psi.mi.jami.model.Alias.ISOFORM_SYNONYM_MI,
                psidev.psi.mi.jami.model.Alias.LOCUS_NAME_MI,
                psidev.psi.mi.jami.model.Alias.ORF_NAME_MI);
    }

    public Field intactToCalimocho(AbstractIntactAlias alias){
        if (alias != null && alias.getName() != null){
            Field field = new DefaultField();

            if (alias.getType() != null && alias.getType().getShortName() != null){
                String type = alias.getType().getShortName();
                String typeMI = alias.getType().getMIIdentifier();

                if (uniprotKeys.contains(typeMI)){
                    field.set(CalimochoKeys.KEY, UNIPROT);
                    field.set(CalimochoKeys.DB, UNIPROT);
                }
                else {
                    field.set(CalimochoKeys.KEY, INTACT);
                    field.set(CalimochoKeys.DB, INTACT);
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
