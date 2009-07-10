/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvInteractionType;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionTypeChecker {

    // will avoid to have to search again later !
    private static final Map cache = new HashMap();

    public static CvInteractionType getCvInteractionType( final String id ) {
        return (CvInteractionType) cache.get( id );
    }

    public static void check( final InteractionTypeTag interactionType ) {

        final XrefTag psiDef = interactionType.getPsiDefinition();
        XrefChecker.check( psiDef );

        final String id = psiDef.getId();

        if ( !cache.keySet().contains( id ) ) {
            CvInteractionType cvInteractionType = null;

            try {
                cvInteractionType = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractionType.class, id);

                if ( cvInteractionType == null ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvInteractionType " +
                                                                                "for the PSI definition: " + id ) );
                } else {
                    System.out.println( "Found InteractionType " + id + " as " + cvInteractionType.getShortLabel() );
                }

            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for CvInteractionType " +
                                                                            "having the PSI definition: " + id ) );
                e.printStackTrace();
            }

            cache.put( id, cvInteractionType );
        }
    }
}
