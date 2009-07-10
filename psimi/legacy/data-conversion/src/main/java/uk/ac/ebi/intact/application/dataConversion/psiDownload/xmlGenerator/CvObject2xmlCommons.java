// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Behaviour shared across CvObject's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class CvObject2xmlCommons {

    ////////////////////////
    // Constants

    // tag names
    public static final String PARTICIPANT_DETECTION_NODE_NAME = "participantDetection";
    public static final String PARTICIPANT_IDENTIFICATION_METHOD_NODE_NAME = "participantIdentificationMethod";

    public static final String INTERACTION_DETECTION_NODE_NAME = "interactionDetection";
    public static final String INTERACTION_DETECTION_METHOD_NODE_NAME = "interactionDetectionMethod";

    public static final String FEATURE_DETECTION_NODE_NAME = "featureDetection";
    public static final String FEATURE_DETECTION_METHOD_NODE_NAME = "featureDetectionMethod";

    public static final String INTERACTION_TYPE_NODE_NAME = "interactionType";

    public static final String FEATURE_DESCRIPTION_NODE_NAME = "featureDescription";
    public static final String FEATURE_TYPE_NODE_NAME = "featureType";

    public static final String CELL_TYPE_NODE_NAME = "cellType";
    public static final String TISSUE_NODE_NAME = "tissue";
    public static final String COMPARTMENT_NODE_NAME = "compartment";

    // from PSI2 onward
    public static final String ROLE_NODE_NAME = "participantRole";
    public static final String EXPERIMENTAL_FORM_NAME = "experimentalForm";
    public static final String INTERACTOR_TYPE_NAME = "interactorType";

    private static final String BIOLOGICAL_ROLE_NODE_NAME = "biologicalRole";
    private static final String EXPERIMENTAL_ROLE_NODE_NAME = "experimentalRole";


    /**
     * Commons association PSI 1 and 2 IntAct CV type -> root tag name
     */
    final Map associations = new HashMap();

    /**
     * PSI 1 only IntAct CV type -> root tag name
     */
    final Map associationsPSI1only = new HashMap();

    /**
     * PSI 2 only IntAct CV type -> root tag name
     */
    final Map associationsPSI2only = new HashMap();

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static CvObject2xmlCommons ourInstance = new CvObject2xmlCommons();

    public static CvObject2xmlCommons getInstance() {
        return ourInstance;
    }

    private CvObject2xmlCommons() {

        // TODO: write in the documentation the XPath mapping to IntAct types and attributes.
        initialiseAssociations();
    }


    /**
     * Initialise the mapping of PSI1, PSI2, PSI2.5
     */
    private void initialiseAssociations() {

        // all PSI version
        associations.put( new CvClass2Source( CvCellType.class ), CELL_TYPE_NODE_NAME );
        associations.put( new CvClass2Source( CvTissue.class ), TISSUE_NODE_NAME );
        associations.put( new CvClass2Source( CvCompartment.class ), COMPARTMENT_NODE_NAME );
        associations.put( new CvClass2Source( CvBiologicalRole.class ), BIOLOGICAL_ROLE_NODE_NAME );
        associations.put( new CvClass2Source( CvExperimentalRole.class ), EXPERIMENTAL_ROLE_NODE_NAME );
        associations.put( new CvClass2Source( CvInteractionType.class ), INTERACTION_TYPE_NODE_NAME );

        // PSI version 1
        associationsPSI1only.put( new CvClass2Source( CvIdentification.class ), PARTICIPANT_DETECTION_NODE_NAME );
        associationsPSI1only.put( new CvClass2Source( CvInteraction.class ), INTERACTION_DETECTION_NODE_NAME );
        associationsPSI1only.put( new CvClass2Source( CvFeatureIdentification.class ), FEATURE_DETECTION_NODE_NAME );
        associationsPSI1only.put( new CvClass2Source( CvFeatureType.class ), FEATURE_DESCRIPTION_NODE_NAME );

        // PSI version 2
        associationsPSI2only.put( new CvClass2Source( CvIdentification.class ), PARTICIPANT_IDENTIFICATION_METHOD_NODE_NAME );
        associationsPSI2only.put( new CvClass2Source( CvInteraction.class ), INTERACTION_DETECTION_METHOD_NODE_NAME );
        associationsPSI2only.put( new CvClass2Source( CvFeatureIdentification.class ), FEATURE_DETECTION_METHOD_NODE_NAME );
        associationsPSI2only.put( new CvClass2Source( CvFeatureType.class, "feature" ), FEATURE_TYPE_NODE_NAME );
        associationsPSI2only.put( new CvClass2Source( CvFeatureType.class, "experimentalFormList" ), EXPERIMENTAL_FORM_NAME );

        // PSI version 2.5 (but still attached to the PSI 2 mapping)
        associationsPSI2only.put( new CvClass2Source( CvInteractorType.class ), INTERACTOR_TYPE_NAME );
    }

    /**
     * Based on a concrete IntAct CvObject, get the name of the corresponding XML Element. <br> This may be specific to
     * the requested PSI version.
     *
     * @param session the PSI version
     * @param clazz   a concrete IntAct CvObject
     *
     * @return the corresponding node name of nulll if none is found.
     */
    public String getNodeName( UserSessionDownload session, Element parent, Class clazz ) {

        // 1) First look for the Node name correcponding to that CvObject class

        CvClass2Source cvClass2Source = new CvClass2Source( clazz );

        String nodeName = (String) associations.get( cvClass2Source );

        if ( nodeName == null ) {

            // if nothing found in the general associations

            if ( PsiVersion.VERSION_1.equals( session.getPsiVersion() ) ) {

                nodeName = (String) associationsPSI1only.get( cvClass2Source );

            } else if ( PsiVersion.VERSION_2.equals( session.getPsiVersion() ) ) {

                nodeName = (String) associationsPSI2only.get( cvClass2Source );

            } else if ( PsiVersion.VERSION_25.equals( session.getPsiVersion() ) ) {

                nodeName = (String) associationsPSI2only.get( cvClass2Source );

            } else {

                throw new IllegalArgumentException( "Unsupported version of PSI: " + session.getPsiVersion().getVersion() );
            }
        }

        // 2) if it wasn't found, then we take into account:
        //     - CvObject class
        //     - the parent node name (as a CvObject type can be inserted under different name)

        if ( nodeName == null ) {
            // take into account the parent name
            cvClass2Source = new CvClass2Source( clazz, parent.getNodeName() );

            nodeName = (String) associations.get( cvClass2Source );

            if ( nodeName == null ) {

                // if nothing found in the general associations

                if ( PsiVersion.VERSION_1.equals( session.getPsiVersion() ) ) {

                    nodeName = (String) associationsPSI1only.get( cvClass2Source );

                } else if ( PsiVersion.VERSION_2.equals( session.getPsiVersion() ) ) {

                    nodeName = (String) associationsPSI2only.get( cvClass2Source );

                } else if ( PsiVersion.VERSION_25.equals( session.getPsiVersion() ) ) {

                    nodeName = (String) associationsPSI2only.get( cvClass2Source );

                } else {

                    throw new IllegalArgumentException( "Unsupported version of PSI: " + session.getPsiVersion().getVersion() );
                }
            }

        }

//        System.out.println( clazz.getName() + " / " + parent.getNodeName() + " ---> " + nodeName );

        return nodeName;
    }

    ///////////////////////
    // Cache management

    /**
     * Checks if the given CvObject has already been generated as XML content. <br> If so, that content is cloned which
     * is faster than recreating it.
     *
     * @param cache    the cache ( cvObject -> DOM Element)
     * @param cvSource the CvObject we want to check.
     *
     * @return the XML representation (as the root of a DOM tree) of the given CvObject, or null if it hasn't been
     *         generated  yet.
     */
    public Element getXmlFromCache( Map cache, Cv2Source cvSource ) {

        Element element = (Element) cache.get( cvSource );

        if ( element != null ) {
            // if that element has already been generated, we clone it.
            element = (Element) element.cloneNode( true );
        }

        return element;
    }

    /**
     * Store in the cache the XML representation related to the given CvObject instance.
     *
     * @param cache    the cache ( cvObject -> DOM Element)
     * @param cvSource the cvObject we wanted to comvert to XML.
     * @param element  The DOM root (as an Element) of the XML representation of the given CvObject.
     */
    public void updateCache( Map cache, Cv2Source cvSource, Element element ) {

        cache.put( cvSource, element );
    }

    ///////////////////////////
    // Utility methods

    /**
     * Create PSI Xrefs from an IntAct cvObject. <br> Put Xref(psi-mi, identity) as primaryRef, any other as
     * secondaryRef. <br> If not psi-mi available, take randomly an other one.
     *
     * @param session
     * @param parent   the DOM element to which we will add the newly generated PSI Xref.
     * @param cvObject the cvObject from which we get the Xref to generate.
     */
    public void createCvObjectXrefs( UserSessionDownload session,
                                     Element parent,
                                     CvObject cvObject ) {

        // 0. Checking...
        if ( cvObject.getXrefs().isEmpty() ) {
            // TODO No PSI-MI Xref, that should be logged.
            return;
        }

        // 1. get psi-mi xrefs (not always there)
        Collection psiXrefs = AbstractAnnotatedObject2xml.getXrefByDatabase( cvObject, CvDatabase.PSI_MI );

        // 2. process the identity
        Xref identity = null;
        for ( Iterator iterator = psiXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( xref.getCvXrefQualifier() != null &&
                 CvXrefQualifier.IDENTITY.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {

                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, parent, identity );

                // break the loop as there is only one identity
                break;
            }
        }

        // 3. handle the case when no psi-mi found.
        if ( identity == null ) {
            // if no identity selected, we pick a random one
            // keep reference
            identity = (Xref) cvObject.getXrefs().iterator().next();

            // create xref
            Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, parent, identity );
        }

        // 4. process remaining xrefs
        for ( Iterator iterator = cvObject.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( xref != identity ) {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, parent, xref );
            }
        }
    }
}