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

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.io.IllegalFieldException;
import org.hupo.psi.calimocho.io.IllegalRowException;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Row;
import org.hupo.psi.calimocho.tab.io.DefaultRowReader;
import org.hupo.psi.calimocho.tab.io.IllegalColumnException;
import org.hupo.psi.calimocho.tab.io.RowReader;
import org.hupo.psi.calimocho.tab.io.formatter.AnnotationFieldFormatter;
import org.hupo.psi.calimocho.tab.io.formatter.XrefFieldFormatter;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import psidev.psi.mi.calimocho.solr.converter.Converter;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldName;
import psidev.psi.mi.calimocho.solr.converter.SolrFieldUnit;
import psidev.psi.mi.calimocho.solr.converter.TextFieldConverter;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension.AnnotationTopicsToEnrichConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension.FeatureTypeToEnrichConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.extension.FieldToEnrichConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.ByInteractorTypeRowDataAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.ConfidenceScoreSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.GeneNameSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.BaseFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.FieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.enricher.OntologyFieldEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.util.IntactSolrUtils;
import uk.ac.ebi.intact.dataexchange.psimi.solr.util.SchemaInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Converts from Row to SolrDocument and vice-versa.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrDocumentConverter extends Converter{

    private SchemaInfo schemaInfo;
    private RowReader rowReader;
    private PsimiTabReader mitabReader;

    /**
     * Access to the Ontology index.
     */
    private FieldEnricher fieldEnricher;
    private FieldToEnrichConverter fieldEnricherConverter;
    private FeatureTypeToEnrichConverter featureTypeFieldEnricher;
    private AnnotationTopicsToEnrichConverter annotationTopicToEnrichConverter;
    private static final String COLUMN_SEPARATOR = "\t";
    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_EMPTY = "-";

    public SolrDocumentConverter(SolrServer solrServer) {
        super();
        this.fieldEnricher = new BaseFieldEnricher();

        try {
            this.schemaInfo = IntactSolrUtils.retrieveSchemaInfo(solrServer);
        } catch (IOException e) {
            throw new RuntimeException("Problem fetching schema info from solr server: "+solrServer);
        }
        rowReader = new DefaultRowReader(MitabDocumentDefinitionFactory.mitab27());
        mitabReader = new PsimiTabReader();

        // call again the initializeKeyMap with the field enricher initialized
        initializeKeyMap();
    }

    @Override
    protected void initializeKeyMap(){
        super.initializeKeyMap();

        // only override parent method when the field enricher is initialized
        if (this.fieldEnricher != null){
            TextFieldConverter textConverter = new TextFieldConverter();
            XrefFieldFormatter textFormatter = new XrefFieldFormatter();
            this.fieldEnricherConverter = new FieldToEnrichConverter(this.fieldEnricher);
            this.featureTypeFieldEnricher = new FeatureTypeToEnrichConverter(this.fieldEnricher);
            this.annotationTopicToEnrichConverter = new AnnotationTopicsToEnrichConverter(this.fieldEnricher);
            AnnotationFieldFormatter annotFormatter = new AnnotationFieldFormatter(":");

            // override source which is an indexed field in IntAct. We don't want this field as store only, that is why we have the boolean false.
            // we don't want to enrich this one, there is no needs.
            keyMap.put(SolrFieldName.source,  new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_SOURCE), textConverter, textFormatter, false));

            // override taxidA and taxidB for ontology enrichment
            keyMap.put(SolrFieldName.taxidA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_TAXID_A), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.taxidB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_TAXID_B), this.fieldEnricherConverter, textFormatter, false));

            // override cvs for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.type, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTION_TYPE), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.detmethod, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_DETMETHOD),this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pbioroleA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_BIOROLE_A), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pbioroleB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_BIOROLE_B), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.ptypeA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTOR_TYPE_A), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.ptypeB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTOR_TYPE_B), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.complex, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_EXPANSION), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pmethodA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_PART_IDENT_METHOD_A), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pmethodB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_PART_IDENT_METHOD_B), this.fieldEnricherConverter, textFormatter, false));

            // override ftypeA and ftypeB for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.ftypeA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_FEATURE_A), this.featureTypeFieldEnricher, textFormatter, false));
            keyMap.put(SolrFieldName.ftypeB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_FEATURE_B), this.featureTypeFieldEnricher, textFormatter, false));

            // override annot for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.annot, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_ANNOTATIONS_I), annotationTopicToEnrichConverter, annotFormatter, false));

            // override xrefs, pxrefA and pxrefB for enrichemnt with synonyms
            keyMap.put(SolrFieldName.pxrefA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_A), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pxrefB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_B), this.fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.xref, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_I), this.fieldEnricherConverter, textFormatter, false));
        }
    }

    public SolrDocumentConverter(SolrServer solrServer, FieldEnricher fieldEnricher) {
        super();
        this.fieldEnricher = fieldEnricher != null ? fieldEnricher : new BaseFieldEnricher();

        try {
            this.schemaInfo = IntactSolrUtils.retrieveSchemaInfo(solrServer);
        } catch (IOException e) {
            throw new RuntimeException("Problem fetching schema info from solr server: "+solrServer);
        }
        rowReader = new DefaultRowReader(MitabDocumentDefinitionFactory.mitab27());
        mitabReader = new PsimiTabReader();

        // call again the initializeKeyMap with the field enricher initialized
        initializeKeyMap();
    }

    public SolrDocumentConverter(SolrServer solrServer, OntologySearcher ontologySearcher) {
        super();
        this.fieldEnricher = ontologySearcher != null ? new OntologyFieldEnricher(ontologySearcher) : new BaseFieldEnricher();

        try {
            this.schemaInfo = IntactSolrUtils.retrieveSchemaInfo(solrServer);
        } catch (IOException e) {
            throw new RuntimeException("Problem fetching schema info from solr server: "+solrServer);
        }
        rowReader = new DefaultRowReader(MitabDocumentDefinitionFactory.mitab27());
        mitabReader = new PsimiTabReader();

        // call again the initializeKeyMap with the field enricher initialized
        initializeKeyMap();
    }

    public SolrInputDocument toSolrDocument(String mitabLine) throws SolrServerException, IllegalFieldException, IllegalColumnException, IllegalRowException {
        Row row = rowReader.readLine(mitabLine);
        return toSolrDocument(row);
    }

    @Override
    public SolrInputDocument toSolrDocument(Row row) throws SolrServerException, IllegalFieldException {
        // use the same indexing logic as psicquic
        SolrInputDocument doc = super.toSolrDocument(row);

        // add mi score
        addCustomFields(row, doc, new ConfidenceScoreSelectiveAdder(FieldNames.INTACT_SCORE_NAME.toLowerCase()));

        // gene names
        addCustomFields(row, doc, new GeneNameSelectiveAdder());

        // add intact by interactor type
        addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A));
        addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B));

        // --------------------------------------------------------- old code ---------------------------------------

        // store the mitab line
        /*doc.addField(FieldNames.LINE, mitabLine);

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
        addColumnToDoc(doc, row, FieldNames.CONFIDENCE, IntactDocumentDefinition.CONFIDENCE);*/

        // extended
        /*if (documentDefintion instanceof IntactDocumentDefinition) {
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
        }*/

        // ac
        //doc.addField(FieldNames.PKEY, "NEW"); // pkey is generated automatically and using UUID

        // add the iRefIndex field from the interaction_id column to the rig field (there should be zero or one)
        //addFilteredField(row, doc, FieldNames.RIGID, IntactDocumentDefinition.INTERACTION_ID, new TypeFieldFilter("irefindex"));

        // ids
        //addCustomFields(row, doc, new IdSelectiveAdder());

        return doc;
    }

    public Row toRow(SolrDocument doc) throws IllegalFieldException, IllegalRowException, IllegalColumnException {
        return toRow((Object)doc);
    }

    public Row toRow(SolrInputDocument doc) throws IllegalFieldException, IllegalRowException, IllegalColumnException {
        return toRow((Object)doc);
    }

    protected Row toRow(Object doc) throws IllegalFieldException, IllegalColumnException, IllegalRowException {

        Row row = rowReader.readLine(toMitabLine(doc));

        return row;
    }

    public BinaryInteraction toBinaryInteraction(SolrDocument doc) throws PsimiTabException {
        return toBinaryInteraction((Object) doc);
    }

    public BinaryInteraction toBinaryInteraction(SolrInputDocument doc) throws PsimiTabException {
        return toBinaryInteraction((Object) doc);
    }

    protected BinaryInteraction toBinaryInteraction(Object doc) throws PsimiTabException {

        BinaryInteraction binaryInteraction = mitabReader.readLine(toMitabLine(doc));

        return binaryInteraction;
    }

    private Collection<Object> getFieldValue(Object doc, String fieldName) {
        if (doc instanceof SolrDocument) {
            return ((SolrDocument)doc).getFieldValues(fieldName);
        } else if (doc instanceof SolrInputDocument) {
            return ((SolrInputDocument)doc).getFieldValues(fieldName);
        }

        throw new IllegalArgumentException("Unexpected object type: "+doc.getClass().getName());
    }

    public String toMitabLine(SolrDocument doc) {
        return toMitabLine((Object)doc);
    }

    public String toMitabLine(SolrInputDocument doc) {
        return toMitabLine((Object)doc);
    }

    protected String toMitabLine(Object doc) {
        int size = PsicquicSolrServer.DATA_FIELDS_27.length;
        int index = 0;

        StringBuffer sb = new StringBuffer(1064);

        // one field name is one column
        for (String fieldName : PsicquicSolrServer.DATA_FIELDS_27){
            // only one value is expected because it should not be multivalued
            Collection<Object> fieldValues = getFieldValue(doc, fieldName);

            if (fieldValues == null || fieldValues.isEmpty()){
                sb.append(FIELD_EMPTY);
            }
            else {
                Iterator<Object> valueIterator = fieldValues.iterator();
                while (valueIterator.hasNext()){
                    sb.append(String.valueOf(valueIterator.next()));

                    if (valueIterator.hasNext()){
                        sb.append(FIELD_SEPARATOR);
                    }
                }
            }

            if (index < size){
                sb.append(COLUMN_SEPARATOR);
            }
            index++;
        }

        return sb.toString();
    }

    private void addCustomFields(Row row, SolrInputDocument doc, RowDataSelectiveAdder selectiveAdder) {
        selectiveAdder.addToDoc(doc, row);
    }
}