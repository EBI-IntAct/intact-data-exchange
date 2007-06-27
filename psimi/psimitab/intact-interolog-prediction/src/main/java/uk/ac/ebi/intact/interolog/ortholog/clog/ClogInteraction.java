/**
 * 
 */
package uk.ac.ebi.intact.interolog.ortholog.clog;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.interolog.mitab.MitabUtils;
import uk.ac.ebi.intact.interolog.prediction.InterologPrediction;
import uk.ac.ebi.intact.interolog.prediction.InterologPredictionException;


/**
 * Represent an interaction between 2 clogs.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 30 avr. 07
 */
public class ClogInteraction {

	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static final Log log = LogFactory.getLog(ClogInteraction.class);
	
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
	 * First clog in the interaction.
	 */
	private Clog clogA;
	
	/**
	 * Second clog in the interaction.
	 */
	private Clog clogB;
	
	/**
	 * Binary interactions which have lead to this clog interaction.
	 */
	private Collection<BinaryInteraction> sourceInteractions;
	
	
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	public ClogInteraction(Clog clogA, Clog clogB) {
		if (clogA.getId() <= clogB.getId()) {
			this.clogA = clogA;
			this.clogB = clogB;
		} else {
			this.clogA = clogB;
			this.clogB = clogA;
		}
		setSourceInteractions(new HashSet<BinaryInteraction>());
	}
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	
	public Clog getClogA() {
		return this.clogA;
	}

	public Clog getClogB() {
		return this.clogB;
	}
	
	public Collection<BinaryInteraction> getSourceInteractions() {
		return this.sourceInteractions;
	}

	public void setSourceInteractions(
			Collection<BinaryInteraction> sourceInteractions) {
		this.sourceInteractions = sourceInteractions;
	}
	
	public static int getNB_LINES_MAX() {
		return NB_LINES_MAX;
	}

	public static void setNB_LINES_MAX(int nb_lines_max) {
		NB_LINES_MAX = nb_lines_max;
	}
	
	///////////////////////////////
	////////// OBJECT OVERRIDE
	/////////

	@Override
	public String toString() {
		StrBuilder buf = new StrBuilder();
		buf.append(getClogA().getId()).append(SEP);
		buf.append(getClogB().getId()).append(SEP);
		// TODO order proteins ac and species ids
		// see the clog file to have a complete description
		//buf.appendWithSeparators(getClogA().getProteomeId2protein().values(), ",").append(SEP);
		//buf.appendWithSeparators(getClogB().getProteomeId2protein().values(), ",").append(SEP);
		//buf.appendWithSeparators(getClogA().getProteomeId2protein().keySet(), ",").append(SEP);
		//buf.appendWithSeparators(getClogB().getProteomeId2protein().keySet(), ",").append(SEP);
		//buf.appendWithSeparators(getSourceInteractions(), ",");
		buf.append(MitabUtils.resume(getSourceInteractions()));
		
		return buf.toString();
	}
	
	

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((this.clogA == null) ? 0 : this.clogA.hashCode());
		result = PRIME * result + ((this.clogB == null) ? 0 : this.clogB.hashCode());
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
		final ClogInteraction other = (ClogInteraction) obj;
		if (this.clogA == null) {
			if (other.clogA != null)
				return false;
		} else if (!this.clogA.equals(other.clogA))
			return false;
		if (this.clogB == null) {
			if (other.clogB != null)
				return false;
		} else if (!this.clogB.equals(other.clogB))
			return false;
		return true;
	}
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	/**
	 * Check if this clog interaction has been predicted by an interaction between the same identifiers.
	 * @param idA
	 * @param idB
	 * @return
	 */
	public boolean isPredictedBy(String idA, String idB) {
		for (BinaryInteraction source : this.getSourceInteractions()) {
			if (areInteractorIds(source, idA, idB)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check if identifiers of the interaction are idA and idB.
	 * The order of the id is not taken into account.
	 * @param interactions
	 * @param idA
	 * @param idB
	 * @return
	 */
	private static boolean areInteractorIds(BinaryInteraction interaction, String idA, String idB) {
		Collection<CrossReference> refsA = interaction.getInteractorA().getIdentifiers();
		String uniprotA = InterologPrediction.getIdentifier(refsA, InterologPrediction.getUNIPROTKB());
				
		Collection<CrossReference> refsB = interaction.getInteractorB().getIdentifiers();
		String uniprotB = InterologPrediction.getIdentifier(refsB, InterologPrediction.getUNIPROTKB());
		
		if (uniprotA!=null && uniprotB!=null) {
			return (  (uniprotA.equals(idA) && uniprotB.equals(idB))  || (uniprotB.equals(idA) && uniprotA.equals(idB)) );
		}
		
		return false;
	}
	
	/**
	 * @return
	 */
	public static StrBuilder getHeaders() {
		StrBuilder buf = new StrBuilder();
		buf.append("ClogIdA").append(SEP);
		buf.append("ClogIdB").append(SEP);
		//buf.append("ProteinsA").append(SEP);
		//buf.append("ProteinsB").append(SEP);
		//buf.append("SpeciesA").append(SEP);
		//buf.append("SpeciesB").append(SEP);
		buf.append("SourceInteractions");
		return buf;
	}
	
	/**
	 * Print a collection of clog interactions in a StrBuilder.
	 * @param interations
	 * @return
	 */
	public static StrBuilder print(Collection<ClogInteraction> interations) {
		StrBuilder buf = new StrBuilder();
		buf.append(ClogInteraction.getHeaders()).append("\n");
		for (ClogInteraction interaction : interations) {
			buf.append(interaction.toString()).append("\n");
		}
		return buf;
	}
	
	/**
	 * Print a colleciton of clog interactions in a file.
	 * @param interations
	 * @param file
	 * @throws InterologPredictionException 
	 */
	public static void print(Collection<ClogInteraction> interations, File file) throws InterologPredictionException {
		
		if (interations.size()<=NB_LINES_MAX) {
			log.info("======== print clog interaction collection =======");
			log.info("print "+interations.size()+ " clog interactions in file "+file.getName());
			
			FileWriter out=null;
			try {
				out = new FileWriter(file);
			} catch (IOException e) {
				throw new InterologPredictionException("Error while opening fileWriter",e);
			}
			try {
				out.write(print(interations).toString());
			} catch (IOException e) {
				throw new InterologPredictionException("Error while writing in file "+file,e);
			}
			try {
				out.close();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while closing fileWriter",e);
			}
		} else {
			log.info(interations.size()+" clog interactions are not printed in a file because there are more than "+NB_LINES_MAX);
		}
		
	}



	
}
