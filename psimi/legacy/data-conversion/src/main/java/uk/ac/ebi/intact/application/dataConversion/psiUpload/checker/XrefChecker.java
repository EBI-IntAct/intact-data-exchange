/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class XrefChecker {

    // will avoid to have to search again later !
    private static final Map cache = new HashMap();

    public static CvDatabase getCvDatabase( String databaseName ) {
        return (CvDatabase) cache.get( databaseName );
    }

    public static void check( final XrefTag xref ) {

        final String db = xref.getDb();

        if ( !cache.keySet().contains( db ) ) {
            CvDatabase cvDatabase = null;
            try {
                cvDatabase = IntactContext.getCurrentInstance().getCvContext().getByLabel(CvDatabase.class, db );

                if ( cvDatabase != null ) {
                    System.out.println( "Found CvDatabase with shortlabel: " + db );
                } else {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvDatabase " +
                                                                                "by shortlabel: " + db ) );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching " +
                                                                            "for CvDatabase " +
                                                                            "having the shortlabel: " + db ) );
                e.printStackTrace();
            }

            cache.put( db, cvDatabase );
        }
    }
}
