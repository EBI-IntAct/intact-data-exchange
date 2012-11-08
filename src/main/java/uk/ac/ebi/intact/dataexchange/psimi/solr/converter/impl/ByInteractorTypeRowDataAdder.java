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
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;

import java.util.Collection;

/**
 * Adds IDs classified by interactor type to the SOlR document.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ByInteractorTypeRowDataAdder implements RowDataSelectiveAdder {

    private String columnKey;
    private String columnInteractorType;
    private String ontologyName = "psi-mi";
    private String databaseName = "intact";

    public ByInteractorTypeRowDataAdder(String columnKey, String columnInteractorType) {
        this.columnKey = columnKey;
        this.columnInteractorType = columnInteractorType;
    }

    public ByInteractorTypeRowDataAdder(String columnKey, String columnInteractorType, String ontologyName, String databaseName) {
        this.columnKey = columnKey;
        this.columnInteractorType = columnInteractorType;

        if (ontologyName != null){
            this.ontologyName = ontologyName;
        }
        if (databaseName != null){
            this.databaseName = databaseName;
        }
    }

    public boolean addToDoc(SolrInputDocument doc, Row row) {
        if (row.getFields(columnInteractorType) == null) {
            return false;
        }

        boolean added = false;

        Collection<Field> colId = row.getFields(columnKey);
        Collection<Field> colType = row.getFields(columnInteractorType);

        String typeId = getInteractorTypeId(colType);

        if (typeId != null){
            // e.g. acByInteractorType_mi1234

            for (Field idField : colId) {
                String value = idField.get(CalimochoKeys.VALUE);
                if (databaseName.equalsIgnoreCase(idField.get(CalimochoKeys.DB)) && value != null) {
                    String fieldName = FieldNames.INTACT_BY_INTERACTOR_TYPE_PREFIX +(typeId.replaceAll(":", "").toLowerCase());
                    doc.addField(fieldName, value);
                    added = true;
                }
            }
        }

        return added;
    }

    private String getInteractorTypeId(Collection<Field> colType) {
        for (Field field : colType) {
            if (ontologyName.equalsIgnoreCase(field.get(CalimochoKeys.DB))) {
                return field.get(CalimochoKeys.VALUE);
            }
        }

        return null;
    }
}
