// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlI;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;

import java.util.Collection;
import java.util.Iterator;

/**
 * Process the common behaviour of an IntAct Experiment when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Feature2xmlPSI25 extends AnnotatedObject2xmlPSI25 implements Feature2xmlI {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Feature2xmlPSI25 ourInstance = new Feature2xmlPSI25();
    private static final String PROTEIN_FEATURE_REF_TAG_NAME = "proteinFeatureRef";
    private static final String DNA_FEATURE_REF_TAG_NAME = "dnaFeatureRef";
    private static final String RNA_FEATURE_REF_TAG_NAME = "rnaFeatureRef";
    private static final String PARTICIPANT_FEATURE_REF_NAME = "participantFeatureRef";

    public static Feature2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Feature2xmlPSI25() {
    }

    ////////////////////////////
    // Encapsulated methods

    /**
     * Generate and add to the given element the Xrefs of the feature.
     *
     * @param session
     * @param parent  The element to which we add the xref tag and its content.
     * @param feature the IntAct feature from which we get the Xrefs.
     */
    private static void createFeatureXrefs( UserSessionDownload session, Element parent, Feature feature ) {

        Feature2xmlCommons.getInstance().createFeatureXrefs( session, parent, feature );
    }

    //////////////////////
    // Public method

    public Element createReference( UserSessionDownload session, Element parent, Feature feature ) {

        // TODO test that.

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build a " +
                                                PROTEIN_FEATURE_REF_TAG_NAME + " / " +
                                                DNA_FEATURE_REF_TAG_NAME + " / " +
                                                RNA_FEATURE_REF_TAG_NAME + "." );
        } else {

            if ( !"participant".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <participant> to build a " +
                                                    PROTEIN_FEATURE_REF_TAG_NAME + " / " +
                                                    DNA_FEATURE_REF_TAG_NAME + " / " +
                                                    RNA_FEATURE_REF_TAG_NAME + "." + "." );
            }
        }

        // 2. Initialising the element...
        Element element = session.createElement( PARTICIPANT_FEATURE_REF_NAME );
        Text refText = session.createTextNode( "" + session.getFeatureIdentifier( feature ) );
        element.appendChild( refText );

//        element.setAttribute( "ref", "" + session.getFeatureIdentifier( feature ) );

        // 3. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }

    /**
     * Generated an feature out of an IntAct Feature.
     * <pre>
     * Rules:
     * <p/>
     * Feature.CvFeatureIdentification is mapped to featureDetection
     * Feature.xref                    is mapped to xref                     // interpro
     * Feature.CvFeatureType           is mapped to featureDescription.xref
     * </pre>
     *
     * @param session
     * @param parent  the Element to which we will add the feature.
     * @param feature the IntAct Feature that we convert to PSI.
     */
    public void create( UserSessionDownload session, Element parent, Feature feature ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent to build an " + FEATURE_TAG_NAME + "." );
        } else {

            if ( !PARENT_TAG_NAME.equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a <" + PARENT_TAG_NAME + "> to build a " + FEATURE_TAG_NAME + "." );
            }
        }

        if ( feature == null ) {
            throw new IllegalArgumentException( "You must give a non null Feature to build an " + FEATURE_TAG_NAME + "." );
        }

        // NOTE: children terms are:
        //       names xref featureType featureDetectionMethod location

        // 2. Initialising the element...
        Element element = session.createElement( FEATURE_TAG_NAME );
        element.setAttribute( "id", "" + session.getFeatureIdentifier( feature ) );

        // 3. Generating names...
        createNames( session, element, feature );

        // 4. Generating xref...
        createFeatureXrefs( session, element, feature );

        // 5. Generating featureType...
        if ( feature.getCvFeatureType() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, feature.getCvFeatureType() );
        } else {
            // TODO Error reporting in the Session.
            System.err.println( "There should be a CvFeatureType in that Feature( " + feature.getAc() + " )." );
        }

        // 6. Generating featureDetection...
        if ( feature.getCvFeatureIdentification() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, feature.getCvFeatureIdentification() );
        }

        // 7. Generating locations
        Collection ranges = feature.getRanges();
        if ( false == ranges.isEmpty() ) {
            Element locationListElement = session.createElement( "featureRangeList" );
            element.appendChild( locationListElement );

            for ( Iterator iterator = ranges.iterator(); iterator.hasNext(); ) {
                Range range = (Range) iterator.next();

                Range2xmlPSI25.getInstance().create( session, locationListElement, range );
            }
        }

        // 8. Attaching the newly created element to the parent...
        parent.appendChild( element );
    }
}