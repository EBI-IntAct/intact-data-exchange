/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class reflects what is needed to create an IntAct <code>CvFeatureIdentification</code>.
 * <pre>
 *          &lt;featureDetection &gt;
 *              &lt;names &gt;
 *                  &lt;shortLabel &gt;western blot&lt;/shortLabel&gt;
 *              &lt;/names&gt;
 *              &lt;xref &gt;
 *                  &lt;primaryRef db="psi-mi" id="MI:0113"/&gt;
 *              &lt;/xref&gt;
 *          &lt;/featureDetection&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.CvFeatureIdentification
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag
 */
public class FeatureDetectionTag {

    private final XrefTag psiDefinition;

    ///////////////////////
    // Constructors

    public FeatureDetectionTag( final XrefTag psiDefinition ) {

        if ( psiDefinition == null ) {
            throw new IllegalArgumentException( "You must give a non null psi definition for an feature detection" );
        }

        if ( !CvDatabase.PSI_MI.equals( psiDefinition.getDb() ) ) {
            throw new IllegalArgumentException( "You must give a psi-mi Xref, not " + psiDefinition.getDb() +
                                                " for a FeatureDetection" );
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
        if ( !( o instanceof FeatureDetectionTag ) ) {
            return false;
        }

        final FeatureDetectionTag featureDetectionTag = (FeatureDetectionTag) o;

        if ( psiDefinition != null ? !psiDefinition.equals( featureDetectionTag.psiDefinition ) : featureDetectionTag.psiDefinition != null ) {
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
        return "FeatureDetectionTag{psiDefinition=" + psiDefinition + "}";
    }
}
