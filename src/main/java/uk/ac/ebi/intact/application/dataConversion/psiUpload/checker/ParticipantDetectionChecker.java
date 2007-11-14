/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ParticipantDetectionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvIdentification;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ParticipantDetectionChecker {

    // will avoid to have to search again later !
    private static final Map cache = new HashMap();

    public static CvIdentification getCvIdentification( String id ) {
        return (CvIdentification) cache.get( id );
    }

    public static void check( final ParticipantDetectionTag participantDetection ) {

        final XrefTag psiDef = participantDetection.getPsiDefinition();
        XrefChecker.check( psiDef );

        final String id = psiDef.getId();

        if ( !cache.keySet().contains( id ) ) {
            CvIdentification cvIdentification = null;

            try {
                cvIdentification = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvIdentification.class,id);

                if ( cvIdentification == null ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvIdentification for the PSI definition: " + id ) );
                } else {
                    System.out.println( "Found ParticipantDetection " + id + " as " + cvIdentification.getShortLabel() );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for CvInteraction " +
                                                                            "having the PSI definition: " + id ) );
                e.printStackTrace();
            }

            cache.put( id, cvIdentification );
        }
    }
}
