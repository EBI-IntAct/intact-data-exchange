/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ReadOnlySet implements Set {

    private Set set;

    public ReadOnlySet( Set set ) {
        this.set = set;
    }

    public boolean contains( Object obj ) {
        return set.contains( obj );
    }

    public boolean containsAll( Collection collection ) {
        return set.containsAll( collection );
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public Iterator iterator() {
        return new ReadOnlyIterator( set.iterator() );
    }

    public int size() {
        return set.size();
    }

    public Object[] toArray() {
        return set.toArray();
    }

    public Object[] toArray( Object obj[] ) {
        return set.toArray( obj );
    }


    //////////////////////////////
    // NOT SUPPORTED METHOD

    public boolean add( Object obj ) {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }

    public boolean addAll( Collection collection ) {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }

    public void clear() {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }

    public boolean remove( Object obj ) {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }

    public boolean removeAll( Collection collection ) {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }

    public boolean retainAll( Collection collection ) {
        throw new UnsupportedOperationException( "That Set is for read only use." );
    }
}
