/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv.model;

import java.util.*;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28-Sep-2005</pre>
 */
public class CvTerm {

    ///////////////////////
    // Instance variables

    private String id;

    private String shortName;
    private String fullName;

    private String definition;
    private Collection xrefs = new HashSet( 2 ); // <CvTermXref>

    private Collection synonyms = new HashSet( 2 ); // <String>
    private Collection parents = new HashSet( 2 );  // <CvTerm>
    private Collection children = new HashSet( 2 ); // <CvTerm>

    private boolean obsolete = false;
    private String obsoleteMessage;

    private Collection annotations = new ArrayList( 2 ); // <CvTermAnnotation>

    /////////////////////////////
    // Constructor

    private CvTerm() {
    } // not allowed

    public CvTerm( String id, String shortname ) {
        setId( id );
        setShortName( shortname );
    }

    //////////////////////
    // Getters

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDefinition() {
        return definition;
    }

    public Collection getXrefs() {
        return xrefs;
    }

    public Collection getSynonyms() {
        return synonyms;
    }

    public Collection getParents() {
        return parents;
    }

    public Collection getChildren() {
        return children;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public String getObsoleteMessage() {
        return obsoleteMessage;
    }

    public Collection getAnnotations() {
        return annotations;
    }

    ///////////////////
    // Setters

    public void setId( String id ) {
        if ( id == null ) {
            throw new IllegalArgumentException( "ID can't be null" );
        }
        this.id = id;
    }

    public void setShortName( String shortName ) {
        if ( shortName == null ) {
            throw new IllegalArgumentException( "shortname can't be null (id: " + id + ")" );
        }
        this.shortName = shortName.trim();
    }

    public void setFullName( String fullName ) {
        if ( fullName != null ) {
            fullName = fullName.trim();
        }
        this.fullName = fullName;
    }

    public void setDefinition( String definition ) {
        this.definition = definition;
    }

    public void addXref( CvTermXref xref ) {
        xrefs.add( xref );
    }

    public void addSynonym( CvTermSynonym synonym ) {
        if ( synonym == null ) {
            throw new IllegalArgumentException( "You must give a non null synonym." );
        }
        synonyms.add( synonym );
    }

    public void addParent( CvTerm parent ) {
        parents.add( parent );
    }

    public void addChild( CvTerm child ) {
        children.add( child );
    }

    public void addAnnotation( CvTermAnnotation annotation ) {
        if ( annotation == null ) {
            throw new IllegalArgumentException( "You must give a non null annotation." );
        }
        annotations.add( annotation );
    }

    public void setObsolete( boolean obsolete ) {
        this.obsolete = obsolete;
    }

    public void setObsoleteMessage( String obsoleteMessage ) {
        this.obsoleteMessage = obsoleteMessage;
    }

    ////////////////////////////
    // DAG Utility

    /**
     * @param term
     * @param children
     */
    private void getAllChildren( CvTerm term, Collection children ) {

        children.add( term );

        for ( Iterator iterator = term.getChildren().iterator(); iterator.hasNext(); ) {
            CvTerm child = (CvTerm) iterator.next();
            getAllChildren( child, children );
        }
    }

    public Set getAllChildren() {
        Set children = new HashSet();
        getAllChildren( this, children );
        return children;
    }

    /////////////////////////
    // Object

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final CvTerm cvTerm = (CvTerm) o;

        if ( annotations != null ? !annotations.equals( cvTerm.annotations ) : cvTerm.annotations != null ) {
            return false;
        }
        if ( definition != null ? !definition.equals( cvTerm.definition ) : cvTerm.definition != null ) {
            return false;
        }
        if ( fullName != null ? !fullName.equals( cvTerm.fullName ) : cvTerm.fullName != null ) {
            return false;
        }
        if ( !id.equals( cvTerm.id ) ) {
            return false;
        }
        if ( !shortName.equals( cvTerm.shortName ) ) {
            return false;
        }
        if ( synonyms != null ? !synonyms.equals( cvTerm.synonyms ) : cvTerm.synonyms != null ) {
            return false;
        }
        if ( xrefs != null ? !xrefs.equals( cvTerm.xrefs ) : cvTerm.xrefs != null ) {
            return false;
        }

        return true;
    }

