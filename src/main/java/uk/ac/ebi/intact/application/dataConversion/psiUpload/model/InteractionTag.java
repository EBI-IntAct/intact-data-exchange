/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.model;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.util.ReadOnlyCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class reflects what is needed to create an IntAct <code>Interaction</code>.
 * <p/>
 * <pre>
 *      &lt;interaction&gt;
 *          &lt;experimentList&gt;
 *              &lt;experimentRef ref="EBI-12"/&gt;
 *          &lt;/experimentList&gt;
 *          &lt;participantList&gt;
 *              &lt;proteinParticipant&gt;
 *                  &lt;proteinInteractorRef ref="EBI-111"/&gt;
 *                  &lt;role&gt;bait&lt;/role&gt;
 *              &lt;/proteinParticipant&gt;
 *              &lt;proteinParticipant&gt;
 *                  &lt;proteinInteractorRef ref="EBI-222"/&gt;
 *                  &lt;role&gt;prey&lt;/role&gt;
 *              &lt;/proteinParticipant&gt;
 *          &lt;/participantList&gt;
 *          &lt;interactionType&gt;
 *              &lt;names&gt;
 *                  &lt;shortLabel&gt;tandem affinity puri&lt;/shortLabel&gt;
 *                  &lt;fullName&gt;tandem affinity purification&lt;/fullName&gt;
 *              &lt;/names&gt;
 *              &lt;xref&gt;
 *                  &lt;primaryRef db="pubmed" id="10504710" secondary="" version=""/&gt;
 *                  &lt;secondaryRef db="psi-mi" id="MI:0109" secondary="" version=""/&gt;
 *              &lt;/xref&gt;
 *          &lt;/interactionType&gt;
 *      &lt;/interaction&gt;
 * </pre>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.model.Interaction
 */
public final class InteractionTag {

    private static final int MIN_PARTICIPANTS = 2;


    private final String shortlabel;
    private final String fullname;
    private final InteractionTypeTag interactionType;

    /**
     * Reflects <experimentList> Collection of ExperimentDescriptionTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ExperimentDescriptionTag
     */
    private final Collection experiments;

    /**
     * Reflects <participantList> Collection of ProteinParticipantTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinParticipantTag
     */
    private final Collection participants;

    /**
     * Reflects <xref> Collection of XrefTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag
     */
    private final Collection xrefs;

    /**
     * Reflects <attributeList> Collection of AnnotationTag
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.AnnotationTag
     */
    private final Collection annotations;
    private final ConfidenceTag confidence;


    /////////////////////////////
    // Constructor

    public InteractionTag( final String shortlabel,
                           final String fullname,
                           final Collection experiments,
                           final Collection participants,
                           final InteractionTypeTag interactionType,
                           final Collection xrefs,
                           final Collection annotations,
                           final ConfidenceTag confidence ) {

        if ( experiments == null ) {
            throw new IllegalArgumentException( "You must give a non null collection of experiments " +
                                                "for an interaction " );
        }

        if ( experiments.size() == 0 ) {
            throw new IllegalArgumentException( "You must give a non empty collection of experiments " +
                                                "for an interaction " );
        }

        // check the collection content
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            Object o = (Object) iterator.next();
            if ( !( o instanceof ExperimentDescriptionTag ) ) {
                throw new IllegalArgumentException( "The experiment collection added to the interaction doesn't " +
                                                    "contains only ExperimentDescriptionTag." );
            }
        }

        if ( participants == null ) {
            throw new IllegalArgumentException( "You must give a non null collection of participants " +
                                                "for an interaction " );
        }

        if ( participants.size() < MIN_PARTICIPANTS ) {

            if ( participants.size() == 1 ) {
                // search which is the given protein participant to facilitate to the user the reserch of
                // that interaction in the XML file.
                String uniprotID = null;
                for ( Iterator iterator = participants.iterator(); iterator.hasNext(); ) {
                    Object o = (Object) iterator.next();
                    if ( o instanceof ProteinParticipantTag ) {
                        uniprotID = ( (ProteinParticipantTag) o ).getProteinInteractor().getPrimaryXref().getId();
                    }
                }
                throw new IllegalArgumentException( "You must give a minimum of " + MIN_PARTICIPANTS + " participants " +
                                                    "for an interaction. You gave only the protein having the uniprot id: " + uniprotID );

            }

            throw new IllegalArgumentException( "You must give a minimum of " + MIN_PARTICIPANTS + " participants " +
                                                "for an interaction. You gave " + participants.size() );
        }

        // check the collection content
        for ( Iterator iterator = participants.iterator(); iterator.hasNext(); ) {
            Object o = (Object) iterator.next();
            if ( !( o instanceof ProteinParticipantTag ) ) {
                throw new IllegalArgumentException( "The participants collection added to the interaction doesn't " +
                                                    "contains only ProteinParticipantTag: " + o.getClass().getName() + "." );
            }
        }

