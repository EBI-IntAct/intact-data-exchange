/**
 * 
 */
package uk.ac.ebi.intact.interolog.mitab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.txt2tab.behaviour.IgnoreAndPrintUnparseableLine;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.tab.processor.ClusterInteractorPairProcessor;
import psidev.psi.mi.tab.utils.PsimiTabFileMerger;
import psidev.psi.mi.xml.converter.ConverterException;


/**
 * All methods that we can apply on mitab files or BinaryInteraction.
 * 
 * @author mmichaut
 * @version $Id$
 * @since 8 juin 07
 */
public class MitabUtils implements MitabFiles {
	
	///////////////////////////////
	////////// FIELDS
	/////////
	
	
	/*----------------------STATIC-----------------------*/
	
	
	public static final Log log = LogFactory.getLog(MitabUtils.class);
	
	/**
	 * String used to separate different columns.
	 */
	private final static String SEP = "\t";
	
	/**
	 * Used to read and write cross-ref.
	 */
	private static final String UNIPROTKB = "uniprotkb";
	
	/**
	 * CV terms.
	 */
	private final static String CV_GENETIC_INTERACTION = "genetic interaction";
	public final static String CV_PHYSICAL_INTERACTION = "physical interaction";
	private final static String CV_COLOCALIZATION = "colocalization";
	
	
	
	///////////////////////////////
	////////// METHODS
	/////////
	
	/* ------------- BinaryInteraction --------------*/
	
	/**
	 * Get Uniprot ac of proteinA in a psimi interaction.
	 * null if it does not exist.
	 * 
	 * @param interaction
	 * @return
	 */
	public static String getUniprotAcA(BinaryInteraction interaction) {
		Collection<CrossReference> refs = interaction.getInteractorA().getIdentifiers();
		for (CrossReference reference : refs) {
			if (reference.getDatabase().equals(UNIPROTKB)) {
				return reference.getIdentifier();
			}
		}
		return null;
	}
	
	/**
	 * Get Uniprot ac of proteinB in a psimi interaction.
	 * null if it does not exist.
	 * 
	 * @param interaction
	 * @return
	 */
	public static String getUniprotAcB(BinaryInteraction interaction) {
		Collection<CrossReference> refs = interaction.getInteractorB().getIdentifiers();
		for (CrossReference reference : refs) {
			if (reference.getDatabase().equals(UNIPROTKB)) {
				return reference.getIdentifier();
			}
		}
		return null;
	}
	
	/**
	 * Get the parent term: genetic, physical or colocalization.
	 * @param psimiInteraction
	 * @return
	 */
	public static String getInteractionType(BinaryInteraction psimiInteraction) {
		String interactionType = null;
		List<InteractionType> types = psimiInteraction.getInteractionTypes();
		
		Iterator iter = types.iterator();
		if (iter.hasNext()) {
			InteractionType type = (InteractionType) iter.next();
			interactionType = parentInteractionType(type.getIdentifier());
		}

		return interactionType;
	}
	
	/**
	 * Test if this interaction has the type 'physical interaction' or any of a children in the ontology.
	 * @return
	 */
	public static boolean isPhysicalInteraction(BinaryInteraction psimiInteraction) {
		String type = getInteractionType(psimiInteraction);
		if (type==null) {
			return false;
		}
		return type.equals(MitabUtils.CV_PHYSICAL_INTERACTION);
	}
	
	/**
	 * Test if both interactors have the same UniprotKB id.
	 * If one UniprotKB id is missing, return false.
	 * @param psimiInteraction
	 * @return
	 */
	public static boolean isSelf(BinaryInteraction psimiInteraction) {	
		String aca = getUniprotAcA(psimiInteraction);
		String acb = getUniprotAcB(psimiInteraction);
		if (aca==null || acb==null) {
			return false;
		}
		return aca.equals(acb);
	}
	
