/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

/**
 * That class reflects what is needed to create a specific IntAct <code>Annotation</code> related to the interaction
 * confidence.
 * <p/>
 * <pre>
 * <p/>
 *     &lt;confidence unit="arbitrary" value="high"/&gt;
 * <p/>
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ConfidenceTag {

    private final String unit;
    private final String value;


    ////////////////////////////////
    // Constructor

    public ConfidenceTag( String unit, String value ) {

        if ( unit == null ) {
            throw new IllegalArgumentException( "You must give a non null unit for an interaction's confidence" );
        }

        if ( value == null ) {
            throw new IllegalArgumentException( "You must give a non null value for an interaction's confidence" );
        }

        this.unit = unit;
        this.value = value;
    }


    /////////////////////////////////
    // Getters

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }


    //////////////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof ConfidenceTag ) ) {
            return false;
        }

        final ConfidenceTag confidenceTag = (ConfidenceTag) o;

        if ( !unit.equals( confidenceTag.unit ) ) {
            return false;
        }
        if ( !value.equals( confidenceTag.value ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = unit.hashCode();
        result = 29 * result + value.hashCode();
        return result;
    }


    /////////////////////////////////////////
    // toString

    public String toString() {
        return "ConfidenceTag{" +
               "unit='" + unit + "'" +
               ", value='" + value + "'" +
               "}";
    }
}
