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
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

/**
 * Gets the identifiers from the two first columns in the row and stores them
 * in a *_id field (e.g. uniprotkb_id)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IdSelectiveAdder implements RowDataSelectiveAdder {

    public void addToDoc(SolrInputDocument doc, Row row) {
        Column colIdA = row.getColumnByIndex(IntactDocumentDefinition.ID_INTERACTOR_A);
        Column colIdB = row.getColumnByIndex(IntactDocumentDefinition.ID_INTERACTOR_B);

        addFields(doc, colIdA);
        addFields(doc, colIdB);
    }

    private void addFields(SolrInputDocument doc, Column column) {
        for (Field field : column.getFields()) {
            String fieldName = field.getType()+"_id";
            doc.addField(fieldName, field.getValue());
        }
    }
}