	/**
	 * Test if both taxids are the same.
	 * If one is null return false.
	 * @param psimiInteraction
	 * @return
	 */
	public static boolean isInterSpecies(BinaryInteraction psimiInteraction) {
		String taxidA = psimiInteraction.getInteractorA().getOrganism().getTaxid();
		String taxidB = psimiInteraction.getInteractorB().getOrganism().getTaxid();
		if (taxidA==null || taxidB==null) {
			return false;
		}
		return !taxidA.equals(taxidB);
	}
	
	/**
	 * @param identifier
	 * @return
	 */
	private static String parentInteractionType(String identifier) {
		boolean withOBOparser = false;
		
		Collection<String> physicalChildren=null, colocalizationChildren=null, geneticChildren=null;
		
		if (withOBOparser) {
			/*OboParser parser = OboUtils.MI_OBO_PARSER;
			physicalChildren = parser.getChildrenIds(OboUtils.ENUM_MI_ID_PHYSICAL_INTERACTION);
			colocalizationChildren = parser.getChildrenIds(OboUtils.ENUM_MI_ID_COLOCALIZATION);
			geneticChildren = parser.getChildrenIds(OboUtils.ENUM_MI_ID_GENETIC_INTERACTION);*/
			
		}else {
			physicalChildren = new HashSet<String>();
			physicalChildren.add("0218");
			physicalChildren.add("0407");
			physicalChildren.add("0195");
			physicalChildren.add("0414");
			physicalChildren.add("0408");
			physicalChildren.add("0556");
			physicalChildren.add("0192");
			physicalChildren.add("0193");
			physicalChildren.add("0194");
			physicalChildren.add("0197");
			physicalChildren.add("0199");
			physicalChildren.add("0203");
			physicalChildren.add("0204");
			physicalChildren.add("0207");
			physicalChildren.add("0210");
			physicalChildren.add("0211");
			physicalChildren.add("0213");
			physicalChildren.add("0217");
			physicalChildren.add("0220");
			physicalChildren.add("0557");
			physicalChildren.add("0558");
			physicalChildren.add("0559");
			physicalChildren.add("0566");
			physicalChildren.add("0567");
			physicalChildren.add("0568");
			physicalChildren.add("0569");
			physicalChildren.add("0701");
			physicalChildren.add("0844");
			physicalChildren.add("0212");
			physicalChildren.add("0570");
			physicalChildren.add("0571");
			physicalChildren.add("0572");
			physicalChildren.add("0206");
			physicalChildren.add("0209");
			physicalChildren.add("0214");
			physicalChildren.add("0216");
			physicalChildren.add("0198");
			physicalChildren.add("0200");
			physicalChildren.add("0201");
			physicalChildren.add("0202");
			
			colocalizationChildren = new HashSet<String>();
			colocalizationChildren.add("0403");
			
			geneticChildren = new HashSet<String>();
			geneticChildren.add("0208");
			geneticChildren.add("0794");
			geneticChildren.add("0795");
			geneticChildren.add("0796");
			geneticChildren.add("0797");
			geneticChildren.add("0798");
			geneticChildren.add("0799");
			geneticChildren.add("0800");
			geneticChildren.add("0801");
			geneticChildren.add("0802");
		}
		
		if (physicalChildren.contains(identifier)) {
			return CV_PHYSICAL_INTERACTION;
		}
		else if (colocalizationChildren.contains(identifier)) {
			return CV_COLOCALIZATION;
		}
		else if (geneticChildren.contains(identifier)) {
			return CV_GENETIC_INTERACTION;
		}
		else {
			return null;
		}
	}
	
	
	
	
	/* ------------- File mitab --------------*/
	
