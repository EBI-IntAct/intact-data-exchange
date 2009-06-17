/**
 * 
 */
package uk.ac.ebi.intact.interolog.ortholog.clog;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.interolog.prediction.InterologPredictionException;



/**
 * Representation of CLustr Ortholog Groups (CLOG).
 * Defined by Paul Kersey from Integr8.
 * 
 * A clog contains a set of proteins from different species.
 * We can have at most one protein from one species.
 * The clog sizes can be from 1 protein to more than 400 proteins.
 * A protein can be in a most one clog.
 * 
 * Proteins are identified by UniprotKB id or IPI id.
 * We note that a single id can be used for different species.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 5 mars 07
 */
public class Clog {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static final Log log = LogFactory.getLog(Clog.class);
	
	
	/**
	 * String used to separate different columns.
	 */
	private final static String SEP = "\t";
	
	/**
	 * When we print some results in a file, we check that there are less lines than that.
	 */
	private static int NB_LINES_MAX = 10000;
	
	
	
	/*----------------------INSTANCES-----------------------*/
	
	/**
	 * Identifier of the clog.
	 */
	private long id;
	
	/**
	 * Name of the clog.
	 */
	private String name;
	
	/**
	 * Access to proteins in this clog by the species proteome id.
	 */
	private Map<Long, String> proteomeId2protein;
	
	/**
	 * NCBI parent taxid to proteins (may be several strains).
	 */
	private Map<Long, Collection<String>> taxid2proteins;
	
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	
	public Clog(long id) {
		setId(id);
		setProteomeId2protein(new HashMap<Long, String>());
		setTaxid2proteins(new HashMap<Long, Collection<String>>());
	}
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	
	public static int getNB_LINES_MAX() {
		return NB_LINES_MAX;
	}


	public static void setNB_LINES_MAX(int nb_lines_max) {
		NB_LINES_MAX = nb_lines_max;
	}
	


	public long getId() {
		return this.id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public String getName() {
		return this.name;
	}



	public void setName(String name) {
		this.name = name;
	}
	
	public Map<Long, String> getProteomeId2protein() {
		return this.proteomeId2protein;
	}

	public void setProteomeId2protein(Map<Long, String> proteins) {
		this.proteomeId2protein = proteins;
	}
	

	public Map<Long, Collection<String>> getTaxid2proteins() {
		return this.taxid2proteins;
	}


	public void setTaxid2proteins(Map<Long, Collection<String>> taxid2proteins) {
		this.taxid2proteins = taxid2proteins;
	}
	
	
	///////////////////////////////
	////////// OBJECT OVERRIDE
	/////////
	
	@Override
	public String toString() {
		StrBuilder buf = new StrBuilder();
		buf.append(getId()).append(SEP);
		buf.append(getTaxid2proteins().size()).append(SEP);
		buf.append(getTaxid2proteins().keySet()).append(SEP);
		buf.append(getProteomeId2protein().size()).append(SEP);
		buf.append(getProteomeId2protein().keySet()).append(SEP);
		buf.append(getProteomeId2protein().values()).append(SEP);
		return buf.toString();
	}
	
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int) (this.id ^ (this.id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Clog other = (Clog) obj;
		if (this.id != other.id)
			return false;
		return true;
	}
	
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	/**
	 * @param ids
	 * @return
	 */
	public Clog keepSpecies(Collection<Long> ids) {
		Map<Long, String> map = getProteomeId2protein();
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			Long id = Long.parseLong(key);
			if (!ids.contains(id)) {
				iter.remove();
			}
		}
		
		return this;
	}
	
	/**
	 * @return
	 */
	public static StrBuilder getHeaders() {
		StrBuilder buf = new StrBuilder();
		buf.append("Id").append(SEP);
		buf.append("Size").append(SEP);
		buf.append("Taxids").append(SEP);
		buf.append("Size").append(SEP);
		buf.append("Species").append(SEP);
		buf.append("Proteins").append(SEP);
		return buf;
	}
	
