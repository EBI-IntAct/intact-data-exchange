/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import uk.ac.ebi.intact.model.CvObject;

/**
 * Class describing the source of the association between a CV class and a node Name.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27-Jun-2005</pre>
 */
public class Cv2Source {
    private CvObject cvObject;
    private String parentNodeName;

    public Cv2Source( CvObject cvObject, String parentNodeName ) {

        if ( cvObject == null ) {
            throw new IllegalArgumentException( "You must give a non null CvObject" );
        }

        this.cvObject = cvObject;
        this.parentNodeName = parentNodeName;
    }

    public Cv2Source( CvObject cvObject ) {
        this.cvObject = cvObject;
        this.parentNodeName = null;
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof Cv2Source ) ) {
            return false;
        }

        final Cv2Source cvClass2Source = (Cv2Source) o;

        if ( !cvObject.equals( cvClass2Source.cvObject ) ) {
            return false;
        }
        if ( parentNodeName != null ? !parentNodeName.equals( cvClass2Source.parentNodeName ) : cvClass2Source.parentNodeName != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = cvObject.hashCode();
        result = 29 * result + ( parentNodeName != null ? parentNodeName.hashCode() : 0 );
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append( "Cv2Source" );
        sb.append( "{cvObject=" ).append( cvObject.getShortLabel() );
        sb.append( " Type=" ).append( cvObject.getClass() );
        sb.append( ", parentNodeName='" ).append( parentNodeName ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
