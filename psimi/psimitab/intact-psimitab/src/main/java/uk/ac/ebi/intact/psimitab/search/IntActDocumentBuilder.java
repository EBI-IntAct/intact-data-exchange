/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import org.apache.lucene.document.Document;

import psidev.psi.mi.search.util.DefaultDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineParser;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

/**
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActDocumentBuilder.java 
 */
public class IntActDocumentBuilder extends DefaultDocumentBuilder {

	private static final String DEFAULT_COL_SEPARATOR = "\t";
	
	public static Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
		String[] tokens = psiMiTabLine.split(DEFAULT_COL_SEPARATOR);
		
		IntActColumnSet columnSet = new IntActColumnSet();
		
		// raw fields
		
		String roleA = tokens[columnSet.getOrder("role_A")];
		String roleB = tokens[columnSet.getOrder("role_B")];
		String propertiesA = tokens[columnSet.getOrder("properties_A")];
		String propertiesB = tokens[columnSet.getOrder("properties_B")];
		String typeA = tokens[columnSet.getOrder("type_A")];
		String typeB = tokens[columnSet.getOrder("type_B")];
		String hostOrganism = tokens[columnSet.getOrder("hostOrganism")];
		
		Document doc = DefaultDocumentBuilder.createDocumentFromPsimiTabLine(psiMiTabLine);
		
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("role_A"),roleA);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("role_B"),roleB);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("properties_A"),propertiesA);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("properties_B"),propertiesB);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("type_A"),typeA);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("type_B"),typeB);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, columnSet.getbyShortname("hostOrganism"),hostOrganism);
		
		return doc;
	}
	
	public static String createPsimiTabLine(Document doc)
	{
    	if (doc == null)
    	{
    		throw new NullPointerException("Document is null");
    	}
    	
		StringBuffer sb = new StringBuffer(256);
		sb.append(DefaultDocumentBuilder.createPsimiTabLine(doc));
		sb.append(doc.get("role_A")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("role_B")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("properties_A")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("properties_B")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("type_A")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("type_B")).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get("hostOrganism")).append(DEFAULT_COL_SEPARATOR);
		
		return  sb.toString();
	}
	
    /**
     * Creates a BinaryInteraction from a lucene document using the new version of psimitab with extra columns
     * @param doc the Document to use
     * @return the binary interaction
     * @throws MitabLineException thrown if there are syntax or other problems parsing the document/line
     */
    public static BinaryInteraction createBinaryInteraction(Document doc) throws MitabLineException
    {
        String line = createPsimiTabLine(doc);

        MitabLineParser parser = new MitabLineParser();
        parser.setBinaryInteractionClass(IntActBinaryInteraction.class);
        parser.setColumnHandler(new IntActColumnHandler());
        
        return parser.parse(line);
    }
	
}
