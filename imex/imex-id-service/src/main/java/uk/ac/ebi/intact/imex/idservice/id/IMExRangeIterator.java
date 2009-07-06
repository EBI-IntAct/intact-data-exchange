/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idservice.id;

import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-May-2006</pre>
 */
public class IMExRangeIterator implements Iterator<Long> {

    private long from;
    private long to;

    private long current;

    public IMExRangeIterator( IMExRange range ) {

        this( range.getFrom(), range.getTo() );
    }

    public IMExRangeIterator( long from, long to ) {

        if ( from > to ) {
            throw new IllegalArgumentException( "lower bound greater than upper bound (from=" + from + ", to=" + to + ")" );
        }

        this.from = from;
        this.to = to;

        current = from;
    }

    ////////////////////////////
    // Iterator

    public boolean hasNext() {
        return current <= to;
    }

    public Long next() {
        return current++;
    }

    public void remove() {
    }
}