/**
 * 
 */
package uk.ac.ebi.intact.interolog.mitab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.utils.PsiCollectionUtils;
import psidev.psi.mi.xml.converter.ConverterException;


/**
 * @author mmichaut
 * @version $Id$
 * @since 22 mai 07
 */
public class MitabStats implements MitabFiles {
	
	/**
	 * String used to separate different columns.
	 */
	private final static String SEP = "\t";
	
	private static File dir = new File("/Users/mmichaut/Documents/EBI/results/stats/");
	
	/**
	 * Should be put in the PsiCollectionUtils;
	 * Return all elements from a which are not in b;
	 * .
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection soustraction(final Collection a, final Collection b) {
        Collection list = new ArrayList();  
        
        for (Object object : a) {
			if (!b.contains(object)) {
				list.add(object);
			}
		}
        
        return list;
    }
	
	/**
	 * Return a new Collection with all elements in A but not in B.
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection getAWithoutB(final Collection a, final Collection b) {
				Collection interactions = new HashSet(a.size());
				for (Iterator iter = a.iterator(); iter.hasNext();) {
					Object element = iter.next();
					if (!b.contains(element)) {
						interactions.add(element);
					}
				}
				return interactions;
			}
	
	/**
	 * Parse a mitab file;
	 * Collect all disctinct publications in a HashSet;
	 * .
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static Collection<CrossReference> collectPublis (File mitab) throws MitabException {
		Collection<CrossReference> publications = new HashSet<CrossReference>();
        boolean hasFileHeader = true;
        PsimiTabReader mitabReader = new PsimiTabReader( hasFileHeader );
        Iterator<psidev.psi.mi.tab.model.BinaryInteraction> iterator;
		try {
			iterator = mitabReader.iterate( mitab );
		} catch (ConverterException e) {
			throw new MitabException("Converter Exception when parsing mitab file "+mitab.getName(), e);
		} catch (IOException e) {
			throw new MitabException("IO Exception when parsing mitab file "+mitab.getName(), e);
		}
        int count = 0;
        while ( iterator.hasNext() ) {
        	count++;
        	psidev.psi.mi.tab.model.BinaryInteraction interaction = iterator.next();
            publications.addAll(interaction.getPublications());
        }
        System.out.println(count+" interactions parsed and "+publications.size()+" publications");
        return publications;
	}
	
	/**
	 * Parse the mitab file;
	 * return a collection of simpler interactions, TwoInteractor;
	 * .
	 * @param mitab
	 * @return
	 * @throws MitabException
	 */
	public static Collection<TwoInteractor> collectTwoInteractors(File mitab) throws MitabException {
		System.out.println("parse file "+mitab.getName());
		Collection<TwoInteractor> twoInteractors = new HashSet<TwoInteractor>();
        boolean hasFileHeader = true;
        PsimiTabReader mitabReader = new PsimiTabReader( hasFileHeader );
        Iterator<psidev.psi.mi.tab.model.BinaryInteraction> iterator;
		try {
			iterator = mitabReader.iterate( mitab );
		} catch (ConverterException e) {
			throw new MitabException("Converter Exception when parsing mitab file "+mitab.getName(), e);
		} catch (IOException e) {
			throw new MitabException("IO Exception when parsing mitab file "+mitab.getName(), e);
		}
        int count = 0;
        while ( iterator.hasNext() ) {
        	count++;
        	//System.out.println(count);
        	psidev.psi.mi.tab.model.BinaryInteraction interaction = iterator.next();
        	TwoInteractor ti = new TwoInteractor( new SimpleInteractor( interaction.getInteractorA() ),
                     new SimpleInteractor( interaction.getInteractorB() ) );
            twoInteractors.add(ti);
        }
        System.out.println(count+" interactions parsed and "+twoInteractors.size()+" twoInteractors");
        return twoInteractors;
	}
	
	

