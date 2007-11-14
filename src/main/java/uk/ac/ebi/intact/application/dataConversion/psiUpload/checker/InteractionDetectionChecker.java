/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionDetectionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvInteraction;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class InteractionDetectionChecker {

    // will avoid to have to search again later !
    private static final Map cache = new HashMap();

    public static CvInteraction getCvInteraction( String id ) {
        return (CvInteraction) cache.get( id );
    }

    public static void check( final InteractionDetectionTag interactionDetection ) {

        final XrefTag psiDef = interactionDetection.getPsiDefinition();
        XrefChecker.check( psiDef );

        final String id = psiDef.getId();

        if ( !cache.keySet().contains( id ) ) {
            CvInteraction cvInteraction = null;

            try {
                cvInteraction = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteraction.class,id);

                if ( cvInteraction == null ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvInteraction by PSI definition: " + id ) );
                } else {
                    System.out.println( "Found CvInteraction " + id + " as " + cvInteraction.getShortLabel() );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for CvInteraction " +
                                                                            "having the PSI definition: " + id ) );
                e.printStackTrace();
            }

            cache.put( id, cvInteraction );
        }
    }
}
