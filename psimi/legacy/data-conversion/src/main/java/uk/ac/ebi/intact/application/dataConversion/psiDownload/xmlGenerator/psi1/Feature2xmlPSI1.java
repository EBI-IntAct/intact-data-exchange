// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Feature2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;

import java.util.Collection;
import java.util.Iterator;

/**
 * Process the common behaviour of an IntAct Feature when exporting PSI version 1.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Feature2xmlPSI1 extends AnnotatedObject2xmlPSI1 implements Feature2xmlI {

    ////////////////////////////
    // Constants

    public static final String NO_RANGE = "-1";

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Feature2xmlPSI1 ourInstance = new Feature2xmlPSI1();

    public static Feature2xmlPSI1 getInstance() {
        return ourInstance;
    }

    private Feature2xmlPSI1() {
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

    ////////////////////////
    // Private methods

    /**
     * That method is used to cope with the fact that IntAct Feature might not have a Range, though PSI 1 requires a
     * location, hence we generate a dummy one + a comment as a work around.
     *
     * @param session
     * @param parent
     */
    private void createUndeterminedLocation( UserSessionDownload session,
                                             Element parent,
                                             Element featureIdentificationElement ) {

        // TODO use the underlying interactor and put range 1 .. n instead of -1 .. -1

        Element locationElement = session.createElement( "location" );

        // Processing begin
        Element beginElement = session.createElement( "begin" );
        beginElement.setAttribute( "position", NO_RANGE );
        locationElement.appendChild( beginElement );

        // Processing end
        Element endElement = session.createElement( "end" );
        endElement.setAttribute( "position", NO_RANGE );
        locationElement.appendChild( endElement );

        Comment comment = session.getPsiDocument().createComment( "The location(-1, -1) is volontary wrong in order to reflect its abscence from the related IntAct Feature." );
        beginElement.getParentNode().insertBefore( comment, beginElement );

        // Attaching the newly created element to the parent...
        if ( featureIdentificationElement == null ) {
            parent.appendChild( locationElement );
        } else {
            // we are looking for the featureDetection tag and insert the location before.
            Collection c = DOMUtil.getDirectElementsByTagName( parent, featureIdentificationElement.getNodeName() );
            parent.insertBefore( locationElement, (Node) c.iterator().next() );
        }
    }

    private void createLocation( UserSessionDownload session,
                                 Element parent,
                                 Range range,
                                 Element featureIdentificationElement ) {

        Element locationElement = session.createElement( "location" );

        // Processing begin
        Element beginElement = null;

        if ( range.getFromIntervalStart() == range.getFromIntervalEnd() ) {

            // build a begin tag
            beginElement = session.createElement( "begin" );
            beginElement.setAttribute( "position", "" + range.getFromIntervalEnd() );

        } else {

            // build a beginInterval tag
            beginElement = session.createElement( "beginInterval" );
            beginElement.setAttribute( "begin", "" + range.getFromIntervalStart() );
            beginElement.setAttribute( "end", "" + range.getFromIntervalEnd() );
        }

        locationElement.appendChild( beginElement );

        // Processing end
        Element endElement = null;

        if ( range.getToIntervalStart() == range.getToIntervalEnd() ) {

            // build a begin tag
            endElement = session.createElement( "end" );
            endElement.setAttribute( "position", "" + range.getToIntervalEnd() );

        } else {

            // build a beginInterval tag
            endElement = session.createElement( "endInterval" );
            endElement.setAttribute( "begin", "" + range.getToIntervalStart() );
            endElement.setAttribute( "end", "" + range.getToIntervalEnd() );
        }

        locationElement.appendChild( endElement );

        // Attaching the newly created element to the parent...
        if ( featureIdentificationElement == null ) {
            parent.appendChild( locationElement );
        } else {
            // we are looking for the featureDetection tag and insert the location before.
            Collection c = DOMUtil.getDirectElementsByTagName( parent, featureIdentificationElement.getNodeName() );
            parent.insertBefore( locationElement, (Node) c.iterator().next() );
        }
    }

    //////////////////////
    // Public method

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

        // Note: as PSIv1 doesn't handle Feature having 0..n Ranges we have to implement a work around:
        //       We will generate one PSI feature per Range and add an additional xref in the Feature
        //       to allow a clustering of those ranges. The feature definition will be the same amongst
        //       the multiple instances.
        //       A strategy could be to create a firt feature Element without location. Let's say there are 3
        //       Ranges, we then clone that feature element 2 times and add the corresponding locations/clusterID to them.

        // NOTE: children terms are:
        //       xref featureDescription location featureDetection

        // 2. Initialising the element...
        Element element = session.createElement( FEATURE_TAG_NAME );

        // 3. Generating xref...
        createFeatureXrefs( session, element, feature );

        // 4. Generating featureDescription...
        if ( feature.getCvFeatureType() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, feature.getCvFeatureType() );
        } else {
            System.err.println( "There should be a CvFeatureType in that Feature( " + feature.getAc() + " )." );
        }

        // 5. Generating featureDetection...
        Element featureDetectionElement = null;
        if ( feature.getCvFeatureIdentification() != null ) {
            CvObject2xmlI cv = CvObject2xmlFactory.getInstance( session );
            featureDetectionElement = cv.create( session, element, feature.getCvFeatureIdentification() );
        }

        // TODO multiranges case ... cf. PsiDataLoader
        Collection ranges = feature.getRanges();

        if ( ranges.isEmpty() ) {

            // 5a. In PSI 1, the location is a mandatory element, hence we need to create a Range (-1, -1) + explainatory comment
            createUndeterminedLocation( session, element, featureDetectionElement );

            // 5b. Attaching the newly created element to the parent...
            parent.appendChild( element );

        } else {

            String clusterID = null;
            if ( ranges.size() > 1 ) {
                // a cluster ID is needed.
                clusterID = FeatureTag.FEATURE_CLUSTER_ID_PREFIX + session.getNextClusterIdSuffix();
            }

            // 5b. Generating location...
            int i = 1;
            for ( Iterator iterator = ranges.iterator(); iterator.hasNext(); ) {
                Range range = (Range) iterator.next();

                Element featureElement = null;
                Element featureDetection2Element = null;
                if ( iterator.hasNext() ) {
                    // clone the current feature and use the clone.
                    featureElement = (Element) element.cloneNode( true );
                    if ( featureDetectionElement != null ) {
                        featureDetection2Element = (Element) featureDetectionElement.cloneNode( true );
                    }
                } else {
                    // last range to be processed, use the element.
                    featureElement = element;
                    if ( featureDetectionElement != null ) {
                        featureDetection2Element = featureDetectionElement;
                    }
                }

                createLocation( session, featureElement, range, featureDetection2Element );

                // add the cluster ID if available
                if ( clusterID != null ) {

                    Element xref = null;
                    String clusterIdElementName = null;

                    // here we need to count the number of tag at the direct level, not a deep search !!
                    // the method Element.getElementsByTagName( String s ) gives more than the direct level of children.
                    final Collection list = DOMUtil.getDirectElementsByTagName( featureElement, "xref" );


                    if ( list.size() == 1 ) {

                        xref = (Element) list.iterator().next();
                        clusterIdElementName = "secondaryRef";

                    } else if ( list.isEmpty() ) {

                        // none, then create the xref tag before
                        xref = session.createElement( "xref" );
                        featureElement.appendChild( xref );
                        clusterIdElementName = "primaryRef";

                    } else {
                        // we should never come here.
                        throw new IllegalStateException();
                    }

                    // Creating the xref
                    Element clusterIdElement = session.createElement( clusterIdElementName );
                    clusterIdElement.setAttribute( "db", FeatureTag.FEATURE_CLUSTER_ID_XREF );
                    clusterIdElement.setAttribute( "id", clusterID );

                    // add the element to the xref
                    xref.appendChild( clusterIdElement );
                }

                // 6. Attaching the newly created element to the parent...
                //    in PSI1 we vreate 1 feature per range.
                parent.appendChild( featureElement );

            } // for all ranges
        } // if any ranges

    }
}