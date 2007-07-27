/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;


/**
 * That class reflects what is needed to create an IntAct <code>Xref</code>.
 * <p/>
 * <pre>
 *     &lt;primaryRef db="pubmed" id="11805826" secondary="" version=""/&gt;
 * <p/>
 *        - OR -
 * <p/>
 *     &lt;secondaryRef db="pubmed" id="11805826" secondary="" version=""/&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Xref
 */
public final class XrefTag {

    public static final short PRIMARY_REF = 0;
    public static final short SECONDARY_REF = 1;

    private final short type; /* MANDATORY - either PRIMARY_REF or SECONDARY_REF */
    private final String db;  /* MANDATORY - non null and non empty */
    private final String id;  /* MANDATORY - non null and non empty */
    private String secondary = null;
    private String version = null;

    ///////////////////////
    // Constructors

    public XrefTag( final short type,
                    final String id,
                    final String db ) {

        if ( type != PRIMARY_REF && type != SECONDARY_REF ) {
            throw new IllegalArgumentException( "An xref must be either PRIMARY_REF or SECONDARY_REF" );
        }

        if ( id == null || id.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "You must give a non null/empty id for an xref" );
        }

        if ( db == null || db.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "You must give a non null/empty id for an xref" );
        }

        this.type = type;
        this.id = id;
        this.db = db;
    }

    public XrefTag( final short type,
                    final String id,
                    final String db,
                    final String secondary,
                    final String version ) {

        this( type, id, db );
        this.secondary = secondary;
        this.version = version;
    }


    ///////////////////////
    // Getters

    public boolean isPrimaryRef() {
        return type == PRIMARY_REF;
    }

    public boolean isSecondaryRef() {
        return type == SECONDARY_REF;
    }

    public String getDb() {
        return db;
    }

    public String getId() {
        return id;
    }

    public String getSecondary() {
        return secondary;
    }

    public String getVersion() {
        return version;
    }

    public short getType() {
        return type;
    }

    ////////////////////////
    // Equality
    
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof XrefTag ) ) {
            return false;
        }

        final XrefTag xrefTag = (XrefTag) o;

        if ( type != xrefTag.type ) {
            return false;
        }
        if ( !db.equals( xrefTag.db ) ) {
            return false;
        }
        if ( !id.equals( xrefTag.id ) ) {
            return false;
        }
        if ( secondary != null ? !secondary.equals( xrefTag.secondary ) : xrefTag.secondary != null ) {
            return false;
        }
        if ( version != null ? !version.equals( xrefTag.version ) : xrefTag.version != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) type;
        result = 29 * result + db.hashCode();
        result = 29 * result + id.hashCode();
        result = 29 * result + ( secondary != null ? secondary.hashCode() : 0 );
        result = 29 * result + ( version != null ? version.hashCode() : 0 );
        return result;
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "XrefTag" );
        buf.append( "{type=" ).append( ( type == PRIMARY_REF ? "primaryRef" : "secondaryRef" ) );
        buf.append( ",db=" ).append( db );
        buf.append( ",id=" ).append( id );
        buf.append( ",secondary=" ).append( secondary );
        buf.append( ",version=" ).append( version );
        buf.append( '}' );
        return buf.toString();
    }
}