	/**
	 * @param crs
	 * @param name
	 * @throws IOException
	 */
	public static void print(Collection obj, String name, boolean createFile) throws IOException {
		System.out.println("file "+name+": "+obj.size());
		if (createFile) {
			FileWriter fw = new FileWriter(new File(dir.getAbsolutePath()+"/"+name+".txt"));
			for (Object o : obj) {
				fw.write(o.toString());
				fw.write("\n");
			}
			fw.close();
		}
		
	}
	
	/**
	 * @param taa
	 * @param ata
	 * @param aat
	 * @throws IOException
	 */
	public static void processSets(Collection taa, Collection ata, Collection aat, boolean print, String type) throws IOException {
		Collection<Collection> sets = new ArrayList<Collection>();
		
		Collection tta = PsiCollectionUtils.intersection(taa, ata);
		Collection tat = PsiCollectionUtils.intersection(taa, aat);
		Collection att = PsiCollectionUtils.intersection(aat, ata);
		
		Collection ttt = PsiCollectionUtils.intersection(tta, aat);
		
		Collection ttf = soustraction(tta, ttt);
		Collection tft = soustraction(tat, ttt);
		Collection ftt = soustraction(att, ttt);
		
		Collection fft = soustraction(aat, tat);//fft = fat-ftt=(aat-tat)-ftt
		fft = soustraction(fft, ftt);
		
		Collection ftf = soustraction(ata, tta);//ftf = fta-ftt=(ata-tta)-ftt
		ftf = soustraction(ftf, ftt);
		
		Collection tff = soustraction(taa, tat);//tff = taf-ttf=(taa-tat)-ttf
		tff = soustraction(tff, ttf);
		
		sets.add(ttt);
		sets.add(ttf);
		sets.add(tft);
		sets.add(ftt);
		sets.add(fft);
		sets.add(ftf);
		sets.add(tff);
		
		// pbm -> name
		print(ttt, type+"ttt", print);
		print(ttf, type+"ttf", print);
		print(tft, type+"tft", print);
		print(ftt, type+"ftt", print);
		print(fft, type+"fft", print);
		print(ftf, type+"ftf", print);
		print(tff, type+"tff", print);
		
		
	}
	
	/**
	 * @throws MitabException
	 * @throws IOException
	 */
	public static void pubmedCollections() throws MitabException, IOException {
		// ORDER = (Intact,MINT,DIP)
		// true=t false=f true or false=a (all)
		// f=a-t
		// t=a-f
		Collection taa, ata, aat;
		taa = collectPublis(MitabFiles.MITAB_INTACT);
		ata = collectPublis(MitabFiles.MITAB_MINT_CLUSTERED);
		aat = collectPublis(MitabFiles.MITAB_DIP_CLUSTERED);
		processSets(taa, ata, aat, true, "publis.");
		
	}
	
	/**
	 * @throws MitabException
	 * @throws IOException
	 */
	public static void interactionStats() throws MitabException, IOException {
		interactionStats(MitabFiles.MITAB_INTACT, MitabFiles.MITAB_MINT_CLUSTERED, MitabFiles.MITAB_DIP_CLUSTERED);
	}
	
	/**
	 * @param mitab1
	 * @param mitab2
	 * @param mitab3
	 * @throws MitabException
	 * @throws IOException
	 */
	public static void interactionStats(File mitab1, File mitab2, File mitab3) throws MitabException, IOException {
		Collection taa, ata, aat;
		taa = collectTwoInteractors(mitab1);
		ata = collectTwoInteractors(mitab2);
		aat = collectTwoInteractors(mitab3);
		processSets(taa, ata, aat, true, "interactions.");
	}
	

