/**
 * 
 */
package uk.ac.ebi.intact.interolog.prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.utils.PsiCollectionUtils;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.interolog.download.Sucker;
import uk.ac.ebi.intact.interolog.mitab.MitabException;
import uk.ac.ebi.intact.interolog.mitab.MitabStats;
import uk.ac.ebi.intact.interolog.mitab.MitabUtils;
import uk.ac.ebi.intact.interolog.ortholog.clog.Clog;
import uk.ac.ebi.intact.interolog.ortholog.clog.ClogInteraction;
import uk.ac.ebi.intact.interolog.ortholog.clog.ClogParser;
import uk.ac.ebi.intact.interolog.proteome.integr8.ProteomeReport;
import uk.ac.ebi.intact.interolog.proteome.integr8.ProteomeReportParser;
import uk.ac.ebi.intact.interolog.util.InterologUtils;
import uk.ac.ebi.intact.interolog.util.NewtException;
import uk.ac.ebi.intact.interolog.util.NewtUtils;

/**
 * @author mmichaut
 * @version $Id$
 * @since 30 avr. 07
 */
public class InterologPrediction {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static final Log log = LogFactory.getLog(InterologPrediction.class);
	
	/**
	 * Integr8 ftp proteome report file is used to link proteome id to NCBI taxid.
	 */
	private static File FILE_DATA_PROTEOME = new File("/Users/mmichaut/Documents/EBI/data/Integr8/proteome_report.txt");
	
	/**
	 * File used to print the down-cast history.
	 */
	private static File FILE_HISTORY_DOWN_CAST;
	
	/**
	 * File used to print all clog interactions if asked.
	 */
	private static File FILE_RESULT_CLOG_INTERACTIONS;
	
	/**
	 * URL of the proteome file from Integr8 ftp.
	 */
	private static String URL_PROTEOME_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/integr8/proteome_report.txt";
	
	/**
	 * URL of Integr8 ftp where clog data are;
	 * Not known yet.
	 */
	private static String URL_CLOG_FILE;
	
	/**
	 * Used to read and write cross-ref in mitab format.
	 */
	private static final String UNIPROTKB = "uniprotkb";
	
	

	
	/*----------------------INSTANCES-----------------------*/
	
	/**
	 * File with source interactions.
	 */
	private File mitab;
	
	/**
	 * Tell if mitab interaction file has headers or not.
	 */
	private boolean mitabHasHeaders = true;
	
	/**
	 * File with clog data.
	 */
	private File clog;
	
	/**
	 * The new clog format contains one protein by line.
	 * True by default.
	 * If false we can use the old parser with one clog by line.
	 */
	private boolean clogFormatByProtein = true;
	
	/**
	 * Name of the result file with predicted interactions in mitab format.
	 */
	private String predictedinteractionsFileName;
	
	/**
	 * Extension used for the mitab file generated with predicted interactions.
	 */
	private String predictedinteractionsFileExtension = ".txt";
	
	
	/**
	 * All NCBI taxids present in the mitab file.
	 */
	private Collection<Long> mitabTaxids;
	
	/**
	 * All proteomeIds present in the clog file.
	 */
	private Collection<Long> clogTaxids;
	
	/**
	 * Get the first level of children in the taxonomy;
	 */
	private Map<Long, Collection<Long>> taxid2ChildrenTaxids = getChildrenMapManually();
	
	/**
	 * List of species for which we want predict interactions (proteomeIds).
	 */
	private Collection<Long> proteomeIdsToDownCast;
	
	/**
	 * If we want to put all taxids present in the mitab source file into the list of species
	 * for which we want to predict interactions.
	 * Can be useful to remove it if we are only interested in 1 or 2 species.
	 * 
	 * Default is true.
	 */
	private boolean downCastOnAllPresentSpecies = true;
	
	/**
	 * List of species for which user wants predict interactions (NCBI taxids).
	 * Are added in the default list.
	 */
	private Collection<Long> userTaxidsToDownCast;
	
	/**
	 * Different sources (mitab and clog data) can be in a different level of specificity in the taxonomy.
	 */
	private Map<Long, Collection<Long>> mitabTaxid2ClogTaxid;
	
	/**
	 * If you want to search mitab taxid children in the taxonomy to transfer on these species too.
	 */
	private boolean downCastOnChildren = false;
	
	/**
	 * Working directory.
	 * Contains source files:
	 * proteome_report.txt from Integr8 to link proteomeId and NCBI taxid.
	 * mitab file used to predict interactions.
	 * Clog data file which describes each clog with its proteins and species (has to be given by Paul).
	 * TODO initialize with a system variable
	 */
	private File workingDir;
	
	/**
	 * List of all clog with access by id.
	 */
	private Map<Long, Clog> clogId2Clog;
	
	/**
	 * Mapping from protein (ac+species) to Clog id in which we find this protein.
	 */
	private Map<String, Long> protein2clog;
	
	/**
	 * Map proteome_id to Taxid.
	 */
	private Map<Long, Long> proteomeId2Taxid = new HashMap<Long, Long>();
	
	/**
	 * Map NCBI taxid to proteomeId.
	 */
	private Map<Long, Long> taxid2proteomeId = new HashMap<Long, Long>();
	
	/**
	 * List of clog interactions that have been up-casted.
	 */
	private Collection<ClogInteraction> clogInteractions;
	
	/**
	 * Predicted interactions by the down-cast of all clog interactions.
	 */
	private Collection<BinaryInteraction> interactions;
	
	/**
	 * We can specify if we want to print the trace of the down-cast process in a file.
	 */
	private boolean writeDownCastHistory = false;
	
