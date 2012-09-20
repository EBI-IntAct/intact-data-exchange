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
    protected FieldEnricher fieldEnricher;
    protected FieldToEnrichConverter fieldEnricherConverter;
    protected FeatureTypeToEnrichConverter featureTypeFieldEnricher;
    protected AnnotationTopicsToEnrichConverter annotationTopicToEnrichConverter;
    private static final String COLUMN_SEPARATOR = "\t";
    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_EMPTY = "-";

    private RowDataSelectiveAdder confidenceSelectiveAdder;
    private RowDataSelectiveAdder geneSelectiveAdder;
    private RowDataSelectiveAdder idATypeSelectiveAdder;
    private RowDataSelectiveAdder idBTypeSelectiveAdder;
    private RowDataSelectiveAdder altidATypeSelectiveAdder;
    private RowDataSelectiveAdder altidBTypeSelectiveAdder;

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

        confidenceSelectiveAdder = new ConfidenceScoreSelectiveAdder(FieldNames.INTACT_SCORE_NAME.toLowerCase());
        geneSelectiveAdder = new GeneNameSelectiveAdder();
        idATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        idBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);
        altidATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        altidBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);

        // override the static initializeKeyMap with the field enricher initialized
        overrideKeyMap();
    }

    protected void overrideKeyMap(){

        // only override parent method when the field enricher is initialized
        if (fieldEnricher != null){
            TextFieldConverter textConverter = new TextFieldConverter();
            XrefFieldFormatter textFormatter = new XrefFieldFormatter();
            fieldEnricherConverter = new FieldToEnrichConverter(fieldEnricher);
            featureTypeFieldEnricher = new FeatureTypeToEnrichConverter(fieldEnricher);
            annotationTopicToEnrichConverter = new AnnotationTopicsToEnrichConverter(fieldEnricher);
            AnnotationFieldFormatter annotFormatter = new AnnotationFieldFormatter(":");

            // override source which is an indexed field in IntAct. We don't want this field as store only, that is why we have the boolean false.
            // we don't want to enrich this one, there is no needs.
            keyMap.put(SolrFieldName.source,  new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_SOURCE), textConverter, textFormatter, false));

            // override taxidA and taxidB for ontology enrichment
            keyMap.put(SolrFieldName.taxidA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_TAXID_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.taxidB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_TAXID_B), fieldEnricherConverter, textFormatter, false));

            // override cvs for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.idA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_ID_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.idB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_ID_B), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.type, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTION_TYPE), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.detmethod, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_DETMETHOD), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pbioroleA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_BIOROLE_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pbioroleB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_BIOROLE_B), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.ptypeA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTOR_TYPE_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.ptypeB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_INTERACTOR_TYPE_B), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.complex, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_EXPANSION), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pmethodA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_PART_IDENT_METHOD_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pmethodB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_PART_IDENT_METHOD_B), fieldEnricherConverter, textFormatter, false));

            // override ftypeA and ftypeB for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.ftypeA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_FEATURE_A), featureTypeFieldEnricher, textFormatter, false));
            keyMap.put(SolrFieldName.ftypeB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_FEATURE_B), featureTypeFieldEnricher, textFormatter, false));

            // override annot for enrichment with parents and synonyms
            keyMap.put(SolrFieldName.annot, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_ANNOTATIONS_I), annotationTopicToEnrichConverter, annotFormatter, false));

            // override xrefs, pxrefA and pxrefB for enrichemnt with synonyms
            keyMap.put(SolrFieldName.pxrefA, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_A), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.pxrefB, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_B), fieldEnricherConverter, textFormatter, false));
            keyMap.put(SolrFieldName.xref, new SolrFieldUnit(Arrays.asList(InteractionKeys.KEY_XREFS_I), fieldEnricherConverter, textFormatter, false));
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

        confidenceSelectiveAdder = new ConfidenceScoreSelectiveAdder(FieldNames.INTACT_SCORE_NAME.toLowerCase());
        geneSelectiveAdder = new GeneNameSelectiveAdder();
        idATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        idBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);
        altidATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        altidBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);

        // override the static initializeKeyMap with the field enricher initialized
        overrideKeyMap();
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

        confidenceSelectiveAdder = new ConfidenceScoreSelectiveAdder(FieldNames.INTACT_SCORE_NAME.toLowerCase());
        geneSelectiveAdder = new GeneNameSelectiveAdder();
        idATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        idBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);
        altidATypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_A,
                InteractionKeys.KEY_INTERACTOR_TYPE_A);
        altidBTypeSelectiveAdder = new ByInteractorTypeRowDataAdder(InteractionKeys.KEY_ALTID_B,
                InteractionKeys.KEY_INTERACTOR_TYPE_B);

        // override the static initializeKeyMap with the field enricher initialized
        overrideKeyMap();
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
        addCustomFields(row, doc, confidenceSelectiveAdder);

        // gene names
        addCustomFields(row, doc, geneSelectiveAdder);

        // add intact by interactor type for idA or idB and if not in altidA/altidB
        if (! addCustomFields(row, doc, idATypeSelectiveAdder)){
            addCustomFields(row, doc, altidATypeSelectiveAdder);
        }
        if (! addCustomFields(row, doc, idBTypeSelectiveAdder)){
            addCustomFields(row, doc, altidBTypeSelectiveAdder);
        }

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

    private boolean addCustomFields(Row row, SolrInputDocument doc, RowDataSelectiveAdder selectiveAdder) {
        return selectiveAdder.addToDoc(doc, row);
    }
}