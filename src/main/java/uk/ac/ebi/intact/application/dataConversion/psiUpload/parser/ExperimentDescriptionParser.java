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
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.LabelValueBean;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentDescriptionParser {

    private ExperimentListParser experimentList;
    private Element root;

    /**
     * @param experimentList global context of that experiment
     * @param root           the XML to extract the data from.
     */
    public ExperimentDescriptionParser( final ExperimentListParser experimentList,
                                        final Element root ) {

        this.experimentList = experimentList;
        this.root = root;
    }

    /**
     * Extract data from an experimentDescription Element and produce an Intact Experiment
     * <p/>
     * <pre>
     *   <experimentDescription id="xray">
     *      <names>
     *         <shortLabel>X-Ray cristallography</shortLabel>
     *      </names>
     * <p/>
     *      <bibref>
     *         <xref>
     *            <primaryRef db="Pubmed" id="1549776"/>
     *         </xref>
     *      </bibref>
     * <p/>
     *      <interactionDetection>
     *         <names>
     *            <shortLabel>X-Ray</shortLabel>
     *         </names>
     * <p/>
     *         <xref>
     *            <primaryRef db="PSI-MI" id="MI:0018"/>
     *         </xref>
     *      </interactionDetection>
     *   </experimentDescription>
     * </pre>
     *
     * @return a map: id --> Experiment to use.
     *
     * @see uk.ac.ebi.intact.model.Experiment
     */
    public LabelValueBean process() {

        ExperimentDescriptionTag experiment = null;

        if ( false == "experimentDescription".equals( root.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, "ERROR - We should be in experimentDescription tag." ) );
        }

        final String id = root.getAttribute( "id" );

        // check if the experiment is existing in the global context
        if ( experimentList != null ) {
            experiment = (ExperimentDescriptionTag) experimentList.getExperiments().get( id );
            if ( experiment != null ) {
                final String msg = "WARNING - the experiment id: " + id + "is defined several times. " +
                                   "The global definition will be used instead.";
                MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );
                return new LabelValueBean( id, experiment );
            }
        }


        // CAUTION - MAY NOT BE THERE
        final Element names = DOMUtil.getFirstElement( root, "names" );
        String shortLabel = null;
        String fullName = null;
        if ( names != null ) {
            shortLabel = DOMUtil.getShortLabel( names );
            fullName = DOMUtil.getFullName( names );
        } else {
            // no hostOrganism
            final String msg = "Can't create an Experiment without a name !";
            MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );
        }

        // CAUTION - MAY NOT BE THERE
        final Element biosourceElement = DOMUtil.getFirstElement( root, "hostOrganism" );
        HostOrganismTag hostOrganism = null;
        if ( biosourceElement != null ) {
            hostOrganism = HostOrganismParser.process( biosourceElement );
        } else {
            // no hostOrganism
            final String msg = "Can't create an Experiment without a BioSource ! We'll check in the Protein";
            MessageHolder.getInstance().addParserMessage( new Message( root, msg ) );
        }


        // CAUTION - MAY NOT BE THERE
        final Element bibref = DOMUtil.getFirstElement( root, "bibref" );
        XrefTag bibXref = null;
        Collection secondaryBibXrefs = null;
        if ( bibref != null ) {
            final Element bibXrefElement = DOMUtil.getFirstElement( bibref, "xref" );
            bibXref = XrefParser.processPrimaryRef( bibXrefElement );
            secondaryBibXrefs = XrefParser.processSecondaryRef( bibXrefElement );
        }


        // xrefs - CAUTION - MAY NOT BE THERE
        final Element xrefElement = DOMUtil.getFirstElement( root, "xref" );
        XrefTag primaryXref = null;
        Collection secondaryXrefs = null;
        Collection xrefs = null;
        if ( xrefElement != null ) {
            primaryXref = XrefParser.processPrimaryRef( xrefElement );
            secondaryXrefs = XrefParser.processSecondaryRef( xrefElement );

            // Build the xrefs collection
            xrefs = new ArrayList( secondaryXrefs.size() + ( primaryXref == null ? 0 : 1 ) );
            xrefs.add( primaryXref );
            xrefs.addAll( secondaryXrefs );
        }


        final Element interactionDetectionElement = DOMUtil.getFirstElement( root, "interactionDetection" );
        InteractionDetectionTag interactionDetection = null;
        if ( interactionDetectionElement != null ) {

            // CAUTION - MAY NOT BE THERE - MAY BE A LIST
            final Element interactionDetectionXrefElement = DOMUtil.getFirstElement( interactionDetectionElement, "xref" );
            if ( null != interactionDetectionXrefElement ) {
                XrefTag interactionXref = XrefParser.getXrefByDb( interactionDetectionXrefElement,
                                                                  CvDatabase.PSI_MI );
                try {
                    interactionDetection = new InteractionDetectionTag( interactionXref );
                } catch ( IllegalArgumentException e ) {
                    MessageHolder.getInstance().addParserMessage( new Message( interactionDetectionXrefElement,
                                                                               e.getMessage() ) );
                }
            }
        }


        // CAUTION - MAY NOT BE THERE
        final Element participantDetectionElement = DOMUtil.getFirstElement( root, "participantDetection" );
        ParticipantDetectionTag participantDetection = null;
        if ( participantDetectionElement != null ) {
            // CAUTION - MAY NOT BE THERE
            final Element participantDetectionXrefElement = DOMUtil.getFirstElement( participantDetectionElement, "xref" );
            if ( null != participantDetectionXrefElement ) {
                XrefTag participantXref = XrefParser.getXrefByDb( participantDetectionXrefElement,
                                                                  CvDatabase.PSI_MI );
                try {
                    participantDetection = new ParticipantDetectionTag( participantXref );
                } catch ( IllegalArgumentException e ) {
                    MessageHolder.getInstance().addParserMessage( new Message( participantDetectionXrefElement,
                                                                               e.getMessage() ) );
                }
            }
        }


        // get eventual annotations
        // CAUTION - MAY NOT BE THERE
        final Element annotationElement = DOMUtil.getFirstElement( root, "attributeList" );
        Collection annotations = null;
        if ( annotationElement != null ) {
            final NodeList someAttributes = annotationElement.getElementsByTagName( "attribute" );
            final int count = someAttributes.getLength();
            annotations = new ArrayList( count );

            for ( int i = 0; i < count; i++ ) {
                final Node entryNode = someAttributes.item( i );
                AnnotationTag annotation = AnnotationParser.process( (Element) entryNode );
                // only add annotation with text
                if ( annotation.hasText() ) {
                    annotations.add( annotation );
                }
            } // attributes
        }


        // create the experiment
        try {
            experiment = new ExperimentDescriptionTag( shortLabel,
                                                       fullName,
                                                       bibXref,
                                                       secondaryBibXrefs,
                                                       xrefs,
                                                       annotations,
                                                       hostOrganism,
                                                       interactionDetection,
                                                       participantDetection );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
        }

        return new LabelValueBean( id, experiment );
    }
}
