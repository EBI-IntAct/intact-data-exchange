/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dataexchange.imex.idassigner.id;

import java.sql.Date;
import java.util.Iterator;

/**
 * Models the return type of a call to the Key Assigner.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14-May-2006</pre>
 */
public class IMExRange {

    /**
     * When was the IMEx range created.
     */
    private Date timestamp = null;

    /**
     * Submission ID in the Key Assigner.
     */
    private long submissionId = -1;

    /**
     * Lower bound of the IMEx Range.
     */
    private long from;

    /**
     * Upper (inclusive) bound of the IMEx Range.
     */
    private long to;

    /**
     * Partner who requested that range.
     */
    private String partner = null;

    //////////////////////////////////////////
    // Constructor

    public IMExRange( long submissionId, long from, long to, String partner ) {

        this( from, to );

        this.submissionId = submissionId;
        this.partner = partner;

        // set time to now...
        timestamp = new Date( System.currentTimeMillis() );
    }

    public IMExRange( long from, long to ) {

        if ( from > to ) {
            throw new IllegalArgumentException( "[from=" + from + ", to=" + to + "] Lower bound cannot be greater than upper." );
        }

        this.from = from;
        this.to = to;
    }

    /////////////////////////////////////////
    // Getters

    public Date getTimestamp() {
        return timestamp;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public String getPartner() {
        return partner;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public Iterator<Long> iterator() {

        return new IMExRangeIterator( this );
    }

    ///////////////////////////////////
    // Object's override

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final IMExRange imExRange = (IMExRange) o;

        if ( from != imExRange.from ) {
            return false;
        }
        if ( submissionId != imExRange.submissionId ) {
            return false;
        }
        if ( to != imExRange.to ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) ( submissionId ^ ( submissionId >>> 32 ) );
        result = 29 * result + (int) ( from ^ ( from >>> 32 ) );
        result = 29 * result + (int) ( to ^ ( to >>> 32 ) );
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "IMExRange" );
        sb.append( "{submissionId=" ).append( submissionId );
        sb.append( ", from=" ).append( from );
        sb.append( ", to=" ).append( to );
        sb.append( ", partner='" ).append( partner ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}