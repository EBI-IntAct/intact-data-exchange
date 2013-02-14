package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl;

import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;

import java.util.Collection;

/**
 * This selective adder will filter unique ids and create a binary field for sorting binary interactions in solr
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/02/13</pre>
 */

public class BinaryIdSelectiveAdder implements RowDataSelectiveAdder{

    private FieldValueComparator fieldComparator = new FieldValueComparator();

    public boolean addToDoc(SolrInputDocument doc, Row row) {
        Collection<Field> uniqueIdsA = row.getFields(InteractionKeys.KEY_ID_A);
        Collection<Field> uniqueIdsB = row.getFields(InteractionKeys.KEY_ID_B);

        boolean added = false;

        if (addBinary(doc, uniqueIdsA, uniqueIdsB)){
            added = true;
        }

        return added;
    }

    private boolean addBinary(SolrInputDocument doc, Collection<Field> idA, Collection<Field> idB) {
        boolean added = false;

        if (idA == null && idB == null){
            return false;
        }
        else if (idA != null && idB != null){
            if (idA.isEmpty() && idB.isEmpty()){
                return false;
            }
            // only idB
            else if (idA.isEmpty()){
                //List<Field> idBs = new ArrayList<Field>(idB);
                //Collections.sort(idBs, fieldComparator);
                Field firstB = idB.iterator().next();
                String value = firstB.get(CalimochoKeys.VALUE);

                if (value != null) {
                    doc.addField(FieldNames.BINARY, value);
                    added = true;
                }
            }
            // only idB
            else if (idB.isEmpty()){
                //List<Field> idAs = new ArrayList<Field>(idA);
                //Collections.sort(idAs, fieldComparator);
                Field firstA = idA.iterator().next();
                String value = firstA.get(CalimochoKeys.VALUE);

                if (value != null) {
                    doc.addField(FieldNames.BINARY, value);
                    added = true;
                }
            }
            else {
                //List<Field> idAs = new ArrayList<Field>(idA);
                //Collections.sort(idAs, fieldComparator);
               // List<Field> idBs = new ArrayList<Field>(idB);
               // Collections.sort(idBs, fieldComparator);

                Field firstA = idA.iterator().next();
                Field firstB = idB.iterator().next();
                String valueA = firstA.get(CalimochoKeys.VALUE);
                String valueB = firstB.get(CalimochoKeys.VALUE);

                if (valueA != null && valueB != null){
                    int comp = valueA.compareTo(valueB);

                    if (comp <= 0){
                        doc.addField(FieldNames.BINARY, valueA+"-"+valueB);
                        added = true;
                    }
                    else{
                        doc.addField(FieldNames.BINARY, valueB+"-"+valueA);
                        added = true;
                    }
                }
                else if (valueA != null){
                    doc.addField(FieldNames.BINARY, valueA);
                    added = true;
                }
                else if (valueB != null){
                    doc.addField(FieldNames.BINARY, valueB);
                    added = true;
                }
            }
        }

        return added;
    }
}
