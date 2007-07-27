/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * That class hold a collection and only allow its user to read it's content.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ReadOnlyCollection implements Collection {

    private Collection collection;

    public ReadOnlyCollection( Collection collection ) {
        if ( collection == null ) {
            throw new IllegalArgumentException( "You must give a non null Collection." );
        }
        this.collection = collection;
    }

    public boolean contains( Object o ) {
        return collection.contains( o );
    }

    public boolean containsAll( Collection c ) {
        return collection.containsAll( c );
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public Iterator iterator() {
        return new ReadOnlyIterator( collection.iterator() );
    }

    public int size() {
        return collection.size();
    }

    public Object[] toArray() {
        return collection.toArray();
    }

    public Object[] toArray( Object a[] ) {
        return collection.toArray( a );
    }

    //////////////////////////////
    // NON SUPPORTED METHOD

    public boolean add( Object o ) {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }

    public boolean addAll( Collection c ) {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }

    public void clear() {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }

    public boolean remove( Object o ) {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }

    public boolean removeAll( Collection c ) {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }

    public boolean retainAll( Collection c ) {
        throw new UnsupportedOperationException( "That Collection is for read only use." );
    }
}
