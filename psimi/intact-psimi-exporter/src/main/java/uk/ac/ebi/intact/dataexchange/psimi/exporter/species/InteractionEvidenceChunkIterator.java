package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import psidev.psi.mi.jami.model.InteractionEvidence;

import java.util.Iterator;

/**
 * A species file unit contain a list of positive and negative interactions to filter and merge
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