    public List diff( CvTerm cvTerm ) {
        if ( this == cvTerm ) {
            return Collections.EMPTY_LIST;
        }
        if ( cvTerm == null ) {
            throw new NullPointerException();
        }

        List diffs = new LinkedList();

        if ( annotations != null ? !annotations.equals( cvTerm.annotations ) : cvTerm.annotations != null ) {
            diffs.add( "annotations" );
        }
        if ( definition != null ? !definition.equals( cvTerm.definition ) : cvTerm.definition != null ) {
            diffs.add( "definition" );
        }
        if ( fullName != null ? !fullName.equals( cvTerm.fullName ) : cvTerm.fullName != null ) {
            diffs.add( "fullname" );
        }
        if ( !id.equals( cvTerm.id ) ) {
            diffs.add( "id" );
        }
        if ( !shortName.equals( cvTerm.shortName ) ) {
            diffs.add( "shortlabel" );
        }
        if ( synonyms != null ? !synonyms.equals( cvTerm.synonyms ) : cvTerm.synonyms != null ) {
            diffs.add( "synonyms" );
        }
        if ( xrefs != null ? !xrefs.equals( cvTerm.xrefs ) : cvTerm.xrefs != null ) {
            diffs.add( "xrefs" );
        }

        return diffs;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 29 * result + shortName.hashCode();
        result = 29 * result + ( fullName != null ? fullName.hashCode() : 0 );
        result = 29 * result + ( definition != null ? definition.hashCode() : 0 );
        result = 29 * result + ( xrefs != null ? xrefs.hashCode() : 0 );
        result = 29 * result + ( synonyms != null ? synonyms.hashCode() : 0 );
        result = 29 * result + ( annotations != null ? annotations.hashCode() : 0 );
        return result;
    }

    public String toString() {

        String NEW_LINE = System.getProperty( "line.separator" );

        final StringBuffer sb = new StringBuffer( 128 );
        sb.append( "CvTerm" );
        sb.append( "{id='" ).append( id ).append( '\'' );
        sb.append( ", shortName='" ).append( shortName ).append( '\'' );
        sb.append( ", fullName='" ).append( fullName ).append( '\'' ).append( NEW_LINE );

        sb.append( "       definition='" ).append( definition ).append( '\'' ).append( NEW_LINE );

        sb.append( "xrefs=" ).append( NEW_LINE );
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            CvTermXref xref = (CvTermXref) iterator.next();
            sb.append( "\t" ).append( xref ).append( NEW_LINE );

        }

        sb.append( "synonyms=" ).append( NEW_LINE );
        for ( Iterator iterator = synonyms.iterator(); iterator.hasNext(); ) {
            CvTermSynonym syn = (CvTermSynonym) iterator.next();
            sb.append( "\t" ).append( syn ).append( NEW_LINE );
        }

        sb.append( "annotations=" ).append( NEW_LINE );
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            CvTermAnnotation annot = (CvTermAnnotation) iterator.next();
            sb.append( "\t" ).append( annot ).append( NEW_LINE );
        }

        sb.append( "parents=" );
        for ( Iterator iterator = parents.iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            sb.append( "\t" ).append( cvTerm.getShortName() ).append( "(" ).append( cvTerm.getId() ).append( ")" ).append( NEW_LINE );
        }

        sb.append( "children=" );
        for ( Iterator iterator = children.iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            sb.append( "\t" ).append( cvTerm.getShortName() ).append( "(" ).append( cvTerm.getId() ).append( ")" ).append( NEW_LINE );
        }

        sb.append( ", obsolete=" ).append( obsolete );
        sb.append( ", obsoleteMessage='" ).append( obsoleteMessage ).append( '\'' );
        sb.append( '}' );

        return sb.toString();
    }
}