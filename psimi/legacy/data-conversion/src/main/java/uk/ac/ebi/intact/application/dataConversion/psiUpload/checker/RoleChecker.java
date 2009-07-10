/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvExperimentalRole;

import java.util.HashMap;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class RoleChecker {

    // will avoid to have to search again later !
    private static final Map cache = new HashMap();

    private static boolean unspecifiedSearched = false;
    private static CvBiologicalRole unspecified;


    public static CvBiologicalRole getDefaultCvBiologicalRole() {
        return unspecified;
    }

    public static CvExperimentalRole getCvExperimentalRole( String role ) {
        return ( CvExperimentalRole ) cache.get( role );
    }

    public synchronized static void check( final String role ) {

        if ( !unspecifiedSearched ) {
            unspecifiedSearched = true;
            unspecified = IntactContext.getCurrentInstance().getCvContext().getByMiRef( CvBiologicalRole.class,
                                                                                        CvBiologicalRole.UNSPECIFIED_PSI_REF );
            if ( unspecified == null ) {
                MessageHolder.getInstance().addCheckerMessage(
                        new Message( "Could not find CvBiologicalRole (unspecified)" +
                                     "by psimi id: " + CvBiologicalRole.UNSPECIFIED_PSI_REF ) );
            }
        }


        if ( !cache.keySet().contains( role ) ) {
            CvExperimentalRole experimentalRole = null;
            try {

                if ( !( "bait".equals( role ) ||
                        "prey".equals( role ) ||
                        "unspecified".equals( role ) ||
                        "neutral".equals( role ) ) ) {

                    final String msg = "The role: " + role +
                                       " is not supported by PSI. It should be either bait, prey, neutral or unspecified";
                    MessageHolder.getInstance().addCheckerMessage( new Message( msg ) );
                }

                if ( "neutral".equals( role ) ) {
                    // we may have either 'neutral' or 'neutral component' in the database ...
                    // handle it !!

                    experimentalRole = IntactContext.getCurrentInstance().getCvContext().getByLabel( CvExperimentalRole.class, role );

                    if ( experimentalRole == null ) {

                        // it was not found, try the other possibility

                        experimentalRole = IntactContext.getCurrentInstance().getCvContext().getByLabel( CvExperimentalRole.class, CvExperimentalRole.NEUTRAL );
                        if ( experimentalRole == null ) {
                            // neither worked, there is a problem of data integrity
                            System.out.println( "ERROR: neither " + role + " nor " + CvExperimentalRole.NEUTRAL +
                                                " could be found in the database (CvExperimentalRole)." );
                        }
                    }

                } else {

                    // any other role is search simply by shorltabel.
                    experimentalRole = IntactContext.getCurrentInstance().getCvContext().getByLabel( CvExperimentalRole.class, role );
                }

                if ( experimentalRole != null ) {
                    System.out.println( "Found CvComponentRole with shortlabel: " + role );
                } else {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvComponentRole " +
                                                                                "by shortlabel: " + role ) );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching " +
                                                                            "for CvComponentRole " +
                                                                            "having the shortlabel: " + role ) );
                e.printStackTrace();
            }

            cache.put( role, experimentalRole );
        }
    }
}
