/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 * <p/>
 * <pre>
 *      &lt;participantDetection&gt;
 *          &lt;names&gt;
 *              &lt;shortLabel&gt;peptide massfingerpr&lt;/shortLabel&gt;
 *              &lt;fullName&gt;peptide massfingerprinting&lt;/fullName&gt;
 *          &lt;/names&gt;
 *          &lt;xref&gt;
 *              &lt;primaryRef db="pubmed" id="11752590" secondary="" version=""/&gt;
 *              &lt;secondaryRef db="psi-mi" id="MI:0082" secondary="" version=""/&gt;
 *              &lt;secondaryRef db="pubmed" id="10967324" secondary="" version=""/&gt;
 *          &lt;/xref&gt;
 *      &lt;/participantDetection&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ParticipantDetectionTag {

    private final XrefTag psiDefinition;

    ///////////////////////
    // Constructors

    public ParticipantDetectionTag( final XrefTag psiDefinition ) {

        if ( psiDefinition == null ) {
            throw new IllegalArgumentException( "You must give a non null psi definition for an participantDetection" );
        }

        if ( !CvDatabase.PSI_MI.equals( psiDefinition.getDb() ) ) {
            throw new IllegalArgumentException( "You must give a psi-mi Xref, not " + psiDefinition.getDb() +
                                                " for an ParticipantDetection" );
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
        if ( !( o instanceof ParticipantDetectionTag ) ) {
            return false;
        }

        final ParticipantDetectionTag participantDetectionTag = (ParticipantDetectionTag) o;

        if ( !psiDefinition.equals( participantDetectionTag.psiDefinition ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return psiDefinition.hashCode();
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "ParticipantDetectionTag" );
        buf.append( "{psiDefinition=" ).append( psiDefinition );
        buf.append( '}' );
        return buf.toString();
    }
}
