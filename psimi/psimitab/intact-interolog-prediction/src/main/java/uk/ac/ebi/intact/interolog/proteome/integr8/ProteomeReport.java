/**
 * 
 */
package uk.ac.ebi.intact.interolog.proteome.integr8;


/**
 * Represents an entry in the proteome_report file from Integr8 ftp.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 7 juin 07
 */
public class ProteomeReport {
	

	///////////////////////////////
	////////// FIELDS
	/////////
	
	/*----------------------STATIC-----------------------*/
	
	/**
	 * String used to separate different columns.
	 */
	private final static String SEP = "\t";
	
	
	
	/*----------------------INSTANCES-----------------------*/
	
	private int proteomeId;
	private String proteomeName;
	private int proteomeTaxid;
	private String fullName;
	
	/**
	 * Number of proteins (present in the Proteome_report Integr8 file).
	 */
	private int size;
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	public ProteomeReport() {
		
	}
		
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getProteomeId() {
		return this.proteomeId;
	}

	public void setProteomeId(int proteomeId) {
		this.proteomeId = proteomeId;
	}

	public String getProteomeName() {
		return this.proteomeName;
	}

	public void setProteomeName(String proteomeName) {
		this.proteomeName = proteomeName;
	}

	public int getProteomeTaxid() {
		return this.proteomeTaxid;
	}

	public void setProteomeTaxid(int proteomeTaxid) {
		this.proteomeTaxid = proteomeTaxid;
	}	

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	///////////////////////////////
	////////// OVERRIDES
	/////////
	
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getProteomeName()).append(SEP);
		buf.append(getProteomeId()).append(SEP);
		buf.append(getProteomeTaxid()).append(SEP);
		buf.append(getFullName());
		return buf.toString();
	}
	
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	


}