	/**
	 * Get an iterator to parse a mitab file with headers.
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static Iterator<BinaryInteraction> getIterator (File mitab) throws MitabException {
		return getIterator(mitab, true);
	}
	
	/**
	 * Get an iterator to parse a mitab file;
	 * .
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static Iterator<BinaryInteraction> getIterator (File mitab, boolean hasFileHeader) throws MitabException {
        PsimiTabReader mitabReader = new PsimiTabReader( hasFileHeader );
        mitabReader.setUnparseableLineBehaviour(new IgnoreAndPrintUnparseableLine(System.out));
        Iterator<BinaryInteraction> iterator;
		try {
			iterator = mitabReader.iterate( mitab );
		} catch (ConverterException e) {
			throw new MitabException("Converter Exception when parsing mitab file "+mitab.getName(), e);
		} catch (IOException e) {
			throw new MitabException("IO Exception when parsing mitab file "+mitab.getName(), e);
		}
		return iterator;
	}
	
	/**
	 * @param mitab
	 * @return all the file as a BinaryInteraction collection
	 * @throws MitabException 
	 */
	public static Collection<BinaryInteraction> readMiTab (File mitab) throws MitabException {
		Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
        Iterator<BinaryInteraction> iterator = getIterator(mitab);
		while ( iterator.hasNext() ) {
        	interactions.add(iterator.next());
        }
        return interactions;
	}
	
	
	/**
	 * Return a description of the file;
	 * -Nb of interactions
	 * -Nb of organisms
	 * -Each organism with its nb of interactions
	 * .
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static StrBuilder describeMitab(File mitab){
		Collection<BinaryInteraction> interactions = null;
		try {
			interactions = MitabUtils.readMiTab(mitab);
		} catch (MitabException e) {
			System.err.println("Error while parsing mitab file "+mitab.getName());
			System.exit(-1);
		}
		
		Map<String, Collection<BinaryInteraction>> speciesMap = new HashMap<String, Collection<BinaryInteraction>>();
		Collection<BinaryInteraction> multiSpecies = new ArrayList<BinaryInteraction>();
		Collection<BinaryInteraction> noOrganism = new ArrayList<BinaryInteraction>();
		Collection<BinaryInteraction> noTaxid = new ArrayList<BinaryInteraction>();
		
		for (BinaryInteraction interaction : interactions) {
			
			if (interaction.getInteractorA().getOrganism()==null
					|| interaction.getInteractorB().getOrganism()==null ) {
				noOrganism.add(interaction);
				continue;
			}
			
			String taxidA = interaction.getInteractorA().getOrganism().getTaxid();
			String taxidB = interaction.getInteractorB().getOrganism().getTaxid();
			
			if (taxidA==null || taxidB==null) {
				noTaxid.add(interaction);
				continue;
			}
			
			if (!taxidA.equals(taxidB)) {
				multiSpecies.add(interaction);
				continue;
			}
			
			if (!speciesMap.containsKey(taxidA)) {
				speciesMap.put(taxidA, new ArrayList<BinaryInteraction>());
			}
			speciesMap.get(taxidA).add(interaction);
		}
      
        StrBuilder buf = new StrBuilder();
        buf.append("file: ").append(mitab.getName()).append("\n");
        buf.append("nb interactions: ").append(interactions.size()).append("\n");
        buf.append("multiSpecies").append(SEP).append(multiSpecies.size()).append("\n");
        buf.append("noOrganism").append(SEP).append(noOrganism.size()).append("\n");
        buf.append("noTaxid").append(SEP).append(noTaxid.size()).append("\n");
        buf.append("nbOrganisms").append(SEP).append(speciesMap.size()).append("\n");
        
        buf.append("Taxid").append(SEP).append("NbInteractions\n");
        for (String taxid : speciesMap.keySet()) {
			buf.append(taxid).append(SEP).append(speciesMap.get(taxid).size()).append("\n");
		}
        
        return buf;
	}
	
	/**
	 * @return
	 */
	public static String getFileDescriptorHeader() {
		StrBuilder buf = new StrBuilder();
		buf.append("File").append(SEP);
		buf.append("NbInteractions").append(SEP);
		buf.append("NbPublis").append(SEP);
		return buf.toString();
	}
	
