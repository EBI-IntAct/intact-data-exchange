/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.gui.Monitor;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ExperimentDescriptionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinInteractorTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntryChecker {

    public static void check( final EntryTag entry,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory ) {

        Collection keys;
        final Map experimentDescriptions = entry.getExperimentDescriptions();
        final Map proteinInteractors = entry.getProteinInteractors();
        final Collection interactions = entry.getInteractions();

        keys = experimentDescriptions.keySet();
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            final String key = (String) iterator.next();
            final ExperimentDescriptionTag experimentDescription =
                    (ExperimentDescriptionTag) experimentDescriptions.get( key );
            ExperimentDescriptionChecker.check( experimentDescription, bioSourceFactory );
        }

        // According to the object model this should not be needed, that test will be done at the interaction level.
        keys = proteinInteractors.keySet();
        boolean guiEnabled = CommandLineOptions.getInstance().isGuiEnabled();
        Monitor monitor = null;
        boolean displayProgressBar = guiEnabled && keys.size() > 0;
        if ( displayProgressBar ) {
            monitor = new Monitor( keys.size(), "Protein update" );
            monitor.setStatus( "Waiting for the checker to start..." );
            monitor.show();
        }
        int current = 0;
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            final String key = (String) iterator.next();
            final ProteinInteractorTag proteinInteractor = (ProteinInteractorTag) proteinInteractors.get( key );
            if ( guiEnabled ) {
                String uniprotID = null;
                String taxid = null;

                if ( proteinInteractor != null && proteinInteractor.getPrimaryXref() != null ) {
                    uniprotID = proteinInteractor.getPrimaryXref().getId();
                }
                if ( proteinInteractor != null && proteinInteractor.getOrganism() != null ) {
                    taxid = proteinInteractor.getOrganism().getTaxId();
                }
                monitor.setStatus( "Checking " + uniprotID + " (" + taxid + ")" );
            }

            ProteinInteractorChecker.check( proteinInteractor, proteinFactory, bioSourceFactory );

            if ( guiEnabled ) {
                monitor.updateProteinProcessedCound( ++current );
            }
        }
        if ( displayProgressBar ) {
            monitor.hide();
        }


        displayProgressBar = guiEnabled && interactions.size() > 0;
        if ( displayProgressBar ) {
            monitor = new Monitor( interactions.size(), "Interaction checking" );
            monitor.setStatus( "Waiting for the checker to start..." );
            monitor.show();
        }
        current = 0;
        for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
            final InteractionTag interaction = (InteractionTag) iterator.next();
            if ( guiEnabled ) {
                String name = interaction.getShortlabel();
                if ( name == null ) {
                    name = "no shortlabel specified";
                }
                monitor.setStatus( "Checking " + name );
                monitor.updateProteinProcessedCound( ++current );
            }
            InteractionChecker.check( interaction, proteinFactory, bioSourceFactory, monitor );
        }
        if ( displayProgressBar ) {
            monitor.setStatus( "finished..." );
            monitor.hide();
        }
    }
}
