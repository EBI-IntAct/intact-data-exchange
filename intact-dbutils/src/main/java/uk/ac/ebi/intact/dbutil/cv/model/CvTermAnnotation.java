/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv.model;

import java.util.regex.Pattern;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Sep-2005</pre>
 */
public class CvTermAnnotation {

    ////////////////////////
    // Instance variables

    private String topic;
    private String annotation;

    /////////////////////////
    // Constructors

    public CvTermAnnotation( String topic, String annotation ) {
        setTopic( topic );
        setAnnotation( annotation );

        if ( topic.equals( "id-validation-regexp" ) ) {
            Pattern.compile( annotation );
        }
    }

    //////////////////////////
    // Getters & Setters

    public String getTopic() {
        return topic;
    }

    public void setTopic( String topic ) {

        if ( topic == null ) {
            throw new IllegalArgumentException( "You must give a non null topic." );
        }

        topic = topic.trim();

        if ( "".equals( topic ) ) {
            throw new IllegalArgumentException( "You must give a non empty topic." );
        }

        this.topic = topic;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation( String annotation ) {
        if ( annotation != null ) {
            annotation = annotation.trim();
        }
        this.annotation = annotation;
    }

    //////////////////////////
    // Object

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final CvTermAnnotation that = (CvTermAnnotation) o;

        if ( annotation != null ? !annotation.equals( that.annotation ) : that.annotation != null ) {
            return false;
        }
        if ( !topic.equals( that.topic ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = topic.hashCode();
        result = 29 * result + ( annotation != null ? annotation.hashCode() : 0 );
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "CvTermAnnotation" );
        sb.append( "{topic='" ).append( topic ).append( '\'' );
        sb.append( ", annotation='" ).append( annotation ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}