	/**
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static StrBuilder describeMitabOneLine(File mitab) throws MitabException {
		Collection<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
		Iterator<BinaryInteraction> iterator = getIterator(mitab);
        int count = 0;
        
        Collection<CrossReference> publis = new HashSet<CrossReference>();
        while ( iterator.hasNext() ) {
        	count++;
        	BinaryInteraction interaction = iterator.next();
            interactions.add(interaction);
            publis.addAll(interaction.getPublications());
        }
        
        StrBuilder buf = new StrBuilder();
        buf.append(mitab.getName()).append(SEP);
        buf.append(count).append(SEP);
        buf.append(publis.size()).append(SEP);
        System.out.println(buf.toString());
        
        return buf;
	}
	
	
	/* ------------- Collection mitab files --------------*/
	
	
	/**
	 * @param files
	 */
	public static void describeMitab(Collection<File> files) {
		StrBuilder buf = new StrBuilder(), tmp = new StrBuilder();
		buf.append(getFileDescriptorHeader()).append("\n");
		for (File file : files) {
			try {
				tmp = describeMitabOneLine(file);
			} catch (MitabException e) {
				e.printStackTrace();
				continue;
			}
			buf.append(tmp).append("\n");
		}
		System.out.println(buf.toString());
	}
	
	

	/**
	 * All mitab files available.
	 * @return
	 */
	public static Collection<File> buildAllFiles() {
		Collection<File> files = new HashSet<File>();
		files.add(MITAB_INTACT);
		files.add(MITAB_MINT_SOURCE);
		files.add(MITAB_MINT_CLUSTERED);
		files.add(MITAB_DIP_SOURCE);
		files.add(MITAB_DIP_CLUSTERED);
		files.add(MITAB_INTACT_MINT);
		files.add(MITAB_INTACT_DIP);
		files.add(MITAB_MINT_DIP);
		files.add(MITAB_GLOBAL);
		return files;
	}
	
	/**
	 * All mitab files after clustering;
	 * IntAct, MINT, DIP;
	 * .
	 * @return
	 */
	public static Collection<File> buildClusteredFiles() {
		Collection<File> files = new HashSet<File>();
		files.add(MITAB_INTACT);
		files.add(MITAB_MINT_CLUSTERED);
		files.add(MITAB_DIP_CLUSTERED);
		return files;
	}
	
	/**
	 * Order always both ac in the same order to be able to merge the identical interactions, interologs and others;
	 * .
	 * @param acA
	 * @param acB
	 * @return
	 */
	public static boolean isInGoodOrder(String acA, String acB) {
		return acA.compareToIgnoreCase(acB) < 1;
	}
	
	
	
	/**
	 * Translate a mitab file into different format for a specific species.
	 * Venn list format:
	 * element group
	 * 
	 * http://www.informatik.uni-ulm.de/ni/mitarbeiter/HKestler/vennm/doc.html#explore;
	 * 
	 * Cytoscape format (SIF):
	 * acA interactionType acB
	 * 
	 * .
	 * @param mitab
	 * @param groupName
	 * @param interactionType
	 * @param taxid
	 * @param vennPs
	 * @param cytoscapePs
	 * @throws MitabException
	 */
	public static void toVennList (File mitab, Long taxid, boolean filterSelf, 
			String groupName, String interactionType,  
			PrintStream vennPs, PrintStream cytoscapePs) throws MitabException {
		
		Iterator<BinaryInteraction> iterator = getIterator(mitab);		
        int count = 0;
        while ( iterator.hasNext() ) {
        	count++;
        	BinaryInteraction interaction = iterator.next();
            
        	Long taxidA, taxidB;
        	try {
        		taxidA = Long.valueOf( interaction.getInteractorA().getOrganism().getTaxid() );
    			taxidB = Long.valueOf( interaction.getInteractorB().getOrganism().getTaxid() );
        	} catch (NumberFormatException e) {
        		log.debug("error with taxid for "+interaction);
        		continue;
        	}
        	
			
			if (taxidA==null || taxidB==null) {
				continue;
			}
			
			
			
			if (taxidA.equals(taxid) && taxidB.equals(taxid)) {
				
				String acA = getUniprotAcA(interaction);
				String acB = getUniprotAcB(interaction);
				
				if (filterSelf && acA.equals(acB)) {
					continue;
				}
				
				// Venn list
				String id;
				if (MitabUtils.isInGoodOrder(acA, acB)) {
					id = acA+"-"+acB;
				} else {
					id = acB+"-"+acA;
				}
				vennPs.append(id).append(SEP);
				vennPs.append(groupName).append("\n");
				
				
				// cytoscape
				cytoscapePs.append(acA).append(SEP).append(interactionType).append(SEP).append(acB).append("\n");
			}
        }
		
	}
	public static void toVennListFormat (File mitab, String groupName, PrintStream vennPs) throws MitabException {
		toVennListFormat(mitab, groupName, vennPs, true);
	}
	
