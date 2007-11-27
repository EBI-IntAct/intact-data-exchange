/**
 * 
 */
package uk.ac.ebi.intact.interolog.prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Class created to have an esay access in command line to predict interactions for one species.
 * You just have to give the mitab file with the source interactions 
 * and the porc file with the orthologous clusters (ftp://ftp.ebi.ac.uk/pub/databases/integr8/porc/proc_gene.dat).
 * 
 * @author mmichaut
 * @version $Id$
 * @since 27 nov. 07
 */
public class RunForOneSpecies {

	/**
	 * @param args
	 * @throws InterologPredictionException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws InterologPredictionException, FileNotFoundException {
		
		if (args.length<4) {
			throw new IllegalArgumentException("Usage: directory mitabFile PorcFile speciesTaxid [log4j-prop-file]");
		}
		
		File dir = new File(args[0]);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(args[0]+" is not a directory");
		}
		
		File mitabFile = new File(args[1]);
		if (!mitabFile.exists()) {
			throw new FileNotFoundException("File "+args[1]+" not found");
		}
		
		File porcFile = new File(args[2]);
		if (!porcFile.exists()) {
			throw new FileNotFoundException("File "+args[2]+" not found");
		}
		
		long taxid = Long.parseLong(args[3]);
		
		if (args.length>4) {
			File propertiesFile = new File(args[4]);
			if (!propertiesFile.exists()) {
				throw new FileNotFoundException("File "+args[4]+" not found");
			} else {
				InterologPrediction.log = InterologPrediction.initLog(propertiesFile.getAbsolutePath());
			}
		}
		
		InterologPrediction up = new InterologPrediction(dir);
		up.setMitab(mitabFile);
		up.setPorc(porcFile);
		Collection<Long> taxids = new HashSet<Long>(1);
		taxids.add(taxid);
		up.setUserTaxidsToDownCast(taxids);
		up.run();

	}

}
