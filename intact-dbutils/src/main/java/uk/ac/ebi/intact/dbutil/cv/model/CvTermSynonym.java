/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv.model;

/**
 * Describe a CvTerm Synonym.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Sep-2005</pre>
 */
public class CvTermSynonym {

    ///////////////////////
    // Instance variables

    /**
     * Type of synonym.
     */
    private String type;

    /**
     * Name of the synonym.
     */
    private String name;

    /////////////////////////
    // Constructors

    /**
     * Constructs a CvTermSynonym.
     *
     * @param name the name
     */
    public CvTermSynonym( String name ) {
        this( null, name );
    }

    /**
     * Constructs a CvTermSynonym.
     *
     * @param name the name
     * @param type the type
     */
    public CvTermSynonym( String type, String name ) {
        setName( name );
        setType( type );
    }

    ///////////////////////////
    // Getters & Setters

    /**
     * Returns a synonym type.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Set a Type.
     *
     * @param type the type
     */
    public void setType( String type ) {
        this.type = type;
    }

    public boolean hasType() {
        return type != null;
    }

    /**
     * Returns the name of the synonym.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the synonym.
     *
     * @param name
     */
    public void setName( String name ) {
        if ( name == null ) {
            throw new IllegalArgumentException( "Please give a non null name for a Synonym." );
        }

        this.name = name;
    }

    ///////////////////////
    // Object

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "CvTermSynonym{" );

        if ( type != null ) {
            sb.append( "type='" ).append( type ).append( '\'' ).append( ", " );
        }

        sb.append( "name='" ).append( name ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            System.out.println( "CvTermSynonym.equals: types are different" );
            return false;
        }

        final CvTermSynonym that = (CvTermSynonym) o;

        if ( !name.equals( that.name ) ) {
            System.out.println( "CvTermSynonym.equals: names are different" );
            return false;
        }
        if ( type != null ? !type.equals( that.type ) : that.type != null ) {
            System.out.println( "CvTermSynonym.equals: types are different" );
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( type != null ? type.hashCode() : 0 );
        result = 29 * result + name.hashCode();
        return result;
    }
}