	/**
	 * @param mitab
	 * @param groupName
	 * @param vennPs
	 * @throws MitabException
	 */
	public static void toVennListFormat (File mitab, String groupName, PrintStream vennPs, boolean withTaxid) throws MitabException {
		
		Iterator<BinaryInteraction> iterator = getIterator(mitab);		
        int count = 0;
        while ( iterator.hasNext() ) {
        	count++;
        	BinaryInteraction interaction = iterator.next();
            
        	Long taxidA, taxidB;
        	try {
        		taxidA = Long.valueOf( interaction.getInteractorA().getOrganism().getTaxid() );
    			taxidB = Long.valueOf( interaction.getInteractorB().getOrganism().getTaxid() );
        	} catch (NumberFormatException e) {
        		log.debug("error with taxid for "+interaction);
        		continue;
        	}
        	
			String taxid;
			if (taxidA.equals(taxidB)) {
				taxid = taxidA.toString();
			} else {
				taxid = taxidA + "-" + taxidB;
			}
			
			
			String acA = getUniprotAcA(interaction);
			String acB = getUniprotAcB(interaction);
			
			
			String id;
			if (acA==null || acB==null) {
				continue;
			}
			if (MitabUtils.isInGoodOrder(acA, acB)) {
				id = acA+"-"+acB;
			} else {
				id = acB+"-"+acA;
			}
			
			if (withTaxid) {
				vennPs.append(taxid).append("_");
			}
			vennPs.append(id).append(SEP);
			vennPs.append(groupName).append("\n");
			
        }
		
	}
	
	/**
	 * Write interactions in Cytoscape format (.sif).
	 * For a specific taxid (for both interactors).
	 * .
	 * @param mitab
	 * @param taxid
	 * @param interactionType
	 * @param ps
	 * @throws MitabException
	 */
	public static void toCytoscapeFormat (File mitab, Long taxid, String interactionType, PrintStream ps) throws MitabException {
		
		Iterator<BinaryInteraction> iterator = getIterator(mitab);		
        int count = 0;
        while ( iterator.hasNext() ) {
        	count++;
        	BinaryInteraction interaction = iterator.next();
            
        	Long taxidA, taxidB;
        	try {
        		taxidA = Long.valueOf( interaction.getInteractorA().getOrganism().getTaxid() );
    			taxidB = Long.valueOf( interaction.getInteractorB().getOrganism().getTaxid() );
        	} catch (NumberFormatException e) {
        		log.debug("error with taxid for "+interaction);
        		continue;
        	}
        	
        	if (!taxidA.equals(taxid) || !taxidB.equals(taxid)) {
        		continue;
        	}
			
			
			String acA = getUniprotAcA(interaction);
			String acB = getUniprotAcB(interaction);
			
			
			ps.append(acA).append(SEP).append(interactionType).append(SEP).append(acB).append("\n");
			
        }
		
	}
	
	
	/**
	 * @param interactions
	 * @return
	 */
	public static StrBuilder resume(Collection<BinaryInteraction> interactions) {
		StrBuilder buf = new StrBuilder();
		for (BinaryInteraction interaction : interactions) {
			buf.append("{");
			buf.append(getUniprotAcA(interaction)).append(",");
			buf.append(getUniprotAcB(interaction)).append(",");
			buf.appendWithSeparators(interaction.getInteractionTypes(), ";").append(",");
			buf.appendWithSeparators(interaction.getSourceDatabases(), ";");
			buf.append("}");
		}
		
		return buf;
	}
	
