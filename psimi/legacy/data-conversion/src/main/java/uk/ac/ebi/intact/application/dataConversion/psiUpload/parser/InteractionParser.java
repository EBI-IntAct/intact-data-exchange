/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.LabelValueBean;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionParser {

    private final static boolean DEBUG = CommandLineOptions.getInstance().isDebugEnabled();

    private ExperimentListParser experimentList;
    private ParticipantListParser interactorList;
    private Element root;


    public InteractionParser( final ExperimentListParser experimentList,
                              final ParticipantListParser interactorList,
                              final Element root ) {

        this.experimentList = experimentList;
        this.interactorList = interactorList;

        // TODO: could throw a IllegalArgumentException here if it is different from <interaction>
        this.root = root;
    }


    public InteractionTag process() {

        if ( false == "interaction".equals( root.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, "ERROR - We should be in interaction tag." ) );
        }


        // CAUTION - MAY NOT BE THERE
        final Element names = DOMUtil.getFirstElement( root, "names" );
        String shortLabel = null;
        String fullName = null;
        if ( names != null ) {
            shortLabel = DOMUtil.getShortLabel( names );
            // CAUTION - if names present, IT MAY NOT BE THERE
            fullName = DOMUtil.getFullName( names );
        }


        // CAUTION - MAY NOT BE THERE
        final Element xrefElement = DOMUtil.getFirstElement( root, "xref" );
        Collection xrefs = null;
        if ( xrefElement != null ) {
            xrefs = XrefParser.process( xrefElement );
        }


        // process the experiment (minimum:1, maximum:-)
        // There is an experiment reference XOR a single experiment discription
        final Element experimentListElement = DOMUtil.getFirstElement( root, "experimentList" );
        // hold the list of experiment locally defined in that interaction
        final Collection localExperiments = new ArrayList();

        // process the experiment references
        /**
         * <experimentList>
         *    <experimentRef ref="abcdefg"/>
         *    <!-- OR -->
         *    <experimentDescription> ... </experimentDescription>
         * </experimentList>
         */

        // Retreive the reference from the global set
        final NodeList someExperimentsRef = experimentListElement.getElementsByTagName( "experimentRef" );
        int i;
        for ( i = 0; i < someExperimentsRef.getLength(); i++ ) {
            final Node experimentRef = someExperimentsRef.item( i );

            // get eventual interactor reference
            final String ref;
            ref = ( (Element) experimentRef ).getAttribute( "ref" );
            // check if there is an existing definition of that experiment.
            final ExperimentDescriptionTag e = (ExperimentDescriptionTag) experimentList.getExperiments().get( ref );
            if ( e != null ) {
                localExperiments.add( e );
            } else {
                MessageHolder.getInstance().addParserMessage( new Message( experimentListElement,
                                                                           "Experiment reference is unknown: " + ref ) );
            }
        } // experiment refs


        // Process the experimentDescription
        final ExperimentListParser experimentList = new ExperimentListParser();
        experimentList.process( root );
        localExperiments.addAll( experimentList.getExperiments().values() );


        // get eventual annotations - has to be done before participant as we can have some annotation
        //                            related to expressedIn.
        // CAUTION - MAY NOT BE THERE
        final Element annotationElement = DOMUtil.getFirstElement( root, "attributeList" );
        Collection annotations = null;
        Collection expressedInAnnotations = null;
        if ( annotationElement != null ) {
            final NodeList someAttributes = annotationElement.getElementsByTagName( "attribute" );
            final int count = someAttributes.getLength();
            annotations = new ArrayList( count );
            expressedInAnnotations = new ArrayList( count );

            for ( i = 0; i < count; i++ ) {
                final Node entryNode = someAttributes.item( i );
                final AnnotationTag annotation = AnnotationParser.process( (Element) entryNode );
                // only add annotation with text
                if ( annotation.hasText() ) {
                    if ( Constants.EXPRESSED_IN.equalsIgnoreCase( annotation.getType() ) ) {
                        // that's an 'expressedIn' ... keep it separately
                        try {
                            expressedInAnnotations.add( new ExpressedInTag( annotation ) );
                        } catch ( IllegalArgumentException e ) {
                            MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
                        }
                    } else {
                        // that's a regular annotation
                        annotations.add( annotation );
                    }
                }
            } // attributes
        }


        // process all eventual participants ( minimum:2, maximum:- )
        final Element participantsList = DOMUtil.getFirstElement( root, "participantList" );
        final NodeList allParticipants = participantsList.getElementsByTagName( "proteinParticipant" );
        final int participantCount = allParticipants.getLength();
        Collection participants = new ArrayList( participantCount );

        for ( i = 0; i < participantCount; i++ ) {
            final Element participantElement = (Element) allParticipants.item( i );
            ProteinInteractorTag interactor = null;

            // get eventual interactor reference
            Element proteinRefElement = DOMUtil.getFirstElement( participantElement, "proteinInteractorRef" );

            // will carry the identifier of the protein interactor.
            String id = null;

            if ( proteinRefElement != null ) {
                id = proteinRefElement.getAttribute( "ref" );
                interactor = (ProteinInteractorTag) interactorList.getInteractors().get( id );
                if ( interactor == null ) {
                    // TODO unknown protein reference
                    final String msg = "ERROR - The given reference (" + id + ") could not be found in the global " +
                                       "interactionList.";
                    MessageHolder.getInstance().addParserMessage( new Message( participantElement, msg ) );
                }

            } else {

                // process the proteinInteractor as no Ref was found - mandatory to find it.
                final Element proteinElement = DOMUtil.getFirstElement( participantElement, "proteinInteractor" );
                if ( proteinElement != null ) {

                    final ProteinInteractorParser proteinInteractor = new ProteinInteractorParser( interactorList,
                                                                                                   proteinElement );
                    final LabelValueBean lvb = proteinInteractor.process();
                    if ( lvb != null ) {
                        interactor = (ProteinInteractorTag) lvb.getValue();
                        id = lvb.getLabel();
                    }
                }
            }

            if ( interactor == null ) {

                final String msg = "ERROR - No participant found (neither proteinInteractorRef " +
                                   "nor proteinInteractor), abort that interaction.";
                MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );

                return null; // TODO enhance that ! not really good to send back null.
            }

            // get the role of that participant in the interaction
            final Element roleElement = DOMUtil.getFirstElement( participantElement, "role" );
            String role = null;
            if ( roleElement != null ) {
                role = DOMUtil.getSimpleElementText( roleElement );
            } else {
                final String msg = "ERROR - No role found under that participant, abort that interaction.";
                MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );

                return null; // TODO enhance that ! not really good to send back null.
            }

            // CAUTION - MAY NOT BE THERE
            final Element featureListElement = DOMUtil.getFirstElement( participantElement, "featureList" );
            Collection features = null;
            if ( featureListElement != null ) {
                features = FeatureListParser.process( featureListElement );
            }

            // Search if there is an expressedIn related to the current interactor
            ExpressedInTag expressedIn = null;
            if ( null != expressedInAnnotations ) {
                for ( Iterator iterator = expressedInAnnotations.iterator(); iterator.hasNext() && null == expressedIn; ) {
                    ExpressedInTag tmp = (ExpressedInTag) iterator.next();
                    if ( tmp.getProteinInteractorID().equals( id ) ) {
                        expressedIn = tmp;
                    }
                }
            }

            // read the confidence tag, eg. <confidence unit="Hybrigenics PBS(r)" value="A"/>
            final Element confidenceElement = DOMUtil.getFirstElement( participantElement, "confidence" );
            ConfidenceTag confidence = null;
            if ( confidenceElement != null ) {
                confidence = ConfidenceParser.process( confidenceElement );
            }
            // TODO do something with it or remove it !!!


            // read the <isTaggedProtein>
            final Element isTaggedProteinElement = DOMUtil.getFirstElement( participantElement, "isTaggedProtein" );
            // TODO does that make a difference if it is true, false or unspecified ?
            Boolean isTaggedProtein = null;
            if ( isTaggedProteinElement != null ) {
                String booleanValue = DOMUtil.getSimpleElementText( isTaggedProteinElement );
                booleanValue = booleanValue.trim();
                if ( booleanValue.equalsIgnoreCase( "true" ) ) {
                    isTaggedProtein = Boolean.TRUE;
                } else if ( booleanValue.equalsIgnoreCase( "false" ) ) {
                    isTaggedProtein = Boolean.FALSE;
                } else {
                    // it contains something else than true or false, error

                }
            }

            // read the isOverexpressedProtein
            final Element isOverexpressedProteinElement = DOMUtil.getFirstElement( participantElement, "isOverexpressedProtein" );
            // TODO does that make a difference if it is true, false or unspecified ?
            Boolean isOverexpressedProtein = null;
            if ( isTaggedProteinElement != null ) {
                String booleanValue = DOMUtil.getSimpleElementText( isOverexpressedProteinElement );
                booleanValue = booleanValue.trim();
                if ( booleanValue.equalsIgnoreCase( "true" ) ) {
                    isOverexpressedProtein = Boolean.TRUE;
                } else if ( booleanValue.equalsIgnoreCase( "false" ) ) {
                    isOverexpressedProtein = Boolean.FALSE;
                } else {
                    // it contains something else than true or false, error

                }
            }

            try {

                // TODO update signature: add confidence tag

                participants.add( new ProteinParticipantTag( interactor,
                                                             role,
                                                             expressedIn,
                                                             features,
                                                             isTaggedProtein,
                                                             isOverexpressedProtein ) );
            } catch ( IllegalArgumentException e ) {
                MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
            }
        } // interactors


        // get the interaction type
        // CAUTION - MAY NOT BE THERE
        final Element interactionTypeElement = DOMUtil.getFirstElement( root, "interactionType" );
        InteractionTypeTag interactionType = null;
        if ( interactionTypeElement != null ) {
            interactionType = InteractionTypeParser.process( interactionTypeElement );
        } else {
            // the PSI file lacks an InteractionType, check if the user requested us to put a
            // default value in such case.
            if ( CommandLineOptions.getInstance().hasDefaultInteractionType() ) {
                final String defaultInteractionType = CommandLineOptions.getInstance().getDefaultInteractionType();
                if ( DEBUG ) {
                    System.err.println( "Give a default interactionType: " + defaultInteractionType );
                }
                final XrefTag type = new XrefTag( XrefTag.PRIMARY_REF, defaultInteractionType, "psi-mi" );
                interactionType = new InteractionTypeTag( type );
            }
        }


        // get eventual confidence data
        // CAUTION - MAY NOT BE THERE
        final Element confidenceElement = DOMUtil.getFirstElement( root, "confidence" );
        ConfidenceTag confidence = null;
        if ( confidenceElement != null ) {
            // <confidence unit="arbitrary" value="high"/>
            confidence = new ConfidenceTag( confidenceElement.getAttribute( "unit" ),
                                            confidenceElement.getAttribute( "value" ) );
        }


        // Create the interaction
        InteractionTag interactionTag = null;
        try {
            interactionTag = new InteractionTag( shortLabel,
                                                 fullName,
                                                 localExperiments,
                                                 participants,
                                                 interactionType,
                                                 xrefs,
                                                 annotations,
                                                 confidence );

        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
        }

        return interactionTag;
    }
}