	/**
	 * We can specify if we want to print clog interactions in a file.
	 */
	private boolean writeClogInteractions = false;
	
	/**
	 * Used to print the history of the down-cast process.
	 */
	private PrintStream downCastHistory;
	
	/**
	 * Time when the process begins.
	 */
	private long start;
	
	/**
	 * Interactions that are not predicted because they were the source of the clog interaction.
	 */
	private int sourceInteraction = 0;
	
	/**
	 * 
	 */
	private int noMatchUniprotId = 0;
	
	/**
	 * 
	 */
	private int IPIid = 0;
	
	
	///////////////////////////////
	////////// CONSTRUCTORS
	/////////
	
	
	public InterologPrediction(File workingDir) {
		setWorkingDir(workingDir);
		clog = new File(workingDir.getAbsolutePath()+"/clog.dat");
		mitab = new File(workingDir.getAbsolutePath()+"/interactions.mitab");
		
		clogInteractions = new ArrayList<ClogInteraction>();
		protein2clog = new HashMap<String, Long>();
		setInteractions(new ArrayList<BinaryInteraction>());
		mitabTaxids = new HashSet<Long>();
		setClogTaxids(new HashSet<Long>());
		
		proteomeId2Taxid = new HashMap<Long, Long>();
		taxid2proteomeId = new HashMap<Long, Long>();
		proteomeIdsToDownCast = new HashSet<Long>();
	}
	
	public InterologPrediction(File workingDir, File mitab, File clog) {
		this(workingDir);
		setMitab(mitab);
		setClog(clog);
	}
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	

	public static File getFILE_HISTORY_DOWN_CAST() {
		return FILE_HISTORY_DOWN_CAST;
	}


	public static File getFILE_RESULT_CLOG_INTERACTIONS() {
		return FILE_RESULT_CLOG_INTERACTIONS;
	}
	
	public static String getUNIPROTKB() {
		return UNIPROTKB;
	}

	public String getPredictedinteractionsFileExtension() {
		return predictedinteractionsFileExtension;
	}

	public void setPredictedinteractionsFileExtension(String mitabFileExtension) {
		if (!mitabFileExtension.startsWith(".")) {
			mitabFileExtension = "."+mitabFileExtension;
		}
		this.predictedinteractionsFileExtension = mitabFileExtension;
	}

	public File getMitab() {
		return this.mitab;
	}
	
	public void setMitab(File mitab) {
		log.info("set mitab file: "+mitab.getName());
		this.mitab = mitab;
	}
	
	public boolean isMitabHasHeaders() {
		return this.mitabHasHeaders;
	}

	public void setMitabHasHeaders(boolean mitabHasHeaders) {
		this.mitabHasHeaders = mitabHasHeaders;
	}
	
	public File getClog() {
		return this.clog;
	}
	
	public void setClog(File clog) {
		log.info("set clog file: "+clog.getName());
		this.clog = clog;
	}
	
	public boolean isClogFormatByProtein() {
		return this.clogFormatByProtein;
	}

	public void setClogFormatByProtein(boolean clogFormatByProtein) {
		this.clogFormatByProtein = clogFormatByProtein;
	}
	
	public String getPredictedinteractionsFileName() {
		return this.predictedinteractionsFileName;
	}

	public void setPredictedinteractionsFileName(
			String predictedinteractionsFileName) {
		this.predictedinteractionsFileName = predictedinteractionsFileName;
	}
	
	public boolean isDownCastOnAllPresentSpecies() {
		return this.downCastOnAllPresentSpecies;
	}

	public void setDownCastOnAllPresentSpecies(boolean downCastOnAllPresentSpecies) {
		this.downCastOnAllPresentSpecies = downCastOnAllPresentSpecies;
	}
	
	public Collection<Long> getUserTaxidsToDownCast() {
		return this.userTaxidsToDownCast;
	}

	public void setUserTaxidsToDownCast(
			Collection<Long> userProteomeIdsToDownCast) {
		this.userTaxidsToDownCast = userProteomeIdsToDownCast;
	}
	
	public boolean isDownCastOnChildren() {
		return this.downCastOnChildren;
	}


	public void setDownCastOnChildren(boolean downCastOnChildren) {
		this.downCastOnChildren = downCastOnChildren;
	}


	public Map<Long, Collection<Long>> getMitabTaxid2ClogTaxid() {
		return this.mitabTaxid2ClogTaxid;
	}


	public void setMitabTaxid2ClogTaxid(
			Map<Long, Collection<Long>> mitabTaxid2ClogTaxid) {
		this.mitabTaxid2ClogTaxid = mitabTaxid2ClogTaxid;
	}
	
	public Collection<Long> getClogTaxids() {
		return this.clogTaxids;
	}


	public void setClogTaxids(Collection<Long> clogTaxids) {
		this.clogTaxids = clogTaxids;
	}
	
	public Map<Long, Long> getProteomeId2Taxid() {
		return this.proteomeId2Taxid;
	}


	public void setProteomeId2Taxid(Map<Long, Long> proteomeIds) {
		log.info(proteomeIds.size()+" proteomes: "+proteomeIds);
		this.proteomeId2Taxid = proteomeIds;
	}
	
	public Collection<BinaryInteraction> getInteractions() {
		return this.interactions;
	}


	public void setInteractions(
			Collection<BinaryInteraction> interactions) {
		this.interactions = interactions;
	}
	
	public boolean isWriteClogInteractions() {
		return this.writeClogInteractions;
	}


	public void setWriteClogInteractions(boolean writeClogInteractions) {
		this.writeClogInteractions = writeClogInteractions;
	}
	

	public boolean isWriteDownCastHistory() {
		return this.writeDownCastHistory;
	}


