/**
 * 
 */
package uk.ac.ebi.intact.interolog.proteome.integr8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author mmichaut
 * @version $Id$
 * @since 9 sept. 08
 */
public class TreeSpecies {
	
	/**
	 * All ranks under the species rank.
	 */
	private static Collection<String> SUB_SPECIES = Arrays.asList(new String[]{"no rank", "subspecies","varietas", "forma"});
	
	/**
	 * For each nodes of rank species gives the collection of children taxids.
	 */
	private static Map<Long, Collection<Long>> species;
	
	/**
	 * Gives mapping from taxid to parent taxid for all nodes under species.
	 */
	private static Hashtable<Long,Long> rank = new Hashtable<Long,Long>(200);
	
	/**
	 * Mapping for each node under species to the species taxid parent.
	 */
	private static Hashtable<Long,Long> taxid2speciesRank = new Hashtable<Long,Long>(200);
	
	/**
	 * To know if we can use this class to get taxonomy information.
	 */
	private static boolean INIT = false;
	
	
	
	/**
	 * Parse a specified part of a line from the nodes.dmp file from NCBI taxonomy.
	 * Returns parentId, taxid, isSpecies (=species rank or not).
	 * Fill the mapping from taxid to parent for nodes under species.
	 * @param line
	 * @return
	 */
	private static Long[] parseLine(String line) {
		String[] words = line.split("\t");
		Long[] ParentId = new Long[3];
		
		if (words.length < 5) {
			throw new IllegalArgumentException("File corrupted. We must have at least 5 columns and we have "+words.length);
		}
		
		//parent taxid
		ParentId[0] = Long.parseLong(words[2]);
		//taxid 
		ParentId[1] = Long.parseLong(words[0]);
		// is species?
		ParentId[2] = words[4].equalsIgnoreCase("species") ? 1l : 0l;
		//is under species?
		if (SUB_SPECIES.contains(words[4])) {
			rank.put(ParentId[1], ParentId[0]);
		}
		
		return ParentId;
	}
	
	/**
	 * Fill taxid2speciesRank mapping.
	 */
	private static void createMapping() {
		if (species.isEmpty()) {
			System.out.println("Warning: species from TreeSpecies is empty");
		}
		
		for (Long speciesTaxid : species.keySet()) {
			taxid2speciesRank.put(speciesTaxid, speciesTaxid);
			for (Long childId : species.get(speciesTaxid)) {
				taxid2speciesRank.put(childId, speciesTaxid);
			}
		}
		System.out.println(taxid2speciesRank.size()+" entries in taxid2speciesrand mapping");
	}

	/**
	 * Parse the nodes.dmp file from NCBI taxonomy.
	 * @param file
	 */
	public static void parse (File file) {
		species = new HashMap<Long, Collection<Long>>();
		Hashtable<Long,Long> parents = new Hashtable<Long,Long>();
		HashSet<Long> noSpecies = new HashSet<Long>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader( new FileReader( file ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		
		try {
			while ( (line = in.readLine()) != null) {
				Long[] ParentId = parseLine(line);
				Long parent = ParentId[0];
				Long id = ParentId[1];
				Long root = parents.get(parent);
				Boolean isSpecie = (ParentId[2] == 1l);
				if (!isSpecie)
					noSpecies.add(id);
				if (root == null) {
					root = parent;
				}
				
				if (!isSpecie) {
					Collection<Long> list = species.get(root);
					if (list == null) {
						list = species.get(id);
						if (list != null) {
							for (Long lind : list) {
								parents.put(lind, root);
							}
							species.put(root, list);
							species.remove(id);
						} else {
							list = new HashSet<Long>();
						}
						list.add(id);
						species.put(root, list);
					} else {
						list.add(id);
						Collection<Long> list2 = species.get(id);
						if (list2 != null) {
							for (Long lid : list2)
								list.add(lid);
						}
					}

					parents.put(id, root);
				}
				
			}
			for (Long lid : noSpecies) {
				species.remove(lid);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	     
	     try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		createMapping();
		
		INIT = true;
	}
	

	public static  Map<Long, Collection<Long>> getSpecies () {
		return species;
	}
	
	public static java.util.Hashtable<Long,Long> getRank() {
		return rank;
	}

	public static Hashtable<Long, Long> getTaxid2speciesRank() {
		return taxid2speciesRank;
	}

	public static boolean isINIT() {
		return INIT;
	}
	
	

}
