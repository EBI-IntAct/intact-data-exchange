/**
 * 
 */
package uk.ac.ebi.intact.interolog.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.text.StrBuilder;

/**
 * @author mmichaut
 * @version $Id$
 * @since 6 juin 07
 */
public class InterologUtils {
	
	/**
	 * Put in a StrBuilder some information about memory.
	 * @return
	 */
	public static StrBuilder printRuntime() {
		StrBuilder buf = new StrBuilder();
		
		Long fm = Runtime.getRuntime().freeMemory() / (1024*1024);
		Long mm = Runtime.getRuntime().maxMemory() / (1024*1024);// alloue en Bytes / 1024*1024 -> M
		Long tm = Runtime.getRuntime().totalMemory() / (1024*1024); // mx
		Long um = tm - fm;
		
		buf.append("Used Memory: ").append(um).append(" M - ");
		buf.append("Free Memory: ").append(fm).append(" M - ");
		buf.append("Total Memory: ").append(tm).append(" M - ");
		buf.append("Max Memory: ").append(mm).append(" M");
		
		return buf;
	}

	/**
	 * Get all values of all collections in the map.
	 * @param map
	 * @return
	 */
	public static Collection getAllValues(Map<Long, Collection<Long>> map) {
		Collection<Long> values = new HashSet<Long>(map.size());
		for (Long key : map.keySet()) {
			values.addAll(map.get(key));
		}
		return values;
	}
	
	
	/**
	 * Pattern of UniprotKB identifiers;
	 * Word of size 6 (e.g. P12345) with eventuel version (e.g. -1);
	 */
	private final static String UNIPROT_ACC_PATTERN = "\\w{6}(-[0-9])?";
	
	/**
	 * @param id
	 * @return
	 */
	public static boolean matchUniprotKbId(String id) {
		//boolean test = id.length()<9 || id.length()>=6;
		Pattern p = Pattern.compile(UNIPROT_ACC_PATTERN);
		Matcher m = p.matcher(id);
		return m.matches();
	}

}
