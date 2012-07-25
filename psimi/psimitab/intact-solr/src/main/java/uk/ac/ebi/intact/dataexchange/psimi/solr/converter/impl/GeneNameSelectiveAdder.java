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
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.RowDataSelectiveAdder;

import java.util.Collection;

/**
 * Looks for gene names and adds them to the "geneName" field.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GeneNameSelectiveAdder implements RowDataSelectiveAdder {
    public final static String GENE_NAME = "gene name";

    public void addToDoc(SolrInputDocument doc, Row row) {
        Collection<Field> aliasA = row.getFields(InteractionKeys.KEY_ALIAS_A);
        Collection<Field> aliasB = row.getFields(InteractionKeys.KEY_ALIAS_B);

        addGeneNames(doc, aliasA);
        addGeneNames(doc, aliasB);
    }

    private void addGeneNames(SolrInputDocument doc, Collection<Field> column) {
        for (Field field : column) {
            String text = field.get(CalimochoKeys.TEXT);
            String value = field.get(CalimochoKeys.VALUE);

            if (GENE_NAME.equalsIgnoreCase(text) && value != null) {
                doc.addField(FieldNames.GENE_NAME, value);
            }
        }
    }
}