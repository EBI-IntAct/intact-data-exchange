/**
 * 
 */
package uk.ac.ebi.intact.interolog.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ebi.intact.util.newt.NewtBridge;
import uk.ac.ebi.intact.util.newt.NewtTerm;

/**
 * @author mmichaut
 * @version $Id$
 * @since 11 juin 07
 */
public class NewtUtils {
	
	//private static NewtTerm rootNode;
	private static NewtTerm viroids;
	private static NewtTerm viruses;
	private static NewtTerm cellularOrganisms;
	private static NewtTerm otherSequences;
	private static NewtTerm unclassifiedSequences;
	
	private static Collection<NewtTerm> rootNodes;
		
		
	private static void initRootTerms() {
		
		try {
			NewtBridge bridge = new NewtBridge();
			//rootNode = bridge.getNewtTerm(1);
			viroids = bridge.getNewtTerm(12884);
			viruses = bridge.getNewtTerm(10239);
			cellularOrganisms = bridge.getNewtTerm(131567);
			otherSequences = bridge.getNewtTerm(28384);
			unclassifiedSequences = bridge.getNewtTerm(12908);
			rootNodes = new HashSet<NewtTerm>(5);
			rootNodes.add(viroids);
			rootNodes.add(viruses);
			rootNodes.add(cellularOrganisms);
			rootNodes.add(otherSequences);
			rootNodes.add(unclassifiedSequences);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	/**
	 * Test if taxidA is a children of taxidB or taxidB is a children of taxidA;
	 * use NEWT taxonomy.
	 * all children are considered, not only the direct ones (can be grand-child...).
	 * .
	 * @param taxidA
	 * @param taxidB
	 * @return
	 * @throws IOException
	 */
	public static boolean existChildrenRelation (int taxidA, int taxidB) throws IOException {
		NewtBridge bridge = new NewtBridge();
    	NewtTerm termA = bridge.getNewtTerm(taxidA);
    	NewtTerm termB = bridge.getNewtTerm(taxidB);
    	bridge.retreiveChildren(termA, true);
    	bridge.retreiveChildren(termB, true);
    	
    	for (NewtTerm child:termA.getChildren()) {
    		int id = child.getTaxid();
    		if (id==taxidB) {
    			return true;
    		}
    	}
    	

    	for (NewtTerm child:termB.getChildren()) {
    		int id = child.getTaxid();
    		if (id==taxidA) {
    			return true;
    		}
    	}
    	return false;
	}
	
	/**
	 * @param taxid
	 * @return
	 * @throws NewtException 
	 * @throws IOException
	 */
	public static int getRank(int taxid) throws NewtException {
		initRootTerms();
		NewtBridge bridge = new NewtBridge();
		NewtTerm current;
		try {
			current = bridge.getNewtTerm(taxid);
		} catch (IOException e) {
			throw new NewtException("Error while collecting term from NEWT "+taxid, e);
		}
		int rank = 0;
		while (!rootNodes.contains(current)) {
			try {
				bridge.retreiveParents(current, true);
			} catch (IOException e) {
				throw new NewtException("Error while collecting parents from NEWT "+taxid, e);
			}
			Collection<NewtTerm> parents = current.getParents();
			if (parents.size()!= 1) {
				throw new NewtException("One NEWT term should have one parent. "+current+" has "+parents.size());
			}
			Iterator iter = parents.iterator();
			current = (NewtTerm) iter.next();
			rank++;
		}
		return rank;
	}
	
	/**
	 * Get NCBI taxid of the parent.
	 * We suppose all term has one parent and only one.
	 * .
	 * @param id
	 * @return
	 * @throws NewtException
	 */
	public static Long getParentTaxid(Long id) throws NewtException {
		long res = 0l;
		NewtBridge bridge = new NewtBridge();
		try {
			NewtTerm term = bridge.getNewtTerm(id.intValue());
			bridge.retreiveParents(term, true);
			Collection<NewtTerm> parents = term.getParents();
			if (parents.size()!=1) {
				throw new NewtException("One NEWT term should have one parent. "+term+" has "+parents.size());
			}
			Iterator iter = parents.iterator();
			NewtTerm parent = (NewtTerm) iter.next();
			res = parent.getTaxid();
		} catch (IOException e) {
			throw new NewtException("IO exception while collecting parent taxid", e);
		} catch (IllegalArgumentException e) {
			throw new NewtException("Taxid pbm when collecting parent taxid", e);
		}
		
		return res;
	}
	
	/**
	 * Get a collection with taxids of the direct children.
	 * @param id
	 * @return
	 * @throws NewtException
	 */
	public static Collection<Long> getChildrenTaxids(Long id, boolean all) throws NewtException {
		Collection<Long> childrenTaxids = new HashSet<Long>();
		NewtBridge bridge = new NewtBridge();
		try {
			NewtTerm term = bridge.getNewtTerm(id.intValue());
			bridge.retreiveChildren(term, all);
			Collection<NewtTerm> children = term.getChildren();
			for (NewtTerm child : children) {
				childrenTaxids.add(Long.valueOf( child.getTaxid()));
			}
		} catch (IOException e) {
			throw new NewtException("IO exception while collecting children taxid", e);
		} catch (IllegalArgumentException e) {
			throw new NewtException("Taxid pbm when collecting children taxid", e);
		}
		
		return childrenTaxids;
	}
	
	/**
	 * Recursive method to have a lsit with all taxid of the complete subgraph.
	 * @param id
	 * @return
	 * @throws NewtException
	 */
	public static Collection<Long> getReallyAllChildrenTaxids(Long id) throws NewtException {
		// TODO implement correctly...
		// just to be sure... should stop at the leaf before
		if (id==null) {
			throw new IllegalArgumentException("Cannot collect children taxids of id null");
		}
		
		Collection<Long> childrenTaxids = new HashSet<Long>();
		NewtBridge bridge = new NewtBridge();
		try {
			NewtTerm term = bridge.getNewtTerm(id.intValue());
			if (term==null) {
				return childrenTaxids;
			}
			bridge.retreiveChildren(term, true);
			Collection<NewtTerm> children = term.getChildren();
			if (children==null || children.isEmpty()) {
				// leaf
				childrenTaxids.add(Long.valueOf(term.getTaxid()));
			} else {
				// iteration on children
				for (NewtTerm child : children) {
					childrenTaxids.addAll(getReallyAllChildrenTaxids(Long.valueOf(child.getTaxid())));
				}
			}
			
		} catch (IOException e) {
			throw new NewtException("IO exception while collecting parent taxid", e);
		} catch (IllegalArgumentException e) {
			throw new NewtException("Taxid pbm when collecting parent taxid", e);
		}
		
		return childrenTaxids;
	}
	
	
	public static void main(String[] argv) throws NewtException{
		//int rk = getRank(83333);
		//System.out.println(rk);
		// TODO reimplement getChildren
		System.out.println( getChildrenTaxids(119163l, true ));
		System.out.println( getChildrenTaxids(119163l, false ));
		System.out.println( getReallyAllChildrenTaxids(119163l) );
		System.out.println( getReallyAllChildrenTaxids(562l) );
		//System.out.println( getReallyAllChildrenTaxids(358l) );
    	
	}

}
