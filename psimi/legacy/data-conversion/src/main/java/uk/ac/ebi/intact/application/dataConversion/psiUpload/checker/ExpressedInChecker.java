/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ExpressedInTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.BioSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ExpressedInChecker {

    // will avoid to have to search again later !
    // shortlabel -> BioSource
    private static final Map cache = new HashMap();


    public static BioSource getBioSource( String shortlabel ) {
        return (BioSource) cache.get( shortlabel );
    }

    public static void check( final ExpressedInTag expressedIn ) {

        if ( expressedIn == null ) {
            throw new IllegalArgumentException( "Could not check ExpressedInTag if the given parameter is null" );
        }
        String shortlabel = expressedIn.getBioSourceShortlabel();

        if ( !cache.keySet().contains( shortlabel ) ) {
            BioSource bioSource = null;
            try {

                Collection bioSources = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBioSourceDao().getByShortLabelLike(shortlabel);

                if ( bioSources.size() == 1 ) {
                    // TODO could be a problem if we want to use a biosource that has cell type [and/or] tissue.
                    System.out.println( "Found ExpressedIn Biosource having the shortlabel: " + shortlabel );
                    bioSource = (BioSource) bioSources.iterator().next();

                } else if ( bioSources.size() > 0 ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Found more than one BioSource (expressedIn) " +
                                                                                "having the shortlabel: " + shortlabel ) );
                } else if ( bioSources.size() == 0 ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Found no BioSource (expressedIn) " +
                                                                                "having the shortlabel: " + shortlabel ) );
                }

            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching " +
                                                                            "for BioSource (expressedIn)" +
                                                                            "having the shortlabel: " + shortlabel ) );
                e.printStackTrace();
            }

            cache.put( shortlabel, bioSource );
        }
    }
}
