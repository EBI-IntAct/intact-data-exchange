/**
 * 
 */
package uk.ac.ebi.intact.interolog.ortholog.clog;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * Parser for clog data file.
 * 
 * The temporary format of clog files may change;
 * This parser will have to be updated;
 * 
 * @author mmichaut
 * @version $Id$
 * @since 5 mars 07
 */
public class ClogParser {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static final Log log = LogFactory.getLog(ClogParser.class);
	
	
	/*----------------------INSTANCES-----------------------*/
	
	
	/**
	 * File describing for each clog all proteins in it (Uniprot acc).
	 */
	private File clogRepeatFile;
	
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	
	public ClogParser() {
	}
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	

	public File getClogRepeatFile() {
		return this.clogRepeatFile;
	}

	public void setClogRepeatFile(File clogRepeatFile) {
		this.clogRepeatFile = clogRepeatFile;
	}
	
	
	
	///////////////////////////////
	////////// OBJECT OVERRIDE
	/////////

	@Override
	public String toString() {
		StrBuilder buf = new StrBuilder();
		buf.append(this.getClogRepeatFile().getName());
		return buf.toString();
	}
	
	
	///////////////////////////////
	////////// METHODS
	/////////	
	
	
	/**
	 * Create the map to describe all clogs (access by their id) using clog file
	 * First version for revised data;
	 * .
	 * @param spIds
	 * @return
	 */
	public Map<Long, Clog> parseClog() {
		Map<Long, Clog> clogId2clog = new HashMap<Long, Clog>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader( new FileReader( getClogRepeatFile() ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		int count=0;
		
		try {
			while ( ( line = in.readLine() ) != null ) {
				count++;
				// clogid proteinAc:proteomeId; proteinAc:proteomeId1;proteomId2;
				Pattern p = Pattern.compile("^(\\d*)(\\s)(.*)");
				Matcher m = p.matcher(line);
				
				if (m.matches()) {
					Long id = Long.parseLong(m.group(1));
					String[] proteins = m.group(3).split("\\t");
					
					Clog clog = new Clog(id);
					for (int i = 0; i < proteins.length; i++) {
						p = Pattern.compile("(\\w*):(.*)");
						m = p.matcher(proteins[i]);
						
						if (m.matches()) {
							String proteinAc = m.group(1);
							String[] proteomeIds = m.group(2).split(";");
							for (int j = 0; j < proteomeIds.length; j++) {
								Long proteomeId = Long.parseLong( proteomeIds[j] );
								clog.getProteomeId2protein().put(proteomeId, proteinAc);
							}
							
						} else {
							log.warn(proteins[i]+" does not match protein and proteome ids description");
							continue;
						}
						
						
					}
					clogId2clog.put(id, clog);
				} else {
					log.warn(line+" does not match clog data pattern");
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	     
	     try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return clogId2clog;
	}
	

	/**
	 * @param args 
	 */
	public static void main(String[] args){
		
		File clogRepeat  = new File("/Users/mmichaut/Documents/EBI/data/Integr8/clog/20070630/clog.revised.dat");
		ClogParser parser = new ClogParser();
		parser.setClogRepeatFile(clogRepeat);
		Map<Long, Clog> clogId2clog = parser.parseClog();
		System.out.println(clogId2clog.size());
		
	}

}
