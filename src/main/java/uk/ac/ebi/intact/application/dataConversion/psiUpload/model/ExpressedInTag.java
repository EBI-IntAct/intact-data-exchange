/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ExpressedInTag {

    private final String proteinInteractorID;
    private final String bioSourceShortlabel;

    public ExpressedInTag( final AnnotationTag annotation ) {

        if ( !Constants.EXPRESSED_IN.equalsIgnoreCase( annotation.getType() ) ) {
            throw new IllegalArgumentException( "You can only create an ExpressedInTag out of an AnnotationTag having " +
                                                Constants.EXPRESSED_IN + " as type." );
        }

        String text = annotation.getText();

        int index = text.indexOf( ':' );
        String id = null;
        String shortlabel = null;
        if ( index == -1 ) {
            throw new IllegalArgumentException( "You can only create an ExpressedInTag out of an AnnotationTag having its text formatted as follow:" +
                                                "<proteinInteractorID>:<biosourceShortlabel>" );
        } else {
            id = text.substring( 0, index );

            if ( id == null || "".equals( id ) ) {
                throw new IllegalArgumentException( "You can only create an ExpressedInTag out of an AnnotationTag having its text formatted as follow:" +
                                                    "<proteinInteractorID>:<biosourceShortlabel>. the id is missing." );
            }

            shortlabel = text.substring( index + 1, text.length() );

            if ( shortlabel == null || "".equals( shortlabel ) ) {
                throw new IllegalArgumentException( "You can only create an ExpressedInTag out of an AnnotationTag having its text formatted as follow:" +
                                                    "<proteinInteractorID>:<biosourceShortlabel>. the shortlabel is missing" );
            }
        }

        this.proteinInteractorID = id;
        this.bioSourceShortlabel = shortlabel;
    }


    //////////////////////////////
    // Getters

    public String getBioSourceShortlabel() {
        return bioSourceShortlabel;
    }

    public String getProteinInteractorID() {
        return proteinInteractorID;
    }


    //////////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof ExpressedInTag ) ) {
            return false;
        }

        final ExpressedInTag expressedInTag = (ExpressedInTag) o;

        if ( !bioSourceShortlabel.equals( expressedInTag.bioSourceShortlabel ) ) {
            return false;
        }
        if ( !proteinInteractorID.equals( expressedInTag.proteinInteractorID ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = proteinInteractorID.hashCode();
        result = 29 * result + bioSourceShortlabel.hashCode();
        return result;
    }


    public String toString() {
        return "ExpressedInTag{" +
               "bioSourceShortlabel='" + bioSourceShortlabel + "'" +
               ", proteinInteractorID='" + proteinInteractorID + "'" +
               "}";
    }
}