        if ( interactionType == null ) {
            throw new IllegalArgumentException( "You must give a non null interactionType for an interaction " );
        }

        if ( xrefs == null ) {
            this.xrefs = new ReadOnlyCollection( new ArrayList( 0 ) );
        } else {
            // check the collection content
            for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof XrefTag ) ) {
                    throw new IllegalArgumentException( "The annotation collection added to the interaction doesn't " +
                                                        "contains only XrefTag: " + o.getClass().getName() + "." );
                }
            }
            this.xrefs = new ReadOnlyCollection( xrefs );
        }

        if ( annotations == null ) {
            this.annotations = new ReadOnlyCollection( new ArrayList( 0 ) );
        } else {
            // check the collection content
            for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
                Object o = (Object) iterator.next();
                if ( !( o instanceof AnnotationTag ) ) {
                    throw new IllegalArgumentException( "The annotation collection added to the interaction doesn't " +
                                                        "contains only AnnotationTag: " + o.getClass().getName() + "." );
                }
            }
            this.annotations = new ReadOnlyCollection( annotations );
        }

        this.shortlabel = shortlabel;
        this.fullname = fullname;
        this.experiments = new ReadOnlyCollection( experiments );
        this.interactionType = interactionType;
        this.participants = new ReadOnlyCollection( participants );
        this.confidence = confidence;
    }


    /////////////////////////////
    // Getters

    public String getShortlabel() {
        return shortlabel;
    }

    public String getFullname() {
        return fullname;
    }

    public Collection getExperiments() {
        return experiments;
    }

    public Collection getParticipants() {
        return participants;
    }

    public InteractionTypeTag getInteractionType() {
        return interactionType;
    }

    public Collection getXrefs() {
        return xrefs;
    }

    public Collection getAnnotations() {
        return annotations;
    }

    public ConfidenceTag getConfidence() {
        return confidence;
    }


    ////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof InteractionTag ) ) {
            return false;
        }

        final InteractionTag interactionTag = (InteractionTag) o;

        if ( !annotations.equals( interactionTag.annotations ) ) {
            return false;
        }
        if ( confidence != null ? !confidence.equals( interactionTag.confidence ) : interactionTag.confidence != null ) {
            return false;
        }
        if ( !experiments.equals( interactionTag.experiments ) ) {
            return false;
        }
        if ( fullname != null ? !fullname.equals( interactionTag.fullname ) : interactionTag.fullname != null ) {
            return false;
        }
        if ( !interactionType.equals( interactionTag.interactionType ) ) {
            return false;
        }
        if ( !participants.equals( interactionTag.participants ) ) {
            return false;
        }
        if ( shortlabel != null ? !shortlabel.equals( interactionTag.shortlabel ) : interactionTag.shortlabel != null ) {
            return false;
        }
        if ( !xrefs.equals( interactionTag.xrefs ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( shortlabel != null ? shortlabel.hashCode() : 0 );
        result = 29 * result + ( fullname != null ? fullname.hashCode() : 0 );
        result = 29 * result + interactionType.hashCode();
        result = 29 * result + experiments.hashCode();
        result = 29 * result + participants.hashCode();
        result = 29 * result + xrefs.hashCode();
        result = 29 * result + annotations.hashCode();
        result = 29 * result + ( confidence != null ? confidence.hashCode() : 0 );
        return result;
    }


    ////////////////////////////
    // toString

    public String toString() {

        final StringBuffer buf = new StringBuffer();
        buf.append( "InteractionTag" );
        buf.append( "{shortlabel=" ).append( shortlabel );
        buf.append( ",fullname=" ).append( fullname );
        buf.append( ",experiments=" );
        for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
            final ExperimentDescriptionTag experimentDescription = (ExperimentDescriptionTag) iterator.next();
            buf.append( experimentDescription );
        }

        buf.append( ",participants=" );
        for ( Iterator iterator = participants.iterator(); iterator.hasNext(); ) {
            final ProteinParticipantTag proteinParticipant = (ProteinParticipantTag) iterator.next();
            buf.append( proteinParticipant ).append( ',' );
        }

        buf.append( ",interactionType=" ).append( interactionType );
        buf.append( ",confidence=" ).append( confidence );

        buf.append( ",xrefs=" );
        if ( xrefs.size() == 0 ) {
            buf.append( "none" );
        }
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            final XrefTag xref = (XrefTag) iterator.next();
            buf.append( xref );
        }

        buf.append( ",annotations=" );
        if ( annotations.size() == 0 ) {
            buf.append( "none" );
        }
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            final AnnotationTag annotation = (AnnotationTag) iterator.next();
            buf.append( annotation );
        }

        buf.append( '}' );
        return buf.toString();
    }
}
