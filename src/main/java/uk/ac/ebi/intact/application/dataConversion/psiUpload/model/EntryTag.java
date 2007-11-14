/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * That class reflects what is needed to create an IntAct <code>Experiment</code> (including interactions).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Experiment
 * @see uk.ac.ebi.intact.model.Interaction
 * @see uk.ac.ebi.intact.model.Protein
 */
public final class EntryTag {

    private static final String NEW_LINE = System.getProperty( "line.separator" );


    /**
     * Collection of ExperimentDescriptionTag.
     */
    private final Map experimentDescriptions;

    /**
     * Collection of ProteinInteractorTag.
     */
    private final Map proteinInteractors;

    /**
     * Collection of InteractionTag.
     */
    private final Collection interactions;

    //////////////////////////////////
    // Contructors

    public EntryTag( final Map experimentDescriptions,
                     final Map proteinInteractors,
                     final Collection interactions ) {

        if ( interactions == null ) {
            throw new IllegalArgumentException( "You must give a non null Collection of interactions for an Entry" );
        }

        if ( interactions.size() == 0 ) {
            throw new IllegalArgumentException( "You must give a non empty Collection of interactions for an Entry" );
        }

        for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
            Object o = (Object) iterator.next();
            if ( !( o instanceof InteractionTag ) ) {
                throw new IllegalArgumentException( "The Interaction collection added to the entry doesn't " +
                                                    "contains only InteractionTag: " + o.getClass().getName() + "." );
            }
        }

        this.interactions = new ReadOnlyCollection( interactions );

        if ( experimentDescriptions == null ) {
            this.experimentDescriptions = new ReadOnlyHashMap( new HashMap( 0 ) );
        } else {

            for ( Iterator iterator = experimentDescriptions.values().iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof ExperimentDescriptionTag ) ) {
                    throw new IllegalArgumentException( "The ExperimentDescription collection added to the entry doesn't " +
                                                        "contains only ExperimentDescriptionTag: " + o.getClass().getName() + "." );
                }
            }

            this.experimentDescriptions = new ReadOnlyHashMap( experimentDescriptions );
        }

        if ( experimentDescriptions == null ) {
            this.proteinInteractors = new ReadOnlyHashMap( new HashMap( 0 ) );
        } else {

            for ( Iterator iterator = proteinInteractors.values().iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof ProteinInteractorTag ) ) {
                    throw new IllegalArgumentException( "The ProteinInteractor collection added to the entry doesn't " +
                                                        "contains only ProteinInteractorTag: " + o.getClass().getName() + "." );
                }
            }

            this.proteinInteractors = new ReadOnlyHashMap( proteinInteractors );
        }
    }


    //////////////////////////////////
    // Getters

    /**
     * return an <code>ExperimentDescriptionTag</code> by id. Bear in mind that if you give a non existing id, you get
     * back null.
     *
     * @param id the id referencing the wanted ExperimentDescriptionTag
     *
     * @return an ExperimentDescriptionTag or null if the id is not found.
     */
    public ExperimentDescriptionTag getExperimentDescriptions( final String id ) {
        return (ExperimentDescriptionTag) experimentDescriptions.get( id );
    }

    /**
     * return an <code>ProteinParticipantTag</code> by id. Bear in mind that if you give a non existing id, you get back
     * null.
     *
     * @param id the id referencing the wanted ProteinParticipantTag
     *
     * @return an ProteinParticipantTag or null if the id is not found.
     */
    public ProteinInteractorTag getProteinInteractors( final String id ) {
        return (ProteinInteractorTag) proteinInteractors.get( id );
    }

    public Collection getInteractions() {
        return interactions;
    }

    public Map getExperimentDescriptions() {
        return experimentDescriptions;
    }

    public Map getProteinInteractors() {
        return proteinInteractors;
    }


    ////////////////////////
    // Equality

    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof EntryTag ) ) {
            return false;
        }

        final EntryTag entryTag = (EntryTag) o;

        if ( !experimentDescriptions.equals( entryTag.experimentDescriptions ) ) {
            return false;
        }
        if ( !interactions.equals( entryTag.interactions ) ) {
            return false;
        }
        if ( !proteinInteractors.equals( entryTag.proteinInteractors ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = experimentDescriptions.hashCode();
        result = 29 * result + proteinInteractors.hashCode();
        result = 29 * result + interactions.hashCode();
        return result;
    }

    public String toString() {
        final char tab = '\t';
        Collection keys;

        final StringBuffer buf = new StringBuffer();

        buf.append( "EntryTag{" ).append( NEW_LINE );

        buf.append( "experimentDescriptions(" ).append( experimentDescriptions.size() ).append( ')' );
        buf.append( NEW_LINE );
        keys = experimentDescriptions.keySet();
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            ExperimentDescriptionTag experimentDescription = (ExperimentDescriptionTag) experimentDescriptions.get( key );
            buf.append( tab ).append( key ).append( " --> " ).append( experimentDescription ).append( NEW_LINE );
        }
        buf.append( NEW_LINE );

        buf.append( "proteinInteractors(" ).append( proteinInteractors.size() ).append( ')' );
        buf.append( NEW_LINE );
        keys = proteinInteractors.keySet();
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            ProteinInteractorTag proteinInteractor = (ProteinInteractorTag) proteinInteractors.get( key );
            buf.append( tab ).append( key ).append( " --> " ).append( proteinInteractor ).append( NEW_LINE );
        }
        buf.append( NEW_LINE );

        buf.append( "interactions(" ).append( interactions.size() ).append( ')' );
        buf.append( NEW_LINE );
        for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
            InteractionTag interaction = (InteractionTag) iterator.next();
            buf.append( tab ).append( interaction ).append( NEW_LINE );
        }

        buf.append( '}' );
        return buf.toString();
    }
}
