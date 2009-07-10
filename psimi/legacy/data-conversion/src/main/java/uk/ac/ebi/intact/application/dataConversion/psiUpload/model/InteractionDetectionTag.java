/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class reflects what is needed to create an IntAct <code>CvInteraction</code>.
 * <p/>
 * <pre>
 *      &lt;interactionDetection&gt;
 *          &lt;names&gt;
 *              &lt;shortLabel&gt;tandem affinity puri&lt;/shortLabel&gt;
 *              &lt;fullName&gt;tandem affinity purification&lt;/fullName&gt;
 *          &lt;/names&gt;
 *          &lt;xref&gt;
 *              &lt;primaryRef db="pubmed" id="10504710"
 *                  secondary="" version=""/&gt;
 *              &lt;secondaryRef db="psi-mi" id="MI:0109"
 *                  secondary="" version=""/&gt;
 *          &lt;/xref&gt;
 *      &lt;/interactionDetection&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.CvInteraction
 */
public final class InteractionDetectionTag {

    // TODO IDEA: this could have extended XrefTag instead of holding one.

    private final XrefTag psiDefinition;


    ///////////////////////
    // Constructors

    public InteractionDetectionTag( final XrefTag psiDefinition ) {

        if ( psiDefinition == null ) {
            throw new IllegalArgumentException( "You must give a non null psi definition for an interactionDetection" );
        }

        if ( !CvDatabase.PSI_MI.equals( psiDefinition.getDb() ) ) {
            throw new IllegalArgumentException( "You must give a pubmed Xref, not " + psiDefinition.getDb() +
                                                " for an InteractionDetection" );
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

    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof InteractionDetectionTag ) ) {
            return false;
        }

        final InteractionDetectionTag interactionDetectionTag = (InteractionDetectionTag) o;

        if ( !psiDefinition.equals( interactionDetectionTag.psiDefinition ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return psiDefinition.hashCode();
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "InteractionDetectionTag" );
        buf.append( "{psiDefinition=" ).append( psiDefinition );
        buf.append( '}' );
        return buf.toString();
    }
}
