/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import psidev.psi.mi.search.util.DefaultDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.Row;
import psidev.psi.mi.tab.model.builder.RowBuilder;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.util.Iterator;

/**
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactDocumentBuilder extends DefaultDocumentBuilder<IntactBinaryInteraction> {

    public IntactDocumentBuilder() {
        super(new IntactDocumentDefinition());
    }

    public Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
        Document doc = super.createDocumentFromPsimiTabLine( psiMiTabLine );

        final RowBuilder rowBuilder = getDocumentDefinition().createRowBuilder();
        final Row row = rowBuilder.createRow(psiMiTabLine);

        if (row.getColumnCount() <= IntactDocumentDefinition.EXPERIMENTAL_ROLE_A) {
            return doc;
        }

        // raw fields
		Column experimentRoleA = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
		Column experimentRoleB = row.getColumnByIndex(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
        Column biologicalRoleA = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
        Column biologicalRoleB = row.getColumnByIndex(IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
        Column propertiesA = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_A);
		Column propertiesB = row.getColumnByIndex(IntactDocumentDefinition.PROPERTIES_B);
		Column typeA = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_A);
		Column typeB = row.getColumnByIndex(IntactDocumentDefinition.INTERACTOR_TYPE_B);
		Column hostOrganism = row.getColumnByIndex(IntactDocumentDefinition.HOST_ORGANISM);
		Column expansion = row.getColumnByIndex(IntactDocumentDefinition.EXPANSION_METHOD);
        Column dataset = row.getColumnByIndex(IntactDocumentDefinition.DATASET);

        // other columns, such as the annotations (corresponding to a second extension of the format)
        // after next section

		doc.add(new Field("roles", isolateDescriptions( experimentRoleA ) + " " + isolateDescriptions( experimentRoleB )
                + " " + isolateDescriptions( biologicalRoleA ) + " " + isolateDescriptions( biologicalRoleB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_A), experimentRoleA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPERIMENTAL_ROLE_B), experimentRoleB);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_A), biologicalRoleA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.BIOLOGICAL_ROLE_B), biologicalRoleB);

        String value = isolateValue(propertiesA) + isolateValue(propertiesB);
		doc.add(new Field("properties", value,
				Field.Store.NO,
				Field.Index.TOKENIZED));

		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_A), propertiesA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.PROPERTIES_B), propertiesB);
		
		doc.add(new Field("interactor_types", isolateDescriptions(typeA) + " " + isolateDescriptions(typeB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_A), typeA);
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.INTERACTOR_TYPE_B), typeB);
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.HOST_ORGANISM), hostOrganism);
		
		addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.EXPANSION_METHOD), expansion);
        addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.DATASET), dataset);


        // second extension

        if (row.getColumnCount() > IntactDocumentDefinition.ANNOTATIONS_B) {
            Column annotationsA = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_A);
            Column annotationsB = row.getColumnByIndex(IntactDocumentDefinition.ANNOTATIONS_B);

            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_A), annotationsA);
            addTokenizedAndSortableField( doc, getDocumentDefinition().getColumnDefinition(IntactDocumentDefinition.ANNOTATIONS_B), annotationsB);

            doc.add(new Field("annotation", isolateValue(annotationsA)+" "+isolateValue(annotationsB),
                    Field.Store.NO,
                    Field.Index.TOKENIZED));
        }

        return doc;
	}

    
    /**
     * Gets only the value part surround with brackets
     * @param column
     * @return
     */
    public String isolateDescriptions(Column column)
    {
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
