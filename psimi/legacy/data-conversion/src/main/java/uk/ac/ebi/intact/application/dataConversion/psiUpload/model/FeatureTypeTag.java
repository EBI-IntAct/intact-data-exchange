/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureTypeTag {
    private final XrefTag psiDefinition;

    ///////////////////////
    // Constructors

    public FeatureTypeTag( final XrefTag psiDefinition ) {

        if ( psiDefinition == null ) {
            throw new IllegalArgumentException( "You must give a non null psi definition for an feature type" );
        }

        if ( !CvDatabase.PSI_MI.equals( psiDefinition.getDb() ) ) {
            throw new IllegalArgumentException( "You must give a psi-mi Xref, not " + psiDefinition.getDb() +
                                                " for a FeatureType" );
        }

        this.psiDefinition = psiDefinition;
    }


    ////////////////////////////
    // Getters

    public XrefTag getPsiDefinition() {
        return psiDefinition;
    }


    ////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof FeatureTypeTag ) ) {
            return false;
        }

        final FeatureTypeTag featureTypeTag = (FeatureTypeTag) o;

        if ( psiDefinition != null ? !psiDefinition.equals( featureTypeTag.psiDefinition ) : featureTypeTag.psiDefinition != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return ( psiDefinition != null ? psiDefinition.hashCode() : 0 );
    }

    /////////////////////////
    // Display

    public String toString() {
        return "FeatureTypeTag{psiDefinition=" + psiDefinition + "}";
    }
}
