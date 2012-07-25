/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl;

import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;

import java.util.Collection;

/**
 * Gets the identifiers from the two first columns in the row and stores them
 * in a *_id field (e.g. uniprotkb_id)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @deprecated : the field _id are now deprecated. If we want to search for a specific xref, we can search for db:value.
 */
public class IdSelectiveAdder implements RowDataSelectiveAdder {

    public void addToDoc(SolrInputDocument doc, Row row) {
        Collection<Field> colIdA = row.getFields(InteractionKeys.KEY_ID_A);
        Collection<Field> colIdB = row.getFields(InteractionKeys.KEY_ID_B);

        addFields(doc, colIdA);
        addFields(doc, colIdB);
    }

    private void addFields(SolrInputDocument doc, Collection<Field> column) {
        for (Field field : column) {
            String fieldName = field.get(CalimochoKeys.DB)+"_id";
            String value = field.get(CalimochoKeys.VALUE);

            if (value != null){
                doc.addField(fieldName, value);
            }
        }
    }
}
