/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import psidev.psi.mi.search.column.PsimiTabColumn;
import psidev.psi.mi.search.util.DefaultDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineParser;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactColumnHandler;
import uk.ac.ebi.intact.psimitab.IntactColumnHandler;

/**
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntactDocumentBuilder extends DefaultDocumentBuilder {

    public Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
		String[] tokens = psiMiTabLine.split(DEFAULT_COL_SEPARATOR);

        int maxColumns = new IntactColumnSet().getPsimiTabColumns().size();

        if (tokens.length != maxColumns) {
            throw new MitabLineException("This line contains an invalid number of columns: "+tokens.length+". IntAct PSI-MITAB expects "+maxColumns+" columns");
        }

        // raw fields
		String experimentRoleA = tokens[IntactColumnSet.EXPERIMENTAL_ROLE_A.getOrder()];
		String experimentRoleB = tokens[IntactColumnSet.EXPERIMENTAL_ROLE_B.getOrder()];
        String biologicalRoleA = tokens[IntactColumnSet.BIOLOGICAL_ROLE_A.getOrder()];
        String biologicalRoleB = tokens[IntactColumnSet.BIOLOGICAL_ROLE_B.getOrder()];
        String propertiesA = tokens[IntactColumnSet.PROPERTIES_A.getOrder()];
		String propertiesB = tokens[IntactColumnSet.PROPERTIES_B.getOrder()];
		String typeA = tokens[IntactColumnSet.INTERACTOR_TYPE_A.getOrder()];
		String typeB = tokens[IntactColumnSet.INTERACTOR_TYPE_B.getOrder()];
		String hostOrganism = tokens[IntactColumnSet.HOSTORGANISM.getOrder()];
		String expansion = tokens[IntactColumnSet.EXPANSION_METHOD.getOrder()];
        String dataset = tokens[IntactColumnSet.DATASET.getOrder()];

        Document doc = super.createDocumentFromPsimiTabLine( psiMiTabLine );
		
		doc.add(new Field("roles", isolateBracket( experimentRoleA ) + " " + isolateBracket( experimentRoleB )
                + " " + isolateBracket( biologicalRoleA ) + " " + isolateBracket( biologicalRoleB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, IntactColumnSet.EXPERIMENTAL_ROLE_A, experimentRoleA);
		addTokenizedAndSortableField( doc, IntactColumnSet.EXPERIMENTAL_ROLE_B, experimentRoleB);
		addTokenizedAndSortableField( doc, IntactColumnSet.BIOLOGICAL_ROLE_A, biologicalRoleA);
		addTokenizedAndSortableField( doc, IntactColumnSet.BIOLOGICAL_ROLE_B, biologicalRoleB);

        String value = isolateValues(propertiesA) + isolateValues(propertiesB);
		doc.add(new Field("properties", value,
				Field.Store.NO,
				Field.Index.TOKENIZED));

		addTokenizedAndSortableField( doc, IntactColumnSet.PROPERTIES_A, propertiesA);
		addTokenizedAndSortableField( doc, IntactColumnSet.PROPERTIES_B, propertiesB);
		
		doc.add(new Field("interactor_types", isolateBracket(typeA) + " " + isolateBracket(typeB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField( doc, IntactColumnSet.INTERACTOR_TYPE_A, typeA);
		addTokenizedAndSortableField( doc, IntactColumnSet.INTERACTOR_TYPE_B, typeB);
		
		addTokenizedAndSortableField( doc, IntactColumnSet.HOSTORGANISM, hostOrganism);
		
		addTokenizedAndSortableField( doc, IntactColumnSet.EXPANSION_METHOD, expansion);
        addTokenizedAndSortableField( doc, IntactColumnSet.DATASET, dataset);

        return doc;
	}

    public void addTokenizedAndSortableField(Document doc, PsimiTabColumn column, String columnValue)
    {
         doc.add(new Field(column.getShortName(),
                columnValue,
                Field.Store.YES,
                Field.Index.TOKENIZED));

        doc.add(new Field(column.getSortableColumnName(),
        		isolateValues(columnValue),
                Field.Store.NO,
                Field.Index.UN_TOKENIZED));
    }

    public String createPsimiTabLine(Document doc)
	{
    	if (doc == null)
    	{
    		throw new NullPointerException("Document is null");
    	}
    	
		StringBuffer sb = new StringBuffer(256);
        DefaultDocumentBuilder builder = new DefaultDocumentBuilder();
        sb.append(builder.createPsimiTabLine(doc));
		sb.append(doc.get(IntactColumnSet.EXPERIMENTAL_ROLE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.EXPERIMENTAL_ROLE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.BIOLOGICAL_ROLE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.BIOLOGICAL_ROLE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
        sb.append(doc.get(IntactColumnSet.PROPERTIES_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.PROPERTIES_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.INTERACTOR_TYPE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.INTERACTOR_TYPE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.HOSTORGANISM.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntactColumnSet.EXPANSION_METHOD.getShortName())).append(DEFAULT_COL_SEPARATOR);
        sb.append(doc.get(IntactColumnSet.DATASET.getShortName())).append(DEFAULT_COL_SEPARATOR);

        return  sb.toString();
	}
	
    /**
     * Creates a BinaryInteraction from a lucene document using the new version of psimitab with extra columns
     * @param doc the Document to use
     * @return the binary interaction
     * @throws MitabLineException thrown if there are syntax or other problems parsing the document/line
     */
    public BinaryInteraction createBinaryInteraction(Document doc) throws MitabLineException
    {
        String line = createPsimiTabLine(doc);

        MitabLineParser parser = new MitabLineParser();
        parser.setBinaryInteractionClass(IntactBinaryInteraction.class);
        parser.setColumnHandler(new IntactColumnHandler());

        return parser.parse(line);
    }
    
    /**
     * Gets only the value part of a column
     * @param column
     * @return
     */
    public String isolateValues(String column)
    {
        String[] values = column.split("\\|");

        StringBuilder sb = new StringBuilder();

        for (String v : values) {
            if (v.contains(":")) {
                int colonIndex = v.indexOf(":");
                v = v.substring(colonIndex+1);
            }
            
            if (v.contains("(")) {
                v = v.split("\\(")[0];
            }

            sb.append( v ).append( " " );
        }

        sb.trimToSize();
        
        return sb.toString();
    }

    /**
     * Gets only the value part surround with brackets
     * @param column
     * @return
     */
    public String isolateBracket(String column)
    {
        String[] values = column.split("\\|");

        StringBuilder sb = new StringBuilder();

        for (String v : values) {

            if (v.contains("(")) {
                v = v.split("\\(")[1];
                v = v.split("\\)")[0];
            }

            sb.append( v ).append( " " );
        }

        sb.trimToSize();

        return sb.toString();
    }
}
