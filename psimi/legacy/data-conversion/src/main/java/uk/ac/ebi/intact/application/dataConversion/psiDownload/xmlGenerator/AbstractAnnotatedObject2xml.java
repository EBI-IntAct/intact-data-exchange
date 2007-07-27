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
import uk.ac.ebi.intact.model.Xref;

import java.util.*;

/**
 * Process common attributes of all AnnotatedObject. eg. shortlabel, fullname, Annotations, Xrefs
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractAnnotatedObject2xml {

    //////////////////////
    // Constants

    public static final String AVAILABILITY_NODE_NAME = "availability";


    /**
     * List of all parents term allowed.
     */
    protected final static Set PARENT_TERM_NAMES = new HashSet();

    static {
        PARENT_TERM_NAMES.add( "experimentDescription" );
        PARENT_TERM_NAMES.add( "proteinInteractor" );
        PARENT_TERM_NAMES.add( "organism" );
        PARENT_TERM_NAMES.add( "hostOrganism" );
        PARENT_TERM_NAMES.add( "interaction" );
        PARENT_TERM_NAMES.add( "feature" );
        PARENT_TERM_NAMES.add( "featureDetection" );
        PARENT_TERM_NAMES.add( "featureType" );
        PARENT_TERM_NAMES.add( "cellType" );
        PARENT_TERM_NAMES.add( "compartment" );
        PARENT_TERM_NAMES.add( "tissue" );
        PARENT_TERM_NAMES.add( "interactionDetection" );
        PARENT_TERM_NAMES.add( "participantDetection" );
        PARENT_TERM_NAMES.add( "interactionType" );
        PARENT_TERM_NAMES.add( "featureDescription" );
        PARENT_TERM_NAMES.add( "participantRole" );    // PSI1 & PSI2
        PARENT_TERM_NAMES.add( "experimentalRole" );
        PARENT_TERM_NAMES.add( "experimentalForm" );

        PARENT_TERM_NAMES.add( "unit" ); // this is not an annotatedObject but PSI2 needs to generate names as well

        PARENT_TERM_NAMES.add( CvObject2xmlCommons.FEATURE_DETECTION_METHOD_NODE_NAME );
        PARENT_TERM_NAMES.add( CvObject2xmlCommons.INTERACTION_DETECTION_METHOD_NODE_NAME );
        PARENT_TERM_NAMES.add( CvObject2xmlCommons.PARTICIPANT_IDENTIFICATION_METHOD_NODE_NAME );

        PARENT_TERM_NAMES.add( "interactor" );     // PSI 2.5
        PARENT_TERM_NAMES.add( "interactorType" ); // PSI 2.5
        PARENT_TERM_NAMES.add( "biologicalRole" ); // PSI 2.5
        PARENT_TERM_NAMES.add( "startStatus" );    // PSI 2.5
        PARENT_TERM_NAMES.add( "endStatus" );      // PSI 2.5
    }

    protected void ckeckParentName( Element parent ) {

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

                sb.append( "<" + name + ">" );

                if ( iterator.hasNext() ) {
                    sb.append( "," ).append( " " );
                }
            }

            throw new IllegalArgumentException( sb.toString() );
        }
    }

    /////////////////////////
    // Abstract mathods

    /**
     * Convert shortlabel and fullname of an AnnotatedObject into PSI XML. <br> The generated tags are attached to the
     * given parent.
     *
     * @param session pre-initialised user's session.
     * @param parent  the tag to which we attached the generated Data.
     * @param object  the AnnotatedObject from which we are converting the shortlabel and fullname.
     *
     * @return the generated data.
     */
    public abstract Element createNames( UserSessionDownload session, Element parent, AnnotatedObject object );

    /////////////////////////////////////
    // Methods common to all subclasses

    /**
     * Create a shortlabel element containing the shortlabel of the given AnnotatatedObject.
     *
     * @param session
     * @param parent  the names element, not null.
     * @param object  the annotatedObject from which we get the shortlabel, not null.
     */
    protected void createShortlabel( UserSessionDownload session, Element parent, AnnotatedObject object ) {
        // shortlabel is mandatory in the IntAct API.
        Element shortlabel = session.createElement( "shortLabel" );
        Text shortlabelText = session.createTextNode( object.getShortLabel() );
        shortlabel.appendChild( shortlabelText );
        parent.appendChild( shortlabel );
    }

    /**
     * Create a fullname element containing the fullname of the given AnnotatatedObject.
     *
     * @param session
     * @param parent  the names element, not null.
     * @param object  the annotatedObject from which we get the fullname, not null.
     */
    protected void createFullname( UserSessionDownload session, Element parent, AnnotatedObject object ) {
        // fullname is NOT mandatory in the IntAct API.
        if ( object.getFullName() != null && !"".equals( object.getFullName().trim() ) ) {

            Element fullname = session.createElement( "fullName" );
            Text fullnameText = session.createTextNode( object.getFullName() );
            fullname.appendChild( fullnameText );
            parent.appendChild( fullname );
        }
    }

    /**
     * Generate an filled attributeList based upon the annotation of the given AnnotatedObject.
     *
     * @param session
     * @param parent  the Element to which we will attach the newly generated AttributeList.
     * @param object  the Annotated object from which we generate the attributes.
     *
     * @return a newly created attributeList or null if none were created.
     */
    protected Element createAttributeList( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        return createAttributeList( session, parent, object, null );
    }

    /**
     * Generate and fill an attributeList based upon the annotation of the given AnnotatedObject. We apply a filter on
     * CvTopic's shortlabel.
     *
     * @param session
     * @param parent        the Element to which we will attach the newly generated AttributeList.
     * @param object        the Annotated object from which we generate the attributes.
     * @param cvTopicFilter a collection of CvTopic Shortlabel not to export in that list.
     *
     * @return a newly created attributeList or null if none were created.
     */
    protected Element createAttributeList( UserSessionDownload session, Element parent, AnnotatedObject object,
                                           Collection cvTopicFilter ) {

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( object == null ) {
            throw new IllegalArgumentException( "You must give a non null AnnotatedObject for which you want to create an AttributeList." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent Element to generate an attributeList." );
        }

        // avoid taking into account null params.
        if ( cvTopicFilter == null ) {
            cvTopicFilter = Collections.EMPTY_LIST;
        }


        Element element = null;

        if ( false == object.getAnnotations().isEmpty() ) {

            element = session.createElement( Annotation2xml.ATTRIBUTE_LIST_NODE_NAME );

            for ( Iterator iterator = object.getAnnotations().iterator(); iterator.hasNext(); ) {
                Annotation annotation = (Annotation) iterator.next();
                if ( false == cvTopicFilter.contains( annotation.getCvTopic().getShortLabel() ) ) {
                    // export only if the CvTopic is not filtered
                    Annotation2xml.createAttribute( session, element, annotation );
                }
            }

            // get rid of it no annotation to export.
            if ( element.getChildNodes().getLength() == 0 ) {
                element = null;
            }
        }

        // attach the attributeList to the parent.
        if ( element != null ) {
            parent.appendChild( element );
        }

        return element;
    }

    /**
     * Generate the availability of the given object based on its annotations. Only the annotation having the
     * CvTopic(copyright) will be exported as availability. We assume that there is at most one of such annotation per
     * AnnotatedObject.
     *
     * @param session
     * @param parent  the object to which we attach the availability Element.
     * @param object  The Object from which we get the annotations.
     *
     * @return the availability Element for the given object. null If no relevant annotation found.
     */
    protected Element createAvailability( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        // TODO allow the availability to be created globaly.

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( object == null ) {
            throw new IllegalArgumentException( "You must give a non null AnnotatedObject for which you want to create an AttributeList." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent Element to generate an attributeList." );
        }

        Element availabilityElement = null;

        for ( Iterator iterator = object.getAnnotations().iterator();
              iterator.hasNext() && availabilityElement == null; ) {
            Annotation annotation = (Annotation) iterator.next();

            if ( CvTopic.COPYRIGHT.equals( annotation.getCvTopic().getShortLabel() ) ) {
                // create availability
                availabilityElement = session.createElement( Annotation2xml.AVAILABILITY_NODE_NAME );
                availabilityElement.setAttribute( "id", "1" );
                Text availabilityText = session.createTextNode( annotation.getAnnotationText() );
                availabilityElement.appendChild( availabilityText );
                parent.appendChild( availabilityElement );
            }
        }

        return availabilityElement;
    }

    /**
     * Generate the confidence for the given annotatedObject according to the PSI version specified in the session.
     *
     * @param session
     * @param parent  the parent Element to which we will attach the generated element.
     * @param object  the object from which we will get the confidence values.
     *
     * @return the generated Element or null if none could be generated.
     */
    protected Element createConfidence( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        Confidence2xmlI confidence = Confidence2xmlFactory.getInstance( session );

        return confidence.create( session, parent, object );
    }

    //////////////////////////////
    // Utility methods

    /**
     * Based upon the given database name (db), select all Xref of the given AnnotatedObject having that specific
     * database (matching Xref.shortlabel).
     *
     * @param object the Object that may contains a Collection of Xref.
     * @param db     the name pf the target database.
     *
     * @return a Collection of Xref, may be empty but never null.
     */
    protected static Collection getXrefByDatabase( AnnotatedObject object, String db ) {

        // TODO test it...

        Collection xrefs = null;

        for ( Iterator iterator = object.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( xref.getCvDatabase().getShortLabel().equals( db ) ) {

                if ( xrefs == null ) {
                    xrefs = new ArrayList();
                }

                xrefs.add( xref );
            }
        }

        if ( xrefs == null ) {
            xrefs = Collections.EMPTY_LIST;
        }
        return xrefs;
    }

    /**
     * Based upon the given CvXrefQualifier name (db), select all Xref of the given AnnotatedObject having that specific
     * CvXrefQualifier (matching CvXrefQualifier.shortlabel).
     *
     * @param object    the Object that may contains a Collection of Xref.
     * @param qualifier the name pf the CvXrefQualifier.
     *
     * @return a Collection of Xref, may be empty but never null.
     */
    protected static Collection getXrefByQualifier( AnnotatedObject object, String qualifier ) {

        // TODO test it...

        Collection xrefs = null;

        for ( Iterator iterator = object.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();


            if ( xref.getCvXrefQualifier() != null &&
                 xref.getCvXrefQualifier().getShortLabel().equals( qualifier ) ) {

                if ( xrefs == null ) {
                    xrefs = new ArrayList();
                }

                xrefs.add( xref );
            }
        }

        if ( xrefs == null ) {
            xrefs = Collections.EMPTY_LIST;
        }
        return xrefs;
    }
}