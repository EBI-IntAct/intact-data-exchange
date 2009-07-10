// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Cv2Source;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlI;
import uk.ac.ebi.intact.model.*;

import java.util.Iterator;
import java.util.Map;

/**
 * Process the common behaviour of an IntAct CvObject when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CvObject2xmlPSI2 extends AnnotatedObject2xmlPSI2 implements CvObject2xmlI {

    public static final String EXPERIMENTAL_ROLE_TAG_NAME = "experimentalRole";
    public static final String PARTICIPANT_ROLE_TAG_NAME = "participantRole";

    public static final String START_STATUS = "startStatus";
    public static final String END_STATUS = "endStatus";

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static CvObject2xmlPSI2 ourInstance = new CvObject2xmlPSI2();

    public static CvObject2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private CvObject2xmlPSI2() {
    }

    ////////////////////////////////////
    // Cache management (encapsulated)

    /**
     * Checks if the given CvObject has already been generated as XML content. <br> If so, that content is cloned which
     * is faster than recreating it.
     *
     * @param cvObject the CvObject we want to check.
     *
     * @return the XML representation (as the root of a DOM tree) of the given CvObject, or null if it hasn't been
     *         generated  yet.
     */
    private Element getXmlFromCache( UserSessionDownload session, Element parent, CvObject cvObject ) {

        Map cache = session.getCvObjectCache();
        return CvObject2xmlCommons.getInstance().getXmlFromCache( cache, new Cv2Source( cvObject, parent.getNodeName() ) );
    }

    /**
     * Store in the cache the XML representation related to the given CvObject instance.
     *
     * @param session  the user session in which the cache is stored.
     * @param cvObject the cvObject we wanted to comvert to XML.
     * @param element  The DOM root (as an Element) of the XML representation of the given CvObject.
     */
    private void updateCache( UserSessionDownload session, Element parent, CvObject cvObject, Element element ) {

        Map cache = session.getCvObjectCache();
        CvObject2xmlCommons.getInstance().updateCache( cache, new Cv2Source( cvObject, parent.getNodeName() ), element );
    }

    ////////////////////////////
    // Encapsulated mehods

    /**
     * Create PSI Xrefs from an IntAct cvObject. <br> Put Xref(psi-mi, identity) as primaryRef, any other as
     * secondaryRef. <br> If not psi-mi available, take randomly an other one.
     *
     * @param session
     * @param parent   the DOM element to which we will add the newly generated PSI Xref.
     * @param cvObject the cvObject from which we get the Xref to generate.
     */
    private void createCvObjectXrefs( UserSessionDownload session, Element parent, CvObject cvObject ) {

        CvObject2xmlCommons.getInstance().createCvObjectXrefs( session, parent, cvObject );
    }

    ///////////////////////////
    // Public methods

    /**
     * Generic call that generates the XML representation of the given CvObject.
     *
     * @param session
     * @param parent   the parent to which we wil attach the generated XML document.
     * @param cvObject the CvObject of which we generate the XML representation.
     *
     * @return the XML representation of the given CvObject.
     */
    public Element create( UserSessionDownload session, Element parent, CvObject cvObject ) {

        // checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( parent == null ) {
            throw new IllegalArgumentException( "You must give a non null parent Element." );
        }

        if ( cvObject == null ) {
            throw new IllegalArgumentException( "You must give a non null cvObject." );
        }

        Element element = getXmlFromCache( session, parent, cvObject );

        if ( element == null ) {

            // get the tag name corresponding to the given instance of CvObject.
            // BUG: if CvIdentification, PSI1 = participantIdentification,
            //                           PSI2 = participantIdentificationMethod
            String tagName = CvObject2xmlCommons.getInstance().getNodeName( session, parent, cvObject.getClass() );

            if ( tagName == null ) {
                throw new IllegalArgumentException( "The CvObject type: " + cvObject.getClass() + " is not supported." );
            }

            // creating the root element...
            element = session.createElement( tagName );

            // generating names...
            createNames( session, element, cvObject );

            // generating xrefs...
            Element xrefElement = session.createElement( "xref" );

            createCvObjectXrefs( session, xrefElement, cvObject );

            if ( xrefElement.hasChildNodes() ) {
                element.appendChild( xrefElement );
            }

            // updating the cache
            updateCache( session, parent, cvObject, element );
        }

        // attaching the element to the given parent...
        parent.appendChild( element );

        return element;
    }

    // ============================================================================
    //
    // NOTE: once we have the new CV in intact to reflect the distinction
    //       between experimentalRole and biologicalRole that will disapear ...
    //
    // ----------------------------------------------------------------------------
    //
    //  Here is the mapping:
    //
    //
    //    CvComponentRole       experimentalRole         biologicalRole
    //    -------------------------------------------------------------
    //    bait                  bait                     unspecified
    //    prey                  prey                     unspecified
    //    neutral component     neutral component        unspecified
    //    self                  self                     self
    //    enzyme                neutral                  enzyme
    //    enzyme target         neutral                  enzyme target
    //    unspecified           neutral(*)               unspecified

    // ============================================================================

    private boolean hasPsiXref( AnnotatedObject annotatedObject, String psiId ) {

        boolean hasIt = false;

        if ( psiId == null ) {
            return false;
        }

        for ( Iterator iterator = annotatedObject.getXrefs().iterator(); iterator.hasNext() && false == hasIt; ) {
            Xref xref = (Xref) iterator.next();

            if ( xref.getCvDatabase().getShortLabel().equals( CvDatabase.PSI_MI ) ) {

                if ( psiId.equals( xref.getPrimaryId() ) ) {
                    hasIt = true;
                }
            }
        }

        return hasIt;
    }

