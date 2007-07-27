// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.model;

import uk.ac.ebi.intact.model.*;

/**
 * Allow to create a Protein to which we can set an AC.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class TestableProtein extends ProteinImpl {

    public TestableProtein() {

    }

    public TestableProtein( String ac, Institution owner, BioSource source, String shortLabel, CvInteractorType type, String aSequence ) {
        super( owner, source, shortLabel, type );

        if ( ac == null ) {
            throw new IllegalArgumentException( "You must give a non null AC." );
        }
        this.ac = ac;

        if (aSequence == null)
        {
            return;
        }

        // count of required chunk to fit the sequence
        int chunkCount = aSequence.length() / MAX_SEQ_LENGTH_PER_CHUNK;
        if ( aSequence.length() % MAX_SEQ_LENGTH_PER_CHUNK > 0 ) {
            chunkCount++;
        }

        String chunk = null;
        for ( int i = 0; i < chunkCount; i++ ) {

            chunk = aSequence.substring( i * MAX_SEQ_LENGTH_PER_CHUNK,
                                         Math.min( ( i + 1 ) * MAX_SEQ_LENGTH_PER_CHUNK,
                                                   aSequence.length() ) );

            // create new chunk
            SequenceChunk s = new SequenceChunk( i, chunk );
            addSequenceChunk( s );
        }
    }
}