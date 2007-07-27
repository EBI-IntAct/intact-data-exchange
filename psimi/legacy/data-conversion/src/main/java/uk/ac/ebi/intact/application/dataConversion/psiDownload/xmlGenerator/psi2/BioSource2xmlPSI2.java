// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlCommons;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.BioSource2xmlI;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.CvObject2xmlFactory;
import uk.ac.ebi.intact.model.BioSource;

import java.util.Map;

/**
 * Process the common behaviour of an IntAct BioSource when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class BioSource2xmlPSI2 extends AnnotatedObject2xmlPSI2 implements BioSource2xmlI {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static BioSource2xmlPSI2 ourInstance = new BioSource2xmlPSI2();

    public static BioSource2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private BioSource2xmlPSI2() {
    }

    ////////////////////////////////////
    // Cache management (encapsulated)

    /**
     * Checks if the given BioSource has already been generated as XML content. <br> If so, that content is cloned which
     * is faster than recreating it.
     *
     * @param session   the user session in which the cache is stored.
     * @param bioSource the bioSource we want to check.
     *
     * @return the XML representation (as the root of a DOM tree) of the given BioSource, or null if it hasn't been
     *         generated  yet.
     */
    private static Element getOrganismFromCache( UserSessionDownload session, BioSource bioSource ) {

        Map cache = session.getOrganismCache();
        return BioSource2xmlCommons.getInstance().getXmlFromCache( cache, bioSource );
    }

    /**
     * Checks if the given BioSource has already been generated as XML content. <br> If so, that content is cloned which
     * is faster than recreating it.
     *
     * @param session   the user session in which the cache is stored.
     * @param bioSource the bioSource we want to check.
     *
     * @return the XML representation (as the root of a DOM tree) of the given BioSource, or null if it hasn't been
     *         generated  yet.
     */
    private static Element getHostOrganismFromCache( UserSessionDownload session, BioSource bioSource ) {

        Map cache = session.getHostOrganismCache();
        return BioSource2xmlCommons.getInstance().getXmlFromCache( cache, bioSource );
    }

    /**
     * Store in the cache the XML representation related to the given BioSource instance.
     *
     * @param session   the user session in which the cache is stored.
     * @param bioSource the bioSource we wanted to comvert to XML.
     * @param element   The DOM root (as an Element) of the XML representation of the given BioSource.
     */
    private static void updateOrganismCache( UserSessionDownload session, BioSource bioSource, Element element ) {

        Map cache = session.getOrganismCache();
        BioSource2xmlCommons.getInstance().updateCache( cache, bioSource, element );
    }

    /**
     * Store in the cache the XML representation related to the given BioSource instance.
     *
     * @param session   the user session in which the cache is stored.
     * @param bioSource the bioSource we wanted to comvert to XML.
     * @param element   The DOM root (as an Element) of the XML representation of the given BioSource.
     */
    private static void updateHostOrganismCache( UserSessionDownload session, BioSource bioSource, Element element ) {

        Map cache = session.getHostOrganismCache();
        BioSource2xmlCommons.getInstance().updateCache( cache, bioSource, element );
    }

    ///////////////////////////
    // Public methods

    public Element createOrganism( UserSessionDownload session,
                                   Element parent,
                                   BioSource bioSource ) {

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null BioSource to build a " + ORGANISM_TAG_NAME + "." );

        } else {

            if ( !"proteinInteractor".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a proteinInteractor parent to build a " + ORGANISM_TAG_NAME + "." );
            }
        }

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null session." );
        }

        Element element = getOrganismFromCache( session, bioSource );

        if ( element == null ) {

            element = create( session, parent, bioSource, ORGANISM_TAG_NAME );
            updateOrganismCache( session, bioSource, element );
        } else {
            parent.appendChild( element );
        }

        return element;
    }


    public Element createHostOrganism( UserSessionDownload session,
                                       Element parent,
                                       BioSource bioSource ) {

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null BioSource to build a " + HOST_ORGANISM_TAG_NAME + "." );

        } else {

            if ( !"experimentDescription".equals( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "You must give a experimentDescription to build a " + ORGANISM_TAG_NAME + "." );
            }
        }

        Element element = getHostOrganismFromCache( session, bioSource );

        if ( element == null ) {

            element = create( session, parent, bioSource, HOST_ORGANISM_TAG_NAME );
            updateHostOrganismCache( session, bioSource, element );
        } else {
            parent.appendChild( element );
        }

        return element;
    }

    ///////////////////////////
    // Private methods

    /**
     * @param session
     * @param parent
     * @param bioSource
     * @param nodeName
     *
     * @return
     */
    private Element create( UserSessionDownload session,
                            Element parent,
                            BioSource bioSource,
                            String nodeName ) {

        // 1. Checking...
        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( nodeName == null ) {
            throw new IllegalArgumentException( "You must give a non null nodeName for an (host)organism. " +
                                                "That will be used to create the root Element's name." );
        }

        if ( bioSource == null ) {
            throw new IllegalArgumentException( "You must give a non null BioSource to build a " + nodeName + "." );
        }

        // NOTE: children terms are:
        //       names cellType compartment tissue

        // 2. Initialising the element...
        Element element = session.createElement( nodeName );
        element.setAttribute( "ncbiTaxId", bioSource.getTaxId() );

        // 3. Generating names...
        createNames( session, element, bioSource );

        // 4. Generating OPTIONAL cellType...
        if ( bioSource.getCvCellType() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, bioSource.getCvCellType() );
        }

        // 5. Generating OPTIONAL compartment...
//        if ( bioSource.getCvCompartment() != null ) {
//            CvObject2xmlFactory.getInstance( session ).create( session, element, bioSource.getCvCompartment() );
//        }

        // 6. Generating OPTIONAL tissue...
        if ( bioSource.getCvTissue() != null ) {
            CvObject2xmlFactory.getInstance( session ).create( session, element, bioSource.getCvTissue() );
        }

        // 7. Attaching the newly created element to the parent...
        parent.appendChild( element );

        return element;
    }
}