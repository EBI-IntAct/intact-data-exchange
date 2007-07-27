// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Confidence2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2.AnnotatedObject2xmlPSI2;
import uk.ac.ebi.intact.model.*;

import java.util.*;

/**
 * Implements the tranformation of an IntAct AnnotatedObject's annotation into a confidence in PSI XML version 1.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Confidence2xmlPSI1 extends AnnotatedObject2xmlPSI2 implements Confidence2xmlI {

    ///////////////////
    // Inner class
    private static class StringComparator implements Comparator {

        ////////////////////////////////////////////////
        // Implementation of the Comparable interface
        ////////////////////////////////////////////////

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.<p>
         *
         * @param o1 the Object to be compared.
         * @param o2 the Object to compare with.
         *
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object.
         *
         * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
         */
        public final int compare( final Object o1, final Object o2 ) {

            final String s1 = ( (String) o1 ).toLowerCase();
            final String s2 = ( (String) o2 ).toLowerCase();

            // the current string comes first if it's before in the alphabetical order
            if ( !( s1.equals( s2 ) ) ) {
                return s1.compareTo( s2 );
            } else {
                return 0;
            }
        }
    }

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Confidence2xmlPSI1 ourInstance = new Confidence2xmlPSI1();

    public static Confidence2xmlPSI1 getInstance() {
        return ourInstance;
    }

    private Confidence2xmlPSI1() {
    }

    ///////////////////////////
    // Public methods

    /**
     * Convert an annotatedObject annotations in to confidence value in PSI.
     *
     * @param session
     * @param parent          the Element to which we will attach the generated confidence.
     * @param annotatedObject the object from which we get the annotations.
     *
     * @return the generated confidence or null if none could be generated.
     */
    public Element create( UserSessionDownload session, Element parent, AnnotatedObject annotatedObject ) {

        Element element = null;

        if ( annotatedObject instanceof Interaction ) {

            element = create( session, parent, annotatedObject, CvTopic.AUTHOR_CONFIDENCE );

        } else if ( annotatedObject instanceof Experiment ) {

            element = create( session, parent, annotatedObject, CvTopic.CONFIDENCE_MAPPING );

        } else {

            // error, type not supported.
            throw new UnsupportedOperationException( "a confidence can only be generated for an interaction or an experiemnt." );
        }

        return element;
    }

    ///////////////////////////
    // Private methods

    /**
     * Generate a confidence by selecting the annotation having the given CvTopic.shortlabel.
     *
     * @param session
     * @param parent          the parent Element to which we will attach the geenrated Element.
     * @param annotatedObject the object from which we get the annotations.
     * @param cvTopicFilter   the shortlabel of the CvTopic to convert into a confidence.
     *
     * @return the generated confidence element or null if no annotation matching the given filter could be found.
     */
    private Element create( UserSessionDownload session,
                            Element parent,
                            AnnotatedObject annotatedObject,
                            final String cvTopicFilter ) {

        // 1. Select all annotationText where Annotation has the required CvTopic.
        List authorConfidences = null;
        for ( Iterator iterator = annotatedObject.getAnnotations().iterator(); iterator.hasNext(); ) {
            Annotation annotation = (Annotation) iterator.next();

            if ( cvTopicFilter.equals( annotation.getCvTopic().getShortLabel() ) ) {

                if ( authorConfidences == null ) {
                    authorConfidences = new ArrayList( 2 );
                }

                authorConfidences.add( annotation.getAnnotationText() );
            }
        }

        if ( authorConfidences == null ) {
            authorConfidences = Collections.EMPTY_LIST;
        }

        // 2. Choose the confidence value. If more than one available we take the first one in alphanumerical order.
        String confidenceValue = null;
        if ( authorConfidences.size() > 1 ) {

            // Sort the values alphanumerically (non case sensitive) and pick the first one.
            Object[] names = authorConfidences.toArray();

            Arrays.sort( names, new StringComparator() );
            confidenceValue = (String) names[ 0 ];

        } else if ( authorConfidences.size() == 1 ) {

            confidenceValue = (String) authorConfidences.get( 0 );
        }

        Element confidenceElement = null;

        if ( confidenceValue != null ) {

            confidenceElement = session.createElement( CONFIDENCE_TAG_NAME );
            confidenceElement.setAttribute( CONFIDENCE_UNIT_TAG_NAME, cvTopicFilter );
            confidenceElement.setAttribute( CONFIDENCE_VALUE_TAG_NAME, confidenceValue );

            // add it to the parent.
            parent.appendChild( confidenceElement );
        }

        return confidenceElement;
    }
}