	public void setWriteDownCastHistory(boolean writeClog) {
		this.writeDownCastHistory = writeClog;
	}
	

	public File getWorkingDir() {
		return this.workingDir;
	}


	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
		
		if (!workingDir.exists()) {
			System.out.println("create directory "+workingDir);
			log.info("create directory "+workingDir);
			File dir = new File(workingDir.getAbsolutePath());
			dir.mkdirs();
		} else if (!workingDir.isDirectory()) {
			throw new IllegalArgumentException(workingDir.getAbsolutePath()+ " is not a directory");
		}
		
		
		FILE_DATA_PROTEOME = new File(workingDir.getAbsolutePath()+"/proteome_report.txt");
		FILE_HISTORY_DOWN_CAST = new File(workingDir.getAbsolutePath()+"/downCast.history.txt");
		FILE_RESULT_CLOG_INTERACTIONS = new File(workingDir.getAbsolutePath()+"/clog.interactions.txt");
		predictedinteractionsFileName = "clog.predictedInteractions";
		
	}
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	
	/**
	 * Check that proteome file is in the directory and download it otherwise.
	 * Check that clog file is in the directory and download it otherwise.
	 * Parse the mitab file to see:
	 * - how many interactions have both uniprotKB ac and can be used
	 * - how many interactions are not physical interactions and will be skipped
	 * - how many species are present in the file (NCBI taxids)
	 * 
	 * @throws InterologPredictionException
	 */
	private void checkFiles () throws InterologPredictionException {
		if (!FILE_DATA_PROTEOME.exists()) {
			try {
				downloadProteome();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while downloading proteome file",e);
			}
		}
		
		if (!getClog().exists()) {
			// check here that we have uniprotKB id in the clog data?
			// no it is not worth doing a whole parsing of the clog file now
			// TODO url not known yet
			try {
				downloadClogs();
			} catch (IOException e) {
				//throw new InterologPredictionException("Error while downloading clog file",e);
				throw new InterologPredictionException("clog file "+getClog().getAbsolutePath()+" not found",e);
			}
		}
		
		
		try {
			checkMitabFile();
		} catch (InterologPredictionException e) {
			throw new InterologPredictionException("Error while reading mitab file "+getMitab(),e);
		}
		
		log.warn("you can use Picr tool to collect UniprotKB identifiers from RefSeq ids");		
	}
	
	/**
	 * Parse the source mitab to see how many interactions can be used.
	 * Collect all NCBI taxids in the file in the same time.
	 * 
	 * @throws InterologPredictionException 
	 * 
	 */
	private void checkMitabFile() throws InterologPredictionException{
		
		Iterator<BinaryInteraction> iterator;
		try {
			iterator = MitabUtils.getIterator(mitab, mitabHasHeaders);
		} catch (MitabException e) {
			throw new InterologPredictionException("Error while parsing mitab file "+mitab.getName(), e);
		}
		
		int count=0, interactionOK=0;
		int proteinWithoutUniprot=0, interactionWithoutUniprot=0, notPhysical=0, self=0, interSpecies=0;
		while ( iterator.hasNext() ) {
			count++;
			BinaryInteraction psimiInteraction = iterator.next();
			
			// count interactions to consider
			boolean fit = true;
			if (!MitabUtils.isPhysicalInteraction(psimiInteraction)) {
				notPhysical++;
				fit=false;
			}
			if (MitabUtils.isSelf(psimiInteraction)) {
				self++;
				fit=false;
			}
			if (MitabUtils.isInterSpecies(psimiInteraction)) {
				interSpecies++;
				fit=false;
			}
			
			Collection<CrossReference> refsA = psimiInteraction.getInteractorA().getIdentifiers();
			String proteinAcA = getIdentifier(refsA, getUNIPROTKB());
			
			Collection<CrossReference> refsB = psimiInteraction.getInteractorB().getIdentifiers();
			String proteinAcB = getIdentifier(refsB, getUNIPROTKB());
			
			if (proteinAcA==null || proteinAcB==null) {
				interactionWithoutUniprot++;
				fit=false;
				
				if (proteinAcA==null) {
					proteinWithoutUniprot++;
				}
				if (proteinAcB==null) {
					proteinWithoutUniprot++;
				}
			}
			
			if (fit) {
				interactionOK++;
			}
			
			
			// retrieve taxids
    		Interactor intA = psimiInteraction.getInteractorA();
    		Interactor intB = psimiInteraction.getInteractorB();
    		
    		if (intA.getOrganism().getTaxid()==null || intB.getOrganism().getTaxid()==null) {
    			continue;
    		}
    		Long taxidA = Long.parseLong( intA.getOrganism().getTaxid() );
    		Long taxidB = Long.parseLong( intB.getOrganism().getTaxid() );
    		mitabTaxids.add(taxidA);
    		mitabTaxids.add(taxidB);
        }
		
		log.warn(mitabTaxids.size()+" taxids in the mitab file");
		log.debug(mitabTaxids);
		
		log.warn("We use UniprotKB ids for proteins. " +
				"Only interactions with both Uniprot ids can be used. " +
				"Among them we only consider physical interactions between 2 different proteins from the same species.");
		log.info(count+" source interactions");
		log.info(proteinWithoutUniprot+" proteins do not have Uniprot id");
		log.info(interactionWithoutUniprot+" interactions do not have both Uniprot ids");
		log.info(notPhysical+" interactions are not physical type");
		log.info(self+" interactions are not self interactions");
		log.info(interSpecies+" interactions are between 2 different species");
		log.warn("--> "+interactionOK+" interactions can be used among the "+count+" of the mitab file "+getMitab()+" (Uniprot id and physical type)");
	}
	
	
	/**
	 * Download proteome file from Integr8 ftp;
	 * .
	 * @throws IOException
	 */
	private void downloadProteome() throws IOException {
		Sucker.main(new String[]{URL_PROTEOME_FILE, FILE_DATA_PROTEOME.getAbsolutePath()});
	}
	
	/**
	 * Download clog data from ??;
	 * .
	 * @throws IOException
	 */
	private void downloadClogs() throws IOException {
		Sucker.main(new String[]{URL_CLOG_FILE, getClog().getAbsolutePath()});
	}
	
	
	/**
	 * Parse proteome_report.txt file.
	 * Fill both Maps taxid2proteomeId and proteomeId2Taxid.
	 * @return
	 * @throws InterologPredictionException
	 */
	private void parseProteomes() throws InterologPredictionException {
		log.info("compute mapping between proteome ids and taxids..");
		
		// Integr8 uses specific proteome identifiers
		// we need to map proteome ids to NCBI taxid to know if species in the clog are in our list
		// this is done by parsing the proteome_report file from Integr8 ftp
		log.info("mapping proteomeId to NCBI taxid from file "+FILE_DATA_PROTEOME);
		Collection<ProteomeReport> entries = ProteomeReportParser.parse(FILE_DATA_PROTEOME);
		for (ProteomeReport report : entries) {
			taxid2proteomeId.put(new Long(report.getProteomeTaxid()), new Long(report.getProteomeId()));
			proteomeId2Taxid.put(new Long(report.getProteomeId()), new Long(report.getProteomeTaxid()));
		}
		log.info(proteomeId2Taxid.size()+" proteome ids");
		log.info(taxid2proteomeId.size()+" taxids");
	}
	
	@SuppressWarnings("unchecked")
	private void testTaxids() {
		log.warn(mitabTaxids.size()+" taxids in the mitab file");
		log.warn(clogTaxids.size()+" ids in the clog file");
		
		Collection<Long> intersection = PsiCollectionUtils.intersection(mitabTaxids, clogTaxids);
		Collection<Long> clogOnly = MitabStats.getAWithoutB(clogTaxids, intersection);
		Collection<Long> mitabOnly = MitabStats.getAWithoutB(mitabTaxids, intersection);
		
		log.warn(intersection.size()+" ids in both the mitab file and the clog data");
		
		System.out.println("clog only:");
		System.out.println(clogOnly);
		System.out.println("mitab only:");
		System.out.println(mitabOnly);
		System.out.println("intersection:");
		System.out.println(intersection);
		
		Map<Long, Collection<Long>> mitab2Children = new HashMap<Long, Collection <Long>>(mitabOnly.size());
		
		for (Long childTaxid : mitabOnly) {
			try {
				Collection<Long> children = NewtUtils.getReallyAllChildrenTaxids(childTaxid);
				System.out.println(childTaxid+" -> "+children);
				Collection<Long> newIntersection = PsiCollectionUtils.intersection(children, clogTaxids);
				if (!newIntersection.isEmpty()) {
					mitab2Children.put(childTaxid, newIntersection);
					System.out.println(childTaxid+" -> "+newIntersection);
				}
			} catch (NewtException e) {
				log.debug("Newt error while retrieving children taxid for: "+childTaxid);
				continue;
			}
		}
		
		System.out.println(mitab2Children.size()+" mitab id have children in the clog list");
		
	}
	
	/**
	 * Try to map taxid between mitab and clog data.
	 * The species can be defined by different taxonomy levels.
	 * All taxids from mitab found in clogs are fine.
	 * If some mitabs taxids are not found in clog but have children in clog
	 * we add the children id in the map so that we will down-cast interactions on them as well.
	 * .
	 * @return
	 * @throws InterologPredictionException 
	 * @throws NewtException 
	 */
	@SuppressWarnings("unchecked")
	private void mapTaxids() throws InterologPredictionException{		
		Collection<Long> mitabTaxidsToTreat = new HashSet<Long>();
		mitabTaxidsToTreat.addAll(mitabTaxids);
		log.info(mitabTaxids.size()+" taxids in mitab file");
		Map<Long, Collection<Long>> mitab2clogTaxids = new HashMap<Long, Collection<Long>>();
		
		int direct=0, splitChildren=0;
		for (Iterator iter = mitabTaxidsToTreat.iterator(); iter.hasNext();) {
			Long mitabTaxid = (Long) iter.next();
			
			// in clog
			if (clogTaxids.contains(mitabTaxid)) {
				iter.remove();
				direct++;
				Collection<Long> tmp = new HashSet<Long>(1);
				tmp.add(mitabTaxid);
				mitab2clogTaxids.put(mitabTaxid, tmp);
			} 
			// (several) children in clog
			else  if (isDownCastOnChildren()){
				Collection<Long> children;
				try {
					children = NewtUtils.getReallyAllChildrenTaxids(mitabTaxid);
				} catch (NewtException e) {
					throw new InterologPredictionException("Error while collecting NEWT children for "+mitabTaxid,e);
				} /*catch (java.lang.IndexOutOfBoundsException e) {
					// TODO remove
					System.out.println(mitabTaxid);
					System.out.println(e);
					continue;
				}*/
				Collection<Long> newIntersection = PsiCollectionUtils.intersection(children, clogTaxids);
				if (!newIntersection.isEmpty()) {
					iter.remove();
					splitChildren++;
					mitab2clogTaxids.put(mitabTaxid, newIntersection);
					taxid2ChildrenTaxids.put(mitabTaxid, newIntersection);
					log.warn(mitabTaxid+" children: "+newIntersection);
				}
			}
		}
		
		log.info(mitab2clogTaxids.size()+" taxids are found in clog data ("+direct+" directly and "+splitChildren+" have children in clog data)");
		log.info(mitabTaxidsToTreat.size()+" are still to treat");
		log.debug(mitabTaxidsToTreat);
		
		mitabTaxid2ClogTaxid = mitab2clogTaxids;
		log.debug(mitab2clogTaxids);
		
	}
	
	/**
	 * @param strategy
	 * @throws InterologPredictionException
	 */
	@SuppressWarnings("unchecked")
	private void chooseSpeciesToPredict() throws InterologPredictionException {
		log.info("choose species for which we will transfer interactions");
		Map<Long, Long> taxid2proteomeIdsToDownCast = new HashMap<Long, Long>();
		
		
		
		// strategy used: only taxids present in both mitab and clog data
		// and some strains as well
//		 taxids from mitab source file
		if (downCastOnAllPresentSpecies) {
			int noProteome = 0;
			for (Long mitabTaxid : mitabTaxids) {
				Long proteomeidToAdd = taxid2proteomeId.get(mitabTaxid);
				if (proteomeidToAdd!=null) {
					taxid2proteomeIdsToDownCast.put(mitabTaxid, proteomeidToAdd);
				} else {
					noProteome++;
					log.debug(mitabTaxid+" has no mapping to a proteome id");
				}
			}
			int nbSp = taxid2proteomeIdsToDownCast.size();
			log.warn(nbSp+" taxids from mitab source file are mapped to proteome ids and added in the list of species");
			log.info(noProteome+" taxids from mitab source file are not mapped to proteome ids");
		}
		
		// add species asked by user
		if (userTaxidsToDownCast!=null && !userTaxidsToDownCast.isEmpty()) {
			for (Long userTaxid : userTaxidsToDownCast) {
				Long userProteomeId = taxid2proteomeId.get(userTaxid);
				if (userProteomeId!=null) {
					taxid2proteomeIdsToDownCast.put(userTaxid, userProteomeId);
				} else {
					log.debug("user species "+userTaxid+" can not be mapped to proteome id");
				}
			}
			
			log.warn(taxid2proteomeIdsToDownCast.size()+" species given by the user are added to the list for the predictions");
			log.info("We now have "+taxid2proteomeIdsToDownCast.size()+" proteome ids in the list");
		}
		
		
		// we add some strains manually if the parent is in the list
		// and if the children can be mapped to a proteome id
		int added = 0;
		for (Long parentTaxid : taxid2ChildrenTaxids.keySet()) {
			if (taxid2proteomeIdsToDownCast.containsKey(parentTaxid)) {
				Collection<Long> childrenTaxids = taxid2ChildrenTaxids.get(parentTaxid);
				for (Long childTaxid : childrenTaxids) {
					Long proteomeIdToAdd = taxid2proteomeId.get(childTaxid);
					if (proteomeIdToAdd!=null) {
						added++;
						//proteomeIdsToDownCast.add(proteomeIdToAdd);
						taxid2proteomeIdsToDownCast.put(childTaxid, proteomeIdToAdd);
					}
				}
			}
		}
		log.info(added+" proteome ids of specific strains are added in the list of species");
		
		// another strategy more general: try to extend the mapping with NEWT
		// a bit long and complicated...if you want to continue it... ;-)
		if (false) {
			mapTaxids();
			for (Object long1 : InterologUtils.getAllValues( mitabTaxid2ClogTaxid )) {
				proteomeIdsToDownCast.add(taxid2proteomeId.get(Long.parseLong( long1.toString() )));
			}
			log.warn(proteomeIdsToDownCast.size()+" species");
		}
		
		proteomeIdsToDownCast.addAll(taxid2proteomeIdsToDownCast.values());
		
		log.warn("list of "+proteomeIdsToDownCast.size()+" proteomes for which we will predict interactions");
		log.debug(proteomeIdsToDownCast);
		
		printMemoryInfo();
	}
	
	/**
	 * Parse clog file and put all clogs in the map with access by their id.
	 * TODO parse only uniprot id?;
	 * Tell if others ids;
	 * .
	 */
	private void parseClogs() {
		
		ClogParser parser = new ClogParser();
		parser.setClogRepeatFile(getClog());
		Map<Long, Clog> clogs = null;
		if (clogFormatByProtein) {
			log.info("parse clogs (format=one protein by line)...");
			clogs = parser.parseClogProteinFormat();
		} else {
			log.info("parse clogs (format=one clog by line)...");
			clogs = parser.parseClog();
		}
		
		clogId2Clog = clogs;
		log.info(clogs.size()+" clogs described in the map");
		printMemoryInfo();
	}
	
	/**
	 * Create a map to have access for each protein to the clog which contains it.
	 * The key is based on proteinAc_taxid.
	 */
	private void mapProtein2ItsClog() {
		log.info("Compute mapping from protein (ac and species) to the clog id...");
		
		if (clogId2Clog==null || clogId2Clog.isEmpty()) {
			throw new IllegalArgumentException("mapping impossible because clogs are not loaded");
		}
		
		for (Clog clog : clogId2Clog.values()) {
			for (Long proteomeId : clog.getProteomeId2protein().keySet()) {
				
				// add all correspondant NCBI taxids in the list
				// to chose after that on which species we want to projet interactions
				Long taxid = proteomeId2Taxid.get(proteomeId);
				clogTaxids.add(taxid);
				String proteinAc = clog.getProteomeId2protein().get(proteomeId);
				String key = proteinAc+"_"+taxid;
				
				// sometimes some different proteomeIds can point to the same NCBI taxid (related strains probably...)
				// for example 
				//A.tumefaciens Cereon | 62          | 176299         | 0    | Agrobacterium tumefaciens Cereon
				//A.tumefaciens Dupont | 73          | 176299         | 0    | Agrobacterium tumefaciens Dupont 
				// In that case we have the same protein Ac and the same taxid that can appear several times in the same clog
				// but it is not a problem as it is the same clog, we are just merging related proteins
				// but we must not have the same thing in different clogs
				if (protein2clog.containsKey(key)) {
					
					if (clog.getId()!=clogId2Clog.get(protein2clog.get(key)).getId()) {
						log.warn(key+" already in the map with different clog id -> "+protein2clog.get(key));
					}
				}
				
				// add this protein of this species in the mapping
				protein2clog.put(key, clog.getId());
			}
			
		}
		
		log.info(protein2clog.size()+ " proteins in the map");
	}
	
	

	/**
	 * @param mitab
	 * @throws InterologPredictionException 
	 */
	private void writeFile(File mitab) throws InterologPredictionException {
		log.warn("Write mitab file...");
		log.info("file name: "+mitab.getName());
		log.info("nb interactions: "+getInteractions().size());
		PsimiTabWriter writer = new PsimiTabWriter();
		try {
			writer.write(getInteractions(), mitab);
		} catch (ConverterException e) {
			throw new InterologPredictionException("Error while writting mitab file "+mitab, e);
		} catch (IOException e) {
			throw new InterologPredictionException("Error while writting mitab file "+mitab,e);
		}
		printMemoryInfo();
	}
	
	/**
	 * Check input files
	 * Set start time
	 * Chose species for which we will predict interactions
	 * Parse clog file
	 * @throws InterologPredictionException
	 */
	private void preProcess() throws InterologPredictionException {
		log.warn("========== Pre-process ==========");
		printMemoryInfo();
		start = System.currentTimeMillis();
		
		checkFiles();// -> collect mitab NCBI taxids list in the same time
		parseProteomes();
		parseClogs();
		mapProtein2ItsClog();// -> collect clog taxids list in the same time
		
		// chose species for which we want to predict interactions
		chooseSpeciesToPredict();
		if (false) {
			testTaxids();
		}
		
	}
	
	/**
	 * Parse the mitab file to compute a collection of clog interactions.
	 * .
	 * @throws InterologPredictionException
	 */
	private void upCast() throws InterologPredictionException {
		log.warn("========== Up-cast ==========");
		
		PrintStream ps = null;
		if (writeClogInteractions) {
			try {
				ps = new PrintStream(getFILE_RESULT_CLOG_INTERACTIONS());
			} catch (FileNotFoundException e) {
				log.warn(getFILE_RESULT_CLOG_INTERACTIONS()+" not found, clog interactions will not be printed.");
				writeClogInteractions = false;
			}
			ps.append(ClogInteraction.getHeaders().toString()).append("\n");
		}
		
		// parse interaction file
		// we need a map to have access to a clog interaction already created and to complete it with new information
		// as for example the source interaction which has enabled to predict this clog interaction
		Map<ClogInteraction, ClogInteraction> clogInteraction2clogInteraction = new HashMap<ClogInteraction, ClogInteraction>();
		Iterator<BinaryInteraction> iterator;
		try {
			iterator = MitabUtils.getIterator(mitab, mitabHasHeaders);
		} catch (MitabException e) {
			throw new InterologPredictionException("Error while parsing mitab file "+mitab.getName(), e);
		}
		
		int count=0, abstracted=0, protNotInClog=0;
		while ( iterator.hasNext() ) {
			count++;
			BinaryInteraction psimiInteraction = iterator.next();
			
			// interaction type should be physical (or a children in the MI ontology)
			if (!MitabUtils.isPhysicalInteraction(psimiInteraction)) {
				continue;
			}
			
			// self interactions are skipped
			if (MitabUtils.isSelf(psimiInteraction)) {
				continue;
			}
			
			// skip interactions between different organisms
			if (MitabUtils.isInterSpecies(psimiInteraction)) {
				continue;
			}
			String taxidString = psimiInteraction.getInteractorA().getOrganism().getTaxid();
			if (taxidString==null) {
				log.debug("no taxid for "+psimiInteraction);
				continue;
			}
			Long taxid = Long.parseLong(taxidString);
			
			
			// Uniprot protein ac and NCBI taxid
			// we have to check the species taxid as well
			// because some identifiers can refer to different proteins in different species
			// thus we can do a false mapping with a clog if it is not the same species and we do not check
			Collection<CrossReference> refsA = psimiInteraction.getInteractorA().getIdentifiers();
			String proteinAcA = getIdentifier(refsA, getUNIPROTKB());
			
			Collection<CrossReference> refsB = psimiInteraction.getInteractorB().getIdentifiers();
			String proteinAcB = getIdentifier(refsB, getUNIPROTKB());
			
			
			// mapping with clog ids
			// proteinAc and taxid
			Long clogIdA = protein2clog.get(proteinAcA+"_"+taxid);
			Long clogIdB = protein2clog.get(proteinAcB+"_"+taxid);
			
			if (clogIdA==null && clogIdB==null && taxid!=null) {
				Collection<Long> childrenTaxids = taxid2ChildrenTaxids.get(taxid);
				if (childrenTaxids!=null) {
					Iterator currentChildren = childrenTaxids.iterator();
					while (clogIdA==null && clogIdB==null && currentChildren.hasNext()) {
						Long newTaxid = (Long) currentChildren.next();
						clogIdA = protein2clog.get(proteinAcA+"_"+newTaxid);
						clogIdB = protein2clog.get(proteinAcB+"_"+newTaxid);
					}
				}
			}
			
			if (clogIdA==null) {
				protNotInClog++;
				continue;
			}
			if (clogIdB==null) {
				protNotInClog++;
				continue;
			}
			
			// create clog interactions
			abstracted++;
			
			
			// self-interaction for clog ? should be impossible because only one protein of each species in a clog...
			// could happen when merging different strains
			// all proteins of the clog will have a putative self interaction
			// do not want to add such questionable things
			if (!clogIdA.equals(clogIdB)) {
				Clog clogA = clogId2Clog.get(clogIdA);
				Clog clogB = clogId2Clog.get(clogIdB);
				
				if (clogA!=null && clogB!=null) {
					ClogInteraction newClogInteraction = new ClogInteraction(clogA, clogB);
					
					if (clogInteraction2clogInteraction.containsKey(newClogInteraction)) {
						// complete the clog interaction already present in the map
						// with the new features of this prediction
						ClogInteraction oldClogInteraction = clogInteraction2clogInteraction.get(newClogInteraction);
						oldClogInteraction.getSourceInteractions().add(psimiInteraction);
					} else {
						newClogInteraction.getSourceInteractions().add(psimiInteraction);
						clogInteraction2clogInteraction.put(newClogInteraction, newClogInteraction);
					}
					
		        	
				}
			} else {
				log.warn("skip self clog interaction clog "+clogIdA+" from proteins "+proteinAcA+" - "+proteinAcB);
			}
				
        }
		
		if (isWriteClogInteractions()) {
			int totalSize = clogInteraction2clogInteraction.size();
			int current=0, done=0;
			for (ClogInteraction interaction : clogInteraction2clogInteraction.values()) {
				current++;
				done++;
				ps.append(interaction.toString()).append("\n");
				if(current%10000==0) {
					current=0;
					log.warn("10000 new interactions written - remain "+(totalSize-done));
				}
			}
			ps.close();
			log.info("file"+getFILE_RESULT_CLOG_INTERACTIONS()+" written");
		}
		
		log.info(count+" source interactions");
		log.info(protNotInClog+" interactions miss at least one protein in the clogs");
		log.info(abstracted+" have been abstracted");
		log.warn(clogInteraction2clogInteraction.size()+" distinct clog interactions");
		
		clogInteractions = clogInteraction2clogInteraction.values();
		
		// remove the clog data to have a little more space
		log.info("remove clog data");
		clogId2Clog = null;
		printMemoryInfo();
	}
	
	
	/**
	 * Down-cast all clog interactions into protein-protein interaction for all considered species.
	 * .
	 * @param clogA
	 * @param clogB
	 * @return
	 */
	private Collection<BinaryInteraction> downCast(ClogInteraction clogInteraction) {
		StrBuilder history = new StrBuilder();
		history.append("clog interaction ").append(clogInteraction.getClogA().getId()).append(" - ").append(clogInteraction.getClogB().getId()).append("\n");
		
		Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
		Collection<Long> speciesA = clogInteraction.getClogA().getProteomeId2protein().keySet();
		Collection<Long> speciesB = clogInteraction.getClogB().getProteomeId2protein().keySet();
		
		for (Long sp:speciesA) {
			//history.append("proteome id = ").append(sp);
			
			// down-cast only on species specified in the list
			// no need to check the different taxid levels as proteomeIds are always as specific as possible
			if (proteomeIdsToDownCast.contains(sp)) {
				Long id = proteomeId2Taxid.get(sp);
				history.append("taxid id ").append(id).append(": ");
				
				if (speciesB.contains(sp)) {
					String uniprotIdA = clogInteraction.getClogA().getProteomeId2protein().get(sp);
					String uniprotIdB = clogInteraction.getClogB().getProteomeId2protein().get(sp);
					history.append(uniprotIdA).append(" - ").append(uniprotIdB).append("\n");
					
					if (!InterologUtils.matchUniprotKbId(uniprotIdA) || !InterologUtils.matchUniprotKbId(uniprotIdB)) {
						if (uniprotIdA.startsWith("IPI") || uniprotIdB.startsWith("IPI")) {
							IPIid++;
						} else {
							noMatchUniprotId++;
							log.debug(uniprotIdA+" or "+uniprotIdB+ " does not match Uniprot pattern");
						}
						continue;
					}
					
					
					int taxid = id.intValue();
					Organism o = new Organism(taxid);
					
					Collection<CrossReference> identifiersA = new ArrayList<CrossReference>(1);
			        identifiersA.add( new CrossReference( getUNIPROTKB(), uniprotIdA ));
			        Interactor iA = new Interactor( identifiersA );
			        iA.setOrganism(o);
			        
			        Collection<CrossReference> identifiersB = new ArrayList<CrossReference>(1);
			        identifiersB.add( new CrossReference( getUNIPROTKB(), uniprotIdB ) );
			        Interactor iB = new Interactor( identifiersB );
			        iB.setOrganism(o);
			        
			        // test here both identifiers A and B and source interactions of this clog interaction
			        //  we do not want to predict interactions from which this clog interaction comes from
			        if (clogInteraction.isPredictedBy(uniprotIdA, uniprotIdB)) {
			        	sourceInteraction++;
			        	history.append(" -> source interaction\n");
			        	continue;
			        }
			        
			        
			        
					BinaryInteraction interaction = new BinaryInteraction(iA, iB);
					
					// authors
					List<Author> authors = new ArrayList<Author>(1);
					Author me = new Author();
					me.setName("Magali Michaut");
					authors.add(me);
					interaction.setAuthors(authors);
					
					// detection method = interologs mapping
					List <InteractionDetectionMethod> methods = new ArrayList<InteractionDetectionMethod>(1);
					InteractionDetectionMethod interologsMapping = new InteractionDetectionMethod();
					interologsMapping.setDatabase("MI");
					interologsMapping.setIdentifier("0064");
					interologsMapping.setText("interologs mapping");
					methods.add(interologsMapping);
					interaction.setDetectionMethods(methods);
					
					interaction.setInteractionAcs(null);
					interaction.setInteractionTypes(null);// physical interaction?
					interaction.setPublications(null);
					interaction.setSourceDatabases(null);
					interaction.setConfidenceValues(null);
					
					interactions.add(interaction);
					history.append(" -> predicted\n");
					
				}
				
			}
			
		}
		
		if (isWriteDownCastHistory()) {
			downCastHistory.append(history.toString()).append("\n");
		}
		
		return interactions;
	}
	
	
	/**
	 * Create all interactions possible from these clog interactions;
	 * We only predict interactions for all species specified in the proteomeId;
	 * .
	 * @return
	 */
	private void downCast() {
		log.warn("========== Down-cast ==========");
				
		if (isWriteDownCastHistory()) {
			try {
				downCastHistory = new PrintStream(new File(workingDir.getAbsolutePath()+"/downCast.history.txt"));
			} catch (FileNotFoundException e) {
				log.warn("down cast history is redirecting on System.out");
				downCastHistory = System.out;
			}
		}
		
		
		for (ClogInteraction clogInteraction : clogInteractions) {
			Collection<BinaryInteraction> downCastedInteractions = downCast(clogInteraction);
			interactions.addAll(downCastedInteractions);
		}
		
		log.warn(interactions.size()+" binary interactions predicted");
		log.info(sourceInteraction+" binary interactions have not been predicted because they are source interactions");
		log.info(IPIid+" binary interactions have not been predicted because IPI id");
		log.info(noMatchUniprotId+" binary interactions have not been predicted because uniprot id missed");
		
		if (isWriteDownCastHistory()) {
			downCastHistory.close();
		}
		
		printMemoryInfo();
	}
	
	
	/**
	 * Write predicted interactions in a mitab file
	 * Printed global execution time
	 * @throws InterologPredictionException
	 */
	private void postProcess() throws InterologPredictionException {
		log.warn("========== Post-process ==========");
		writeFile(new File(workingDir.getAbsoluteFile()+"/"+predictedinteractionsFileName+predictedinteractionsFileExtension));
		
		float elapsedTimeMin = (System.currentTimeMillis()-start)/(60*1000F);
		log.info("time elapsed: "+elapsedTimeMin+" min");
		log.warn("==================================");
	}
	
	/**
	 * @throws InterologPredictionException 
	 */
	public void run() throws InterologPredictionException {
		preProcess();
		upCast();
		downCast();
		postProcess();
	}
	
	
	/**
	 * Give direct children taxids of some species
	 * yeast pylori
	 * @return
	 */
	private static Map<Long, Collection<Long>> getChildrenMapManually() {
		Map<Long, Collection<Long>> map = new HashMap<Long, Collection<Long>>();
		
		// yeast
		Long[] coliTab = new Long[] {244314l, 244318l, 199310l, 364106l, 244323l, 362663l, 366838l, 244319l, 340185l, 244316l, 244322l, 155864l, 316401l, 244324l, 358709l, 316435l, 397449l, 168807l, 397447l, 217992l, 168927l, 366837l, 244326l, 340186l, 316397l, 397454l, 244325l, 397453l, 366839l, 244321l, 341037l, 373045l, 397448l, 331111l, 397452l, 83333l, 183192l, 366836l, 344610l, 37762l, 405955l, 397450l, 216592l, 244317l, 331112l, 344601l, 244315l, 340197l, 216593l, 83334l, 397451l, 340184l, 244320l};
		map.put(562l, Arrays.asList( coliTab ));
		
		// pylori
		Long[] pyloriTab = new Long[] {85963l, 102617l, 85962l};
		map.put(210l, Arrays.asList( pyloriTab ));
		
		return map;
	}
	
	/**
	 * @param refs collection of cross-ref
	 * @param database 
	 * @return identifier for the specified database
	 */
	public static String getIdentifier(Collection<CrossReference> refs, String database) {
		String ac = null;
		for (CrossReference ref:refs) {
			if (ref.getDatabase().equals(database)) {
				if (ac!=null) {
				log.info("several Uniprot ac: "+ac+ " and "+ref.getIdentifier());
				}
				ac = ref.getIdentifier().trim();
			}
		}
		return ac;
	}
	
	/**
	 * Put memory information in the log.
	 */
	public static void printMemoryInfo() {
		log.info(InterologUtils.printRuntime());
	}
	
	/**
	 * @param args
	 * @throws InterologPredictionException 
	 * @throws NewtException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterologPredictionException, NewtException{
		
		File dir = new File("/Users/mmichaut/Documents/EBI/results/clogs/");
		InterologPrediction up = new InterologPrediction(dir);
		//File mitab = new File("/Users/mmichaut/Documents/EBI/data/IntAct/intact.mitab");
		File mitab = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/global.mitab");
		up.setMitab(mitab);
		up.setClog(new File("/Users/mmichaut/Documents/EBI/data/Integr8/clog/20070630/clog.revised.dat"));
		up.setDownCastOnAllPresentSpecies(false);
		up.setClogFormatByProtein(false);
		Collection<Long> taxids = new HashSet<Long>(1);
		taxids.add(1148l);
		up.setUserTaxidsToDownCast(taxids);
		up.setWriteDownCastHistory(true);
		ClogInteraction.setNB_LINES_MAX(100000);
		up.setWriteClogInteractions(false);
		up.setDownCastOnChildren(false);
		up.run();
	}
	
}
