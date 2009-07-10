/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

/**
 * That class reflects what is needed to create an IntAct <code>Range</code>.
 * <p/>
 * <pre>
 *      Examples:
 * <p/>
 *      &lt;location&gt;
 *          &lt;begin position="2"/&gt;
 *          &lt;endInterval begin="10" end="13"/&gt;
 *      &lt;/location&gt;
 * <p/>
 *      &lt;location &gt;
 *          &lt;beginInterval begin="10" end="13/&gt;
 *          &lt;end position="2"/&gt;
 *      &lt;/location&gt;
 * <p/>
 *      &lt;location &gt;
 *          &lt;position position="7"/&gt;
 *      &lt;/location&gt;
 * <p/>
 *      &lt;location &gt;
 *          &lt;site position="122"/&gt;
 *      &lt;/location&gt;
 * <p/>
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Range
 * @see uk.ac.ebi.intact.model.Feature
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag
 */
public class LocationTag {

    ////////////////////////////
    // Instance variables

    private long fromIntervalStart = 0;
    private long fromIntervalEnd = 0;

    private long toIntervalStart = 0;
    private long toIntervalEnd = 0;


    ///////////////////////////
    // Constructors

    public LocationTag( LocationIntervalTag from, LocationIntervalTag to ) {

        if ( from == null ) {
            throw new IllegalArgumentException( "From interval must not be null." );
        }

        if ( to == null ) {
            throw new IllegalArgumentException( "To interval must not be null." );
        }

        // ............***.... From
        // .....***........... To
        if ( from.getStart() > to.getEnd() ) {
            throw new IllegalArgumentException( "From lower bound (" + from.getStart() +
                                                ") greater than the To upper bound (" +
                                                to.getEnd() + ")." );
        }

        this.fromIntervalStart = from.getStart();
        this.fromIntervalEnd = from.getEnd();

        this.toIntervalStart = to.getStart();
        this.toIntervalEnd = to.getEnd();
    }


    ///////////////////////////
    // Getters

    public long getFromIntervalStart() {
        return fromIntervalStart;
    }

    public long getFromIntervalEnd() {
        return fromIntervalEnd;
    }

    public long getToIntervalStart() {
        return toIntervalStart;
    }

    public long getToIntervalEnd() {
        return toIntervalEnd;
    }


    ///////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof LocationTag ) ) {
            return false;
        }

        final LocationTag locationTag = (LocationTag) o;

        if ( fromIntervalEnd != locationTag.fromIntervalEnd ) {
            return false;
        }
        if ( fromIntervalStart != locationTag.fromIntervalStart ) {
            return false;
        }
        if ( toIntervalEnd != locationTag.toIntervalEnd ) {
            return false;
        }
        if ( toIntervalStart != locationTag.toIntervalStart ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) ( fromIntervalStart ^ ( fromIntervalStart >>> 32 ) );
        result = 29 * result + (int) ( fromIntervalEnd ^ ( fromIntervalEnd >>> 32 ) );
        result = 29 * result + (int) ( toIntervalStart ^ ( toIntervalStart >>> 32 ) );
        result = 29 * result + (int) ( toIntervalEnd ^ ( toIntervalEnd >>> 32 ) );
        return result;
    }


    /////////////////////////
    // Display

    public String toString() {
        return "LocationTag{" +
               "from=" + fromIntervalStart + ( fromIntervalEnd == 0 ? "" : ".." + fromIntervalEnd ) +
               ", to=" + toIntervalStart + ( toIntervalEnd == 0 ? "" : ".." + toIntervalEnd ) + "}";
    }
}
