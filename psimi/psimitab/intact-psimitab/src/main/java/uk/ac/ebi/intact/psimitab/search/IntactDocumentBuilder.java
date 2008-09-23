/**
 *
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import psidev.psi.mi.search.util.AbstractInteractionDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;
import psidev.psi.mi.tab.model.builder.Row;
import psidev.psi.mi.tab.model.builder.RowBuilder;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.util.ols.OlsClient;
import uk.ac.ebi.intact.util.ols.OlsUtils;
import uk.ac.ebi.intact.util.ols.Term;
import uk.ac.ebi.ook.web.services.Query;

import java.rmi.RemoteException;
import java.util.*;

/**
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactDocumentBuilder extends AbstractInteractionDocumentBuilder<IntactBinaryInteraction> {

    private static final Log log = LogFactory.getLog(IntactDocumentBuilder.class);

    private boolean includeParentsForCvTerms = false;

    private Map<String,List<psidev.psi.mi.tab.model.builder.Field>> parentsMapCache;

    public IntactDocumentBuilder() {
        this(false);
    }

    public IntactDocumentBuilder(boolean includeParentsForCvTerms) {
        super(new IntactDocumentDefinition());
        this.includeParentsForCvTerms = includeParentsForCvTerms;

        parentsMapCache = new HashMap<String,List<psidev.psi.mi.tab.model.builder.Field>>();
    }

    public Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException {
        Document doc = super.createDocumentFromPsimiTabLine(psiMiTabLine);

        final RowBuilder rowBuilder = getDocumentDefinition().createRowBuilder();
        final Row row = rowBuilder.createRow(psiMiTabLine);

        if (row.getColumnCount() <= IntactDocumentDefinition.EXPERIMENTAL_ROLE_A) {
            return doc;
        }

        // raw fields
        final Column experimentRoleA = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
        final Column experimentRoleB = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
        final Column biologicalRoleA = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
        final Column biologicalRoleB = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
        final Column propertiesA = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_A);
        final Column propertiesB = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_B);
        final Column typeA = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_A);
        final Column typeB = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_B);
        final Column hostOrganism = row.getColumnByIndex(IntactDocumentDefinition.HOST_ORGANISM);
        final Column expansion = row.getColumnByIndex(IntactDocumentDefinition.EXPANSION_METHOD);
        final Column dataset = row.getColumnByIndex(IntactDocumentDefinition.DATASET);

        // other columns, such as the annotations (corresponding to a second extension of the format)
        // after next section

        doc.add(new Field("roles", isolateDescriptions(experimentRoleA) + " " + isolateDescriptions(experimentRoleB)
                                   + " " + isolateDescriptions(biologicalRoleA) + " " + isolateDescriptions(biologicalRoleB),
                          Field.Store.NO,
                          Field.Index.TOKENIZED));

        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A), experimentRoleA);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B), experimentRoleB);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_A), biologicalRoleA);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_B), biologicalRoleB);

        String value = isolateValue(propertiesA) + " " + isolateValue(propertiesB);
        doc.add(new Field("properties_exact", value,
                          Field.Store.NO,
                          Field.Index.TOKENIZED));

        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_A), propertiesA);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_B), propertiesB);

        doc.add(new Field("interactor_types", isolateDescriptions(typeA) + " " + isolateDescriptions(typeB),
                          Field.Store.NO,
                          Field.Index.TOKENIZED));

        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_A), typeA);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_B), typeB);

        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.HOST_ORGANISM), hostOrganism);

        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPANSION_METHOD), expansion);
        addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.DATASET), dataset);


        // second extension

        if (row.getColumnCount() > IntactDocumentDefinition.ANNOTATIONS_B) {
            Column annotationsA = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_A);
            Column annotationsB = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_B);

            addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_A), annotationsA);
            addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_B), annotationsB);

            doc.add(new Field("annotation", isolateValue(annotationsA) + " " + isolateValue(annotationsB),
                              Field.Store.NO,
                              Field.Index.TOKENIZED));
        }

        if (row.getColumnCount() > IntactDocumentDefinition.PARAMETERS_INTERACTION) {
            Column parametersA = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_A);
            Column parametersB = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_B);
            Column parametersInteraction = row.getColumnByIndex(IntactDocumentDefinition.PARAMETERS_INTERACTION);


            addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_A), parametersA);
            addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_B), parametersB);
            addTokenizedAndSortableField(doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PARAMETERS_INTERACTION), parametersInteraction);

            doc.add(new Field("parameter", isolateValue(parametersA) + " " + isolateValue(parametersB) + " " + isolateValue(parametersInteraction),
                              Field.Store.NO,
                              Field.Index.TOKENIZED));
        }


        //Expand interaction detection method and interaction type columns by including parent cv terms only for search, no store
        Column detMethodsExtended = row.getColumnByIndex(MitabDocumentDefinition.INT_DET_METHOD);
        Column interactionTypesExtended = row.getColumnByIndex(MitabDocumentDefinition.INT_TYPE);
        Column propertiesAExtended = propertiesA;
        Column propertiesBExtended = propertiesB;

        if (includeParentsForCvTerms) {
            detMethodsExtended = getColumnWithParents(detMethodsExtended);
            interactionTypesExtended = getColumnWithParents(interactionTypesExtended);
            propertiesAExtended = getColumnWithParents(propertiesAExtended);
            propertiesBExtended = getColumnWithParents(propertiesBExtended);
        }

        doc.add(new Field("detmethod", detMethodsExtended.toString(),
                          Field.Store.YES,
                          Field.Index.TOKENIZED));
        doc.add(new Field("type", interactionTypesExtended.toString(),
                          Field.Store.YES,
                          Field.Index.TOKENIZED));
        doc.add(new Field("properties", propertiesAExtended.toString()+Column.FIELD_DELIMITER+propertiesBExtended,
                          Field.Store.YES,
                          Field.Index.TOKENIZED));

        return doc;
    }


    public String isolateIdentifier(Column column) {
        StringBuilder sb = new StringBuilder(256);

        for (Iterator<psidev.psi.mi.tab.model.builder.Field> iterator = column.getFields().iterator(); iterator.hasNext();) {
            psidev.psi.mi.tab.model.builder.Field field = iterator.next();

            sb.append(field.getType()).append(":").append(field.getValue());


            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }


    public Column getColumnWithParents(Column cvColumn) {

        Column column = new Column();

        for (psidev.psi.mi.tab.model.builder.Field field : cvColumn.getFields()) {

            List<psidev.psi.mi.tab.model.builder.Field> currentFieldListWithParents = getListOfFieldsWithParents(field);
            column.getFields().addAll(currentFieldListWithParents);

        }//end for

        return column;
    }

    private boolean isGoField(psidev.psi.mi.tab.model.builder.Field field) {
        return OlsUtils.GO_ONTOLOGY.equalsIgnoreCase(field.getType());
    }

    private String prefixIdentifierIfNecessary(String identifier) {
        if (!identifier.startsWith(OlsUtils.PSI_MI_ONTOLOGY)) {
            return OlsUtils.PSI_MI_ONTOLOGY+":"+identifier;
        }
        return identifier;
    }

    private boolean isPsiMiField(psidev.psi.mi.tab.model.builder.Field field) {
        return OlsUtils.PSI_MI_ONTOLOGY.equals(field.getType())
                || "psi-mi".equals(field.getType());
    }

    /**
     * @param field the field for which we want to get the parents
     * @return list of cv terms with parents and itself
     * @throws RemoteException thrown by OlsUtils
     */
    private List<psidev.psi.mi.tab.model.builder.Field> getAllParents(psidev.psi.mi.tab.model.builder.Field field) {
        if (parentsMapCache.containsKey(field.getValue())) {
            return parentsMapCache.get(field.getValue());
        }

        List<psidev.psi.mi.tab.model.builder.Field> allParents = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();
        try {
            OlsClient olsClient = new OlsClient();
            Query ontologyQuery = olsClient.getOntologyQuery();

            Collection<Term> terms = new ArrayList<Term>();

            if (isPsiMiField(field)) {
                String identifier = prefixIdentifierIfNecessary(field.getValue());
                terms = OlsUtils.getAllParents(identifier, OlsUtils.PSI_MI_ONTOLOGY, ontologyQuery, new ArrayList<Term>(), true);

            } else if (isGoField(field)) {
                terms = OlsUtils.getAllParents(field.getValue(), OlsUtils.GO_ONTOLOGY, ontologyQuery, new ArrayList<Term>(), true);
            }

            allParents = convertTermsToFields(field.getType(), terms);

        } catch (RemoteException e) {
            throw new IllegalStateException("Couldn't fetch parent terms for field: "+field, e);
        }

        parentsMapCache.put(field.getValue(), allParents);

        return allParents;

    }

    private List<psidev.psi.mi.tab.model.builder.Field> convertTermsToFields(String type, Collection<Term> terms) {
        List<psidev.psi.mi.tab.model.builder.Field> fields = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();

        for (Term term : terms) {
            psidev.psi.mi.tab.model.builder.Field field =
                    new psidev.psi.mi.tab.model.builder.Field(type, term.getId(), term.getName());
            fields.add(field);
        }

        return fields;
    }

    public List<psidev.psi.mi.tab.model.builder.Field> getListOfFieldsWithParents(psidev.psi.mi.tab.model.builder.Field field) {
        if (field == null) {
            throw new NullPointerException("You must give a non null field");
        }

        List<psidev.psi.mi.tab.model.builder.Field> fields = new ArrayList<psidev.psi.mi.tab.model.builder.Field>();

        //Construct new field
        fields.add(field);

        //deal with parents
        List<psidev.psi.mi.tab.model.builder.Field> allParents = getAllParents(field);
        fields.addAll(allParents);

        return fields;
    }

    /**
     * Gets only the value part surround with brackets
     *
     * @param column the Column with a collection of fields from which the desc has to be isolated
     * @return a concatinated String of desc
     */
    public String isolateDescriptions(Column column) {
        StringBuilder sb = new StringBuilder(256);

        for (Iterator<psidev.psi.mi.tab.model.builder.Field> iterator = column.getFields().iterator(); iterator.hasNext();) {
            psidev.psi.mi.tab.model.builder.Field field = iterator.next();

            if (field.getDescription() != null) {
                sb.append(field.getDescription());
            }

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
