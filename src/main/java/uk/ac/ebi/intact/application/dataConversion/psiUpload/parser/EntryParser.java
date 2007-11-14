/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;

import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntryParser {

    private static ExperimentListParser globalExperiments;
    private static ParticipantListParser globalParticipants;


    /////////////////////////
    // Getters

    public ExperimentListParser getGlobalExperiments() {
        return globalExperiments;
    }


    public ParticipantListParser getGlobalParticipants() {
        return globalParticipants;
    }


    /**
     * Load all global Experiment and Interaction succeptible to be used by the interactions described in the PSI data.
     *
     * @param entry
     */
    private static void loadGlobalContext( Element entry ) {

        // process all eventual experiment (COULD BE EMPTY)
        globalExperiments = new ExperimentListParser();
        globalExperiments.process( entry );

        // process all eventual interactors (COULD BE EMPTY)
        globalParticipants = new ParticipantListParser();
        globalParticipants.process( entry );
    }

    public static EntryTag process( Element entry ) {

        loadGlobalContext( entry );

        // process all interactions
        InteractionListParser interactionList = new InteractionListParser( globalExperiments, globalParticipants );
        Collection interactions = interactionList.process( entry );


        EntryTag entryTag = null;
        try {
            entryTag = new EntryTag( globalExperiments.getExperiments(),
                                     globalParticipants.getInteractors(),
                                     interactions );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( entry, e.getMessage() ) );
        }

        return entryTag;
    }
}