	/**
	 * @param lineProt
	 * @param lineSp
	 * @return
	 */
	public static Clog parseLine(String lineProt, String lineSp) {
		String[] elementsProt = lineProt.split("\t");
		String[] elementsSp   = lineSp.split("\t");
		if (elementsProt.length!=elementsSp.length) {
			throw new IllegalArgumentException("Prot and species have not the same size");
		}
		long idProt = Long.parseLong(elementsProt[0]);
		long idSp   = Long.parseLong(elementsSp[0]);
		if (idProt != idSp) {
			throw new IllegalArgumentException("Prot and species have not the same clog id");
		}
		
		Clog entry = new Clog(idSp);		
		for (int i=1; i<(elementsProt.length-1); i++) {
			String ac = elementsProt[i];
			Long sp = Long.parseLong( elementsSp[i] );
			entry.getProteomeId2protein().put(sp, ac);
		}
		
		return entry;
	}
	
	/**
	 * @param interations
	 * @return
	 */
	public static StrBuilder print(Collection<Clog> clogs) {
		StrBuilder buf = new StrBuilder();
		buf.append(Clog.getHeaders()).append("\n");
		for (Clog clog : clogs) {
			buf.append(clog.toString()).append("\n");
		}
		return buf;
	}
	
	/**
	 * Print the collection of clog only if less clogs than NB_LINES_MAX.
	 * @param interations
	 * @param file
	 * @throws InterologPredictionException 
	 */
	public static void print(Collection<Clog> clogs, File file) throws InterologPredictionException {
		
		if (clogs.size()<=NB_LINES_MAX) {
			log.info("======== print clog collection =======");
			log.info("print "+clogs.size()+ " clogs in file "+file.getName());
			
			FileWriter out;
			try {
				out = new FileWriter(file);
			} catch (IOException e) {
				throw new InterologPredictionException("Error while opening fileWriter",e);
			}
			
			try {
				out.write(print(clogs).toString());
			} catch (IOException e) {
				throw new InterologPredictionException("Error while writing clogs in file "+file,e);
			}
			
			try {
				out.close();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while closing fileWriter",e);
			}
		} else {
			log.info(clogs.size()+" clog are not printed in a file because there are more than "+NB_LINES_MAX);
		}
		
	}

	/**
	 * Print clogs only for this proteomeId and with the corredsponding protein in the first column;
	 * .
	 * @param clogs
	 * @param file
	 * @param proteomeId
	 * @throws InterologPredictionException
	 */
	public static void printForSpecies(Collection<Clog> clogs, File file, Long proteomeId) throws InterologPredictionException {

		if (clogs.size()<=NB_LINES_MAX) {
			log.info("======== print clog collection for species "+proteomeId+" =======");
			log.info("filter "+clogs.size()+ " clogs and print in file "+file.getName());

			FileWriter out;
			try {
				out = new FileWriter(file);
			} catch (IOException e) {
				throw new InterologPredictionException("Error while opening fileWriter",e);
			}

			StrBuilder buf = new StrBuilder();
			buf.append("ProteinAc").append(SEP);
			buf.append(Clog.getHeaders()).append("\n");
			int printed=0;
			for (Clog clog : clogs) {
				String proteinAc = clog.getProteomeId2protein().get(proteomeId.toString());
				if (proteinAc!=null) {
					buf.append(proteinAc).append(SEP).append(clog.toString()).append("\n");
					printed++;
				}
			}
			
			try {
				out.write(buf.toString());
			} catch (IOException e) {
				throw new InterologPredictionException("Error while writing",e);
			}
			

			try {
				out.close();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while closing fileWriter",e);
			}
			log.warn(printed+" clogs printed");
			
		} else {
			log.info(clogs.size()+" clog are not printed in a file because there are more than "+NB_LINES_MAX);
		}

	}

}
