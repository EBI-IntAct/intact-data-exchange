/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinInteractorTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinParticipantTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;

import java.util.Collection;
import java.util.Iterator;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinParticipantChecker {

    public static void check( final ProteinParticipantTag proteinParticipant,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory ) {

        final String role = proteinParticipant.getRole();
        RoleChecker.check( role );

        final ProteinInteractorTag proteinInteractor = proteinParticipant.getProteinInteractor();
        ProteinInteractorChecker.check( proteinInteractor, proteinFactory, bioSourceFactory );

        final Collection features = proteinParticipant.getFeatures();
        for ( Iterator iterator = features.iterator(); iterator.hasNext(); ) {
            FeatureTag feature = (FeatureTag) iterator.next();

            FeatureChecker.ckeck( feature );
        }

        // check feature clustering (specific to PSI version 1)
        try {
            proteinParticipant.getClusteredFeatures();
        } catch ( IllegalArgumentException iae ) {
            MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while checking if the feature were clusterizable: " +
                                                                        iae.getMessage() ) );
        }

        // TODO check isOverExpressed
        // TODO check isTaggedProtein: as we have to create a Feature having CvFeatureType(tagged-protein) with undetermined range
    }
}
