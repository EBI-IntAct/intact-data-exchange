package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;

import java.util.*;

/**
 * Biosource converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class BioSourceConverter {

    public static String TAXID = "taxid";

    public Collection<Field> intactToCalimocho(IntactOrganism organism){
        if (organism != null){
            Collection<Field> fields = new ArrayList<Field>(2);

            Field common = new DefaultField();

            String name = organism.getCommonName();
            String fullName = organism.getScientificName();
            String taxId = String.valueOf(organism.getTaxId());

            common.set(CalimochoKeys.KEY, TAXID);
            common.set(CalimochoKeys.DB, TAXID);
            common.set(CalimochoKeys.VALUE, taxId);

            if (name != null){
                common.set(CalimochoKeys.TEXT, name);
            }

            fields.add(common);

            if (fullName != null){
                Field scientific = new DefaultField();
                scientific.set(CalimochoKeys.KEY, TAXID);
                scientific.set(CalimochoKeys.DB, TAXID);
                scientific.set(CalimochoKeys.VALUE, taxId);

                scientific.set(CalimochoKeys.TEXT, fullName);

                fields.add(scientific);
            }

            return fields;
        }

        return Collections.EMPTY_LIST;
    }
}