	/**
	 * @param source
	 * @param target
	 * @throws MitabException
	 */
	public static void clusterFile(File source, File target) throws MitabException{
		System.out.println("clustering file "+source.getName()+" into file "+target.getName());
		System.out.print("parsing source interaction file ... ");
		ClusterInteractorPairProcessor prc = new ClusterInteractorPairProcessor();
		Collection<BinaryInteraction> mitabInteractions = readMiTab(source);
		System.out.println("DONE -> "+mitabInteractions.size()+" source interactions");
		System.out.print("clustering file ... ");
		mitabInteractions = prc.process(mitabInteractions);
		System.out.println("DONE");
		PsimiTabWriter writer = new PsimiTabWriter();
		try {
			System.out.println("writing file "+target.getName()+" ... ");
			writer.write(mitabInteractions, target);
		} catch (ConverterException e) {
			throw new MitabException("Error while writting clustered file "+target, e);
		} catch (IOException e) {
			throw new MitabException("IO error while writting clustered file "+target, e);
		}
		System.out.println("DONE");
	}
	
	
	/**
	 * Merge mitab files from IntAct, MINT, and DIP;
	 * .
	 */
	public static void mergeAllFile() throws IOException, MitabException{
		Collection<File> files = new ArrayList<File>();
		files.add(MITAB_INTACT);
		files.add(MITAB_MINT_CLUSTERED);
		files.add(MITAB_DIP_CLUSTERED);
		try {
			PsimiTabFileMerger.merge(files, MITAB_GLOBAL);
		} catch (ConverterException e) {
			throw new MitabException("Error while merging mitab files", e);
		}
	}
	
	/**
	 * Cluster and merge all files.
	 */
	public static void updateFiles() throws MitabException, IOException{
		
		// cluster MINT
		clusterFile(MITAB_MINT_SOURCE, MITAB_MINT_CLUSTERED);
		
		// complete file with Picr
		//... and maybe the others?
		
		// cluster DIP
		clusterFile(MITAB_DIP_SOURCE, MITAB_DIP_CLUSTERED);
		
		// merge all files
		mergeAllFile();
		
	}
	
	public static void main(String[] argv) throws MitabException, FileNotFoundException{
		//clusterFile(MITAB_PREDICTION_CLOG_GLOBAL, MITAB_PREDICTION_CLOG_GLOBAL_CLUSTERED);
		//clusterFile(MITAB_PREDICTION_CLOG_INTACT, MITAB_PREDICTION_CLOG_INTACT_CLUSTERED);
		
//		mergeAllFile();
		//updateFiles();
		
		/*File cytos = new File(CommonConstant.getTMP_DIR()+"/cytoscape.sif");
		PrintStream ps = new PrintStream(cytos);
		toCytoscapeFormat(MITAB_GLOBAL, 1148l, "synecho_experimental", ps);
		toCytoscapeFormat(MITAB_PREDICTION_CLOG_GLOBAL, 1148l, "synecho_predicted_clog", ps);
		toCytoscapeFormat(MITAB_PREDICTION_INTEROLOG, 1148l, "synecho_predicted_interolog", ps);
		ps.close();*/
		
		File dir = new File("/Users/mmichaut/Documents/EBI/results/tmp/");
		File cytos = new File(dir.getAbsolutePath()+"/exp.sif");
		File venn = new File(dir.getAbsolutePath()+"/exp.list");
		File interolog = new File("/Users/mmichaut/Documents/EBI/results/transfer/10.0_BNRH_70.0/predictions.mitab");
		File clog = new File("/Users/mmichaut/Documents/EBI/results/clogs/07.06.27_global/clog.predictedInteractions.txt");
		PrintStream ps = new PrintStream(cytos);
		PrintStream psV = new PrintStream(venn);
		toVennList(MITAB_GLOBAL, 1148l, true, "exp", "exp", psV, ps);
		toVennList(interolog, 1148l, true, "interolog", "interolog", psV, ps);
		toVennList(clog, 1148l, true, "clog", "clog", psV, ps);
		ps.close();
		psV.close();
		
	}

}
