// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;

/**
 * Convert an IntAct Annotation into PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Annotation2xml {

    public static final String ATTRIBUTE_LIST_NODE_NAME = "attributeList";
    public static final String ATTRIBUTE_NODE_NAME = "attribute";
    public static final String NAME = "name";

    public static final String AVAILABILITY_NODE_NAME = "availability";
    public static final String AVAILABILITY_ID = "id";


    /**
     * Export the Annotation to PSI XML. <br> The decision of exporting it or not is delegated to the
     * UserSessionDownload that should have been setup accordingly.
     *
     * @param session    pre-initialized user environment.
     * @param parent     dom element on which the Attribute will be added.
     * @param annotation the annotation object to add in psi format
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload#addAnnotationFilter(uk.ac.ebi.intact.model.CvTopic)
     * @see uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload#isExportable(uk.ac.ebi.intact.model.Annotation)
     */
    public static Element createAttribute( UserSessionDownload session, Element parent, Annotation annotation ) {

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( annotation == null ) {
            throw new IllegalArgumentException( "You must give a non null Annotation." );
        }

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null parent Element." );

        } else {

            if ( !ATTRIBUTE_LIST_NODE_NAME.equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "The parent term (" + parent.getNodeName() + ") should be <" + ATTRIBUTE_LIST_NODE_NAME + ">." );
            }
        }

        Element element = null;

        // check that the Annotation is exportable.
        if ( session.isExportable( annotation ) ) {

            String sText = annotation.getAnnotationText();
            if ( null == sText ) {
                sText = "";
            }

            element = session.createElement( ATTRIBUTE_NODE_NAME );
            Text text = session.createTextNode( sText );
            element.appendChild( text );

            String name = null;
            if ( annotation.getCvTopic() != null ) {
                name = annotation.getCvTopic().getShortLabel();
            } else {
                name = "";
            }

            element.setAttribute( NAME, name );

            // append the new alement to the given parent.
            parent.appendChild( element );
        }

        return element;
    }

    /**
     * Export the Annotation to PSI XML. <br> The decision of exporting it or not is delegated to the
     * UserSessionDownload that should have been setup accordingly.
     *
     * @param session    pre-initialized user environment.
     * @param parent     dom element on which the Attribute will be added.
     * @param annotation the annotation object to add in psi format
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload#addAnnotationFilter(uk.ac.ebi.intact.model.CvTopic)
     * @see uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload#isExportable(uk.ac.ebi.intact.model.Annotation)
     */
    public static Element createAvailability( UserSessionDownload session,
                                              AnnotatedObject annotatedObject,
                                              Annotation annotation ) {

        Element parent = session.getAvailabilityListElement();

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( annotation == null ) {
            throw new IllegalArgumentException( "You must give a non null Annotation." );
        }

        if ( CvTopic.COPYRIGHT.equals( annotation.getCvTopic().getShortLabel() ) ) {
            throw new IllegalArgumentException( "The given annotation should have a CvTopic(" + CvTopic.COPYRIGHT + ")" );
        }

        Element element = null;

        String sText = annotation.getAnnotationText();
        if ( null == sText ) {
            sText = "";
        }

        element = session.createElement( AVAILABILITY_NODE_NAME );
        Text text = session.createTextNode( sText );
        element.appendChild( text );

        // set the id, should be ac
        String id = annotatedObject.getAc();
        if ( id == null ) {
            id = "" + annotatedObject.hashCode();
        }
        element.setAttribute( AVAILABILITY_ID, id );

        // append the new alement to the given parent.
        parent.appendChild( element );

        return element;
    }


}