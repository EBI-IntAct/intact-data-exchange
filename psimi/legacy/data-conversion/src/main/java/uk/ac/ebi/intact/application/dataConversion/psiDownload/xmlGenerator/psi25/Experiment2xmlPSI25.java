// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlI;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;

import java.util.*;

/**
 * Process the common behaviour of an IntAct Experiment when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Experiment2xmlPSI25 extends AnnotatedObject2xmlPSI25 implements Experiment2xmlI {

    ///////////////////////////
    // Constants

    public static final Collection attributeListFilter = new ArrayList( 3 );

    static {
        attributeListFilter.add( CvTopic.CONFIDENCE_MAPPING );
        attributeListFilter.add( CvTopic.COPYRIGHT );
    }


    /**
     * List of all parents term allowed.
     */
    protected final static Set PARENT_TERM_NAMES = new HashSet();

    static {
        PARENT_TERM_NAMES.add( "experimentList" );
        PARENT_TERM_NAMES.add( "proteinExperimentalForm" );
        PARENT_TERM_NAMES.add( "inferredInteraction" );
        PARENT_TERM_NAMES.add( "experimentRefList" );
    }

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Experiment2xmlPSI25 ourInstance = new Experiment2xmlPSI25();

    public static Experiment2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Experiment2xmlPSI25() {
    }

    //////////////////////////
    // Encapsulated methods

    /**
     * Generate and add to the given element the Xrefs of the experiment (but no pubmed). <br> The given set of pubmed
     * Xref is used to filter those not to generate again.
     *
     * @param session
     * @param element     The element to which we add the xref tag and its content.
     * @param experiment  the IntAct experiment from which we get the Xrefs.
     * @param pubmedXrefs the Xrefs that have already been generated. Is not null, but can be empty.
     */
    private void createExperimentXrefs( UserSessionDownload session,
                                        Element element,
                                        Experiment experiment,
                                        Collection pubmedXrefs ) {

        Experiment2xmlCommons.getInstance().createExperimentXrefs( session, element, experiment, pubmedXrefs );
    }

    /**
     * generate the bibliographical reference of the Experiment out of the Xref having the CvDatabase pubmed. We
     * distinguish primary and secondaryRef using the CvXrefQualifier of the Xref: primary-reference gives the
     * primaryRef and see-also gives the secondaryRef.
     *
     * @param session
     * @param parent     the element to which we will add the bibRef.
     * @param experiment the experiment from which we get the Xrefs.
     *
     * @return the subset of the Experiment's Xref out of which we have generated the bibRef. Never null but can be
     *         empty.
     */
    private Collection createBibRef( UserSessionDownload session,
                                     Element parent,
                                     Experiment experiment ) {

        // TODO in PSI2 we can have an attributeList attached to the bibRef
        return Experiment2xmlCommons.getInstance().createBibRef( session, parent, experiment );
    }

    /**
     * get the value what will be used as ID of the experiment.
     *
     * @param experiment the experiment for which we need an ID.
     *
     * @return the ID of the experiment.
     */
    private String getExperimentId( UserSessionDownload session, Experiment experiment ) {

        long id = session.getExperimentIdentifier( experiment );
        return "" + id;
    }

    /////////////////////
    // Private methods

    private void ckeckExperimentParentName( Element parent ) {

        if ( !PARENT_TERM_NAMES.contains( parent.getNodeName() ) ) {

            StringBuffer sb = new StringBuffer( 128 );
            sb.append( "The given parent term (" );
            sb.append( parent.getNodeName() );
            sb.append( ") is not one of the following: " );

            if ( PARENT_TERM_NAMES.isEmpty() ) {
                sb.append( "none specified." );
            }

            for ( Iterator iterator = PARENT_TERM_NAMES.iterator(); iterator.hasNext(); ) {
                String name = (String) iterator.next();

                sb.append( '<' ).append( name ).append( '>' );

                if ( iterator.hasNext() ) {
                    sb.append( ',' ).append( ' ' );
                }
            }

            throw new IllegalArgumentException( sb.toString() );
        }
    }

    ////////////////////
    // Public methods

    /**
     * Generated an experimentDescription out of an IntAct Experiment.
     *
     * @param session
     * @param parent     the Element to which we will add the experimentDescription.
     * @param experiment the IntAct experiment that we convert to PSI.
     *
     * @return the generated experimentDescription Element.
     */
    public Element createReference( UserSessionDownload session,
                                    Element parent,
                                    Experiment experiment ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + EXPERIMENT_REF_TAG_NAME + "." );
        } else {

            ckeckExperimentParentName( parent );
        }

        if ( experiment == null ) {
            throw new IllegalArgumentException( "You must give a non null Experiment to build an " + EXPERIMENT_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( EXPERIMENT_REF_TAG_NAME );
        Text refText = session.createTextNode( getExperimentId( session, experiment ) );
        element.appendChild( refText );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generated an experimentDescription out of an IntAct Experiment.
     *
     * @param session
     * @param parent     the Element to which we will add the experimentDescription.
     * @param experiment the IntAct experiment that we convert to PSI.
     *
     * @return the generated experimentDescription Element.
     */
    public Element create( UserSessionDownload session,
                           Element parent,
                           Experiment experiment ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an experimentDescription." );
        } else {

            if ( !"experimentList".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <experimentList> to build a " + EXPERIMENT_DESCRIPTION_TAG_NAME + "." );
            }

        }

        if ( experiment == null ) {
            throw new IllegalArgumentException( "You must give a non null Experiment to build an experimentDescription." );
        }

        // Note: children terms are:
        //       names bibref xref hostOrganism interactionDetectionMethod participantIdentificationMethod
        //       featureDetectionMethod confidenceList attributeList

        //       names
        //       bibref
        //       xref
        //       hostOrganismList
        //       interactionDetectionMethod
        //       participantIdentificationMethod
        //       featureDetectionMethod
        //       sampleProcess                    -- should be deleted 
        //       confidenceList
        //       attributeList

        // 2. Initialising the element...
        Element element = session.createElement( EXPERIMENT_DESCRIPTION_TAG_NAME );
        element.setAttribute( "id", getExperimentId( session, experiment ) );

        // 3. Generating names...
        createNames( session, element, experiment );

        // 4. Generating bibRef (if any)...
        // TODO what if no pubmed Xref ... no bibRef section ?
        // TODO and an attributeList is now attached to the bibRef
        Collection pubmedXrefs = createBibRef( session, element, experiment );

        // 5. Generating xref (if any)...
        createExperimentXrefs( session, element, experiment, pubmedXrefs );

        // 6. Generating hostOrganismList...
        Element bioSourceListElement = session.createElement( "hostOrganismList" );
        element.appendChild( bioSourceListElement );
        BioSource2xmlFactory.getInstance( session ).createHostOrganism( session, bioSourceListElement, experiment.getBioSource() );

        // 7. Generating OPTIONAL interactionDetectionMethod ... mapped to Experiment.CvInteraction.
        if ( experiment.getCvInteraction() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, experiment.getCvInteraction() );
        }

        // 8. Generating OPTIONAL participantIdentificationMethod ... mapped to Experiment.CvIdentification.
        // note: it was called participantDetection in PSI1
        if ( experiment.getCvIdentification() != null ) {
            // participantDetectionMethod !!
            CvObject2xmlFactory.getInstance( session ).create( session, element, experiment.getCvIdentification() );
        }

        // 9. Generating OPTIONAL featureDetectionMethod...
        // note: it was called featureDetection in PSI1
        // actually, we have (so far) nothing like that in the IntAct Experiment object ... skip that tag.

        // 10. Generating confidenceList
        createConfidence( session, element, experiment );

        // 11. Generating attributeList (if any)...
        if ( ! experiment.getAnnotations().isEmpty() ) {
            createAttributeList( session, element, experiment, attributeListFilter );
        }

        // 12. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}