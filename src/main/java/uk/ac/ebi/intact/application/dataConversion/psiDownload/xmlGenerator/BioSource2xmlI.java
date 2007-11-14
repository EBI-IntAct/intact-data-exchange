// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.BioSource;

/**
 * Define a common interface between various implementation of BioSource's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface BioSource2xmlI {

    ///////////////////////////
    // Constants

    public static final String ORGANISM_TAG_NAME = "organism";
    public static final String HOST_ORGANISM_TAG_NAME = "hostOrganism";

    ///////////////////////////
    // Public methods

    /**
     * Gemerate the XML representation of an organism based on an IntAct BioSource.
     *
     * @param session   the user session.
     * @param parent    the XML element to which we attach the generated content.
     * @param bioSource the BioSource to convert into an organism.
     *
     * @return an XML Element of an organism.
     */
    public Element createOrganism( UserSessionDownload session, Element parent, BioSource bioSource );

    /**
     * Gemerate the XML representation of a hostOrganism based on an IntAct BioSource.
     *
     * @param session   the user session.
     * @param parent    the XML element to which we attach the generated content.
     * @param bioSource the BioSource to convert into a hostOrganism.
     *
     * @return an XML Element of a hostOrganism.
     */
    public Element createHostOrganism( UserSessionDownload session, Element parent, BioSource bioSource );
}