	/**
	 * @param args
	 * @throws MitabException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws MitabException, IOException {
		File dir = new File("/Users/mmichaut/Documents/EBI/results/stats/");
		File intactVenn = new File(dir.getAbsolutePath()+"/intact.list");
		File mintVenn = new File(dir.getAbsolutePath()+"/mint.list");
		File dipVenn = new File(dir.getAbsolutePath()+"/dip.list");
		
		MitabUtils.toVennListFormat(MITAB_INTACT, "IntAct", new PrintStream(intactVenn));
		MitabUtils.toVennListFormat(MITAB_MINT_CLUSTERED, "MINT", new PrintStream(mintVenn));
		MitabUtils.toVennListFormat(MITAB_DIP_COMPLETED, "DIP", new PrintStream(dipVenn));
		
		//pubmedCollections();
		//interactionStats();
		
		//interactionStats(intact1, mint, dip);
		//describeMitab(buildAllFiles());
		//describeMitabOneLine(BinaryInteraction.MITAB_DIP_COMPLETED);
		//System.out.println( describeMitab(BinaryInteraction.MITAB_GLOBAL) );
	}
	
	
	/*
	 * Beware: these inner classes are a copy of psimitab-parser FileMerger.
	 * I have added a overrride for the toString method for TwoInteractor.
	 * TODO something cleaner!!
	 */
	
	
	
	
    /////////////////////
    // Inner classes

    /**
     * The simple interactor comparison is based on the comparison of their identifier coupled with taxonomy id.
     */
    protected static class SimpleInteractor {

        Collection<CrossReference> identifiers;
        Integer taxid;

        public SimpleInteractor( Interactor interactor ) {
            if ( interactor == null ) {
                throw new IllegalArgumentException( "Interactor should not be null." );
            }
            this.identifiers = interactor.getIdentifiers();
            if ( interactor.hasOrganism() ) {
                for ( CrossReference cr : interactor.getOrganism().getIdentifiers() ) {
                    if ( cr.getDatabase().equals( "taxid" ) ) {
                        taxid = new Integer( cr.getIdentifier() );
                    }
                }
            }
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }

            SimpleInteractor that = ( SimpleInteractor ) o;
            
            if ( taxid != null ? !taxid.equals( that.taxid ) : that.taxid != null ) {
                return false;
            }
            // Equals if at least one of the identifier is the same
            if ( PsiCollectionUtils.intersection( identifiers, that.identifiers ).isEmpty() ) {
                return false;
            }
            

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            result = identifiers.hashCode();
            result = 31 * result + ( taxid != null ? taxid.hashCode() : 0 );
            return result;
        }
        
        @Override
        public String toString() {
        	return identifiers.toString()+SEP+taxid;
        }
    }

    /**
     * Simple object modeling a couple of SimpleInteractor and giving comparison feature.
     */
    protected static class TwoInteractor {
        private SimpleInteractor interactorA;
        private SimpleInteractor interactorB;

        public TwoInteractor( SimpleInteractor interactorA, SimpleInteractor interactorB ) {
            if ( interactorA == null ) {
                throw new IllegalArgumentException();
            }
            if ( interactorB == null ) {
                throw new IllegalArgumentException();
            }
            this.interactorA = interactorA;
            this.interactorB = interactorB;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }

            TwoInteractor that = ( TwoInteractor ) o;

            // A = A' and B = B'     OR     A = B' and B = A'
            return ( ( interactorA.equals( that.interactorA ) && interactorB.equals( that.interactorB ) )
                     ||
                     ( interactorA.equals( that.interactorB ) && interactorB.equals( that.interactorA ) ) );
        }

        @SuppressWarnings({ "unchecked", "unchecked" })
		@Override
        public int hashCode() {
        	// new implementation of the intersection in PsiCollectionUtils is faster
        	//Collection<CrossReference> inter = CollectionUtils.intersection(interactorA.identifiers, interactorB.identifiers);
        	Collection<CrossReference> inter = PsiCollectionUtils.intersection(interactorA.identifiers, interactorB.identifiers);
        	int res = 1;
            for (CrossReference reference : inter) {
				res = 31*res*reference.hashCode();
			}
            res*=( interactorA.taxid != null ? interactorA.taxid.hashCode(): 1 );
            res*=( interactorB.taxid != null ? interactorB.taxid.hashCode(): 1 );
            
        	return res;
        	//return 31 * interactorA.hashCode() * interactorB.hashCode();
        }
        
        @Override
        public String toString() {
        	return interactorA.toString()+SEP+interactorB.toString();
        }
    }

}
