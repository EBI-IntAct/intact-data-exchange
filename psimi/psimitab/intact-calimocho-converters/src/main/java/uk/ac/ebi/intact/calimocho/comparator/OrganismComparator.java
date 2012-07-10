package uk.ac.ebi.intact.calimocho.comparator;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.Field;

import java.util.Comparator;

/**
 * Comparator of organisms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/07/12</pre>
 */

public class OrganismComparator implements Comparator<Field>{
    @Override
    public int compare(Field field1, Field field2) {
        int BEFORE = -1;
        int AFTER = 1;
        int EQUALS = 0;
        
        if (field1 == null && field2 == null){
            return 0;
        }
        else if (field1 == null){
            return AFTER;
        }
        else if (field2 == null){
            return BEFORE;
        }
        else{
            String taxId1 = field1.get(CalimochoKeys.VALUE);
            String taxId2 = field2.get(CalimochoKeys.VALUE);

            if (taxId1 == null && taxId2 == null){
                String name1 = field1.get(CalimochoKeys.TEXT);
                String name2 = field2.get(CalimochoKeys.TEXT);

                if (name1 == null && name2 == null){
                    return 0;
                }
                else if (name1 == null){
                    return AFTER;
                }
                else if (name2 == null){
                    return BEFORE;
                }
                else{
                    return name1.compareTo(name2);
                }
            }
            else if (taxId1 == null){
                return AFTER;
            }
            else if (taxId2 == null){
                return BEFORE;
            }
            else{
                int taxIdComparison = taxId1.compareTo(taxId2);

                // same taxId
                if (taxIdComparison == 0){
                    String name1 = field1.get(CalimochoKeys.TEXT);
                    String name2 = field2.get(CalimochoKeys.TEXT);

                    if (name1 == null && name2 == null){
                        return 0;
                    }
                    else if (name1 == null){
                        return AFTER;
                    }
                    else if (name2 == null){
                        return BEFORE;
                    }
                    else{
                        return name1.compareTo(name2);
                    }
                }
                // different taxid
                else {
                    return taxIdComparison;
                }
            }
        }
    }
}
