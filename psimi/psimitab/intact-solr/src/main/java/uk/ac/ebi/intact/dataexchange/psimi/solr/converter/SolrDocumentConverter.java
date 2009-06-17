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
package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.*;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.FieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BaseFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.ByInteractorTypeRowDataAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.GeneNameSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.IdSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.TypeFieldFilter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.util.*;

/**
 * Converts from Row to SolrDocument and viceversa.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrDocumentConverter {

    private DocumentDefinition documentDefintion;

    /**
     * Access to the Ontology index.
     */
    private FieldEnricher fieldEnricher;

    public SolrDocumentConverter() {
        this(new IntactDocumentDefinition());
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion) {
        this.documentDefintion = documentDefintion;
        this.fieldEnricher = new BaseFieldEnricher();
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion,
                                 FieldEnricher fieldEnricher) {
        this(documentDefintion);
        this.fieldEnricher = fieldEnricher;
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion, OntologySearcher ontologySearcher) {
        this(documentDefintion, new OntologyFieldEnricher(ontologySearcher));
    }

    public SolrInputDocument toSolrDocument(String mitabLine) throws SolrServerException {
        Row row = documentDefintion.createRowBuilder().createRow(mitabLine);
        return toSolrDocument(row, mitabLine);
    }

    public SolrInputDocument toSolrDocument(BinaryInteraction binaryInteraction) throws SolrServerException {
        Row row = documentDefintion.createInteractionRowConverter().createRow(binaryInteraction);
        return toSolrDocument(row);
    }

    public SolrInputDocument toSolrDocument(Row row) throws SolrServerException {
        return toSolrDocument(row, row.toString());
    }

    protected SolrInputDocument toSolrDocument(Row row, String mitabLine) throws SolrServerException {
        SolrInputDocument doc = new SolrInputDocument();

        // store the mitab line
        doc.addField(FieldNames.LINE, mitabLine);

        addColumnToDoc(doc, row, FieldNames.ID_A, IntactDocumentDefinition.ID_INTERACTOR_A, 10f, true);
        addColumnToDoc(doc, row, FieldNames.ID_B, IntactDocumentDefinition.ID_INTERACTOR_B, 10f, true);
        addColumnToDoc(doc, row, FieldNames.ALTID_A, IntactDocumentDefinition.ALTID_INTERACTOR_A, 8f);
        addColumnToDoc(doc, row, FieldNames.ALTID_B, IntactDocumentDefinition.ALTID_INTERACTOR_B, 8f);
        addColumnToDoc(doc, row, FieldNames.ALIAS_A, IntactDocumentDefinition.ALIAS_INTERACTOR_A, 7f);
        addColumnToDoc(doc, row, FieldNames.ALIAS_B, IntactDocumentDefinition.ALIAS_INTERACTOR_B, 7f);
        addColumnToDoc(doc, row, FieldNames.DETMETHOD, IntactDocumentDefinition.INT_DET_METHOD, true);
        addColumnToDoc(doc, row, FieldNames.PUBAUTH, IntactDocumentDefinition.PUB_AUTH);
        addColumnToDoc(doc, row, FieldNames.PUBID, IntactDocumentDefinition.PUB_ID);
        addColumnToDoc(doc, row, FieldNames.TAXID_A, IntactDocumentDefinition.TAXID_A, true);
        addColumnToDoc(doc, row, FieldNames.TAXID_B, IntactDocumentDefinition.TAXID_B, true);
        addColumnToDoc(doc, row, FieldNames.TYPE, IntactDocumentDefinition.INT_TYPE, true);
        addColumnToDoc(doc, row, FieldNames.SOURCE, IntactDocumentDefinition.SOURCE);
        addColumnToDoc(doc, row, FieldNames.INTERACTION_ID, IntactDocumentDefinition.INTERACTION_ID, 11f);
        addColumnToDoc(doc, row, FieldNames.CONFIDENCE, IntactDocumentDefinition.CONFIDENCE);

        // extended
        if (documentDefintion instanceof IntactDocumentDefinition) {
            addColumnToDoc(doc, row, FieldNames.EXPERIMENTAL_ROLE_A, IntactDocumentDefinition.EXPERIMENTAL_ROLE_A, true);
            addColumnToDoc(doc, row, FieldNames.EXPERIMENTAL_ROLE_B, IntactDocumentDefinition.EXPERIMENTAL_ROLE_B, true);
            addColumnToDoc(doc, row, FieldNames.BIOLOGICAL_ROLE_A, IntactDocumentDefinition.BIOLOGICAL_ROLE_A, true);
            addColumnToDoc(doc, row, FieldNames.BIOLOGICAL_ROLE_B, IntactDocumentDefinition.BIOLOGICAL_ROLE_B, true);
            addColumnToDoc(doc, row, FieldNames.PROPERTIES_A, IntactDocumentDefinition.PROPERTIES_A, true);
            addColumnToDoc(doc, row, FieldNames.PROPERTIES_B, IntactDocumentDefinition.PROPERTIES_B, true);
            addColumnToDoc(doc, row, FieldNames.TYPE_A, IntactDocumentDefinition.INTERACTOR_TYPE_A, true);
            addColumnToDoc(doc, row, FieldNames.TYPE_B, IntactDocumentDefinition.INTERACTOR_TYPE_B, true);
            addColumnToDoc(doc, row, FieldNames.HOST_ORGANISM, IntactDocumentDefinition.HOST_ORGANISM, true);
            addColumnToDoc(doc, row, FieldNames.EXPANSION, IntactDocumentDefinition.EXPANSION_METHOD);
            addColumnToDoc(doc, row, FieldNames.DATASET, IntactDocumentDefinition.DATASET);
            addColumnToDoc(doc, row, FieldNames.ANNOTATION_A, IntactDocumentDefinition.ANNOTATIONS_A);
            addColumnToDoc(doc, row, FieldNames.ANNOTATION_B, IntactDocumentDefinition.ANNOTATIONS_B);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_A, IntactDocumentDefinition.PARAMETERS_A);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_B, IntactDocumentDefinition.PARAMETERS_B);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_INTERACTION, IntactDocumentDefinition.PARAMETERS_INTERACTION);

            addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(IntactDocumentDefinition.ID_INTERACTOR_A,
                                                                      IntactDocumentDefinition.INTERACTOR_TYPE_A));
            addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(IntactDocumentDefinition.ID_INTERACTOR_B,
                                                                      IntactDocumentDefinition.INTERACTOR_TYPE_B));
        }

        // ac
        //doc.addField(FieldNames.PKEY, "NEW"); // pkey is generated automatically and using UUID

        // add the iRefIndex field from the interaction_id column to the rig field (there should be zero or one)
        addFilteredField(row, doc, FieldNames.RIGID, IntactDocumentDefinition.INTERACTION_ID, new TypeFieldFilter("irefindex"));

        // ids
        addCustomFields(row, doc, new IdSelectiveAdder());

        // gene names
        addCustomFields(row, doc, new GeneNameSelectiveAdder());

        return doc;
    }

    public BinaryInteraction toBinaryInteraction(SolrDocument doc) {
        return documentDefintion.createInteractionRowConverter().createBinaryInteraction(toRow(doc));
    }

    public BinaryInteraction toBinaryInteraction(SolrInputDocument doc) {
        return documentDefintion.createInteractionRowConverter().createBinaryInteraction(toRow(doc));
    }

    public Row toRow(SolrDocument doc) {
        return toRow((Object)doc);
    }
    
    public Row toRow(SolrInputDocument doc) {
        return toRow((Object)doc);
    }
    
    protected Row toRow(Object doc) {
        int i = 0;

        Row row = new Row();
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ID_A), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ID_B), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ALTID_A), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ALTID_B), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ALIAS_A), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ALIAS_B), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.DETMETHOD_EXACT), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PUBAUTH), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PUBID), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.TAXID_A_EXACT), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.TAXID_B_EXACT), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.TYPE_EXACT), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.SOURCE), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.INTERACTION_ID), i++));
        row.appendColumn(toColumn(getFieldValue(doc, FieldNames.CONFIDENCE), i++));

        // extended
        if (documentDefintion instanceof IntactDocumentDefinition) {
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.EXPERIMENTAL_ROLE_A_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.EXPERIMENTAL_ROLE_B_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.BIOLOGICAL_ROLE_A_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.BIOLOGICAL_ROLE_B_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PROPERTIES_A_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PROPERTIES_B_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.TYPE_A_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.TYPE_B_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.HOST_ORGANISM_EXACT), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.EXPANSION), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.DATASET), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ANNOTATION_A), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.ANNOTATION_B), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PARAMETER_A), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PARAMETER_B), i++));
            row.appendColumn(toColumn(getFieldValue(doc, FieldNames.PARAMETER_INTERACTION), i++));
        }

        return row;
    }

    private Collection<Object> getFieldValue(Object doc, String fieldName) {
        if (doc instanceof SolrDocument) {
            return ((SolrDocument)doc).getFieldValues(fieldName);
        } else if (doc instanceof SolrInputDocument) {
            return ((SolrInputDocument)doc).getFieldValues(fieldName);
        }
       
        throw new IllegalArgumentException("Unexpected object type: "+doc.getClass().getName());
    }

    protected Column toColumn(Collection<Object> strCol, int docDefinitionIndex) {
        return toColumn(strCol, documentDefintion.getColumnDefinition(docDefinitionIndex).getBuilder());
    }

    protected Column toColumn(Collection<Object> strFields, FieldBuilder fieldBuilder) {
        if (strFields == null || strFields.isEmpty()) {
            return new Column();
        }

        List<Field> fields = new ArrayList<Field>(strFields.size());

        for (Object strField : strFields) {
            Field field = fieldBuilder.createField((String)strField);
            fields.add(field);
        }

        return new Column(fields);
    }

    public String toMitabLine(SolrDocument doc) {
        return toRow(doc).toString();
    }

    public String toMitabLine(SolrInputDocument doc) {
        return toRow(doc).toString();
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex) throws SolrServerException {
        addColumnToDoc(doc, row, fieldName, columnIndex, 1f, false);
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex, float boost) throws SolrServerException {
        addColumnToDoc(doc, row, fieldName, columnIndex, boost, false);
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex,  boolean expandableColumn) throws SolrServerException {
        addColumnToDoc(doc, row, fieldName, columnIndex, 1f, expandableColumn); 
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex, float boost, boolean expandableColumn) throws SolrServerException {
        // do not process columns not found in the row
        if (row.getColumnCount() <= columnIndex) {
            return;
        }

        Column column = row.getColumnByIndex( columnIndex );

        for (Field field : column.getFields()) {
            if (fieldEnricher.isExpandableOntology(field.getType())) {
                try {
                    field = fieldEnricher.enrich(field);
                } catch (Exception e) {
                    throw new SolrServerException("Problem enriching field: "+field, e);
                }

                doc.addField(field.getType(), field.getValue());

                if (field.getDescription() != null) {
                    doc.addField("spell", field.getDescription());
                }

                boolean includeItself = true;

                for (Field parentField : fieldEnricher.getAllParents(field, includeItself)) {
                    addExpandedFields(doc, fieldName, parentField);
                }
            }

            if (expandableColumn) {
                doc.addField(fieldName+"_exact", field.toString(), boost);
                doc.addField(fieldName+"_exact_id", field.getValue(), boost);
            }
            addDescriptionField(doc, field.getType(), field);
            doc.addField(fieldName, field.toString(), boost);
            doc.addField(fieldName+"_ms", field.toString(), boost);

            if (field.getType() != null) {
                doc.addField(field.getType()+"_xref", field.getValue(), boost);
                doc.addField(fieldName+"_"+field.getType()+"_xref", field.getValue(), boost);
                doc.addField(fieldName+"_"+field.getType()+"_xref_ms", field.toString(), boost);
            }

            addDescriptionField(doc, field.getType(), field);
            addDescriptionField(doc, fieldName, field);

        }
    }


    private void addFilteredField(Row row, SolrInputDocument doc, String fieldName, int columnIndex, FieldFilter filter) {
        Collection<Field> fields = getFieldsFromColumn(row, columnIndex, filter);

        if (fields == null) {
            return;
        }

        for (Field field : fields) {
            doc.addField(fieldName, field.getValue());
        }
    }

    private void addCustomFields(Row row, SolrInputDocument doc, RowDataSelectiveAdder selectiveAdder) {
        selectiveAdder.addToDoc(doc, row);
    }

    private Collection<Field> getFieldsFromColumn(Row row, int columnIndex, FieldFilter filter) {
        List<Field> fields = new ArrayList<Field>();

        // do not process columns not found in the row
        if (row.getColumnCount() <= columnIndex) {
            return null;
        }

        Column column = row.getColumnByIndex( columnIndex );

        for (Field field : column.getFields()) {
            if (field != null && filter.acceptField(field)) {
                fields.add(field);
            }
        }

        return fields;
    }

    private void addExpandedFields(SolrInputDocument doc, String fieldName, Field field) {
        addExpandedField(doc, field, fieldName);
        addExpandedField(doc, field, field.getType());
    }

    private void addExpandedField(SolrInputDocument doc, Field field, String fieldPrefix) {
        doc.addField(fieldPrefix+"_expanded", field.toString());
        doc.addField(fieldPrefix+"_expanded_id", field.getValue());
        doc.addField(fieldPrefix+"_expanded_ms", field.toString());

        addDescriptionField(doc, fieldPrefix+"_expanded", field);
    }

    private void addDescriptionField(SolrInputDocument doc, String fieldPrefix, Field field) {
        if (field.getDescription() != null) {

            doc.addField(fieldPrefix+"_desc", field.getDescription());
            doc.addField(fieldPrefix+"_desc_s", field.getDescription());
        }
    }



   
}
