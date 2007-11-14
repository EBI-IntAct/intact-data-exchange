/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvFeatureIdentification;
import uk.ac.ebi.intact.model.CvFeatureType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureChecker {

    // will avoid to have to search again later !
    private static final Map featureIdentificationCache = new HashMap();

    public static CvFeatureIdentification getCvFeatureIdentification( String id ) {
        return (CvFeatureIdentification) featureIdentificationCache.get( id );
    }

    // will avoid to have to search again later !
    private static final Map featureTypeCache = new HashMap();

    public static CvFeatureType getCvFeatureType( String id ) {
        return (CvFeatureType) featureTypeCache.get( id );
    }

    // TODO where does the CvFuzzyType takes place.

    public static void ckeck( FeatureTag feature ) {

        if ( feature.hasFeatureDetection() ) {
            checkCvFeatureIdentification( feature );
        }

        checkCvTypeIdentification( feature );

        for ( Iterator iterator = feature.getXrefs().iterator(); iterator.hasNext(); ) {
            XrefTag xref = (XrefTag) iterator.next();
            XrefChecker.check( xref );
        }
    }

    private static void checkCvTypeIdentification( FeatureTag feature ) {

        XrefTag psiDef = feature.getFeatureType().getPsiDefinition();
        XrefChecker.check( psiDef );

        final String id = psiDef.getId();

        if ( !featureTypeCache.keySet().contains( id ) ) {
            CvFeatureType featureType = null;

            try {
                featureType = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvFeatureType.class,id);

                if ( featureType == null ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvFeatureType by PSI definition: " + id ) );
                } else {
                    System.out.println( "Found CvFeatureType " + id + " as " + featureType.getShortLabel() );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for CvFeatureType " +
                                                                            "having the PSI definition: " + id ) );
                e.printStackTrace();
            }

            featureTypeCache.put( id, featureType );
        }
    }


    private static void checkCvFeatureIdentification( FeatureTag feature ) {

        final XrefTag psiDef = feature.getFeatureDetection().getPsiDefinition();

        XrefChecker.check( psiDef );

        final String id = psiDef.getId();

        if ( !featureIdentificationCache.keySet().contains( id ) ) {
            CvFeatureIdentification cvFeatureIdentification = null;

            try {
                cvFeatureIdentification = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvFeatureIdentification.class,id);

                if ( cvFeatureIdentification == null ) {
                    MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvFeatureIdentification by PSI definition: " + id ) );
                } else {
                    System.out.println( "Found CvFeatureIdentification " + id + " as " + cvFeatureIdentification.getShortLabel() );
                }
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for CvFeatureIdentification " +
                                                                            "having the PSI definition: " + id ) );
                e.printStackTrace();
            }

            featureIdentificationCache.put( id, cvFeatureIdentification );
        }
    }
}
