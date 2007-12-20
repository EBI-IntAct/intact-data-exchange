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
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.0
 */
public class IntActDocumentBuilder extends DefaultDocumentBuilder {

	private Document doc;
	
	public Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
		String[] tokens = psiMiTabLine.split(DEFAULT_COL_SEPARATOR);
			
		// raw fields
		String experimentRoleA = tokens[IntActColumnSet.EXPERIMENTAL_ROLE_A.getOrder()];
		String experimentRoleB = tokens[IntActColumnSet.EXPERIMENTAL_ROLE_B.getOrder()];
        String biologicalRoleA = tokens[IntActColumnSet.BIOLOGICAL_ROLE_A.getOrder()];
        String biologicalRoleB = tokens[IntActColumnSet.BIOLOGICAL_ROLE_B.getOrder()];
        String propertiesA = tokens[IntActColumnSet.PROPERTIES_A.getOrder()];
		String propertiesB = tokens[IntActColumnSet.PROPERTIES_B.getOrder()];
		String typeA = tokens[IntActColumnSet.INTERACTOR_TYPE_A.getOrder()];
		String typeB = tokens[IntActColumnSet.INTERACTOR_TYPE_B.getOrder()];
		String hostOrganism = tokens[IntActColumnSet.HOSTORGANISM.getOrder()];
		String expansion = tokens[IntActColumnSet.EXPANSION_METHOD.getOrder()];
        String dataset = tokens[IntActColumnSet.DATASET.getOrder()];

        doc = super.createDocumentFromPsimiTabLine(psiMiTabLine);
		
		doc.add(new Field("roles", isolateBracket( experimentRoleA ) + " " + isolateBracket( experimentRoleB )
                + " " + isolateBracket( biologicalRoleA ) + " " + isolateBracket( biologicalRoleB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField(doc, IntActColumnSet.EXPERIMENTAL_ROLE_A, experimentRoleA);
		addTokenizedAndSortableField(doc, IntActColumnSet.EXPERIMENTAL_ROLE_B, experimentRoleB);
		addTokenizedAndSortableField(doc, IntActColumnSet.BIOLOGICAL_ROLE_A, biologicalRoleA);
		addTokenizedAndSortableField(doc, IntActColumnSet.BIOLOGICAL_ROLE_B, biologicalRoleB);

        String value = isolateValues(propertiesA) + isolateValues(propertiesB);
		doc.add(new Field("properties", value,
				Field.Store.NO,
				Field.Index.TOKENIZED));

		addTokenizedAndSortableField(doc, IntActColumnSet.PROPERTIES_A, propertiesA);
		addTokenizedAndSortableField(doc, IntActColumnSet.PROPERTIES_B, propertiesB);
		
		doc.add(new Field("interactor_types", isolateBracket(typeA) + " " + isolateBracket(typeB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		addTokenizedAndSortableField(doc, IntActColumnSet.INTERACTOR_TYPE_A, typeA);
		addTokenizedAndSortableField(doc, IntActColumnSet.INTERACTOR_TYPE_B, typeB);
		
		addTokenizedAndSortableField(doc, IntActColumnSet.HOSTORGANISM, hostOrganism);
		
		addTokenizedAndSortableField(doc, IntActColumnSet.EXPANSION_METHOD, expansion);
        addTokenizedAndSortableField(doc, IntActColumnSet.DATASET, dataset);

        return doc;
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
		sb.append(doc.get(IntActColumnSet.EXPERIMENTAL_ROLE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.EXPERIMENTAL_ROLE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.BIOLOGICAL_ROLE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.BIOLOGICAL_ROLE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
        sb.append(doc.get(IntActColumnSet.PROPERTIES_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.PROPERTIES_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.INTERACTOR_TYPE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.INTERACTOR_TYPE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.HOSTORGANISM.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.EXPANSION_METHOD.getShortName())).append(DEFAULT_COL_SEPARATOR);
        sb.append(doc.get(IntActColumnSet.DATASET.getShortName())).append(DEFAULT_COL_SEPARATOR);

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
        parser.setBinaryInteractionClass(IntActBinaryInteraction.class);
        parser.setColumnHandler(new IntActColumnHandler());

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
}
