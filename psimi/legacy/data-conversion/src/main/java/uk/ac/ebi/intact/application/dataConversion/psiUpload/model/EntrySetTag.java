/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;

import java.util.Collection;
import java.util.Iterator;

/**
 * That class reflects what is needed to create a set of IntAct <code>Experiment</code>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Experiment
 * @see uk.ac.ebi.intact.model.Interaction
 * @see uk.ac.ebi.intact.model.Protein
 */
public final class EntrySetTag {

    private static final String NEW_LINE = System.getProperty( "line.separator" );

    /**
     * Collection of EntryTag. Collection of EntryTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag
     */
    final Collection entries;

    ////////////////////////
    // Constructors

    public EntrySetTag( final Collection entries ) {

        if ( entries == null ) {
            throw new IllegalArgumentException( "You must give a non null Collection of entry for an EntryList" );
        }

        if ( entries.size() == 0 ) {
            throw new IllegalArgumentException( "You must give a non empty Collection of entry for an EntryList" );
        }

        // check the collection content
        for ( Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
            Object o = (Object) iterator.next();
            if ( !( o instanceof EntryTag ) ) {
                final String name = ( o == null ? "null" : o.getClass().getName() );
                throw new IllegalArgumentException( "The entry collection added to the EntrySet doesn't " +
                                                    "contains only EntryTag (eg. " + name + ")." );
            }
        }

        this.entries = new ReadOnlyCollection( entries );
    }


    ////////////////////////
    // Getter

    public Collection getEntries() {
        return entries;
    }


    ////////////////////////
    // Equality

    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof EntrySetTag ) ) {
            return false;
        }

        final EntrySetTag entrySetTag = (EntrySetTag) o;

        if ( !entries.equals( entrySetTag.entries ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return entries.hashCode();
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "EntrySetTag(" ).append( entries.size() ).append( ") {" );
        int i = 0;
        for ( Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
            EntryTag entryTag = (EntryTag) iterator.next();
            buf.append( "entry " ).append( i ).append( ':' ).append( entryTag ).append( NEW_LINE );
        }
        buf.append( '}' );
        return buf.toString();
    }
}