//    public Element createExperimentalRole( UserSessionDownload session, Element parent, CvExperimentalRole cvObject ) {
//        // checking...
//        if ( session == null ) {
//            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
//        }
//
//        if ( parent == null ) {
//            throw new IllegalArgumentException( "You must give a non null parent Element." );
//        }
//
//        if ( cvObject == null ) {
//            throw new IllegalArgumentException( "You must give a non null cvObject." );
//        }
//
//        Element element = null;
////        Element element = getXmlFromCache( session, cvObject );
//
//        CvObject newCvObject = null;
//
//        if ( element == null ) {
//
//            // get the tag name corresponding to the given instance of CvObject.
//            // BUG: if CvIdentification, PSI1 = participantIdentification,
//            //                           PSI2 = participantIdentificationMethod
//
//            /////////////////////////////////////////
//            // Swapping CVs if necessary
//            //
//            //    CvComponentRole       experimentalRole         biologicalRole
//            //    -------------------------------------------------------------
//            //    bait                  bait                     unspecified
//            //    prey                  prey                     unspecified
//            //    neutral component     neutral component        unspecified
//            //    self                  self                     self
//            //    enzyme                neutral                  enzyme
//            //    enzyme target         neutral                  enzyme target
//            //    unspecified           neutral(*)               unspecified
//
////            System.out.println( "=================  ExperimentalRole  =============================" );
////            System.out.println( "Xref count: " + cvObject.getXrefs().size() );
//
//            try {
//                if ( hasPsiXref( cvObject, CvComponentRole.BAIT_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvComponentRole.PREY_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvComponentRole.NEUTRAL_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvComponentRole.SELF_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvComponentRole.ENZYME_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getNeutral();
//                } else if ( hasPsiXref( cvObject, CvComponentRole.ENZYME_TARGET_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getNeutral();
//                } else if ( hasPsiXref( cvObject, CvComponentRole.UNSPECIFIED_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getNeutral();
//                } else {
//                    // TODO Log this !!
//                    newCvObject = cvObject;
//                }
//            } catch ( IntactException e ) {
//                // TODO Log this !!
//                e.printStackTrace();
//                newCvObject = cvObject;
//            }
//
//            // creating the root element...
//            element = session.createElement( EXPERIMENTAL_ROLE_TAG_NAME );
//
//            // generating names...
//            createNames( session, element, newCvObject );
//
//            // generating xrefs...
//            Element xrefElement = session.createElement( "xref" );
//
////            System.out.println( "BEFORE: " + cvObject );
////            System.out.println( "AFTER: " + newCvObject );
//
////            System.out.println( "--------------------------------------------------------" );
//
//            createCvObjectXrefs( session, xrefElement, newCvObject );
//            element.appendChild( xrefElement );
//
//            // updating the cache
////            updateCache( session, cvObject, element );
//        }
//
//        // attaching the element to the given parent...
//        parent.appendChild( element );
//
//        return element;
//    }
//
//    public Element createBiologicalRole( UserSessionDownload session, Element parent, CvBiologicalRole cvObject ) {
//        // checking...
//        if ( session == null ) {
//            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
//        }
//
//        if ( parent == null ) {
//            throw new IllegalArgumentException( "You must give a non null parent Element." );
//        }
//
//        if ( cvObject == null ) {
//            throw new IllegalArgumentException( "You must give a non null cvObject." );
//        }
//
//        Element element = null;
////        Element element = getXmlFromCache( session, cvObject );
//
//        CvObject newCvObject = null;
//
//        if ( element == null ) {
//
//            // get the tag name corresponding to the given instance of CvObject.
//            // BUG: if CvIdentification, PSI1 = participantIdentification,
//            //                           PSI2 = participantIdentificationMethod
//
//            /////////////////////////////////////////
//            // Swapping CVs if necessary
//            //
//            //    CvComponentRole       experimentalRole         biologicalRole
//            //    -------------------------------------------------------------
//            //    bait                  bait                     unspecified
//            //    prey                  prey                     unspecified
//            //    neutral component     neutral component        unspecified
//            //    self                  self                     self
//            //    enzyme                neutral                  enzyme
//            //    enzyme target         neutral                  enzyme target
//            //    unspecified           neutral(*)               unspecified
//
////            System.out.println( "=================  BiologicalRole  =============================" );
////            System.out.println( "Xref count: " + cvObject.getXrefs().size() );
//
//            try {
//                if ( hasPsiXref( cvObject, CvExperimentalRole.BAIT_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getUnspecified();
//                } else if ( hasPsiXref( cvObject, CvExperimentalRole.PREY_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getUnspecified();
//                } else if ( hasPsiXref( cvObject, CvExperimentalRole.NEUTRAL_PSI_REF ) ) {
//                    newCvObject = IntactContext.getCurrentInstance().getCvContext().getUnspecified();
//                } else if ( hasPsiXref( cvObject, CvExperimentalRole.SELF_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvBiologicalRole.ENZYME_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvBiologicalRole.ENZYME_TARGET_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else if ( hasPsiXref( cvObject, CvBiologicalRole.UNSPECIFIED_PSI_REF ) ) {
//                    newCvObject = cvObject;
//                } else {
//                    // TODO Log this !!
//                    newCvObject = cvObject;
//                }
//            } catch ( IntactException e ) {
//                // TODO Log this !!
//                e.printStackTrace();
//                newCvObject = cvObject;
//            }
//
////            System.out.println( "BEFORE: " + cvObject );
////            System.out.println( "AFTER: " + newCvObject );
//
////            System.out.println( "--------------------------------------------------------" );
//
//            // creating the root element...
//            if ( session.getPsiVersion().equals( PsiVersion.getVersion2() ) ) {
//                element = session.createElement( PARTICIPANT_ROLE_TAG_NAME );
//            } else if ( session.getPsiVersion().equals( PsiVersion.getVersion25() ) ) {
//                element = session.createElement( "biologicalRole" );
//            } else {
//                throw new UnsupportedOperationException();
//            }
//
//            // generating names...
//            createNames( session, element, newCvObject );
//
//            // generating xrefs...
//            Element xrefElement = session.createElement( "xref" );
//            createCvObjectXrefs( session, xrefElement, newCvObject );
//            element.appendChild( xrefElement );
//
//            // updating the cache
////            updateCache( session, cvObject, element );
//        }
//
//        // attaching the element to the given parent...
//        parent.appendChild( element );
//
//        return element;
//    }

    private Element createStatus( UserSessionDownload session, Element parent, CvFuzzyType fuzzyType, String tagName ) {

        Element element = session.createElement( tagName );

        // 1. Generating names...
        createNames( session, element, fuzzyType );

        // 2. generating xref...
        Element xrefElement = session.createElement( "xref" );
        createCvObjectXrefs( session, xrefElement, fuzzyType );
        if ( xrefElement.hasChildNodes() ) {
            element.appendChild( xrefElement );
        }

        parent.appendChild( element );

        return element;
    }

    public Element createStartStatus( UserSessionDownload session, Element parent, CvFuzzyType fuzzyType ) {

        return createStatus( session, parent, fuzzyType, START_STATUS );
    }

    public Element createEndStatus( UserSessionDownload session, Element parent, CvFuzzyType fuzzyType ) {

        return createStatus( session, parent, fuzzyType, END_STATUS );
    }

    private Element createCertainStatus( UserSessionDownload session, Element parent, String tagName ) {

        Element element = session.createElement( tagName );

        // names
        Element nameElement = session.createElement( "names" );
        element.appendChild( nameElement );
        Element shortlabelElement = session.createElement( "shortLabel" );
        nameElement.appendChild( shortlabelElement );
        Text shortlabelTextElement = session.createTextNode( "certain" );
        shortlabelElement.appendChild( shortlabelTextElement );

        // xref
        Element xrefElement = session.createElement( "xref" );
        element.appendChild( xrefElement );
        Element primaryRefElement = session.createElement( "primaryRef" );
        xrefElement.appendChild( primaryRefElement );
        primaryRefElement.setAttribute( "db", CvDatabase.PSI_MI );
        primaryRefElement.setAttribute( "dbAc", CvDatabase.PSI_MI_MI_REF );
        primaryRefElement.setAttribute( "id", "MI:0335" ); // certain, cf. http://psidev.sourceforge.net/mi/rel25/data/psi-mi25.dag
        primaryRefElement.setAttribute( "refType", CvXrefQualifier.IDENTITY );
        primaryRefElement.setAttribute( "refTypeAc", CvXrefQualifier.IDENTITY_MI_REF );

        parent.appendChild( element );

        return element;
    }

    public Element createStartCertainStatus( UserSessionDownload session, Element parent ) {

        return createCertainStatus( session, parent, START_STATUS );
    }

    public Element createEndCertainStatus( UserSessionDownload session, Element parent ) {

        return createCertainStatus( session, parent, END_STATUS );
    }
}