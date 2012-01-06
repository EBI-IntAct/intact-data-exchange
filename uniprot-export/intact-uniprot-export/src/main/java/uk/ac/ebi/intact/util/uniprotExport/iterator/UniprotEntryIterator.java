package uk.ac.ebi.intact.util.uniprotExport.iterator;

import com.google.common.collect.UnmodifiableIterator;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Iterator of a sorted list of interactor uniprot acs.
 * This iterator will return a uniprotEntry containing master uniprot ac, uniprot acs which are from the same uniprot entry (master uniprot plus isoforms and feature chains).
 * This is relying on the fact that isoforms and feature chains start with the master uniprot ac. If the isoform does not start with same uniprot ac,
 * it will be considered as coming from a different uniprot entry.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05/01/12</pre>
 */

public class UniprotEntryIterator extends UnmodifiableIterator<UniprotEntry> {
    
    private Iterator<String> positiveInteractorsIterator;
    private Iterator<String> negativeInteractorsIterator;
    
    private String lastPositiveInteractor;
    private String lastNegativeInteractor;

    public UniprotEntryIterator(SortedSet<String> positiveInteractors, SortedSet<String> negativeInteractors){

        if (positiveInteractors == null){
            positiveInteractorsIterator = new TreeSet<String>().iterator();
        }
        else {
            positiveInteractorsIterator = positiveInteractors.iterator();
        }

        if (negativeInteractors == null){
            negativeInteractorsIterator = new TreeSet<String>().iterator();
        }
        else {
            negativeInteractorsIterator = negativeInteractors.iterator();
        }
        
        if (positiveInteractorsIterator.hasNext()){
            lastPositiveInteractor = positiveInteractorsIterator.next();
        }
        else {
            lastPositiveInteractor = null;
        }

        if (negativeInteractorsIterator.hasNext()){
            lastNegativeInteractor = negativeInteractorsIterator.next();
        }
        else {
            lastNegativeInteractor = null;
        }
    }

    @Override
    public boolean hasNext() {
        return (lastPositiveInteractor != null || lastNegativeInteractor != null);
    }

    private UniprotEntry createNextElement(){
        // the master uniprot is by default the latest processed interactor which comes first
        String masterUniprot = null;

        // positive null, we take the negative
        if (lastPositiveInteractor == null){
            masterUniprot = lastNegativeInteractor;
        }
        // negative null, we take the positive
        else if (lastNegativeInteractor == null){
            masterUniprot = lastPositiveInteractor;
        }
        // both positive and negative are not null so we compare which one comes first
        else {
            int comparison = lastPositiveInteractor.compareTo(lastNegativeInteractor);

            // identical, we take the positive by default
            if (comparison == 0){
                masterUniprot = lastPositiveInteractor;
            }
            // the last positive interactor comes before the last negative one
            else if (comparison < 0){
                masterUniprot = lastPositiveInteractor;
            }
            // the last negative interactor comes before the last positive one
            else {
                masterUniprot = lastNegativeInteractor;
            }
        }

        // if the first interactor is isoform or feature chain, we need to extract the parent ac
        if (!UniprotExportUtils.isMasterProtein(masterUniprot)){
            masterUniprot = UniprotExportUtils.extractMasterProteinFrom(masterUniprot);
        }

        UniprotEntry uniprotEntry = new UniprotEntry(masterUniprot);

        // process positive interactors
        while (lastPositiveInteractor != null && lastPositiveInteractor.startsWith(masterUniprot)){
            // we add the processed interactor because matches the master uniprot ac
            if (!masterUniprot.equals(lastPositiveInteractor)){
                uniprotEntry.getPositiveInteractors().add(lastPositiveInteractor);
            }

            if (positiveInteractorsIterator.hasNext()){
                lastPositiveInteractor = positiveInteractorsIterator.next();
            }
            else {
                lastPositiveInteractor = null;
            }
        }

        // process negative interactors
        while (lastNegativeInteractor != null && lastNegativeInteractor.startsWith(masterUniprot)){
            // we add the processed interactor because matches the master uniprot ac
            if (!masterUniprot.equals(lastNegativeInteractor)){
                uniprotEntry.getNegativeInteractors().add(lastNegativeInteractor);
            }

            if (negativeInteractorsIterator.hasNext()){
                lastNegativeInteractor = negativeInteractorsIterator.next();
            }
            else {
                lastNegativeInteractor = null;
            }
        }

        return uniprotEntry;
    }

    @Override
    public UniprotEntry next() {
        
        if (lastPositiveInteractor == null){
            throw new NoSuchElementException("This Simple interactor iterator does not have any elements to process");
        }

        return createNextElement();
    }
}
