/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ReadOnlyHashMap implements Map {

    private Map map;

    public ReadOnlyHashMap( Map map ) {
        if ( map == null ) {
            throw new IllegalArgumentException( "You must give a non null Map." );
        }
        this.map = map;
    }

    public boolean containsKey( Object key ) {
        return map.containsKey( key );
    }

    public boolean containsValue( Object value ) {
        return map.containsValue( value );
    }

    public Object get( Object key ) {
        return map.get( key );
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public Set keySet() {
        return new ReadOnlySet( map.keySet() );
    }

    public Set entrySet() {
        return new ReadOnlySet( map.entrySet() );
    }

    public Collection values() {
        return new ReadOnlyCollection( map.values() );
    }


    ///////////////////////////////
    // NON SUPPORTED METHODS

    public void clear() {
        throw new UnsupportedOperationException( "That Map is for read only use." );
    }

    public Object put( Object key, Object value ) {
        throw new UnsupportedOperationException( "That Map is for read only use." );
    }

    public void putAll( Map t ) {
        throw new UnsupportedOperationException( "That Map is for read only use." );
    }

    public Object remove( Object key ) {
        throw new UnsupportedOperationException( "That Map is for read only use." );
    }
}
