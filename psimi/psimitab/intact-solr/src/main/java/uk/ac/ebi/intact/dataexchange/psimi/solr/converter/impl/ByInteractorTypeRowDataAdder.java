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

import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;
import org.apache.solr.common.SolrInputDocument;

/**
 * Adds IDs classified by interactor type to the SOlR document.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ByInteractorTypeRowDataAdder implements RowDataSelectiveAdder {

    private int columnId;
    private int columnInteractorType;

    public ByInteractorTypeRowDataAdder(int columnId, int columnInteractorType) {
        this.columnId = columnId;
        this.columnInteractorType = columnInteractorType;
    }

    public void addToDoc(SolrInputDocument doc, Row row) {
        if (row.getColumnCount() <= columnInteractorType) {
            return;
        }

        Column colId = row.getColumnByIndex(columnId);
        Column colType = row.getColumnByIndex(columnInteractorType);

        String typeMi = getInteractorTypeMi(colType);

        // e.g. acByInteractorType_mi1234
        for (Field idField : colId.getFields()) {
            if ("intact".equals(idField.getType())) {
                String fieldName = FieldNames.INTACT_BY_INTERACTOR_TYPE_PREFIX +(typeMi.replaceAll(":", "").toLowerCase());
                doc.addField(fieldName, idField.getValue());
            }
        }
    }

    private String getInteractorTypeMi(Column colType) {
        for (Field field : colType.getFields()) {
            if ("psi-mi".equals(field.getType())) {
                return field.getValue();
            }
        }

        return null;
    }
}
