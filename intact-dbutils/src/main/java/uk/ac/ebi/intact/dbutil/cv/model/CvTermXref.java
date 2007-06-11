/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv.model;

/**
 * Represents the Xref of a CvTerm.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see CvTerm
 * @since <pre>28-Sep-2005</pre>
 */
public class CvTermXref {

    //////////////////////////
    // Instance variables

    private String id;
    private String database;
    private String qualifier;

    //////////////////////////
    // Constructors

    /**
     * Constructs a CvTermXref.
     *
     * @param id        xref's id (must be non null)
     * @param database  xref's database (must be non null)
     * @param qualifier xref's qualifier
     */
    public CvTermXref( String id, String database, String qualifier ) {
        setId( id );
        setDatabase( database );
        setQualifier( qualifier );
    }

    /**
     * Constructs a CvTermXref.
     *
     * @param id       xref's id (must be non null)
     * @param database xref's database (must be non null)
     */
    public CvTermXref( String id, String database ) {
        this( id, database, null );
    }

    ///////////////////////////
    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "You must give a non null id." );
        }
        this.id = id.trim();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase( String database ) {
        if ( database == null ) {
            throw new IllegalArgumentException( "You must give a non null database." );
        }
        this.database = database.trim();
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier( String qualifier ) {
        this.qualifier = qualifier;
    }

    ////////////////////////
    // Object

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final CvTermXref that = (CvTermXref) o;

        if ( !database.equals( that.database ) ) {
            return false;
        }
        if ( !id.equals( that.id ) ) {
            return false;
        }
        if ( qualifier != null ? !qualifier.equals( that.qualifier ) : that.qualifier != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 29 * result + database.hashCode();
        result = 29 * result + ( qualifier != null ? qualifier.hashCode() : 0 );
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "CvTermXref" );
        sb.append( "{id='" ).append( id ).append( '\'' );
        sb.append( ", database='" ).append( database ).append( '\'' );
        sb.append( ", qualifier='" ).append( qualifier ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}