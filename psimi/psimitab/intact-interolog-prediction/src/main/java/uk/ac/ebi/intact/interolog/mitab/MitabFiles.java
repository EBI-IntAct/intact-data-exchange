/**
 * 
 */
package uk.ac.ebi.intact.interolog.mitab;

import java.io.File;

/**
 * All mitab files.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 9 juin 07
 */
public interface MitabFiles {
	
	
	
	///////////////////////////////
	///////// EXPERIMENTAL 
	///////////////////////////////
	
	/**
	 * IntAct mitab file (clustered).
	 */
	public static File MITAB_INTACT = new File("/Users/mmichaut/Documents/EBI/data/IntAct/intact.mitab");
	
	
	
	/**
	 * MINT mitab file (not clustered).
	 */
	public static File MITAB_MINT_SOURCE = new File("/Users/mmichaut/Documents/EBI/data/MINT/mint.mitab");
	
	/**
	 * MINT mitab file (clustered).
	 */
	public static File MITAB_MINT_CLUSTERED = new File("/Users/mmichaut/Documents/EBI/data/MINT/mint.clustered.mitab");
	
	
	
	/**
	 * DIP mitab file (not clustered).
	 */
	public static File MITAB_DIP_SOURCE = new File("/Users/mmichaut/Documents/EBI/data/DIP/dip.mitab");
	
	/**
	 * DIP mitab file (not clustered) but completed by Picr with more Uniprot acc.
	 */
	public static File MITAB_DIP_COMPLETED = new File("/Users/mmichaut/Documents/EBI/data/DIP/dip.completed.mitab");
	
	/**
	 * DIP mitab file (clustered).
	 */
	public static File MITAB_DIP_CLUSTERED = new File("/Users/mmichaut/Documents/EBI/data/DIP/dip.clustered.mitab");
	
	
	
	/**
	 * Both IntAct and MINT mitab files merged together.
	 */
	public static File MITAB_INTACT_MINT = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/IntAct_MINT.mitab");
	
	
	/**
	 * Both IntAct and DIP mitab files merged together.
	 */
	public static File MITAB_INTACT_DIP = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/IntAct_DIP.mitab");
	
	
	/**
	 * Both MINT and DIP mitab files merged together.
	 */
	public static File MITAB_MINT_DIP = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/DIP_MINT.mitab");
	
	
	
	/**
	 * Both IntAct, MINT and DIP mitab files merged together.
	 */
	public static File MITAB_GLOBAL = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/global.mitab");
	
	
	
	
	///////////////////////////////
	///////// CLOG MAPPING
	///////////////////////////////
	
	
	/**
	 * Results of the clog mapping prediction with the global source file.
	 */
	public static File MITAB_PREDICTION_CLOG_GLOBAL = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/clog.predictedInteractions.mitab");
	
	/**
	 * Results of the clog mapping prediction with the global source file after clustering.
	 */
	public static File MITAB_PREDICTION_CLOG_GLOBAL_CLUSTERED = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/clog.predictedInteractions.clustered.mitab");
	
	
	/**
	 * Results of the clog mapping prediction with the intact source file.
	 */
	public static File MITAB_PREDICTION_CLOG_INTACT = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/clog.predictedInteractions.intact.mitab");
	
	/**
	 * Results of the clog mapping prediction with the intact source file after clustering.
	 */
	public static File MITAB_PREDICTION_CLOG_INTACT_CLUSTERED = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/clog.predictedInteractions.intact.clustered.mitab");
	
	
	
	///////////////////////////////
	///////// INTEROLOG
	///////////////////////////////
	
	
	/**
	 * Results of the clog mapping prediction with the global source file.
	 */
	public static File MITAB_PREDICTION_INTEROLOG = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/interolog.mitab");
	
	/**
	 * Results of the clog mapping prediction with the global source file after clustering.
	 */
	public static File MITAB_PREDICTION_INTEROLOG_CLUSTERED = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/interolog.clustered.mitab");
	
	

}
