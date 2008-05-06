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
import org.apache.log4j.PropertyConfigurator;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.converter.tab2xml.XmlConvertionException;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.AuthorImpl;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionDetectionMethodImpl;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.Organism;
import psidev.psi.mi.tab.model.OrganismFactory;
import psidev.psi.mi.tab.model.OrganismImpl;
import psidev.psi.mi.tab.utils.PsiCollectionUtils;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlWriterException;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
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
 * This is the main part of the prediction process.
 * 
 * 08/03/29: by default, the tool does not generate XML files (keep an option to do it). We use a separate tool to convert
 *           generated MITAB25 into PSI25-XML
 * 08/03/28: added a threshold to compute files in XML format or not (too heavy...)
 * 08/03/27: generate result files in PSI25-XML format in addition to MITAB format
 *           added global files with known and predicted interactions together
 *           added the pubmed database and id (00000000 so far because not known)
 * 08/02/12: added the associated publication in the generated file
 * 08/01/25: added a file with known interactions in mitab format
 * 08/01/15: little improvment, the program stop when the list of taxids is empty (the list of species for which we will predict interactions).
 * 07/11/26: the clog data are now public and called the porc data (for Putative ORthologous Clusters, see http://www.ebi.ac.uk/integr8/EBI-Integr8-HomePage.do)
 * the URL is known and added in this class. Nevertheless, it is recommended to download the file manually and then run the program pointing to the downloaded file (160257 KB  	15/11/07).
 * 
 * 
 * @author mmichaut
 * @version $Id$
 * @since 30 avr. 07
 */
public class InterologPrediction {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	public static Log log = LogFactory.getLog(InterologPrediction.class);
	
	private final static String SEP = "\t";
	
	/**
	 * All information for generated interactions.
	 */
	private final static String AUTHORS = "Michaut et al. (2008)";
	private final static String PUBMED_DB = "pubmed";
	private final static String MICHAUT_ET_AL_ID = "00000000";
	private final static String METHOD_DATABASE = "MI";
	private final static String METHOD_ID = "0064";
	private final static String METHOD_NAME = "interologs mapping";
	
	/**
	 * To indent all numeric results during the process.
	 */
	private final static String INDENT_STRING = "   > ";
	
	/**
	 * Integr8 ftp proteome report file is used to link proteome id to NCBI taxid.
	 */
	private static File FILE_DATA_PROTEOME;
	
	/**
	 * Integr8 porg file is used to describe orthologous clusters.
	 */
	private static File FILE_DATA_PORC;
	
	/**
	 * File used to print the down-cast history.
	 */
	private static File FILE_HISTORY_DOWN_CAST;
	
	/**
	 * File used to print the source interactions used.
	 */
	private static File FILE_SOURCE_INTERACTIONS;
	
	/**
	 * File used to print all clog interactions if asked.
	 */
	private static File FILE_RESULT_PORC_INTERACTIONS;
	
	/**
	 * URL of the proteome file from Integr8 ftp.
	 */
	private static String URL_PROTEOME_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/integr8/proteome_report.txt";
	
	/**
	 * URL of Integr8 ftp where porc data are (previously called clog);
	 */
	private static String URL_PORC_FILE = "ftp://ftp.ebi.ac.uk/pub/databases/integr8/porc/proc_gene.dat";
	
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
	 * File with porc data.
	 */
	private File porc;
	
	/**
	 * The new clog format contains one protein by line.
	 * True by default.
	 * If false we can use the old parser with one clog by line.
	 * 
	 * Now the final porc format is one gene by line. So this boolean is true by default and should be used as true.
	 */
	private boolean classicPorcFormat = true;
	
	/**
	 * Name of the result file with predicted interactions.
	 */
	private String predictedInteractionsFileName = "InteroPorc.predictedInteractions";
	
	/**
	 * Name of the result file with known interactions.
	 */
	private String knownInteractionsFileName = "KnownInteractions";
	
	/**
	 * Name of the result file with all interactions (known and predicted).
	 */
	private String allInteractionsFileName = "AllInteractions";
	
	/**
	 * Extension used for the mitab files generated.
	 */
	private String mitabFileExtension = ".mitab";
	
	/**
	 * Extension used for the XML files generated.
	 */
	private String xmlFileExtension = ".xml";
	
	/**
	 * By default, no XML files are generated (can be too heavy when lots of predictions).
	 * We use a separate tool. Nevertheless, the option is kept to generate XML files.
	 */
	private boolean generateXml = false;
	
	/**
	 * Maximum nb of interactions to generate a XML file.
	 */
	private int nbInterMaxForXml = 15000;
	
	/**
	 * All NCBI taxids present in the mitab file.
	 */
	private Collection<Long> mitabTaxids;
	
	/**
	 * All proteomeIds present in the porc file.
	 */
	private Collection<Long> porcTaxids;
	
	/**
	 * Get the first level of children in the taxonomy;
	 */
	private Map<Long, Collection<Long>> taxid2ChildrenTaxids = getChildrenMapManually();
	
	/**
	 * List of species for which we want to predict interactions (proteomeIds).
	 */
	private Collection<Long> proteomeIdsToDownCast;
	
	/**
	 * List of species for which we want to predict interactions (NCBI taxids).
	 */
	private Collection<Long> taxidsIdsToDownCast;
	
	/**
	 * If we want to put all taxids present in the mitab source file into the list of species
	 * for which we want to predict interactions.
	 * Can be useful to remove it if we are only interested in 1 or 2 species.
	 * 
	 * Default is true.
	 */
	private boolean downCastOnAllPresentSpecies = false;
	
	/**
	 * List of species for which user wants predict interactions (NCBI taxids).
	 * Are added in the default list.
	 */
	private Collection<Long> userTaxidsToDownCast;
	
	/**
	 * Different sources (mitab and porc data) can be in a different level of specificity in the taxonomy.
	 */
	private Map<Long, Collection<Long>> mitabTaxid2PorcTaxid;
	
	/**
	 * If you want to search mitab taxid children in the taxonomy to transfer on these species too.
	 * 
	 * Default is false.
	 */
	private boolean downCastOnChildren = false;
	
	/**
	 * Working directory.
	 * Contains source files:
	 * proteome_report.txt from Integr8 to link proteomeId and NCBI taxid.
	 * mitab file used to predict interactions.
	 * Porc data file which describes each porc with its genes and species.
	 * TODO initialize with a system variable
	 */
	private File workingDir;
	
	/**
	 * List of all clog with access by id.
	 */
	private Map<Long, Clog> porcId2Porc;
	
	/**
	 * Mapping from protein (ac+species) to Porc id in which we find this protein.
	 */
	private Map<String, Long> protein2porc;
	
	/**
	 * Map proteome_id to Taxid.
	 */
	private Map<Long, Long> proteomeId2Taxid = new HashMap<Long, Long>();
	
	/**
	 * Map NCBI taxid to proteomeId.
	 */
	private Map<Long, Long> taxid2proteomeId = new HashMap<Long, Long>();
	
	/**
	 * List of porc interactions that have been up-casted.
	 */
	private Collection<ClogInteraction> porcInteractions;
	
	/**
	 * Predicted interactions by the down-cast of all porc interactions.
	 */
	private Collection<BinaryInteraction> interactions;
	
	/**
	 * We can specify if we want to print the trace of the down-cast process in a file.
	 */
	private boolean writeDownCastHistory = true;
	
	/**
	 * We can specify if we want to print all source interactions used in a file.
	 */
	private boolean writeSrcInteractions = true;
	
	/**
	 * We can specify if we want to print porc interactions in a file.
	 */
	private boolean writePorcInteractions = false;
	
	/**
	 * Used to print the history of the down-cast process.
	 */
	private PrintStream downCastHistory;
	
	/**
	 * Used to print the source interactions used.
	 */
	private PrintStream srcInteractions;
	
	/**
	 * Time when the process begins.
	 */
	private long start;
	
	/**
	 * Interactions that are not predicted because they were the source of the porc interaction.
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
		porc  = new File(workingDir.getAbsolutePath()+"/porc_gene.dat");
		mitab = new File(workingDir.getAbsolutePath()+"/interactions.mitab");
		
		porcInteractions = new ArrayList<ClogInteraction>();
		protein2porc     = new HashMap<String, Long>();
		setInteractions(new ArrayList<BinaryInteraction>());
		mitabTaxids = new HashSet<Long>();
		setPorcTaxids(new HashSet<Long>());
		
		proteomeId2Taxid = new HashMap<Long, Long>();
		taxid2proteomeId = new HashMap<Long, Long>();
		proteomeIdsToDownCast = new HashSet<Long>();
		taxidsIdsToDownCast   = new HashSet<Long>();
	}
	
	public InterologPrediction(File workingDir, File mitab, File porc) {
		this(workingDir);
		setMitab(mitab);
		setPorc(porc);
	}
	
	
	///////////////////////////////
	////////// GETTERS AND SETTERS
	/////////
	

	public static File getFILE_HISTORY_DOWN_CAST() {
		return FILE_HISTORY_DOWN_CAST;
	}


	public static File getFILE_RESULT_PORC_INTERACTIONS() {
		return FILE_RESULT_PORC_INTERACTIONS;
	}
	
	public static File getFILE_SOURCE_INTERACTIONS() {
		return FILE_SOURCE_INTERACTIONS;
	}

	public static void setFILE_SOURCE_INTERACTIONS(File file_source_interactions) {
		FILE_SOURCE_INTERACTIONS = file_source_interactions;
	}
	
	public static File getFILE_DATA_PROTEOME() {
		return FILE_DATA_PROTEOME;
	}

	public static void setFILE_DATA_PROTEOME(File file_data_proteome) {
		FILE_DATA_PROTEOME = file_data_proteome;
	}
	
	public static File getFILE_DATA_PORC() {
		return FILE_DATA_PORC;
	}

	public static void setFILE_DATA_PORC(File file_data_porc) {
		FILE_DATA_PORC = file_data_porc;
	}

	public static String getUNIPROTKB() {
		return UNIPROTKB;
	}

	public String getMitabFileExtension() {
		return mitabFileExtension;
	}

	public void setMitabFileExtension(String mitabFileExtension) {
		if (!mitabFileExtension.startsWith(".")) {
			mitabFileExtension = "."+mitabFileExtension;
		}
		this.mitabFileExtension = mitabFileExtension;
	}

	public File getMitab() {
		return this.mitab;
	}
	
	public void setMitab(File mitab) {
		log.info("Set mitab file: "+mitab.getName());
		this.mitab = mitab;
	}
	
	public boolean isMitabHasHeaders() {
		return this.mitabHasHeaders;
	}

	public void setMitabHasHeaders(boolean mitabHasHeaders) {
		this.mitabHasHeaders = mitabHasHeaders;
	}
	
	public File getPorc() {
		return this.porc;
	}
	
	public void setPorc(File clog) {
		log.info("Set PORC file: "+clog.getName());
		this.porc = clog;
	}
	
	public boolean isClassicPorcFormat() {
		return this.classicPorcFormat;
	}

	public void setClassicPorcFormat(boolean clogFormatByProtein) {
		this.classicPorcFormat = clogFormatByProtein;
	}
	
	public String getPredictedInteractionsFileName() {
		return this.predictedInteractionsFileName;
	}

	public void setPredictedInteractionsFileName(
			String predictedinteractionsFileName) {
		this.predictedInteractionsFileName = predictedinteractionsFileName;
	}
	
	public String getKnownInteractionsFileName() {
		return this.knownInteractionsFileName;
	}

	public void setKnownInteractionsFileName(String knowninteractionsFileName) {
		this.knownInteractionsFileName = knowninteractionsFileName;
	}
	
	public String getAllInteractionsFileName() {
		return this.allInteractionsFileName;
	}

	public void setAllInteractionsFileName(String allInteractionsFileName) {
		this.allInteractionsFileName = allInteractionsFileName;
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

	public void setUserTaxidsToDownCast(Collection<Long> userTaxidsToDownCast) {
		this.userTaxidsToDownCast = userTaxidsToDownCast;
	}
	
	public boolean isDownCastOnChildren() {
		return this.downCastOnChildren;
	}


	public void setDownCastOnChildren(boolean downCastOnChildren) {
		this.downCastOnChildren = downCastOnChildren;
	}


	public Map<Long, Collection<Long>> getMitabTaxid2PorcTaxid() {
		return this.mitabTaxid2PorcTaxid;
	}


	public void setMitabTaxid2PorcTaxid(
			Map<Long, Collection<Long>> mitabTaxid2ClogTaxid) {
		this.mitabTaxid2PorcTaxid = mitabTaxid2ClogTaxid;
	}
	
	public Collection<Long> getPorcTaxids() {
		return this.porcTaxids;
	}


	public void setPorcTaxids(Collection<Long> clogTaxids) {
		this.porcTaxids = clogTaxids;
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
	
	public boolean isWritePorcInteractions() {
		return this.writePorcInteractions;
	}


	public void setWritePorcInteractions(boolean writeClogInteractions) {
		this.writePorcInteractions = writeClogInteractions;
	}
	

	public boolean isWriteDownCastHistory() {
		return this.writeDownCastHistory;
	}


	public void setWriteDownCastHistory(boolean writeClog) {
		this.writeDownCastHistory = writeClog;
	}
	
	public boolean isWriteSrcInteractions() {
		return this.writeSrcInteractions;
	}

	public void setWriteSrcInteractions(boolean writeSrcInteractions) {
		this.writeSrcInteractions = writeSrcInteractions;
	}
	

	public File getWorkingDir() {
		return this.workingDir;
	}


	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
		
		if (!workingDir.exists()) {
			log.warn("Create directory "+workingDir);
			File dir = new File(workingDir.getAbsolutePath());
			dir.mkdirs();
		} else if (!workingDir.isDirectory()) {
			throw new IllegalArgumentException(workingDir.getAbsolutePath()+ " is not a directory");
		}
		
		
		FILE_DATA_PROTEOME            = new File(workingDir.getAbsolutePath()+"/proteome_report.txt");
		FILE_HISTORY_DOWN_CAST        = new File(workingDir.getAbsolutePath()+"/downCast.history.txt");
		FILE_SOURCE_INTERACTIONS      = new File(workingDir.getAbsolutePath()+"/srcInteractionsUsed.txt");
		FILE_RESULT_PORC_INTERACTIONS = new File(workingDir.getAbsolutePath()+"/clog.interactions.txt");
		
	}
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	
	/////
	/////       some getters to have dynamically the name of the files (after the dir has been set)
	/////
	
	public File getPredictedMitabFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+predictedInteractionsFileName+mitabFileExtension);
	}
	
	public File getPredictedXmlFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+predictedInteractionsFileName+xmlFileExtension);
	}
	
	public File getKnownMitabFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+knownInteractionsFileName+mitabFileExtension);
	}
	
	public File getKnownXmlFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+knownInteractionsFileName+xmlFileExtension);
	}
	
	public File getGlobalMitabFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+allInteractionsFileName+mitabFileExtension);
	}
	
	public File getGlobalXmlFile() {
		return new File(workingDir.getAbsoluteFile()+"/"+allInteractionsFileName+xmlFileExtension);
	}
	
	public int getNbInterMaxForXml() {
		return this.nbInterMaxForXml;
	}

	public void setNbInterMaxForXml(int nbInterMaxForXml) {
		this.nbInterMaxForXml = nbInterMaxForXml;
	}
	
	public boolean isGenerateXml() {
		return this.generateXml;
	}

	public void setGenerateXml(boolean generateXml) {
		this.generateXml = generateXml;
	}
	
	
	/**
	 * @param mitab
	 * @return
	 * @throws InterologPredictionException
	 */
	private Collection<BinaryInteraction> readMitabInteractions(File mitab) throws InterologPredictionException {
		PsimiTabReader reader = new PsimiTabReader(true);
		Collection<BinaryInteraction> binaryInteractions = null;
		try {
			binaryInteractions = reader.read(mitab);
			log.info(binaryInteractions.size()+" interactions read from mitab file");
		} catch (IOException e) {
			throw new InterologPredictionException("Error while reading interactions from file " +mitab.getAbsolutePath(),e);
		} catch (ConverterException e) {
			throw new InterologPredictionException("Error while reading interactions from file " +mitab.getAbsolutePath(),e);
		}
		
		return binaryInteractions;
	}
	
	
	/**
	 * Check that proteome file is in the directory and download it otherwise.
	 * Check that porc file is in the directory and download it otherwise.
	 * Parse the mitab file to see:
	 * - how many interactions have both uniprotKB ac and can be used
	 * - how many interactions are not physical interactions and will be skipped
	 * - how many species are present in the file (NCBI taxids)
	 * 
	 * @throws InterologPredictionException
	 */
	private void checkFiles () throws InterologPredictionException {
		if (!FILE_DATA_PROTEOME.exists()) {
			log.info("Proteome report file "+getFILE_DATA_PROTEOME().getAbsolutePath()+" is not found. It will be automatically downloaded.");
			try {
				downloadProteome();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while downloading proteome file",e);
			}
		}
		
		if (!getPorc().exists()) {
			log.info("PORC file "+getPorc().getAbsolutePath()+" is not found. It will be automatically downloaded.");
			// check here that we have uniprotKB id in the clog data?
			// no it is not worth doing a whole parsing of the clog file now
			try {
				downloadClogs();
			} catch (IOException e) {
				throw new InterologPredictionException("Error while downloading porc file",e);
			}
		}
		
		
		try {
			checkMitabFile();
		} catch (InterologPredictionException e) {
			throw new InterologPredictionException("Error while reading mitab file "+getMitab(),e);
		}
		
		log.debug("You can use Picr tool to collect UniprotKB identifiers from RefSeq ids");		
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
		
		log.info(mitabTaxids.size()+" different taxids in the source interactions (mitab file)");
		log.debug(mitabTaxids);
		
		log.warn("We use UniprotKB ids for proteins. Only interactions with both Uniprot ids can be used.");
		log.warn("Among them, we only consider physical interactions between 2 different proteins from the same species.");
		log.info(count+" source interactions");
		log.info(proteinWithoutUniprot+" proteins do not have Uniprot id");
		log.info(interactionWithoutUniprot+" interactions do not have both Uniprot ids");
		log.info(notPhysical+" interactions are not physical type");
		log.info(self+" interactions are self interactions");
		log.info(interSpecies+" interactions are between 2 different species");
		log.warn(INDENT_STRING+interactionOK+" interactions can be used among the "+count+" of the mitab file "+getMitab());
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
	 * Download porc data file from Integr8 ftp;
	 * .
	 * @throws IOException
	 */
	private void downloadClogs() throws IOException {
		Sucker.main(new String[]{URL_PORC_FILE, FILE_DATA_PORC.getAbsolutePath()});
	}
	
	
	/**
	 * Parse proteome_report.txt file.
	 * Fill both Maps taxid2proteomeId and proteomeId2Taxid.
	 * @return
	 * @throws InterologPredictionException
	 */
	private void parseProteomes() throws InterologPredictionException {
		
		// Integr8 uses specific proteome identifiers
		// we need to map proteome ids to NCBI taxid to know if species in the clog are in our list
		// this is done by parsing the proteome_report file from Integr8 ftp
		log.info("Compute the mapping from proteomeIds to NCBI taxids from file "+FILE_DATA_PROTEOME);
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
		log.info(mitabTaxids.size()+" taxids in the mitab file");
		log.info(porcTaxids.size()+" ids in the porc file");
		
		Collection<Long> intersection = PsiCollectionUtils.intersection(mitabTaxids, porcTaxids);
		Collection<Long> clogOnly = MitabStats.getAWithoutB(porcTaxids, intersection);
		Collection<Long> mitabOnly = MitabStats.getAWithoutB(mitabTaxids, intersection);
		
		log.info(intersection.size()+" ids in both the mitab file and the clog data");
		
		log.info("clog only:"+clogOnly);
		log.info("mitab only:"+mitabOnly);
		log.info("intersection:"+intersection);
		
		Map<Long, Collection<Long>> mitab2Children = new HashMap<Long, Collection <Long>>(mitabOnly.size());
		
		for (Long childTaxid : mitabOnly) {
			try {
				Collection<Long> children = NewtUtils.getReallyAllChildrenTaxids(childTaxid);
				log.info(childTaxid+" -> "+children);
				Collection<Long> newIntersection = PsiCollectionUtils.intersection(children, porcTaxids);
				if (!newIntersection.isEmpty()) {
					mitab2Children.put(childTaxid, newIntersection);
					log.info(childTaxid+" -> "+newIntersection);
				}
			} catch (NewtException e) {
				log.debug("Newt error while retrieving children taxid for: "+childTaxid);
				continue;
			}
		}
		
		log.info(mitab2Children.size()+" mitab id have children in the clog list");
		
	}
	
	/**
	 * Try to map taxid between mitab and porc data.
	 * The species can be defined by different taxonomy levels.
	 * All taxids from mitab found in porcs are fine.
	 * If some mitabs taxids are not found in porc but have children in clog
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
			
			// in porc
			if (porcTaxids.contains(mitabTaxid)) {
				iter.remove();
				direct++;
				Collection<Long> tmp = new HashSet<Long>(1);
				tmp.add(mitabTaxid);
				mitab2clogTaxids.put(mitabTaxid, tmp);
			} 
			// (several) children in porc
			else  if (isDownCastOnChildren()){
				Collection<Long> children;
				try {
					children = NewtUtils.getReallyAllChildrenTaxids(mitabTaxid);
				} catch (NewtException e) {
					throw new InterologPredictionException("Error while collecting NEWT children for "+mitabTaxid,e);
				}
				Collection<Long> newIntersection = PsiCollectionUtils.intersection(children, porcTaxids);
				if (!newIntersection.isEmpty()) {
					iter.remove();
					splitChildren++;
					mitab2clogTaxids.put(mitabTaxid, newIntersection);
					taxid2ChildrenTaxids.put(mitabTaxid, newIntersection);
					log.warn(mitabTaxid+" children: "+newIntersection);
				}
			}
		}
		
		log.debug(mitab2clogTaxids.size()+" taxids are found in PORC data ("+direct+" directly and "+splitChildren+" have children in clog data)");
		log.debug(mitabTaxidsToTreat.size()+" are still to treat");
		log.debug(mitabTaxidsToTreat);
		
		mitabTaxid2PorcTaxid = mitab2clogTaxids;
		log.debug(mitab2clogTaxids);
		
	}
	
	/**
	 * @throws InterologPredictionException
	 */
	@SuppressWarnings("unchecked")
	private void chooseSpeciesToPredict() throws InterologPredictionException {
		log.debug("Choose species for which we will transfer interactions");
		Map<Long, Long> taxid2proteomeIdsToDownCast = new HashMap<Long, Long>();
		
		Collection<Long> taxidsNotMappedToProteomeId = new HashSet<Long>();
		
		// strategy used: only taxids present in both mitab and porc data
		// and some strains as well
		// taxids from mitab source file
		if (downCastOnAllPresentSpecies) {
			for (Long mitabTaxid : mitabTaxids) {
				Long proteomeidToAdd = taxid2proteomeId.get(mitabTaxid);
				if (proteomeidToAdd!=null) {
					taxid2proteomeIdsToDownCast.put(mitabTaxid, proteomeidToAdd);
				} else {
					taxidsNotMappedToProteomeId.add(mitabTaxid);
					log.debug(mitabTaxid+" has no mapping to a proteome id");
				}
			}
			int nbSp = taxid2proteomeIdsToDownCast.size();
			log.warn(nbSp+" taxids from mitab source file are mapped to proteome ids and added in the list of species");
			log.info(taxidsNotMappedToProteomeId.size()+" taxids from mitab source file are not mapped to proteome ids");
		}
		
		// add species asked by user
		if (userTaxidsToDownCast!=null && !userTaxidsToDownCast.isEmpty()) {
			for (Long userTaxid : userTaxidsToDownCast) {
				Long userProteomeId = taxid2proteomeId.get(userTaxid);
				if (userProteomeId!=null) {
					taxid2proteomeIdsToDownCast.put(userTaxid, userProteomeId);
				} else {
					taxidsNotMappedToProteomeId.add(userTaxid);
					log.warn("The taxid "+userTaxid+" can not be mapped to Integr8 identifier. It is either incorrect or not in Integr8 species list. " +
							"Check http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=browseOrgs&currentclicked=BROWSE_SPECIES");
				}
			}
			
			if (taxid2proteomeIdsToDownCast.size()==1) {
				log.info("The predictions will be made for taxid "+taxid2proteomeIdsToDownCast.keySet().iterator().next());
			} else {
				log.info(taxid2proteomeIdsToDownCast.size()+" species given by the user are added to the list for the predictions");
			}
			
			log.debug("We now have "+taxid2proteomeIdsToDownCast.size()+" proteome ids in the list");
		}
		
		
		// we add some strains manually if the parent is in the list
		// and if the children can be mapped to a proteome id
		if (isDownCastOnChildren()) {
			int added = 0;
			
			Collection<Long> allTaxidsToTest = new HashSet<Long>(taxid2proteomeIdsToDownCast.size() + taxidsNotMappedToProteomeId.size());
			allTaxidsToTest.addAll(taxid2proteomeIdsToDownCast.keySet());
			allTaxidsToTest.addAll(taxidsNotMappedToProteomeId);
			
			for (Long parentTaxid : taxid2ChildrenTaxids.keySet()) {
				if (allTaxidsToTest.contains(parentTaxid)) {
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
			if (added!=0) {
				log.info(added+" proteome id of a specific strain is added in the list of species");
			} else if (added > 1) {
				log.info(added+" proteome ids of specific strains are added in the list of species");
			}
			
		}
		
		
		// another strategy more general: try to extend the mapping with NEWT
		// a bit long and complicated...if you want to continue it... ;-)
		if (false) {
			mapTaxids();
			for (Object id : InterologUtils.getAllValues( mitabTaxid2PorcTaxid )) {
				proteomeIdsToDownCast.add(taxid2proteomeId.get(Long.parseLong( id.toString() )));
			}
			log.info(proteomeIdsToDownCast.size()+" species to downcast");
		}
		
		proteomeIdsToDownCast.addAll(taxid2proteomeIdsToDownCast.values());
		taxidsIdsToDownCast.addAll(taxid2proteomeIdsToDownCast.keySet());
		
		if (taxidsIdsToDownCast.size()==1) {
			String tmp = "";
			if (proteomeIdsToDownCast.size()==1) {
				tmp = proteomeIdsToDownCast.iterator().next()+" proteome id in Integr8 data";
			} else {
				tmp = proteomeIdsToDownCast.size()+" proteome ids in Integr8 data";
			}
			log.warn("Interactions will be predicted for NCBI taxid "+taxidsIdsToDownCast.iterator().next());
			log.info("This NCBI taxid corresponds to "+tmp);
		} else {
			log.warn("We have a list of "+taxidsIdsToDownCast.size()+" NCBI taxids (corresponding to "
					+proteomeIdsToDownCast.size()+" proteome ids) for which we will predict interactions");
		}
		
		log.debug(proteomeIdsToDownCast);
		log.debug(taxidsIdsToDownCast);
		
		printMemoryInfo();
	}
	
	/**
	 * Collect all interactions in the source mitab file with at least one taxid in the list to downcast.
	 * @throws InterologPredictionException
	 */
	private void generateKnownInteractionFile() throws InterologPredictionException {
		Collection<BinaryInteraction> interactions = null;
		try {
			interactions = MitabUtils.extractSpeciesInteractionsOneTaxid(mitab, taxidsIdsToDownCast);
		} catch (MitabException e) {
			log.warn("Error while collecting all known interactions for taxid(s) "+taxidsIdsToDownCast);
		}
		
		log.warn(INDENT_STRING+interactions.size()+" interactions are present in the source file and will be written.");
		writeFile(getKnownMitabFile(), interactions);
		convertMitabFileToXML(getKnownMitabFile(), getKnownXmlFile(), interactions.size());
		
	}
	
	/**
	 * Parse clog file and put all porcs in the map with access by their id.
	 * TODO parse only uniprot id?;
	 * Tell if others ids;
	 * .
	 */
	private void parsePorcs() {
		
		ClogParser parser = new ClogParser();
		parser.setClogRepeatFile(getPorc());
		Map<Long, Clog> id2porc = null;
		if (classicPorcFormat) {
			log.info("Parse PORC data (format = one gene by line)...");
			id2porc = parser.parseClogProteinFormat();
		} else {
			log.warn("old file format -> parse clogs (format=one porc by line)...");
			id2porc = parser.parseClog();
		}
		
		porcId2Porc = id2porc;
		log.warn(INDENT_STRING+id2porc.size()+" clusters are described in the PORC data");
		printMemoryInfo();
	}
	
	/**
	 * Create a map to have access for each protein to the porc which contains it.
	 * The key is based on proteinAc_taxid.
	 */
	private void mapProtein2ItsPorc() {
		log.debug("Compute mapping from protein (ac and species) to the clog id...");
		
		if (porcId2Porc==null || porcId2Porc.isEmpty()) {
			throw new IllegalArgumentException("mapping impossible because PORCs are not loaded");
		}
		
		for (Clog porc : porcId2Porc.values()) {
			for (Long proteomeId : porc.getProteomeId2protein().keySet()) {
				
				// add all correspondant NCBI taxids in the list
				// to chose after that on which species we want to projet interactions
				Long taxid = proteomeId2Taxid.get(proteomeId);
				porcTaxids.add(taxid);
				String proteinAc = porc.getProteomeId2protein().get(proteomeId);
				String key = proteinAc+"_"+taxid;
				
				// sometimes some different proteomeIds can point to the same NCBI taxid (related strains probably...)
				// for example 
				//A.tumefaciens Cereon | 62          | 176299         | 0    | Agrobacterium tumefaciens Cereon
				//A.tumefaciens Dupont | 73          | 176299         | 0    | Agrobacterium tumefaciens Dupont 
				// In that case we have the same protein Ac and the same taxid that can appear several times in the same clog
				// but it is not a problem as it is the same clog, we are just merging related proteins
				// but we must not have the same thing in different clogs
				if (protein2porc.containsKey(key)) {
					
					if (porc.getId()!=porcId2Porc.get(protein2porc.get(key)).getId()) {
						log.warn(key+" already in the map with a different PORC id -> "+protein2porc.get(key));
					}
				}
				
				// add this protein of this species in the mapping
				protein2porc.put(key, porc.getId());
			}
			
		}
		
		log.warn(INDENT_STRING+protein2porc.size()+ " proteins are found in the PORC data");
	}
	
	/**
	 * @param porcInteraction
	 * @param uniprotAcA
	 * @param uniprotAcB
	 * @return
	 */
	private static StrBuilder printAllInformation(ClogInteraction porcInteraction, String uniprotAcA, String uniprotAcB) {
		
		StrBuilder buf = new StrBuilder();
		for (BinaryInteraction src : porcInteraction.getSourceInteractions()) {
			buf.append(uniprotAcA).append(SEP);
			buf.append(uniprotAcB).append(SEP);
			//buf.append(src.toString()).append("\n");
			buf.append(printMitabInteraction(src)).append("\n");
		}
		
		return buf;
	}
	
	/**
	 * @return
	 */
	public static StrBuilder getMitabHeaders() {
		StrBuilder buf = new StrBuilder();
		buf.append("UniprotA").append(SEP);
		buf.append("UniprotB").append(SEP);
		buf.append("Taxid").append(SEP);
		buf.append("DetectionMethodIds").append(SEP);
		buf.append("DetctionMethodNames");
		return buf;
	}
	
	/**
	 * @param interaction
	 * @return
	 */
	private static StrBuilder printMitabInteraction(BinaryInteraction interaction) {
		StrBuilder buf = new StrBuilder();
		buf.append(MitabUtils.getUniprotAcA(interaction)).append(SEP);
		buf.append(MitabUtils.getUniprotAcB(interaction)).append(SEP);
		String spId = MitabUtils.getSpeciesTaxidA(interaction);
		if (!spId.equals(MitabUtils.getSpeciesTaxidB(interaction))) {
			log.warn("A inter-species interaction has been used here as a source interaction"+ interaction);
		}
		buf.append(spId).append(SEP);
		buf.appendWithSeparators(MitabUtils.getDetectionMethodsIds(interaction), ";").append(SEP);
		buf.appendWithSeparators(MitabUtils.getDetectionMethodsNames(interaction), ";");
		return buf;
	}
	
	/**
	 * @param mitab source file MITAB25
	 * @param xml output file PSI25-XML
	 * @param nbINteractions number of interactions in the source file.
	 * @throws InterologPredictionException
	 */
	private void convertMitabFileToXML(File mitab, File xml, int nbInteractions) throws InterologPredictionException {
		
		if (isGenerateXml()) {
			
			if (nbInteractions <= nbInterMaxForXml) {
				Collection<BinaryInteraction> binaryInteractions = readMitabInteractions(mitab);
				writeXMLFile(xml, binaryInteractions);
				
			} else {
				log.warn("The file "+xml.getAbsolutePath()+
						" is not generated because there are more than "+nbInterMaxForXml+" interactions ("+nbInteractions+")");
				if (xml.exists()) {
					log.warn("The previsous "+xml.getAbsolutePath()+" file is deleted");
					xml.delete();
				}
			}
			
		}
		
	}
	
	/**
	 * @param xml
	 * @param interactions
	 * @throws InterologPredictionException
	 */
	private void writeXMLFile(File xml, Collection<BinaryInteraction> interactions) throws InterologPredictionException {
		log.warn("Write "+interactions.size()+" interactions in the xml file "+xml.getName());
		
		Tab2Xml converter = new Tab2Xml();
		
		EntrySet entry = null;
		try {
			entry = converter.convert(interactions);
		} catch (IllegalAccessException e) {
			throw new InterologPredictionException("Error while converting MITAB to XML",e);
		} catch (XmlConvertionException e) {
			throw new InterologPredictionException("Error while converting MITAB to XML",e);
		}
		
		PsimiXmlWriter writer = new PsimiXmlWriter();
		try {
			writer.write(entry,xml);
		} catch (PsimiXmlWriterException e) {
			throw new InterologPredictionException("Error while writting XML file "+xml.getAbsolutePath(),e);
		}
		
		printMemoryInfo();
		
	}
	
	
	/**
	 * @param mitab
	 * @param interactions
	 * @throws InterologPredictionException
	 */
	private void writeFile(File mitab, Collection<BinaryInteraction> interactions) throws InterologPredictionException {
		log.warn("Write "+interactions.size()+" interactions in the mitab file "+mitab.getName());
		PsimiTabWriter writer = new PsimiTabWriter();
		try {
			writer.write(interactions, mitab);
		} catch (ConverterException e) {
			throw new InterologPredictionException("Error while writting mitab file "+mitab, e);
		} catch (IOException e) {
			throw new InterologPredictionException("Error while writting mitab file "+mitab,e);
		}
		printMemoryInfo();
	}
	
	/**
	 * @throws InterologPredictionException
	 */
	private void createGlobalFiles() throws InterologPredictionException {
		Collection<BinaryInteraction> all = readMitabInteractions(getPredictedMitabFile());
		Collection<BinaryInteraction> known = readMitabInteractions(getKnownMitabFile());
		all.addAll(known);
		writeFile(getGlobalMitabFile(), all);
		convertMitabFileToXML(getGlobalMitabFile(), getGlobalXmlFile(), all.size());
		
	}
	
	/**
	 * Check input files
	 * Set start time
	 * Chose species for which we will predict interactions
	 * Parse porc file
	 * @throws InterologPredictionException
	 */
	private void preProcess() throws InterologPredictionException {
		log.warn("==================== Pre-process  ====================");
		printMemoryInfo();
		start = System.currentTimeMillis();
		
		checkFiles();// -> collect mitab NCBI taxids list in the same time
		parseProteomes();
		parsePorcs();
		mapProtein2ItsPorc();// -> collect clog taxids list in the same time
		
		// chose species for which we want to predict interactions
		chooseSpeciesToPredict();
		if (false) {
			testTaxids();
		}
		if (proteomeIdsToDownCast.isEmpty()) {
			log.warn("The list of species is empty, it is not useful to work... no interaction is predicted.");
			System.exit(0);
		}
		
		// generate file with known interactions
		generateKnownInteractionFile();
		
	}
	
	/**
	 * Parse the mitab file to compute a collection of porc interactions.
	 * .
	 * @throws InterologPredictionException
	 */
	private void upCast() throws InterologPredictionException {
		log.warn("==================== Up-cast      ====================");
		log.warn("Combine protein-protein interactions and clusters of orthologous proteins.");
		
		PrintStream ps = null;
		if (writePorcInteractions) {
			try {
				ps = new PrintStream(getFILE_RESULT_PORC_INTERACTIONS());
			} catch (FileNotFoundException e) {
				log.warn(getFILE_RESULT_PORC_INTERACTIONS()+" not found, porc interactions will not be printed.");
				writePorcInteractions = false;
			}
			ps.append(ClogInteraction.getHeaders().toString()).append("\n");
		}
		
		// parse interaction file
		// we need a map to have access to a clog interaction already created and to complete it with new information
		// as for example the source interaction which has enabled to predict this porc interaction
		Map<ClogInteraction, ClogInteraction> porcInteraction2porcInteraction = new HashMap<ClogInteraction, ClogInteraction>();
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
			Long porcIdA = protein2porc.get(proteinAcA+"_"+taxid);
			Long porcIdB = protein2porc.get(proteinAcB+"_"+taxid);
			
			if (porcIdA==null && porcIdB==null && taxid!=null) {
				Collection<Long> childrenTaxids = taxid2ChildrenTaxids.get(taxid);
				if (childrenTaxids!=null) {
					Iterator currentChildren = childrenTaxids.iterator();
					while (porcIdA==null && porcIdB==null && currentChildren.hasNext()) {
						Long newTaxid = (Long) currentChildren.next();
						porcIdA = protein2porc.get(proteinAcA+"_"+newTaxid);
						porcIdB = protein2porc.get(proteinAcB+"_"+newTaxid);
					}
				}
			}
			
			if (porcIdA==null) {
				protNotInClog++;
				continue;
			}
			if (porcIdB==null) {
				protNotInClog++;
				continue;
			}
			
			// create porc interactions
			abstracted++;
			
			
			// self-interaction for porc ? should be impossible because only one protein of each species in a porc...
			// could happen when merging different strains
			// all proteins of the clog will have a putative self interaction
			// do not want to add such questionable things
			if (!porcIdA.equals(porcIdB)) {
				Clog porcA = porcId2Porc.get(porcIdA);
				Clog porcB = porcId2Porc.get(porcIdB);
				
				if (porcA!=null && porcB!=null) {
					ClogInteraction newPorcInteraction = new ClogInteraction(porcA, porcB);
					
					if (porcInteraction2porcInteraction.containsKey(newPorcInteraction)) {
						// complete the clog interaction already present in the map
						// with the new features of this prediction
						ClogInteraction oldClogInteraction = porcInteraction2porcInteraction.get(newPorcInteraction);
						oldClogInteraction.getSourceInteractions().add(psimiInteraction);
					} else {
						newPorcInteraction.getSourceInteractions().add(psimiInteraction);
						porcInteraction2porcInteraction.put(newPorcInteraction, newPorcInteraction);
					}
					
		        	
				}
			} else {
				log.info("skip self porc interaction porc "+porcIdA+" from proteins "+proteinAcA+" - "+proteinAcB);
			}
				
        }
		
		if (isWritePorcInteractions()) {
			int totalSize = porcInteraction2porcInteraction.size();
			int current=0, done=0;
			for (ClogInteraction interaction : porcInteraction2porcInteraction.values()) {
				current++;
				done++;
				ps.append(interaction.toString()).append("\n");
				if(current%10000==0) {
					current=0;
					log.info("10000 new interactions written - remain "+(totalSize-done));
				}
			}
			ps.close();
			log.info("file"+getFILE_RESULT_PORC_INTERACTIONS()+" written");
		}
		
		log.info(count+" source interactions");
		log.info(protNotInClog+" interactions miss at least one protein in the porcs");
		log.info(abstracted+" have been abstracted");
		log.warn(INDENT_STRING+porcInteraction2porcInteraction.size()+" distinct interactions between 2 clusters are constructed");
		
		porcInteractions = porcInteraction2porcInteraction.values();
		
		// remove the clog data to have a little more space
		log.debug("remove porc data");
		porcId2Porc = null;
		printMemoryInfo();
	}
	
	
	/**
	 * Down-cast all porc interactions into protein-protein interaction for all considered species.
	 * 
	 * @return
	 */
	private Collection<BinaryInteraction> downCast(ClogInteraction porcInteraction) {
		StrBuilder history = new StrBuilder();
		history.append("porc interaction ").append(porcInteraction.getClogA().getId()).append(" - ").append(porcInteraction.getClogB().getId()).append("\n");
		
		StrBuilder scenario = new StrBuilder();
		scenario.append(porcInteraction.getClogA().getId()).append("\t");
		scenario.append(porcInteraction.getClogB().getId()).append("\t");
		scenario.append(porcInteraction.getClogA().getProteomeId2protein().size()).append("\t");
		scenario.append(porcInteraction.getClogB().getProteomeId2protein().size()).append("\t");
		scenario.append(porcInteraction.getSourceInteractions().size()).append("\t");
		int inference = 0;
		
		StrBuilder srcInteractionsLocal = new StrBuilder();
		
		Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
		Collection<Long> speciesA = porcInteraction.getClogA().getProteomeId2protein().keySet();
		Collection<Long> speciesB = porcInteraction.getClogB().getProteomeId2protein().keySet();
		
		for (Long sp:speciesA) {
			//history.append("proteome id = ").append(sp);
			
			// down-cast only on species specified in the list
			// no need to check the different taxid levels as proteomeIds are always as specific as possible
			if (proteomeIdsToDownCast.contains(sp)) {
				Long id = proteomeId2Taxid.get(sp);
				history.append("taxid id ").append(id).append(": ");
				
				if (speciesB.contains(sp)) {
					String uniprotIdA = porcInteraction.getClogA().getProteomeId2protein().get(sp);
					String uniprotIdB = porcInteraction.getClogB().getProteomeId2protein().get(sp);
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
					Organism o = new OrganismImpl(taxid);
					
					Collection<CrossReference> identifiersA = new ArrayList<CrossReference>(1);
			        identifiersA.add( new CrossReferenceImpl( getUNIPROTKB(), uniprotIdA ) );
			        Interactor iA = new Interactor( identifiersA );
			        iA.setOrganism(o);
			        
			        Collection<CrossReference> identifiersB = new ArrayList<CrossReference>(1);
			        identifiersB.add( new CrossReferenceImpl( getUNIPROTKB(), uniprotIdB ) );
			        Interactor iB = new Interactor( identifiersB );
			        iB.setOrganism(o);
			        
			        // test here both identifiers A and B and source interactions of this clog interaction
			        //  we do not want to predict interactions from which this porc interaction comes from
			        if (porcInteraction.isPredictedBy(uniprotIdA, uniprotIdB)) {
			        	sourceInteraction++;
			        	history.append(" -> source interaction\n");
			        	continue;
			        }
			        
			        
			        
					BinaryInteraction interaction = new BinaryInteractionImpl(iA, iB);
					inference++;
					
					// authors - publi
					List<Author> authors = new ArrayList<Author>(1);
					Author me = new AuthorImpl();
					me.setName(AUTHORS);
					authors.add(me);
					interaction.setAuthors(authors);
					List<CrossReference> publis = new ArrayList<CrossReference>(1);
					publis.add( new CrossReferenceImpl( PUBMED_DB, MICHAUT_ET_AL_ID ) );
					interaction.setPublications(publis);
					
					// detection method = interologs mapping
					List <InteractionDetectionMethod> methods = new ArrayList<InteractionDetectionMethod>(1);
					InteractionDetectionMethod interologsMapping = new InteractionDetectionMethodImpl();
					interologsMapping.setDatabase(METHOD_DATABASE);
					interologsMapping.setIdentifier(METHOD_ID);
					interologsMapping.setText(METHOD_NAME);
					methods.add(interologsMapping);
					interaction.setDetectionMethods(methods);
					
					interaction.setInteractionAcs(null);
					interaction.setInteractionTypes(null);// physical interaction?
					interaction.setSourceDatabases(null);
					interaction.setConfidenceValues(null);
					
					interactions.add(interaction);
					history.append(" -> predicted\n");
					
					srcInteractionsLocal.append(printAllInformation(porcInteraction, uniprotIdA, uniprotIdB));
					
				}
				
			}
			
		}
		
		scenario.append(inference);
		
		if (isWriteDownCastHistory()) {
			//downCastHistory.append(history.toString()).append("\n");
			downCastHistory.append(scenario.toString()).append("\n");
		}
		
		if (isWriteSrcInteractions()) {
			srcInteractions.append(srcInteractionsLocal.toString());
		}
		
		return interactions;
	}
	
	
	/**
	 * Create all interactions possible from these porc interactions;
	 * We only predict interactions for all species specified in the proteomeId;
	 * .
	 * @return
	 */
	private void downCast() {
		log.warn("==================== Down-cast    ====================");
				
		if (isWriteDownCastHistory()) {
			try {
				downCastHistory = new PrintStream(getFILE_HISTORY_DOWN_CAST());
				downCastHistory.append("porcA\tporcB\tprot1\tprot2\tsources\tinferences\n");
				log.warn("Create file "+getFILE_HISTORY_DOWN_CAST().getName());
			} catch (FileNotFoundException e) {
				log.warn("down cast history is redirected on System.out");
				downCastHistory = System.out;
			}
		}
		
		if (isWriteSrcInteractions()) {
			try {
				srcInteractions = new PrintStream(getFILE_SOURCE_INTERACTIONS());
				srcInteractions.append("ProteinAcA\tProteinAcB\t");
				srcInteractions.append(getMitabHeaders().toString()).append("\n");
				log.warn("Create file "+getFILE_SOURCE_INTERACTIONS().getName());
			} catch (FileNotFoundException e) {
				log.warn("down source interactions printstream is redirected on System.out");
				srcInteractions = System.out;
			}
		}
		
		
		for (ClogInteraction porcInteraction : porcInteractions) {
			Collection<BinaryInteraction> downCastedInteractions = downCast(porcInteraction);
			interactions.addAll(downCastedInteractions);
		}
		
		log.warn(INDENT_STRING+interactions.size()+" binary interactions predicted");
		log.info(sourceInteraction+" binary interactions have not been predicted because they are source interactions");
		log.debug(IPIid+" binary interactions have not been predicted because IPI id");
		log.info(noMatchUniprotId+" binary interactions have not been predicted because uniprot id missed");
		
		if (isWriteDownCastHistory()) {
			downCastHistory.close();
		}
		
		if (isWriteSrcInteractions()) {
			srcInteractions.close();
		}
		
		printMemoryInfo();
	}
	
	
	/**
	 * Write predicted interactions in mitab and psi25-XML files.
	 * Write all interactions (known and predicted together) in mitab and psi25-XML files.
	 * Print global execution time.
	 * @throws InterologPredictionException
	 */
	private void postProcess() throws InterologPredictionException {
		log.warn("==================== Post-process ====================");
		
		// known interactions only have alread been written in files during pre-process.
		
		// predicted interactions
		writeFile(getPredictedMitabFile(), getInteractions());
		convertMitabFileToXML(getPredictedMitabFile(), getPredictedXmlFile(), getInteractions().size());
		
		// global files (predicted and known)
		createGlobalFiles();
		
		float elapsedTimeMin = (System.currentTimeMillis()-start)/(60*1000F);
		log.info("time elapsed: "+elapsedTimeMin+" min");
		log.warn("======================================================");
		
		log.warn("");
		log.warn("Thanks for using the interoPORC tool. All information is available on http://biodev.extra.cea.fr/interoporc/");
		log.warn("");
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
		Long[] coliTab = new Long[] {511145l,244314l, 244318l, 199310l, 364106l, 244323l, 362663l, 366838l, 244319l, 340185l, 244316l, 244322l, 155864l, 316401l, 244324l, 358709l, 316435l, 397449l, 168807l, 397447l, 217992l, 168927l, 366837l, 244326l, 340186l, 316397l, 397454l, 244325l, 397453l, 366839l, 244321l, 341037l, 373045l, 397448l, 331111l, 397452l, 83333l, 183192l, 366836l, 344610l, 37762l, 405955l, 397450l, 216592l, 244317l, 331112l, 344601l, 244315l, 340197l, 216593l, 83334l, 397451l, 340184l, 244320l};
		map.put(562l, Arrays.asList( coliTab ));
		
		// pylori
		Long[] pyloriTab = new Long[] {85963l, 102617l, 85962l, 357544l, 102618l, 102619l};
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
	 * A test function to return a non-null and non-empty interaction collection.
	 * @return
	 */
	public static Collection<BinaryInteraction> createTestInteractionCollection() {
		Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
		
		//Organism o = new OrganismImpl(1148);
		OrganismFactory f = OrganismFactory.getInstance();
		Organism o = f.build(1148);
		//o.setIdentifiers(identifiers);
		
		Collection<CrossReference> identifiersA = new ArrayList<CrossReference>(1);
        identifiersA.add( new CrossReferenceImpl( getUNIPROTKB(), "P74638" ) );
        Interactor iA = new Interactor( identifiersA );
        iA.setOrganism(o);
        
        Collection<CrossReference> identifiersB = new ArrayList<CrossReference>(1);
        identifiersB.add( new CrossReferenceImpl( getUNIPROTKB(), "Q57417" ) );
        Interactor iB = new Interactor( identifiersB );
        iB.setOrganism(o);
		BinaryInteraction interaction = new BinaryInteractionImpl(iA, iB);
		List<CrossReference> interactionAcs = new ArrayList<CrossReference>();
		interactionAcs.add(new CrossReferenceImpl( getUNIPROTKB(), "interactionsAcs" ));
		interaction.setInteractionAcs(interactionAcs);
		
		interactions.add(interaction);
		
		
		return interactions;
	}
	
	/**
	 * Put memory information in the log.
	 */
	public static void printMemoryInfo() {
		log.info(InterologUtils.printRuntime());
	}
	
	/**
	 * @param propFileName
	 * @return
	 */
	public static Log initLog(String propFileName) {
		PropertyConfigurator.configure(propFileName);
		return LogFactory.getLog(InterologPrediction.class);
	}
	
	
	/**
	 * @param args
	 * @throws InterologPredictionException 
	 * @throws NewtException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterologPredictionException, NewtException{
		
		
		File dir = new File("/Users/mmichaut/Documents/These/Results/Inference/interoPorc/test/");
		
		//File mitab = new File("/Users/mmichaut/Documents/EBI/data/IntAct/intact.mitab");
		File mitab = new File("/Users/mmichaut/Documents/Data/interaction/psimi/tab/07.05.24_global_only7speciesWithStrains.mitab");
		//File mitab = new File("/Users/mmichaut/Documents/EBI/data/PsimiTab/global.mitab");
		
		//File porcFileOldFormat = new File("/Users/mmichaut/Documents/EBI/data/Integr8/clog/20070630/clog.revised2.dat");
		File porcFile = new File("/Users/mmichaut/Documents/Data/integr8/PORC/porc_gene.dat");
		
		InterologPrediction up = new InterologPrediction(dir);
		up.setMitab(mitab);
		up.setPorc(porcFile);
		Collection<Long> taxids = new HashSet<Long>(1);
		taxids.add(1148l);
		up.setUserTaxidsToDownCast(taxids);
		
		up.setDownCastOnAllPresentSpecies(false);
		//up.setClassicPorcFormat(true);
		up.setWriteDownCastHistory(true);
		up.setWriteSrcInteractions(true);
		ClogInteraction.setNB_LINES_MAX(100000);
		up.setWritePorcInteractions(false);
		up.setDownCastOnChildren(true);
		
		
		//up.writeXMLFile(new File("/Users/mmichaut/Desktop/testXMLwriter.txt"), createTestInteractionCollection());
		
		
		up.run();
	}
	
}
