/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

/**
 * That class represents an interval on a protein sequence.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class LocationIntervalTag {

    public static final long NOT_SPECIFIED = 0;

    ////////////////////////
    // Instance variables

    private long start = NOT_SPECIFIED; // must be 1..n
    private long end = NOT_SPECIFIED;   // must be >= start, if == 0, means not specified (ie, start is a position.)


    ////////////////////
    // Constructors

    public LocationIntervalTag( long start, long end ) {

        if ( start < 1 && start != NOT_SPECIFIED ) {
            throw new IllegalArgumentException( "start (" + start + ") must be greater than 1 or unspecified (ie. 0)." );
        }

        if ( end < 1 && end != NOT_SPECIFIED ) {
            throw new IllegalArgumentException( "end (" + end + ") must be greater than 1 or unspecified (ie. 0)." );
        }

        if ( start > end ) {
            throw new IllegalArgumentException( "start (" + start + ") must be <= end (" + end + ")." );
        }

        this.end = end;
        this.start = start;
    }

    public LocationIntervalTag( long start ) {

        this( start, 0L );
    }


    ////////////////////
    // Getters

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }


    /////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof LocationIntervalTag ) ) {
            return false;
        }

        final LocationIntervalTag locationIntervalTag = (LocationIntervalTag) o;

        if ( end != locationIntervalTag.end ) {
            return false;
        }
        if ( start != locationIntervalTag.start ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) ( start ^ ( start >>> 32 ) );
        result = 29 * result + (int) ( end ^ ( end >>> 32 ) );
        return result;
    }


    ///////////////////////
    // Display

    public String toString() {
        return "LocationIntervalTag{" +
               "start=" + start +
               ", end=" + end +
               "}";
    }
}
