// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Experiment2xmlI;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Experiment;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Process the common behaviour of an IntAct Experiment when exporting PSI version 1.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Experiment2xmlPSI1 extends AnnotatedObject2xmlPSI1 implements Experiment2xmlI {

    //////////////////////////
    // Constants

    public static final Collection attributeListFilter = new ArrayList( 2 );

    static {
        attributeListFilter.add( CvTopic.CONFIDENCE_MAPPING );
        attributeListFilter.add( CvTopic.COPYRIGHT );
    }

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Experiment2xmlPSI1 ourInstance = new Experiment2xmlPSI1();

    public static Experiment2xmlPSI1 getInstance() {
        return ourInstance;
    }

    private Experiment2xmlPSI1() {
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

        return Experiment2xmlCommons.getInstance().createBibRef( session, parent, experiment );
    }

    /**
     * get the value what will be used as ID of the experiment.
     *
     * @param experiment the experiment for which we need an ID.
     *
     * @return the ID of the experiment.
     */
    private String getExperimentId( Experiment experiment ) {

        return Experiment2xmlCommons.getInstance().getExperimentId( experiment );
    }

    //////////////////////
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

            if ( !"experimentList".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <experimentList> to build a " + EXPERIMENT_REF_TAG_NAME + "." );
            }

        }

        if ( experiment == null ) {
            throw new IllegalArgumentException( "You must give a non null Experiment to build an " + EXPERIMENT_REF_TAG_NAME + "." );
        }

        // 2. Initialising the element...
        Element element = session.createElement( EXPERIMENT_REF_TAG_NAME );
        element.setAttribute( "ref", getExperimentId( experiment ) );

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
        //       names bibref xref hostOrganism interactionDetection participantDetection featureDetection confidence attributeList

        // 2. Initialising the element...
        Element element = session.createElement( EXPERIMENT_DESCRIPTION_TAG_NAME );
        element.setAttribute( "id", getExperimentId( experiment ) );

        // 3. Generating names...
        createNames( session, element, experiment );

        // 4. Generating bibRef (if any)...
        // TODO what if no pubmed Xref ... no bibRef section ?
        Collection pubmedXrefs = createBibRef( session, element, experiment );

        // 5. Generating xref (if any)...
        createExperimentXrefs( session, element, experiment, pubmedXrefs );

        // 6. Generating hostOrganism...
        BioSource2xmlFactory.getInstance( session ).createHostOrganism( session, element, experiment.getBioSource() );

        // 7. Generating OPTIONAL interactionDetection ... mapped to Experiment.CvInteraction.
        if ( experiment.getCvInteraction() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, experiment.getCvInteraction() );
        }

        // 8. Generating OPTIONAL participantDetection ... mapped to Experiment.CvIdentification.
        if ( experiment.getCvIdentification() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, experiment.getCvIdentification() );
        }

        // 9. Generating OPTIONAL featureDetection...
        // actually, we have (so far) nothing like that in the IntAct Experiment object ... skip that tag.

        // 10. Generating OPTIONAL confidence...
        createConfidence( session, element, experiment );

        // 12. Generating attributeList (if any)...
        if ( false == experiment.getAnnotations().isEmpty() ) {
            createAttributeList( session, element, experiment, attributeListFilter );
        }

        // 12. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}