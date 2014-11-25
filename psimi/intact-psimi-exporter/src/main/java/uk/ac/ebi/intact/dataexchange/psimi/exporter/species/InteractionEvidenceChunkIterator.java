package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import psidev.psi.mi.jami.model.InteractionEvidence;

import java.util.Iterator;

/**
 * Iterator of interaction evidences which wraps another interaction evidence iterator.
 *
 * If the largescale property of this iterator is not null, it will stop when it has reached this number of interaction evidences.
 *
 * The method hasNextChunk allows to know if this iterator sops because it reached the maximum number of interactions or if the delegate iterator does not
 * have any more interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/09/11</pre>
 */

public class InteractionEvidenceChunkIterator implements Iterator<InteractionEvidence>{

    private Integer largeScale;
    private Iterator<InteractionEvidence> delegateIterator;
    private int currentNumberInteractions = 0;

    public InteractionEvidenceChunkIterator(Integer largeScale, Iterator<InteractionEvidence> delegateIterator){
        if (delegateIterator == null){
            throw new IllegalArgumentException("The iterator of interaction evidences cannot be null");
        }
        this.delegateIterator =delegateIterator;
        this.largeScale = largeScale;
    }

    @Override
    public boolean hasNext() {
        return (largeScale != null && largeScale < currentNumberInteractions && delegateIterator.hasNext())
                || (largeScale == null && delegateIterator.hasNext());
    }

    @Override
    public InteractionEvidence next() {
        if (!hasNext()){
            throw new UnsupportedOperationException("This iterator does not have anymore interactions to iterate through");
        }
        this.currentNumberInteractions++;
        return this.delegateIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }


    public boolean hasNextChunk(){
        return this.delegateIterator.hasNext();
    }
}
