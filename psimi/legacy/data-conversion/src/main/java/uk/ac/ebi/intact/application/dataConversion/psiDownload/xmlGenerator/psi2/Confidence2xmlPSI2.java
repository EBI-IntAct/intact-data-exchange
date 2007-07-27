// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Confidence2xmlI;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implements the tranformation of an IntAct AnnotatedObject's annotation into a confidence in PSI XML version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Confidence2xmlPSI2 extends AnnotatedObject2xmlPSI2 implements Confidence2xmlI {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Confidence2xmlPSI2 ourInstance = new Confidence2xmlPSI2();

    public static Confidence2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private Confidence2xmlPSI2() {
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

        Collection confidences = new ArrayList( 2 ); // 2 will be enough in most cases.

        for ( Iterator iterator = annotatedObject.getAnnotations().iterator(); iterator.hasNext(); ) {
            Annotation annotation = (Annotation) iterator.next();

            if ( cvTopicFilter.equals( annotation.getCvTopic().getShortLabel() ) ) {
                // export it
                confidences.add( annotation );
            }
        }

        Element confidenceListElement = null;
        if ( false == confidences.isEmpty() ) {
            // create the list element and export the annotations...
            confidenceListElement = session.createElement( CONFIDENCE_LIST_TAG_NAME );

            for ( Iterator iterator = confidences.iterator(); iterator.hasNext(); ) {
                Annotation annotation = (Annotation) iterator.next();

                // 1. Create a confidence element
                Element confidenceElement = session.createElement( CONFIDENCE_TAG_NAME );

                // 2. Generating Unit...
                Element unitElement = session.createElement( CONFIDENCE_UNIT_TAG_NAME );
                createNames( session, unitElement, annotation.getCvTopic() );
                confidenceElement.appendChild( unitElement );

                // TODO check if it has MI reference, if so export them as xref of the confidence.unit.

                // 3. Generating value...
                Element valueElement = session.createElement( CONFIDENCE_VALUE_TAG_NAME );
                Text valueTextElement = session.createTextNode( annotation.getAnnotationText() );
                valueElement.appendChild( valueTextElement );
                confidenceElement.appendChild( valueElement );

                // 4. Appending the confidence to the list...
                confidenceListElement.appendChild( confidenceElement );
            }

            // Appending the confidenceList to the parent Element...
            parent.appendChild( confidenceListElement );
        }

        return confidenceListElement;
    }
}