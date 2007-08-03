/**
 * 
 */
package uk.ac.ebi.intact.interolog.proteome.integr8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Parser for proteome_report Integr8 file.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 1 juin 07
 */
public class ProteomeReportParser {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static final Log log = LogFactory.getLog(ProteomeReportParser.class);
	
	
	/*----------------------INSTANCES-----------------------*/
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	
	public ProteomeReportParser() {
		
	}
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	
	
	///////////////////////////////
	////////// OBJECT OVERRIDE
	/////////

	@Override
	public String toString() {
		StrBuilder buf = new StrBuilder();
		buf.append("not implemented");
		return buf.toString();
	}
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	/**
	 * Parse a line of the file and create a ProteomeReport object.
	 * @param line
	 * @return
	 */
	private static ProteomeReport parseLine(String line) {
		String[] words = line.split("\t");
		
		if (words.length!=5) {
			throw new IllegalArgumentException("File corrupted. We must have 5 columns and we have "+words.length);
		}
		
		ProteomeReport p = new ProteomeReport();
		p.setProteomeId( Integer.parseInt(words[0]) );
		p.setProteomeTaxid( Integer.parseInt(words[1]) ); 
		p.setFullName(words[2]);
		p.setProteomeName(words[3]);
		p.setSize( Integer.parseInt(words[4]) );
		return p;
	}
	
	/**
	 * Parse proteome_report file from Integr8 ftp site.
	 * Return a collection of ProteomeReport.
	 * @param file
	 * @return
	 */
	public static Collection<ProteomeReport> parse(File file) {
		Collection<ProteomeReport> entries = new ArrayList<ProteomeReport>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader( new FileReader( file ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		
		try {
			while ( (line = in.readLine()) != null) {
				ProteomeReport p = parseLine(line);
				entries.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	     
	     try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return entries;
	}
	
	/**
	 * Parse proteome_report file from Integr8 ftp site.
	 * Return a map proteomeId to NCBI taxids.
	 * If taxids is not null, we only get these NCBI taxids.
	 * 
	 * @param file
	 * @return
	 */
	public static Map<Long, Long> getProteomeid2NcbiTaxid(File file, Collection<Long> taxids) {
		Map<Long, Long> map = new HashMap<Long, Long>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader( new FileReader( file ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		int count = 0;
		
		try {
			while ( (line = in.readLine()) != null) {
				count++;
				ProteomeReport p = parseLine(line);
				if (taxids==null || taxids.contains( new Long(p.getProteomeTaxid()) )) {
					map.put( new Long(p.getProteomeId()), new Long(p.getProteomeTaxid()) );
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
		
		log.warn(count+" lines in the file "+file.getName());
		return map;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File proteome = new File("/Users/mmichaut/Documents/EBI/data/Integr8/proteome_report.txt");
		Collection<ProteomeReport> proteomes = parse(proteome);
		System.out.println(proteomes.size()+" proteomes");
		Collection<Long> taxids = new HashSet<Long>(3);
		taxids.add(1148l);
		Map<Long, Long> map = getProteomeid2NcbiTaxid(proteome, null);//taxids);
		System.out.println(map.size());

	}
	
	

}
