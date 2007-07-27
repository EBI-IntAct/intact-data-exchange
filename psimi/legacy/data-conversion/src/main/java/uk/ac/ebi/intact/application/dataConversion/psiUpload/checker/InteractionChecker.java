/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.gui.Monitor;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvInteractorType;
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
public final class InteractionChecker {

    private static CvInteractorType cvInteractionType = null;

    public static boolean interatorTypeChecked = false;

    public static void checkCvInteractorType( ) {

        if ( false == interatorTypeChecked ) {

            // Load CvInteractorType( interaction / MI: )
            cvInteractionType = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractorType.class, CvInteractorType.getInteractionMI());
            if ( cvInteractionType == null ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvInteractorType( interaction )." ) );
            }
            interatorTypeChecked = true;
        }
    }

    public static CvInteractorType getCvInteractionType() {
        return cvInteractionType;
    }

    public static void check( final InteractionTag interaction,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory,
                              final Monitor monitor ) {

        // check that the CvInteractorType( interaction is available ).
        checkCvInteractorType( );

        // experiment
        Collection experiments = interaction.getExperiments();
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            ExperimentDescriptionTag experimentDescription = (ExperimentDescriptionTag) iterator.next();
            ExperimentDescriptionChecker.check( experimentDescription, bioSourceFactory );
        }

        // participants
        final Collection participants = interaction.getParticipants();
        // TODO do we have to check that we have at least 1 bait and 1..m preys/neutrals
        for ( Iterator iterator = participants.iterator(); iterator.hasNext(); ) {
            ProteinParticipantTag proteinParticipant = (ProteinParticipantTag) iterator.next();

            if ( monitor != null ) {
                ProteinInteractorTag proteinInteractor = proteinParticipant.getProteinInteractor();
                String uniprotID = null;
                String taxid = null;

                if ( proteinInteractor != null && proteinInteractor.getPrimaryXref() != null ) {
                    uniprotID = proteinInteractor.getPrimaryXref().getId();
                }

                if ( proteinInteractor != null && proteinInteractor.getOrganism() != null ) {
                    taxid = proteinInteractor.getOrganism().getTaxId();
                } else {
                    taxid = "taxid not specified";
                }

                monitor.setStatus( "Checking " + uniprotID + " (" + taxid + ")" );
            }

            // check the participant
            ProteinParticipantChecker.check( proteinParticipant, proteinFactory, bioSourceFactory );

            // check the expressedIn if it is there.
            ExpressedInTag expressedIn = proteinParticipant.getExpressedIn();
            if ( null != expressedIn ) {
                ExpressedInChecker.check( expressedIn );
            }
        }

        // interactionType
        final InteractionTypeTag interactionType = interaction.getInteractionType();
        InteractionTypeChecker.check( interactionType );

        // xrefs
        Collection xrefs = interaction.getXrefs();
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            XrefTag xref = (XrefTag) iterator.next();
            XrefChecker.check( xref );
        }

        // annotations
        final Collection annotations = interaction.getAnnotations();
        int countKd = 0;
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            AnnotationTag annotation = (AnnotationTag) iterator.next();
            if ( annotation.isDissociationConstant() ) {
                countKd++;
            }
            AnnotationChecker.check( annotation );
        }

        // 0 or 1 dissociation constant can be specified, no more.
        if ( countKd > 1 ) {
            // TODO find a way to indicate to the user which interaction we are talking about
            MessageHolder.getInstance().addCheckerMessage( new Message( "More than one dissociation constant specified " +
                                                                        "in an Interaction (one at most is allowed)." ) );
        }
    }
}