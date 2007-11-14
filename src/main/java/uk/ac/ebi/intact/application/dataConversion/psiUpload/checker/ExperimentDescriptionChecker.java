/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ExperimentDescriptionChecker {

    // Store existing IntAct Experiment
    // shortlabel -> Experiment
    private static final Map cache = new HashMap();

    public static Experiment getIntactExperiment( final String shortlabel ) {
        return (Experiment) cache.get( shortlabel );
    }


    public static void check( final ExperimentDescriptionTag experimentDescription,
                              final BioSourceFactory bioSourceFactory ) {

        final String shortlabel = experimentDescription.getShortlabel();
        if ( cache.get( shortlabel ) == null ) {
            // we check once per shortlabel...
            Experiment experiment = null;
            try {
                experiment = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getExperimentDao().getByShortLabel( shortlabel );
            } catch ( IntactException e ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "An error occured while searching for " +
                                                                            "Experiment having the shortlabel: " + shortlabel ) );
                e.printStackTrace();
            }

            if ( experiment != null ) {
                // keep it for later use !
                System.out.println( "The experiments having the shortlabel: " + shortlabel + " already exists! " +
                                    "It will be reused." );
                cache.put( shortlabel, experiment );
            }
        }

        // We still check the rest of the ExperimentTag object.

        final HostOrganismTag hostOrganism = experimentDescription.getHostOrganism();
        HostOrganismChecker.check( hostOrganism, bioSourceFactory );

        final InteractionDetectionTag interactionDetection = experimentDescription.getInteractionDetection();
        InteractionDetectionChecker.check( interactionDetection );

        final ParticipantDetectionTag participantDetection = experimentDescription.getParticipantDetection();
        ParticipantDetectionChecker.check( participantDetection );

        // bibrefs - primary and secondary.
        XrefChecker.check( experimentDescription.getBibRef() );
        Collection additionalBibRef = experimentDescription.getAdditionalBibRef();
        for ( Iterator iterator = additionalBibRef.iterator(); iterator.hasNext(); ) {
            XrefTag bibRef = (XrefTag) iterator.next();
            XrefChecker.check( bibRef );
        }

        // Xrefs
        final Collection xrefs = experimentDescription.getXrefs();
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            XrefTag xref = (XrefTag) iterator.next();
            XrefChecker.check( xref );
        }

        // annotations
        final Collection annotations = experimentDescription.getAnnotations();
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            AnnotationTag annotation = (AnnotationTag) iterator.next();
            AnnotationChecker.check( annotation );
        }
    }
}
